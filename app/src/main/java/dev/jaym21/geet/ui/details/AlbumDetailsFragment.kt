package dev.jaym21.geet.ui.details

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dev.jaym21.geet.R
import dev.jaym21.geet.databinding.FragmentAlbumDetailsBinding


class AlbumDetailsFragment : Fragment() {

    private var _binding: FragmentAlbumDetailsBinding? = null
    private val binding: FragmentAlbumDetailsBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAlbumDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}