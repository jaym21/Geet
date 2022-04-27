package dev.jaym21.geet.ui

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dev.jaym21.geet.adapters.ISongsRVAdapter
import dev.jaym21.geet.adapters.SongsRVAdapter
import dev.jaym21.geet.databinding.FragmentSongsBinding
import dev.jaym21.geet.models.Song
import dev.jaym21.geet.utils.Constants
import dev.jaym21.geet.utils.PreferencesHelper
import dev.jaym21.geet.viewmodels.MainViewModel

class SongsFragment : BaseFragment(), ISongsRVAdapter {

    private var _binding: FragmentSongsBinding? = null
    private val binding: FragmentSongsBinding
        get() = _binding!!
    private val songsAdapter: SongsRVAdapter = SongsRVAdapter(this, this, nowPlayingViewModel)
    private var songs = listOf<Song>()

    private var readPermissionGranted = false
    private var writePermissionGranted = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSongsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()

        if (readPermissionGranted) {
            if (writePermissionGranted) {
                mainViewModel.loadSongs()

                mainViewModel.songs.observe(viewLifecycleOwner) {
                    songs = it
                    if (!it.isNullOrEmpty()) {
                        songsAdapter.submitList(it)
                    }
                }
            } else {
                ActivityCompat.requestPermissions(requireActivity(),  arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), Constants.WRITE_EXTERNAL_STORAGE_REQUEST_CODE)
            }
        } else {
            ActivityCompat.requestPermissions(requireActivity(),  arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), Constants.READ_EXTERNAL_STORAGE_REQUEST_CODE)
        }
    }

    private fun setUpRecyclerView() {
        binding.apply {
            rvAllSongs.adapter = songsAdapter
            rvAllSongs.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onSongClicked(song: Song) {
        val idList = mutableListOf<Long>()
        for (i in songs) {
            idList.add(i.id)
        }

        val idArray = idList.toLongArray()
        val index = idArray.indexOf(song.id)

        val reqArray = idArray.copyOfRange(index, idArray.size)

        PreferencesHelper.setQueueIds(requireContext(), reqArray)

    }
}