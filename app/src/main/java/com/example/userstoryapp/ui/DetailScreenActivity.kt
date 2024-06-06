package com.example.userstoryapp.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import coil.load
import com.example.userstoryapp.data.model.DetailStory
import com.example.userstoryapp.databinding.ActivityDetailScreenBinding
import com.example.userstoryapp.helper.ResultStory
import com.example.userstoryapp.viewmodel.DetailScreenViewModel

class DetailScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailScreenBinding
    private lateinit var viewModel: DetailScreenViewModel
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeComponents()
        observeViewModel()
        fetchStoryDetails()
    }

    private fun initializeComponents() {
        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        viewModel = ViewModelProvider(this).get(DetailScreenViewModel::class.java)
    }

    private fun observeViewModel() {
        viewModel.resultDetailStory.observe(this) { result ->
            when (result) {
                is ResultStory.Success<*> -> showDetailStory(result.data as? DetailStory)
                is ResultStory.Error -> showError(result.exception.message)
                is ResultStory.Loading -> showLoading()
            }
        }
    }

    private fun fetchStoryDetails() {
        val storyId = intent.getStringExtra("story_id")
        val token = sharedPreferences.getString("token", null)

        if (storyId.isNullOrEmpty()) {
            showToast("Story ID not found")
            finish()
            return
        }

        if (token.isNullOrEmpty()) {
            showToast("Token not found")
            redirectToLogin()
            return
        }

        viewModel.getStoryDetail(token, storyId)
    }

    private fun showDetailStory(detailStory: DetailStory?) {
        if (detailStory != null) {
            binding.apply {
                imageView.load(detailStory.photoUrl)
                txtJudul.text = detailStory.name
                txtDeskripsi.text = detailStory.description
            }
        } else {
            showToast("Detail story not found")
            finish()
        }
    }

    private fun showError(message: String?) {
        showToast(message ?: "An error occurred")
    }

    private fun showLoading() {
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun redirectToLogin() {
        startActivity(Intent(this, LoginScreenActivity::class.java))
        finish()
    }
}
