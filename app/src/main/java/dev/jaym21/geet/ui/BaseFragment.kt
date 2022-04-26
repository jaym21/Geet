package dev.jaym21.geet.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import dev.jaym21.geet.R
import dev.jaym21.geet.extensions.safeActivity

open class BaseFragment: Fragment() {

    protected val mainViewModel by activityViewModels<MainViewModel>()
    protected val nowPlayingViewModel by activityViewModels<NowPlayingViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nowPlayingViewModel.currentData.observe(this) {
          changeBottomSheetState()
        }
    }

    override fun onPause() {
        super.onPause()
    }

    private fun changeBottomSheetState() {
        nowPlayingViewModel.currentData.value?.let {
            val activity = safeActivity as MainActivity
            if (!it.title.isNullOrEmpty()) {
                if (activity.supportFragmentManager.findFragmentById(R.id.mainContainer) is NowPlayingFragment) {
                    activity.hideBottomSheet()
                } else {
                    activity.showBottomSheet()
                }
            } else {
                activity.hideBottomSheet()
            }
        }
    }
}