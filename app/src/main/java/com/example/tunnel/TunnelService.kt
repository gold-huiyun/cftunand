
package com.example.tunnel

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class TunnelService : Service() {
    private var process: Process? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val token = intent?.getStringExtra("token") ?: return START_NOT_STICKY
        val binDir = applicationInfo.nativeLibraryDir
        val exe = "$binDir/libcloudflared.so"
        val f = File(exe)
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        fun sendStatus(msg: String) {
            nm.notify(1, buildNotification(msg))
            sendBroadcast(Intent("CLOUDFLARED_STATUS").putExtra("status", msg))
        }
        fun sendLog(line: String) {
            sendBroadcast(Intent("CLOUDFLARED_LOG").putExtra("line", line))
        }

        if (!f.exists()) { startForeground(1, buildNotification("cloudflared 不存在")); stopSelf(); return START_NOT_STICKY }
        if (!f.canExecute()) { startForeground(1, buildNotification("cloudflared 不可执行")); stopSelf(); return START_NOT_STICKY }

        // 前台通知
        startForeground(1, buildNotification("Cloudflared 正在启动…"))

        val logFile = File(filesDir, "cloudflared.log").absolutePath
        val metrics = "127.0.0.1:43100"
        val cmd = listOf(
            exe,
            "tunnel", "run",
            "--token", token,
            "--no-autoupdate",
            "--edge-ip-version", "4",
            "--loglevel", "debug",
            "--logfile", logFile,
            "--metrics", metrics
        )

        try {
            val pb = ProcessBuilder(cmd).redirectErrorStream(true)
            process = pb.start()

            Thread {
                BufferedReader(InputStreamReader(process!!.inputStream)).use { br ->
                    var line: String?
                    while (br.readLine().also { line = it } != null) {
                        sendLog(line!!)
                    }
                }
            }.start()

            // 简单健康检查：反复拉 metrics，看到关键指标就报告 HEALTHY
            Thread {
                repeat(30) {
                    try {
                        val url = URL("http://$metrics/metrics")
                        val conn = url.openConnection() as HttpURLConnection
                        conn.connectTimeout = 1500; conn.readTimeout = 1500
                        if (conn.responseCode == 200) {
                            val text = conn.inputStream.bufferedReader().readText()
                            // 关键指标粗略判断
                            if (text.contains("cloudflared_tunnel_")) {
                                sendStatus("Cloudflared 运行中 (metrics OK)")
                                return@Thread
                            }
                        }
                    } catch (_: Throwable) {}
                    Thread.sleep(1000)
                }
                sendStatus("仍未获取到 metrics，可在日志中排查")
            }.start()

            Thread {
                val exit = try { process?.waitFor() ?: -1 } catch (_: Throwable) { -1 }
                sendStatus("Cloudflared 退出，code=$exit；日志: $logFile")
                stopSelf()
            }.start()
        } catch (t: Throwable) {
            sendStatus("启动失败: ${t.message}")
            stopSelf()
        }

        return START_STICKY
    }

    override fun onDestroy() { process?.destroy(); super.onDestroy() }
    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(text: String): Notification {
        val channelId = "tunnel"
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            nm.createNotificationChannel(NotificationChannel(channelId, "Cloudflare Tunnel", NotificationManager.IMPORTANCE_LOW))
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Cloudflare Tunnel")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setOngoing(true)
            .build()
    }
}
