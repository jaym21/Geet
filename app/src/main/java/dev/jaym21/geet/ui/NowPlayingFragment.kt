package dev.jaym21.geet.ui

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dev.jaym21.geet.databinding.FragmentNowPlayingBinding

class NowPlayingFragment : BaseFragment() {

    private var _binding: FragmentNowPlayingBinding? = null
    private val binding: FragmentNowPlayingBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentNowPlayingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        attachOnClickListeners()
    }

    override fun onResume() {
        super.onResume()
        //passing mediaController to seekBar and current time text view to update it
        mainViewModel.mediaController.observe(this) { mediaController ->
            binding.tvCurrentTime.setMediaController(mediaController)
            binding.seekBar.setMediaController(mediaController)
        }
    }

    override fun onStop() {
        binding.seekBar.disconnectMediaController()
        binding.tvCurrentTime.disconnectMediaController()
        super.onStop()
    }

    private fun attachOnClickListeners() {
        binding.ivPlayPause.setOnClickListener {
            nowPlayingViewModel.currentData.value?.let { mediaData ->
                mainViewModel.mediaItemClicked(mediaData.toDummySong(), null)
            }
        }

        binding.ivNext.setOnClickListener {
            mainViewModel.transportControls().skipToNext()
        }

        binding.ivPrevious.setOnClickListener {
            mainViewModel.transportControls().skipToPrevious()
        }

        binding.ivRepeat.setOnClickListener {
            //moving in cyclic manner between repeat modes according to the previous mode from currentData (NONE -> ONE -> ALL)
            when (nowPlayingViewModel.currentData.value?.repeatMode) {
                PlaybackStateCompat.REPEAT_MODE_NONE -> mainViewModel.transportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ONE)

                PlaybackStateCompat.REPEAT_MODE_ONE -> mainViewModel.transportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL)

                PlaybackStateCompat.REPEAT_MODE_ALL -> mainViewModel.transportControls().setRepeatMode(PlaybackStateCompat.REPEAT_MODE_NONE)
            }
        }

        binding.ivShuffle.setOnClickListener{
            when (nowPlayingViewModel.currentData.value?.shuffleMode) {
                PlaybackStateCompat.SHUFFLE_MODE_NONE -> mainViewModel.transportControls().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_ALL)

                PlaybackStateCompat.SHUFFLE_MODE_ALL -> mainViewModel.transportControls().setShuffleMode(PlaybackStateCompat.SHUFFLE_MODE_NONE)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}