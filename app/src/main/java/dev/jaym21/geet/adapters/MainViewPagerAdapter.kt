package dev.jaym21.geet.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import dev.jaym21.geet.ui.AlbumsFragment
import dev.jaym21.geet.ui.ArtistsFragment
import dev.jaym21.geet.ui.GenresFragment
import dev.jaym21.geet.ui.PlaylistsFragment
import dev.jaym21.geet.ui.SongsFragment
import dev.jaym21.geet.utils.Constants

class MainViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle): FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return Constants.MAIN_VIEW_PAGER_SIZE
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return SongsFragment()
            1 -> return AlbumsFragment()
            2 -> return ArtistsFragment()
            3 -> return GenresFragment()
        }
        return PlaylistsFragment()
    }

}