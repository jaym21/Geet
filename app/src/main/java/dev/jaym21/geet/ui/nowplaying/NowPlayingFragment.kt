package dev.jaym21.geet.ui.nowplaying

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
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
    private var currentSong: Song? = null
    private var playbackService: PlaybackService? = null
    private var playIntent: Intent? = null
    private var queuedSongs = listOf<Song>()
    private var isSongBound = false
    private var currentPlayingPosition = 0

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

        currentSong = arguments?.getParcelable("currentSong")

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        if (currentSong != null) {
            attachListeners()
            val queueIds = PreferencesHelper.getQueueIds(requireContext())
            viewModel.getSongForIds(queueIds)

            if (playIntent == null) {
                playIntent = Intent(requireContext(), PlaybackService::class.java)
                requireContext().bindService(playIntent, songConnection, Context.BIND_AUTO_CREATE)
                requireContext().startService(playIntent)
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
                    currentPlayingPosition = newPosition
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

    private fun attachListeners() {
        viewModel.songsForIds.observe(viewLifecycleOwner) {
            queuedSongs = it
        }

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
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().stopService(playIntent)
        requireContext().unbindService(songConnection)
        playbackService = null
        _binding = null
    }
}