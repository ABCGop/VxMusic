package com.abcg.music.viewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.abcg.music.common.SELECTED_LANGUAGE
import com.abcg.music.data.model.explore.mood.genre.GenreObject
import com.abcg.music.utils.Resource
import com.abcg.music.viewModel.base.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class GenreViewModel(
    application: Application,
) : BaseViewModel(application) {
    private val _genreObject: MutableLiveData<Resource<GenreObject>> = MutableLiveData()
    var genreObject: LiveData<Resource<GenreObject>> = _genreObject
    var loading = MutableLiveData<Boolean>()

    private var regionCode: String? = null
    private var language: String? = null

    init {
        regionCode = runBlocking { dataStoreManager.location.first() }
        language = runBlocking { dataStoreManager.getString(SELECTED_LANGUAGE).first() }
    }

    fun getGenre(params: String) {
        loading.value = true
        viewModelScope.launch {
            mainRepository.getGenreData(params).collect { values ->
                _genreObject.value = values
            }
            withContext(Dispatchers.Main) {
                loading.value = false
            }
        }
    }
}