package dev.jaym21.geet.ui

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import dev.jaym21.geet.R
import dev.jaym21.geet.databinding.FragmentPlaybackBarBinding
import dev.jaym21.geet.models.MainNavigationAction


class PlaybackBarFragment : BaseFragment() {

    private var _binding: FragmentPlaybackBarBinding? = null
    private val binding: FragmentPlaybackBarBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPlaybackBarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        nowPlayingViewModel.currentData.observe(this) {
            binding.tvSongNameBar.text = it.title
            binding.tvSongArtistBar.text = it.artist
            Glide.with(requireContext()).load(it.artwork).into(binding.ivArtworkBar)
            if (it.state == PlaybackStateCompat.STATE_PLAYING) {
                binding.ivPlayPauseBar.setImageResource(R.drawable.ic_pause)
            } else {
                binding.ivPlayPauseBar.setImageResource(R.drawable.ic_play)
            }
        }

        attachClickListeners()
    }

    override fun onResume() {
        super.onResume()
        //passing mediaController to seekBar and current time text view to update it
        mainViewModel.mediaController.observe(this) { mediaController ->
            binding.playbackProgressBar.setMediaController(mediaController)
        }
    }

    override fun onStop() {
        binding.playbackProgressBar.disconnectMediaController()
        super.onStop()
    }
    private fun attachClickListeners() {

        binding.ivPlayPauseBar.setOnClickListener {
            nowPlayingViewModel.currentData.value?.let { metaData ->
                mainViewModel.mediaItemClicked(metaData.toDummySong(), null)
            }
        }

        binding.ivNextBar.setOnClickListener {
            mainViewModel.transportControls().skipToNext()
        }

        binding.ivPreviousBar.setOnClickListener {
            mainViewModel.transportControls().skipToPrevious()
        }

        binding.clPlaybackBarRoot.setOnClickListener {
            navigationViewModel.mainNavigateTo(MainNavigationAction.EXPAND)
        }
    }
}