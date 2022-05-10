package dev.jaym21.geet.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dev.jaym21.geet.R
import dev.jaym21.geet.adapters.*
import dev.jaym21.geet.databinding.FragmentSearchBinding
import dev.jaym21.geet.extensions.getExtraBundle
import dev.jaym21.geet.extensions.toSongIds
import dev.jaym21.geet.models.Album
import dev.jaym21.geet.models.Artist
import dev.jaym21.geet.models.Song
import dev.jaym21.geet.utils.Constants
import dev.jaym21.geet.viewmodels.MainViewModel
import dev.jaym21.geet.viewmodels.SearchViewModel

class SearchFragment : BaseFragment(), IAlbumsRVAdapter, IArtistsRVAdapter, ISongsRVAdapter {

    private var _binding: FragmentSearchBinding? = null
    private val binding: FragmentSearchBinding
        get() = _binding!!
    private val searchViewModel by activityViewModels<SearchViewModel>()
    private var songsAdapter: SongsRVAdapter? = null
    private var albumAdapter = AlbumsRVAdapter(this)
    private var artistAdapter = ArtistsRVAdapter(this)
    private var songs = listOf<Song>()
    private var albums = listOf<Album>()
    private var artists = listOf<Artist>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        songsAdapter = SongsRVAdapter(this, this, nowPlayingViewModel)

        setUpRecyclerView()

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(cs: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun onTextChanged(cs: CharSequence?, p1: Int, p2: Int, p3: Int) {
                searchViewModel.search(cs.toString())
            }

            override fun afterTextChanged(p0: Editable?) {
                songsAdapter?.submitList(emptyList())
            }
        })

        searchViewModel.searchResults.observe(viewLifecycleOwner) {
            songs = it.songs
            albums = it.albums
            artists = it.artists

            songsAdapter?.submitList(it.songs)
            albumAdapter.submitList(it.albums)
            artistAdapter.submitList(it.artists)

            if (it.songs.isNullOrEmpty()) {
                binding.tvNoSongsText.visibility = View.VISIBLE
            } else {
                binding.tvNoSongsText.visibility = View.GONE
            }
            if (it.albums.isNullOrEmpty()) {
                binding.tvNoAlbumsText.visibility = View.VISIBLE
            } else {
                binding.tvNoAlbumsText.visibility = View.GONE
            }
            if (it.artists.isNullOrEmpty()) {
                binding.tvNoArtistsText.visibility = View.VISIBLE
            } else {
                binding.tvNoArtistsText.visibility = View.GONE
            }

            if (it.songs.isNullOrEmpty() && it.albums.isNullOrEmpty() && it.artists.isNullOrEmpty()) {
                binding.tvNoResultsText.visibility = View.VISIBLE
            } else {
                binding.tvNoResultsText.visibility = View.GONE
            }
        }

        binding.ivBackButton.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setUpRecyclerView() {
        binding.apply {
            rvSongsSearch.adapter = songsAdapter
            rvSongsSearch.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            rvAlbumsSearch.adapter = albumAdapter
            rvAlbumsSearch.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            rvArtistSearch.adapter = artistAdapter
            rvArtistSearch.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
    }

    override fun onSongClicked(song: Song) {
        val extras = getExtraBundle(songs.toSongIds(), getString(R.string.all_songs))
        mainViewModel.mediaItemClicked(song, extras)
    }

    override fun onMoreMenuClicked(song: Song) {
        val songBottomSheetFragment = SongBottomSheetFragment(mainViewModel, viewLifecycleOwner, findNavController())
        val bundle = Bundle().apply {
            putParcelable(Constants.SONG_BOTTOM_SHEET_ARG, song)
        }
        songBottomSheetFragment.arguments = bundle
        songBottomSheetFragment.show(requireActivity().supportFragmentManager, "SongBottomSheetFragment")
    }

    override fun onAlbumClick(album: Album) {
        mainViewModel.mediaItemClicked(album, null)
    }

    override fun onArtistClick(artist: Artist) {
        mainViewModel.mediaItemClicked(artist, null)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}