package dev.jaym21.geet.ui

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import dev.jaym21.geet.R
import dev.jaym21.geet.adapters.IPlaylistsRVAdapter
import dev.jaym21.geet.adapters.PlaylistsRVAdapter
import dev.jaym21.geet.databinding.FragmentAddToPlaylistBinding
import dev.jaym21.geet.models.MediaID
import dev.jaym21.geet.models.Playlist
import dev.jaym21.geet.utils.Constants

class AddToPlaylistFragment : BaseFragment(), IPlaylistsRVAdapter {

    private var _binding: FragmentAddToPlaylistBinding? = null
    private val binding: FragmentAddToPlaylistBinding
        get() = _binding!!
    private val playlistAdapter = PlaylistsRVAdapter(this)
    private var songId: Long? = null
    private var songTitle: String? = null

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
        songTitle = arguments?.getString(Constants.ADD_TO_PLAYLIST_SONG_TITLE_ARG)

        if (songId != null && songTitle != null) {

            setUpRecyclerView()

            mainViewModel.loadPlaylists()

            mainViewModel.playlists.observe(viewLifecycleOwner) {
                if (it.isNotEmpty()) {
                    binding.tvNoPlaylistAdded.visibility = View.GONE
                }
                playlistAdapter.submitList(it)
            }

            binding.btnCreateNewPlaylist.setOnClickListener {
                showCreateNewPlaylistDialog()
            }

            binding.ivBackButton.setOnClickListener {
                findNavController().popBackStack()
            }
        } else {
            findNavController().popBackStack()
        }
    }

    private fun showCreateNewPlaylistDialog() {
        val alertBuilder = AlertDialog.Builder(requireContext())
        val dialogLayout = layoutInflater.inflate(R.layout.create_playlist_dialog_layout, null)

        val btnCreate: TextView = dialogLayout.findViewById(R.id.tvCreateDialog)
        val btnCancel: TextView = dialogLayout.findViewById(R.id.tvCancelDialog)
        val nameEditText: EditText = dialogLayout.findViewById(R.id.etPlaylistName)

        alertBuilder.setView(dialogLayout)
        val createPlaylistDialog = alertBuilder.create()
        createPlaylistDialog.setCanceledOnTouchOutside(false)

        btnCreate.setOnClickListener {
            val playlistName = nameEditText.text.toString()
            if (playlistName.isNotEmpty()) {
                val playlistId = mainViewModel.createPlaylist(playlistName)
                val ids: LongArray
                if (songId == null) {
                    ids = LongArray(0)
                } else {
                    ids = LongArray(1)
                    ids[0] = songId!!
                }
                mainViewModel.addToPlaylist(playlistId, ids)
                Snackbar.make(binding.root, "Created $playlistName playlist and added song", Snackbar.LENGTH_SHORT).show()
            }
            createPlaylistDialog.dismiss()
            findNavController().popBackStack()
        }

        btnCancel.setOnClickListener {
            createPlaylistDialog.dismiss()
        }

        createPlaylistDialog.show()
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

        mainViewModel.getPlaylistSongs(MediaID.currentCaller, playlist.id)

        mainViewModel.playlistSongs.observe(viewLifecycleOwner) {
            Log.d("TAGYOYO", "songId: $songId")
            var isPresent = false
            for (i in it) {
                Log.d("TAGYOYO", "id ${i.title}")
                if (i.title == songTitle) {
                    isPresent = true
                }
            }
            Log.d("TAGYOYO", "AFTER: isPresent $isPresent")
            if (!isPresent) {
                mainViewModel.addToPlaylist(playlist.id, ids)
                Snackbar.make(binding.root, "Song added to ${playlist.name}", Snackbar.LENGTH_SHORT).show()
                findNavController().popBackStack()
            } else {
                Snackbar.make(binding.root, "Song already present in playlist", Snackbar.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}