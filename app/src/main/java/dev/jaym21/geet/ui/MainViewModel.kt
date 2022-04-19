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

    private val _songs: MutableLiveData<List<Song>> =  MutableLiveData()
    val songs: LiveData<List<Song>> = _songs

    private val _songsForIds: MutableLiveData<List<Song>> =  MutableLiveData()
    val songsForIds: LiveData<List<Song>> = _songsForIds

    private val _songForId: MutableLiveData<Song> = MutableLiveData()
    val songForId: LiveData<Song> = _songForId

    fun loadSongs() = viewModelScope.launch(Dispatchers.IO) {
        _songs.postValue(repository.getSongs())
    }

    fun getSongForIds(ids: LongArray) = viewModelScope.launch {
        _songsForIds.postValue(repository.getSongsForIds(ids))
    }

    fun getSongForId(id: Long) = viewModelScope.launch {
        _songForId.postValue(repository.getSongForId(id))
    }
}