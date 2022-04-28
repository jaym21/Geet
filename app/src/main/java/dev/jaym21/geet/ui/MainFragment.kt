package dev.jaym21.geet.ui

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import dev.jaym21.geet.R
import dev.jaym21.geet.databinding.FragmentMainBinding
import dev.jaym21.geet.models.MainNavigationAction
import dev.jaym21.geet.models.MetaData
import dev.jaym21.geet.repository.SongsRepository
import dev.jaym21.geet.widgets.BottomSheetLayout
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : BaseFragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding
        get() = _binding!!
    @Inject lateinit var songsRepository: SongsRepository
    private var callback: DynamicBackPressedCallback? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, DynamicBackPressedCallback().also { callback = it })

        mainViewModel.rootMediaId.observe(this) {
            handlePlaybackIntent((activity as MainActivity).intent)
        }

        navigationViewModel.mainNavigationAction.observe(viewLifecycleOwner, ::handleMainNavigation)
        navigationViewModel.homeNavigationAction.observe(viewLifecycleOwner, ::handleHomeNavigation)

        nowPlayingViewModel.currentData.observe(this) {
            changePlaybackPanelState()
        }
    }

    private fun changePlaybackPanelState() {
        nowPlayingViewModel.currentData.value?.let {
            if (!it.title.isNullOrEmpty()) {
                when (binding.bottomSheetLayout.getPanelState()) {
                    BottomSheetLayout.PanelState.EXPANDED -> binding.bottomSheetLayout.hide()
                    BottomSheetLayout.PanelState.HIDDEN -> binding.bottomSheetLayout.show()
                    else -> binding.bottomSheetLayout.show()
                }
            } else {
                binding.bottomSheetLayout.hide()
            }
        }
    }

    private fun handlePlaybackIntent(intent: Intent?) {
        if (intent == null || intent.action == null) return

        when (intent.action!!) {
            MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH -> {
                val songTitle = intent.extras?.getString(MediaStore.EXTRA_MEDIA_TITLE, null)
                mainViewModel.transportControls().playFromSearch(songTitle, null)
            }
            Intent.ACTION_VIEW -> {
                val path = (activity as MainActivity).intent.data?.path ?: return
                val song = songsRepository.getSongFromPath(path)
                mainViewModel.mediaItemClicked(song, null)
            }
        }
    }

    private fun handleMainNavigation(action: MainNavigationAction?) {
        if (action == null)
            return

        when (action) {
            MainNavigationAction.EXPAND -> binding.bottomSheetLayout.expand()

            MainNavigationAction.COLLAPSE -> binding.bottomSheetLayout.collapse()

            MainNavigationAction.QUEUE -> findNavController().navigate(R.id.action_mainFragment_to_queueFragment)
        }

        navigationViewModel.finishMainNavigation()
    }

    private fun handleHomeNavigation(metaData: MetaData?) {
        if (metaData != null) {
            binding.bottomSheetLayout.collapse()
        }
    }

    inner class DynamicBackPressedCallback: OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            if (!binding.bottomSheetLayout.collapse()) {
                val homeNavController = binding.homeNavHost.findNavController()

                if (homeNavController.currentDestination?.id == homeNavController.graph.startDestinationId) {
                    isEnabled = false
                    requireActivity().onBackPressed()
                    isEnabled = true
                } else {
                    homeNavController.navigateUp()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        callback?.isEnabled = true
    }

    override fun onPause() {
        super.onPause()
        callback?.isEnabled = false
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}