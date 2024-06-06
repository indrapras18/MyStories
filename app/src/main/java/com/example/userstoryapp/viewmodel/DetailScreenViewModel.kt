package com.example.userstoryapp.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.userstoryapp.data.network.ApiConfig
import com.example.userstoryapp.helper.ResultStory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailScreenViewModel : ViewModel() {

    val resultDetailStory = MutableLiveData<ResultStory>()

fun getStoryDetail(token: String, id: String) {
    viewModelScope.launch {
        try {
            resultDetailStory.value = ResultStory.Loading(true)
            val response = withContext(Dispatchers.IO) {
                ApiConfig.getApiService().getStoryDetail("Bearer $token", id)
            }
            resultDetailStory.value = ResultStory.Loading(false)

            if (response != null && !response.error && response.story != null) {
                resultDetailStory.value = ResultStory.Success(response.story)
            } else {
                resultDetailStory.value = ResultStory.Error(Exception("Detail story not found"))
            }
        } catch (e: Exception) {
            Log.e("DetailScreenViewModel", "Error fetching story details", e)
            resultDetailStory.value = ResultStory.Loading(false)
            resultDetailStory.value = ResultStory.Error(Exception("Failed to fetch detail story"))
        }
    }
}

}