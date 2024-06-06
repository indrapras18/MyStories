package com.example.userstoryapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.userstoryapp.data.database.StoryEntity
import com.example.userstoryapp.data.database.StoryRepository
import kotlinx.coroutines.flow.Flow

class MainViewModel(private val repository: StoryRepository) : ViewModel() {

    fun getStories(): Flow<PagingData<StoryEntity>> {
        return repository.getStories().cachedIn(viewModelScope)
    }
}