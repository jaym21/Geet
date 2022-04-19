package dev.jaym21.geet.ui.nowplaying

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.palette.graphics.Palette
import dev.jaym21.geet.R
import dev.jaym21.geet.databinding.FragmentNowPlayingBinding
import dev.jaym21.geet.models.Song
import dev.jaym21.geet.services.PlaybackService
import dev.jaym21.geet.ui.MainViewModel
import dev.jaym21.geet.utils.Constants
import dev.jaym21.geet.utils.PreferencesHelper
import dev.jaym21.geet.utils.SongUtils
import kotlinx.coroutines.launch

class NowPlayingFragment : Fragment() {

    private var _binding: FragmentNowPlayingBinding? = null
    private val binding: FragmentNowPlayingBinding
        get() = _binding!!
    private lateinit var viewModel: MainViewModel
    private var currentSongPosition: Int = 0
    private var currentSong: Song? = null
    private var playbackService: PlaybackService? = null
    private var playIntent: Intent? = null
    private var queuedSongs = listOf<Song>()
    private var gradientDrawable: GradientDrawable? = null
    private var isSongBound = false

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

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        val queueIds = PreferencesHelper.getQueueIds(requireContext())
        for (i in queueIds) {
            Log.d("TAGYOYO", "queueIds: $i")
        }
        viewModel.getSongForIds(queueIds)

        viewModel.songsForIds.observe(viewLifecycleOwner) {
            Log.d("TAGYOYO", "queuedSongs: $it")
            queuedSongs = it
        }

        if(queuedSongs.isNotEmpty()) {
            initialize()
            attachListeners()

            if (playIntent == null) {
                playIntent = Intent(requireContext(), PlaybackService::class.java)
                requireContext().bindService(playIntent, songConnection, Context.BIND_AUTO_CREATE)
                requireContext().startService(playIntent)
            }
        }
    }

    private fun initialize() {
        val albumArtBitmap = SongUtils.getAlbumArtBitmap(requireContext(), queuedSongs[currentSongPosition].albumId)
        if (albumArtBitmap != null)
            changeBackground(albumArtBitmap)
        else
            changeBackground((0xFF616261).toInt())

        binding.tvSongTitle.text = queuedSongs[currentSongPosition].title
        binding.tvSongArtist.text = queuedSongs[currentSongPosition].artist
        binding.tvDuration.text = SongUtils.formatTimeStringShort(requireContext(), queuedSongs[currentSongPosition].duration)

        binding.seekBar.max = (queuedSongs[currentSongPosition].duration / 100).toInt()

        if (PreferencesHelper.getIsRepeatOn(requireContext())) {
            binding.ivRepeat.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorAccent))
        }

    }

    private fun attachListeners() {

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, i: Int, b: Boolean) {
                if (playbackService != null && b) {
                    playbackService?.seek(i * 100)
                }
                binding.tvCurrentTime.text = SongUtils.formatTimeStringShort(requireContext(), (i * 100).toLong())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                playbackService?.pauseSong()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                playbackService?.playSong()
            }
        })

        binding.ivPlay.setOnClickListener {
            if (playbackService?.songState != Constants.SONG_LOADED) {
                if (playbackService?.isPlaying() == true) {
                    playbackService?.pauseSong()
                } else {
                    playbackService?.playSong()
                }
            }
        }

        binding.ivPrevious.setOnClickListener {
            if (currentSongPosition - 1 >= 0) {
                currentSongPosition--
            }
            playbackService?.playPreviousSong()
        }

        binding.ivNext.setOnClickListener {
            if (currentSongPosition + 1 < queuedSongs.size) {
                currentSongPosition++
            }
            playbackService?.playNextSong()
        }

        binding.ivRepeat.setOnClickListener {
            if (PreferencesHelper.getIsRepeatOn(requireContext())) {
                binding.ivRepeat.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white))
                PreferencesHelper.setIsRepeatOn(requireContext(), false)
            } else {
                binding.ivRepeat.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorAccent))
                PreferencesHelper.setIsRepeatOn(requireContext(), true)
            }
        }
    }

    private val songConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
            val binder = service as PlaybackService.PlaybackBinder
            playbackService = binder.getService()
            isSongBound = true

            playbackService?.setViewSongInterface(object : PlaybackService.ViewSongInterface {
                override fun onSongDisturbed(state: String, song: Song) {
                    when(state) {
                        Constants.SONG_LOADED -> {
                            binding.tvSongTitle.text = song.title
                            binding.tvSongArtist.text = song.artist
                            binding.tvDuration.text =  SongUtils.formatTimeStringShort(requireContext(), song.duration)
                            binding.tvCurrentTime.text = SongUtils.formatTimeStringShort(requireContext(), 0)
                            binding.seekBar.progress = 0
                            binding.seekBar.isEnabled = false
                            binding.ivPlay.isClickable = false
                            binding.seekBar.max = (song.duration / 100).toInt()
                            binding.ivPlay.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_pause))
                        }
                        Constants.SONG_STARTED -> {
                            binding.ivPlay.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_pause))
                            binding.seekBar.isEnabled = true
                            binding.ivPlay.isClickable = true
                        }
                        Constants.SONG_PLAYED -> {
                            binding.ivPlay.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_pause))
                        }
                        Constants.SONG_PAUSED -> {
                            binding.ivPlay.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_play))
                        }
                        Constants.SONG_ENDED -> {
                            binding.ivPlay.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_play))
                        }
                    }
                }

                override fun onSongChanged(newPosition: Int) {
                    currentSongPosition = newPosition
                }

                override fun onSongProgress(position: Int) {
                    binding.seekBar.progress = position
                }
            })
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isSongBound = false
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
        if (playIntent != null) {
            requireContext().stopService(playIntent)
            requireContext().unbindService(songConnection)
            playbackService = null
        }
        _binding = null
    }
}