package dev.jaym21.geet.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import dev.jaym21.geet.adapters.IPlaylistsRVAdapter
import dev.jaym21.geet.adapters.PlaylistsRVAdapter
import dev.jaym21.geet.databinding.FragmentAddToPlaylistBinding
import dev.jaym21.geet.models.Playlist
import dev.jaym21.geet.utils.Constants

class AddToPlaylistFragment : BaseFragment(), IPlaylistsRVAdapter {

    private var _binding: FragmentAddToPlaylistBinding? = null
    private val binding: FragmentAddToPlaylistBinding
        get() = _binding!!
    private val playlistAdapter = PlaylistsRVAdapter(this)
    private var songId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAddToPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        songId = arguments?.getLong(Constants.ADD_TO_PLAYLIST_SONG_ID_ARG)

        if (songId != null) {

            setUpRecyclerView()

            mainViewModel.loadPlaylists()

            mainViewModel.playlists.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    binding.tvNoPlaylistAdded.visibility = View.GONE
                }
                playlistAdapter.submitList(it)
            }

            binding.btnCreateNewPlaylist.setOnClickListener {

            }

        } else {
            findNavController().popBackStack()
        }
    }

    private fun setUpRecyclerView() {
        binding.apply {
            rvAllPlaylistsAdd.adapter = playlistAdapter
            rvAllPlaylistsAdd.layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
        }
    }

    override fun onPlaylistClicked(playlist: Playlist) {
        val ids: LongArray
        if (songId == null) {
            ids = LongArray(0)
        } else {
            ids = LongArray(1)
            ids[0] = songId!!
        }
        val inserted = mainViewModel.addToPlaylist(playlist.id, ids)
        Log.d("TAGYOYO", "onPlaylistClicked: $inserted")
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}