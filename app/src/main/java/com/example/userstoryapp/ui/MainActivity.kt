package com.example.userstoryapp.ui

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyapps.viewmodel.factory.MainViewModelFactory
import com.example.userstoryapp.R
import com.example.userstoryapp.adapter.AdapterStory
import com.example.userstoryapp.adapter.LoadStateAdapter
import com.example.userstoryapp.data.database.StoryDatabase
import com.example.userstoryapp.data.database.StoryRepository
import com.example.userstoryapp.data.network.ApiConfig
import com.example.userstoryapp.databinding.ActivityMainBinding
import com.example.userstoryapp.viewmodel.MainViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences

    private val mainViewModel by viewModels<MainViewModel> {
        val token = sharedPreferences.getString("token", "") ?: ""
        MainViewModelFactory(
            StoryRepository(
                ApiConfig.getApiService(),
                StoryDatabase.getDatabase(this),
                token
            )
        )
    }

    private val storyAdapter by lazy {
        AdapterStory { story -> navigateToDetail(story.id) }
    }

    private val startForResultAddStory =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                storyAdapter.refresh()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        setupUI()
        setupRecyclerView()
        observeViewModel()

        binding.btnAdd.setOnClickListener {
            startForResultAddStory.launch(Intent(this, AddScreenActivity::class.java))
        }
    }

    private fun setupUI() {
        val username = sharedPreferences.getString("username", "")
        binding.txtUsername.text = username
        binding.swipeRefreshLayout.setOnRefreshListener {
            storyAdapter.refresh()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewMain.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
            adapter = storyAdapter.withLoadStateHeaderAndFooter(
                header = LoadStateAdapter { storyAdapter.retry() },
                footer = LoadStateAdapter { storyAdapter.retry() }
            )
        }

        lifecycleScope.launch {
            storyAdapter.loadStateFlow.collectLatest { loadStates ->
                val refreshState = loadStates.refresh
                binding.progressStory.isVisible = refreshState is LoadState.Loading
                if (refreshState is LoadState.Error) {
                    Toast.makeText(this@MainActivity, "Error: ${refreshState.error.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            mainViewModel.getStories().collectLatest {
                storyAdapter.submitData(it)
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun navigateToDetail(id: String) {
        Log.d("MainActivity", "Navigating to detail with ID: $id")
        startActivity(Intent(this, DetailScreenActivity::class.java).apply {
            putExtra("story_id", id)
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.logout -> {
                performLogout()
                true
            }
            R.id.maps -> {
                startActivity(Intent(this, MapsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun performLogout() {
        sharedPreferences.edit().clear().apply()
        startActivity(Intent(this, LoginScreenActivity::class.java))
        finish()
    }
}
