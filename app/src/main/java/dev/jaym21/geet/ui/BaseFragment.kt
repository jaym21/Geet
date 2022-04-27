package dev.jaym21.geet.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dev.jaym21.geet.R
import dev.jaym21.geet.extensions.safeActivity
import dev.jaym21.geet.viewmodels.MainViewModel
import dev.jaym21.geet.viewmodels.NavigationViewModel
import dev.jaym21.geet.viewmodels.NowPlayingViewModel

open class BaseFragment: Fragment() {

    protected val mainViewModel by activityViewModels<MainViewModel>()
    protected val navigationViewModel by activityViewModels<NavigationViewModel>()
    protected val nowPlayingViewModel by activityViewModels<NowPlayingViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}