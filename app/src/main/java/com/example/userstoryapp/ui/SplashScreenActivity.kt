package com.example.userstoryapp.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.userstoryapp.R
import com.example.userstoryapp.databinding.ActivitySplashScreenBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)

        val backgroundImage: ImageView = binding.splashLogo
        val sideAnimation = AnimationUtils.loadAnimation(this, R.anim.slide)
        backgroundImage.startAnimation(sideAnimation)

        lifecycleScope.launch {
            delay(3000)
            if (isLoggedIn()) {
                startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
            } else {
                startActivity(Intent(this@SplashScreenActivity, LoginScreenActivity::class.java))
            }
            finish()
        }
    }

    private fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("is_logged_in", false)
    }
}
