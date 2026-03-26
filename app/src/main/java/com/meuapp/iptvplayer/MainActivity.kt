package com.meuapp.iptvplayer

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.meuapp.iptvplayer.activities.HomeActivity
import com.meuapp.iptvplayer.databinding.ActivityMainBinding
import com.meuapp.iptvplayer.helper.PreferenceHelper

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (PreferenceHelper.isLoggedIn(this)) {
            goHome()
            return
        }

        binding.btnConnect.setOnClickListener {
            val url = binding.etUrl.text.toString().trim()
            val user = binding.etUsername.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()
            val name = binding.etPlaylistName.text.toString().trim().ifEmpty { "Minha Lista" }

            if (url.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                binding.etUrl.error = if (url.isEmpty()) "Informe a URL" else null
                binding.etUsername.error = if (user.isEmpty()) "Informe o usuário" else null
                binding.etPassword.error = if (pass.isEmpty()) "Informe a senha" else null
                return@setOnClickListener
            }

            val cleanUrl = if (url.startsWith("http")) url else "http://$url"
            PreferenceHelper.setServerUrl(this, cleanUrl)
            PreferenceHelper.setUsername(this, user)
            PreferenceHelper.setPassword(this, pass)
            PreferenceHelper.setPlaylistName(this, name)
            goHome()
        }
    }

    private fun goHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
