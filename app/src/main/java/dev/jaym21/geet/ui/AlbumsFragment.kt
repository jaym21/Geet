package dev.jaym21.geet.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import dev.jaym21.geet.R
import dev.jaym21.geet.adapters.AlbumsRVAdapter
import dev.jaym21.geet.adapters.IAlbumsRVAdapter
import dev.jaym21.geet.databinding.FragmentAlbumsBinding
import dev.jaym21.geet.extensions.filter
import dev.jaym21.geet.models.Album

class AlbumsFragment : BaseFragment(), IAlbumsRVAdapter {

    private var _binding: FragmentAlbumsBinding? = null
    private val binding: FragmentAlbumsBinding
        get() = _binding!!
    private var albumsAdapter = AlbumsRVAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAlbumsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()

        mainViewModel.loadAlbums()

        mainViewModel.albums.observe(viewLifecycleOwner) {
            albumsAdapter.submitList(it)
        }

        playbackSessionViewModel?.mediaItems
            ?.filter { it.isNotEmpty() }
            ?.observe(this) {
                albumsAdapter.submitList(it as List<Album>)
            }
    }

    private fun setUpRecyclerView() {
        binding.apply {
            rvAllAlbums.adapter = albumsAdapter
            rvAllAlbums.layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onAlbumClick(album: Album) {

    }
}