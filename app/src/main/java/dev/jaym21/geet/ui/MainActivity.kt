package dev.jaym21.geet.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import dev.jaym21.geet.R
import dev.jaym21.geet.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding!!
    var tabs = arrayOf("Songs", "Albums", "Artists", "Genres", "Playlists")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}