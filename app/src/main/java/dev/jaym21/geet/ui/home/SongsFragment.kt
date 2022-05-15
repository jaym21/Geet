package dev.jaym21.geet.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dev.jaym21.geet.R
import dev.jaym21.geet.adapters.ISongsRVAdapter
import dev.jaym21.geet.adapters.SongsRVAdapter
import dev.jaym21.geet.databinding.FragmentSongsBinding
import dev.jaym21.geet.extensions.filter
import dev.jaym21.geet.extensions.getExtraBundle
import dev.jaym21.geet.extensions.toSongIds
import dev.jaym21.geet.models.Song
import dev.jaym21.geet.models.SortType
import dev.jaym21.geet.ui.BaseFragment
import dev.jaym21.geet.ui.SongBottomSheetFragment
import dev.jaym21.geet.utils.Constants

class SongsFragment : BaseFragment(), ISongsRVAdapter {

    private var _binding: FragmentSongsBinding? = null
    private val binding: FragmentSongsBinding
        get() = _binding!!
    private var songsAdapter: SongsRVAdapter? = null
    private var songs = listOf<Song>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSongsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        songsAdapter = SongsRVAdapter(this, this, nowPlayingViewModel)

        setUpRecyclerView()

        mainViewModel.loadSongs(SortType.A_Z)

        mainViewModel.sortType.observe(viewLifecycleOwner) {
            mainViewModel.loadSongs(it)
        }

        mainViewModel.songs.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) {
                binding.tvNoSongsText.visibility = View.VISIBLE
            } else {
                binding.tvNoSongsText.visibility = View.GONE
                songs = it
                songsAdapter?.submitList(it)
            }
        }

        playbackSessionViewModel?.mediaItems
            ?.filter { it.isNotEmpty() }
            ?.observe(this) {
                songsAdapter?.submitList(it as List<Song>)
            }
    }

    private fun setUpRecyclerView() {
        binding.apply {
            rvAllSongs.adapter = songsAdapter
            rvAllSongs.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
    }

    override fun onSongClicked(song: Song) {
        val extras = getExtraBundle(songs.toSongIds(), getString(R.string.all_songs))
        mainViewModel.mediaItemClicked(song, extras)
    }

    override fun onMoreMenuClicked(song: Song) {
        val songBottomSheetFragment = SongBottomSheetFragment(mainViewModel, viewLifecycleOwner, findNavController(), Constants.CLICK_FROM_ALL_SONG_DETAILS)
        val bundle = Bundle().apply {
            putParcelable(Constants.SONG_BOTTOM_SHEET_ARG, song)
        }
        songBottomSheetFragment.arguments = bundle
        songBottomSheetFragment.show(requireActivity().supportFragmentManager, "SongBottomSheetFragment")
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}