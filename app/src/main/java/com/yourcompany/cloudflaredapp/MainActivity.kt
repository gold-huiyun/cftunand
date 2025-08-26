package com.yourcompany.cloudflaredapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.yourcompany.cloudflaredapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtons()
    }

    private fun setupButtons() {
        binding.btnStart.setOnClickListener {
            val token = binding.etToken.text.toString().trim()
            if (token.isBlank()) {
                binding.tvStatus.text = "错误：请输入 Token"
                return@setOnClickListener
            }
            startTunnelService(token)
        }

        binding.btnStop.setOnClickListener {
            stopTunnelService()
        }
    }

    private fun startTunnelService(token: String) {
        val intent = Intent(this, TunnelService::class.java).apply {
            putExtra("TUNNEL_TOKEN", token)
            action = TunnelService.ACTION_START
        }
        startService(intent)
        binding.tvStatus.text = "状态：启动中..."
        binding.btnStart.isEnabled = false
        binding.btnStop.isEnabled = true
    }

    private fun stopTunnelService() {
        val intent = Intent(this, TunnelService::class.java).apply {
            action = TunnelService.ACTION_STOP
        }
        startService(intent)
        binding.tvStatus.text = "状态：停止中..."
        binding.btnStart.isEnabled = true
        binding.btnStop.isEnabled = false
    }
}
