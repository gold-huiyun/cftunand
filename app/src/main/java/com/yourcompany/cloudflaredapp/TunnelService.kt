package com.yourcompany.cloudflaredapp

import android.app.*
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.*

class TunnelService : Service() {

    private var process: Process? = null
    private val binder = LocalBinder()
    private lateinit var notificationManager: NotificationManager

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val CHANNEL_ID = "TunnelServiceChannel"
        const val NOTIFICATION_ID = 1
    }

    inner class LocalBinder : Binder() {
        fun getService(): TunnelService = this@TunnelService
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NotificationManager::class.java)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val token = intent.getStringExtra("TUNNEL_TOKEN") ?: ""
                startTunnel(token)
            }
            ACTION_STOP -> {
                stopTunnel()
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startTunnel(token: String) {
        val binaryFile = File(filesDir, "cloudflared")
        if (!binaryFile.exists()) {
            assets.open("cloudflared").use { inputStream ->
                binaryFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            binaryFile.setExecutable(true)
        }

        startForeground(NOTIFICATION_ID, createNotification())

        try {
            val command = listOf(
                binaryFile.absolutePath,
                "tunnel",
                "--token",
                token,
                "run"
            )

            process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()

            Thread {
                val reader = BufferedReader(InputStreamReader(process!!.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    Log.d("Cloudflared", line!!)
                }
            }.start()

        } catch (e: Exception) {
            Log.e("Cloudflared", "启动失败", e)
            stopSelf()
        }
    }

    private fun stopTunnel() {
        process?.destroy()
        process = null
        stopForeground(true)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_description)
        }

        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_content))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onDestroy() {
        stopTunnel()
        super.onDestroy()
    }
}
