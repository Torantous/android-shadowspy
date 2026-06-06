package com.shadowspy

object Config {
    const val C2_BASE_URL = "http://10.0.2.2:5000"  // For emulator localhost
    // const val C2_BASE_URL = "http://192.168.x.x:5000" // For real device on same network
    const val ENCRYPTION_KEY = "local-shadowspy-key-32bytes-long!!"
    const val DEVICE_ID = "shadow-device-001" // TODO: make unique per device
}