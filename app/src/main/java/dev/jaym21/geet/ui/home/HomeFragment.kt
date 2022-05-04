package dev.jaym21.geet.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import dev.jaym21.geet.R
import dev.jaym21.geet.adapters.MainViewPagerAdapter
import dev.jaym21.geet.databinding.FragmentHomeBinding
import dev.jaym21.geet.extensions.filter
import dev.jaym21.geet.extensions.map
import dev.jaym21.geet.models.MediaID
import dev.jaym21.geet.repository.SongsRepository
import dev.jaym21.geet.ui.BaseFragment
import dev.jaym21.geet.utils.Constants
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding: FragmentHomeBinding
        get() = _binding!!
    @Inject lateinit var songsRepository: SongsRepository
    private lateinit var mainViewPagerAdapter: MainViewPagerAdapter
    private var tabs = arrayOf("Songs", "Albums", "Artists", "Playlists", "Genres")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewPagerAdapter = MainViewPagerAdapter(childFragmentManager, lifecycle)

        binding.vpMain.adapter = mainViewPagerAdapter

        //integrating tabLayout
        TabLayoutMediator(binding.tlMain, binding.vpMain) { tab, position ->
            tab.text = tabs[position]
        }.attach()


        mainViewModel.navigateToMediaItem
            .map { it.getContentIfNotHandled() }
            .filter { it != null }
            .observe(this) {
                val mediaID = it
                if (mediaID != null) {
                    navigateToMediaItem(mediaID)
                }
            }
    }

    private fun navigateToMediaItem(mediaId: MediaID) {
        when (mediaId.type?.toInt()) {
            Constants.ALL_SONGS_MODE -> {

            }
            Constants.ALL_ALBUMS_MODE -> {

            }
            Constants.ALL_ARTISTS_MODE -> {

            }
            Constants.ALL_PLAYLISTS_MODE -> {

            }
            Constants.ALL_GENRES_MODE -> {

            }
            Constants.ARTIST_MODE -> {

            }
            Constants.ALBUM_MODE -> {
                findNavController().navigate(R.id.action_homeFragment_to_albumDetailsFragment)
            }
            Constants.PLAYLIST_MODE -> {

            }
            Constants.GENRE_MODE -> {

            }
            else -> {}
        }
    }
}