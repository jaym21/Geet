package dev.jaym21.geet.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jaym21.geet.models.SearchResult
import dev.jaym21.geet.repository.AlbumRepository
import dev.jaym21.geet.repository.ArtistRepository
import dev.jaym21.geet.repository.SongsRepository
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val songsRepository: SongsRepository,
    private val albumsRepository: AlbumRepository,
    private val artistsRepository: ArtistRepository
): ViewModel() {

    private val _searchResults = MutableLiveData<SearchResult>()
    val searchResults: LiveData<SearchResult> = _searchResults

    private val searchResultData = SearchResult()

    fun search(query: String) {
        if (query.length >= 3) {
            //searching for songs
            viewModelScope.launch {
                val songs = withContext(IO) {
                    songsRepository.searchSongs(query, 10)
                }
                if (songs.isNotEmpty()) {
                    searchResultData.songs = songs.toMutableList()
                }
                _searchResults.postValue(searchResultData)
            }

            //searching for albums
            viewModelScope.launch {
                val albums = withContext(IO) {
                    albumsRepository.searchAlbums(query, 7)
                }
                if (albums.isNotEmpty()) {
                    searchResultData.albums = albums.toMutableList()
                }
                _searchResults.postValue(searchResultData)
            }

            //searching for artists
            viewModelScope.launch {
                val artists = withContext(IO) {
                    artistsRepository.searchArtist(query, 7)
                }
                if (artists.isNotEmpty()) {
                    searchResultData.artists = artists.toMutableList()
                }
                _searchResults.postValue(searchResultData)
            }
        } else {
            _searchResults.postValue(searchResultData.clearResults())
        }
    }
}