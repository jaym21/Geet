package dev.jaym21.geet.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import dev.jaym21.geet.models.Song
import dev.jaym21.geet.repository.SongsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(application: Application): AndroidViewModel(application) {

    private val repository = SongsRepository(application.applicationContext)

    private val _songs =  MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> = _songs


    fun loadSongs() = viewModelScope.launch(Dispatchers.IO) {
        _songs.postValue(repository.getSongs())
    }
}