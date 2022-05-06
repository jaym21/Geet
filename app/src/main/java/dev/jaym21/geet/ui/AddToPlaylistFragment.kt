package dev.jaym21.geet.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dev.jaym21.geet.R
import dev.jaym21.geet.databinding.FragmentAddToPlaylistBinding

class AddToPlaylistFragment : Fragment() {

    private var _binding: FragmentAddToPlaylistBinding? = null
    private val binding: FragmentAddToPlaylistBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAddToPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}