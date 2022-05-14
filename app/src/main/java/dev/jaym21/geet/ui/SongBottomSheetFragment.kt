package dev.jaym21.geet.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.snackbar.Snackbar
import dev.jaym21.geet.R
import dev.jaym21.geet.databinding.FragmentSongBottomSheetBinding
import dev.jaym21.geet.models.Song
import dev.jaym21.geet.utils.Constants
import dev.jaym21.geet.utils.SongUtils
import dev.jaym21.geet.viewmodels.MainViewModel

class SongBottomSheetFragment(private val mainViewModel: MainViewModel, private val lifecycleOwner: LifecycleOwner, private val navController: NavController, private val clickFrom: String) : BottomSheetDialogFragment() {

    private var _binding: FragmentSongBottomSheetBinding? = null
    private val binding: FragmentSongBottomSheetBinding
        get() = _binding!!
    private var song: Song? = null
    private var playlistId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSongBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        song = arguments?.getParcelable(Constants.SONG_BOTTOM_SHEET_ARG)

        playlistId = arguments?.getLong(Constants.PLAYLIST_ID_BOTTOM_SHEET_ARG)

        if (song != null) {

            if (clickFrom == Constants.CLICK_FROM_PLAYLIST_DETAILS) {
                binding.llRemoveFromPlaylist.visibility = View.VISIBLE
            }

            binding.tvSongNameSheet.text = song?.title
            binding.tvArtistNameSheet.text = song?.artist
            val albumArtUri = SongUtils.getAlbumArtUri(song?.albumId!!)
            Glide.with(requireContext()).load(albumArtUri).transform(RoundedCorners(12)).into(binding.ivSongArtworkSheet)

            binding.llGoToAlbum.setOnClickListener {
                mainViewModel.goToAlbum(song!!)
                dismiss()
            }

            binding.llGoToArtist.setOnClickListener {
                mainViewModel.goToArtist(song!!)
                dismiss()
            }

            binding.llAddToPlaylist.setOnClickListener {
                val bundle = Bundle().apply {
                    putLong(Constants.ADD_TO_PLAYLIST_SONG_ID_ARG, song?.id!!)
                    putString(Constants.ADD_TO_PLAYLIST_SONG_TITLE_ARG, song?.title)
                }

                when (clickFrom) {
                    Constants.CLICK_FROM_ALL_SONG_DETAILS -> {
                        navController.navigate(R.id.action_homeFragment_to_addToPlaylistFragment, bundle)
                    }
                    Constants.CLICK_FROM_ALBUM_DETAILS -> {
                        navController.navigate(R.id.action_albumDetailsFragment_to_addToPlaylistFragment, bundle)
                    }
                    Constants.CLICK_FROM_ARTIST_DETAILS -> {
                        navController.navigate(R.id.action_artistDetailsFragment_to_addToPlaylistFragment, bundle)
                    }
                    Constants.CLICK_FROM_PLAYLIST_DETAILS -> {
                        navController.navigate(R.id.action_playlistDetailsFragment_to_addToPlaylistFragment, bundle)
                    }
                    Constants.CLICK_FROM_SEARCH -> {
                        navController.navigate(R.id.action_searchFragment_to_addToPlaylistFragment, bundle)
                    }
                }

                dismiss()
            }

            binding.llRemoveFromPlaylist.setOnClickListener {
                playlistId?.let { id -> mainViewModel.deleteSongFromPlaylist(song?.id!!, id) }
                Snackbar.make(binding.root, "${song?.title} deleted from playlist", Snackbar.LENGTH_SHORT).show()
                dismiss()
            }

        } else {
            dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}