package dev.jaym21.geet.ui.nowplaying

import android.graphics.Bitmap
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import dev.jaym21.geet.R
import dev.jaym21.geet.databinding.FragmentNowPlayingBinding
import dev.jaym21.geet.models.MainNavigationAction
import dev.jaym21.geet.ui.BaseFragment
import dev.jaym21.geet.utils.SongUtils

class NowPlayingFragment : BaseFragment() {

    private var _binding: FragmentNowPlayingBinding? = null
    private val binding: FragmentNowPlayingBinding
        get() = _binding!!
    private var gradientDrawable: GradientDrawable? = null

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

        nowPlayingViewModel.currentData.observe(this) {
            binding.tvSongTitle.text = it.title
            binding.tvSongArtist.text = it.artist
            Glide.with(requireContext()).load(it.artwork).transform(RoundedCorners(12)).into(binding.ivSongArtwork)

            if(it.artwork != null) {
                changeBackground(it.artwork!!)
            } else {
                changeBackground((0xFF616261).toInt())
            }

            if (it.position != null)
                binding.tvCurrentTime.text = SongUtils.formatTimeStringShort(it.position!!.toLong())

            if (it.duration != null)
                binding.tvDuration.text = SongUtils.formatTimeStringShort(it.duration!!.toLong())

            if (it.state == PlaybackStateCompat.STATE_PLAYING) {
                binding.ivPlayPause.setImageResource(R.drawable.ic_pause_circle)
            } else {
                binding.ivPlayPause.setImageResource(R.drawable.ic_play_circle)
            }

            when (it.repeatMode) {
                PlaybackStateCompat.REPEAT_MODE_NONE -> {
                    binding.ivRepeat.setImageResource(R.drawable.ic_repeat)
                    binding.ivRepeat.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white_alpha_60))
                }
                PlaybackStateCompat.REPEAT_MODE_ONE -> {
                    binding.ivRepeat.setImageResource(R.drawable.ic_repeat_one)
                    binding.ivRepeat.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                }
                PlaybackStateCompat.REPEAT_MODE_ALL -> {
                    binding.ivRepeat.setImageResource(R.drawable.ic_repeat)
                    binding.ivRepeat.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                }
            }

            when (it.shuffleMode) {
                PlaybackStateCompat.SHUFFLE_MODE_NONE -> {
                    binding.ivShuffle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white_alpha_60))
                }
                PlaybackStateCompat.SHUFFLE_MODE_ALL -> {
                    binding.ivShuffle.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                }
            }
        }

        nowPlayingViewModel.queueData.observe(this){
            binding.tvQueueTitle.text = it.queueTitle
        }

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

        binding.ivOpenQueue.setOnClickListener {
            navigationViewModel.mainNavigateTo(MainNavigationAction.QUEUE)
        }

        binding.ivBackButton.setOnClickListener {
            navigationViewModel.mainNavigateTo(MainNavigationAction.COLLAPSE)
        }
    }

    private fun changeBackground(bitmap: Bitmap) {
        val getColorPaletteFromSongImage = Palette.from(bitmap).generate()
        changeBackground(SongUtils.getBackgroundColorFromPalette(getColorPaletteFromSongImage))
    }

    private fun changeBackground(color: Int) {
        gradientDrawable = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(color, ContextCompat.getColor(requireContext(), R.color.bottom_gradient)))
        binding.clNowPlayingRoot.background = gradientDrawable
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}