package dev.jaym21.geet.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dev.jaym21.geet.R
import dev.jaym21.geet.databinding.FragmentPlaybackBarBinding


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

        attachClickListeners()
    }

    private fun attachClickListeners() {

        binding.ivPlayPauseBottomSheet.setOnClickListener {
            nowPlayingViewModel.currentData.value?.let { metaData ->
                mainViewModel.mediaItemClicked(metaData.toDummySong(), null)
            }
        }

        binding.ivNextBottomSheet.setOnClickListener {
            mainViewModel.transportControls().skipToNext()
        }

        binding.ivPreviousBottomSheet.setOnClickListener {
            mainViewModel.transportControls().skipToPrevious()
        }

        (activity as? MainActivity)?.let { mainActivity ->
            binding
        }
    }
}