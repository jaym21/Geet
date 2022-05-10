package dev.jaym21.geet.models

data class SearchResult (
    var songs: MutableList<Song> = mutableListOf(),
    var albums: MutableList<Album> = mutableListOf(),
    var artists: MutableList<Artist> = mutableListOf()
) {

    fun clearResults(): SearchResult {
        songs.clear()
        albums.clear()
        artists.clear()
        return this
    }
}