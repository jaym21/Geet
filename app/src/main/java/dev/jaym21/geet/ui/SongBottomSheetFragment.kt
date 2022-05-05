package dev.jaym21.geet.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.jaym21.geet.databinding.FragmentSongBottomSheetBinding
import dev.jaym21.geet.models.Song
import dev.jaym21.geet.utils.Constants
import dev.jaym21.geet.utils.SongUtils
import dev.jaym21.geet.viewmodels.MainViewModel

class SongBottomSheetFragment(private val mainViewModel: MainViewModel, private val lifecycleOwner: LifecycleOwner) : BottomSheetDialogFragment() {

    private var _binding: FragmentSongBottomSheetBinding? = null
    private val binding: FragmentSongBottomSheetBinding
        get() = _binding!!
    private var song: Song? = null

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

        if (song != null) {

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
        } else {
            dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}