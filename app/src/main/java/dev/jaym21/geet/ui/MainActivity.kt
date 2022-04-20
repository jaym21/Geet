package dev.jaym21.geet.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.tabs.TabLayoutMediator
import dev.jaym21.geet.R
import dev.jaym21.geet.adapters.MainViewPagerAdapter
import dev.jaym21.geet.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!
    private lateinit var mainViewPagerAdapter: MainViewPagerAdapter
    var tabs = arrayOf("Songs", "Albums", "Artists", "Genres", "Playlists")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewPagerAdapter = MainViewPagerAdapter(supportFragmentManager, lifecycle)

        binding.vpMain.adapter = mainViewPagerAdapter

        //integrating tabLayout
        TabLayoutMediator(binding.tlMain, binding.vpMain) { tab, position ->
            tab.text = tabs[position]
        }.attach()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}