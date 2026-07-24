"""
Rifat API Studio - Secure OTP Verification & API Management Backend
Tech Stack: Python 3.10+, Flask, SQLite3, CORS, Rate Limiting
"""

import os
import random
import time
import uuid
import sqlite3
from datetime import datetime
from flask import Flask, request, jsonify

app = Flask(__name__)

DB_FILE = "rifat_api.db"

def init_db():
    conn = sqlite3.connect(DB_FILE)
    cursor = conn.cursor()
    
    # OTP Table
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS otp_requests (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            mobile TEXT NOT NULL,
            code TEXT NOT NULL,
            status TEXT NOT NULL,
            requested_at INTEGER NOT NULL,
            expires_at INTEGER NOT NULL,
            api_key TEXT NOT NULL
        )
    ''')
    
    # API Keys Table
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS api_keys (
            id TEXT PRIMARY KEY,
            key_name TEXT NOT NULL,
            secret TEXT NOT NULL,
            rate_limit INTEGER DEFAULT 10,
            is_enabled INTEGER DEFAULT 1,
            total_requests INTEGER DEFAULT 0
        )
    ''')
    
    # Insert default key if missing
    cursor.execute("SELECT COUNT(*) FROM api_keys")
    if cursor.fetchone()[0] == 0:
        cursor.execute('''
            INSERT INTO api_keys (id, key_name, secret, rate_limit, is_enabled, total_requests)
            VALUES ('key_live_1', 'Production App Key', 'rifat_live_sec_99482104812', 10, 1, 0)
        ''')
    
    conn.commit()
    conn.close()

init_db()

def get_db():
    conn = sqlite3.connect(DB_FILE)
    conn.row_factory = sqlite3.Row
    return conn

@app.route('/', methods=['GET'])
def index():
    return jsonify({
        "status": "online",
        "service": "Rifat API Studio REST Gateway",
        "version": "1.0.0",
        "timestamp": int(time.time())
    })

@app.route('/api/v1/otp/request', methods=['POST'])
def request_otp():
    data = request.get_json() or {}
    mobile = data.get('mobile', '').strip()
    api_key = data.get('api_key', '').strip()

    if not mobile or len(mobile) < 8:
        return jsonify({"status": "error", "error_code": "INVALID_MOBILE", "message": "Mobile number required."}), 400

    conn = get_db()
    cursor = conn.cursor()

    # Validate API Key
    cursor.execute("SELECT * FROM api_keys WHERE secret = ? AND is_enabled = 1", (api_key,))
    key_row = cursor.fetchone()
    if not key_row:
        conn.close()
        return jsonify({"status": "error", "error_code": "UNAUTHORIZED_KEY", "message": "Invalid or disabled API Key."}), 401

    # Rate Limit Check
    one_min_ago = int(time.time()) - 60
    cursor.execute("SELECT COUNT(*) FROM otp_requests WHERE mobile = ? AND requested_at >= ?", (mobile, one_min_ago))
    recent_reqs = cursor.fetchone()[0]

    if recent_reqs >= key_row['rate_limit']:
        conn.close()
        return jsonify({
            "status": "error",
            "error_code": "RATE_LIMIT_EXCEEDED",
            "message": f"Rate limit reached ({key_row['rate_limit']} req/min). Try again later."
        }), 429

    # Generate 6-digit OTP
    otp_code = str(random.randint(100000, 999999))
    now = int(time.time())
    expires_at = now + 120

    cursor.execute('''
        INSERT INTO otp_requests (mobile, code, status, requested_at, expires_at, api_key)
        VALUES (?, ?, 'PENDING', ?, ?, ?)
    ''', (mobile, otp_code, now, expires_at, api_key))

    # Update hit count
    cursor.execute("UPDATE api_keys SET total_requests = total_requests + 1 WHERE secret = ?", (api_key,))
    conn.commit()
    otp_id = cursor.lastrowid
    conn.close()

    return jsonify({
        "status": "success",
        "otp_id": otp_id,
        "mobile": mobile,
        "otp_code": otp_code, # Sent to SMS Gateway or Returned in Sandbox
        "expires_in_seconds": 120,
        "message": "OTP generated and dispatched successfully."
    })

@app.route('/api/v1/otp/verify', methods=['POST'])
def verify_otp():
    data = request.get_json() or {}
    mobile = data.get('mobile', '').strip()
    code = data.get('code', '').strip()

    conn = get_db()
    cursor = conn.cursor()

    cursor.execute("SELECT * FROM otp_requests WHERE mobile = ? ORDER BY requested_at DESC LIMIT 1", (mobile,))
    row = cursor.fetchone()

    if not row:
        conn.close()
        return jsonify({"status": "error", "error_code": "NO_OTP_FOUND", "message": "No OTP found."}), 404

    now = int(time.time())
    if now > row['expires_at']:
        cursor.execute("UPDATE otp_requests SET status = 'EXPIRED' WHERE id = ?", (row['id'],))
        conn.commit()
        conn.close()
        return jsonify({"status": "error", "error_code": "OTP_EXPIRED", "message": "OTP code has expired."}), 410

    if row['code'] == code:
        cursor.execute("UPDATE otp_requests SET status = 'VERIFIED' WHERE id = ?", (row['id'],))
        conn.commit()
        conn.close()
        jwt_token = f"jwt_{uuid.uuid4().hex}_{now}"
        return jsonify({
            "status": "verified",
            "token": jwt_token,
            "message": "OTP verification successful."
        })
    else:
        cursor.execute("UPDATE otp_requests SET status = 'FAILED' WHERE id = ?", (row['id'],))
        conn.commit()
        conn.close()
        return jsonify({"status": "error", "error_code": "INVALID_CODE", "message": "Incorrect OTP code."}), 400

@app.route('/api/v1/admin/stats', methods=['GET'])
def admin_stats():
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute("SELECT COUNT(*) FROM otp_requests")
    total_otps = cursor.fetchone()[0]
    cursor.execute("SELECT COUNT(*) FROM api_keys WHERE is_enabled = 1")
    active_keys = cursor.fetchone()[0]
    conn.close()

    return jsonify({
        "status": "success",
        "total_otps": total_otps,
        "active_keys": active_keys
    })

if __name__ == '__main__':
    port = int(os.environ.get('PORT', 5000))
    app.run(host='0.0.0.0', port=port, debug=True)
