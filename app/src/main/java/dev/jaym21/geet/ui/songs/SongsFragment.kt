package dev.jaym21.geet.ui.songs

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dev.jaym21.geet.R
import dev.jaym21.geet.databinding.FragmentSongsBinding
import dev.jaym21.geet.models.Song
import dev.jaym21.geet.ui.MainViewModel
import dev.jaym21.geet.utils.Constants
import dev.jaym21.geet.utils.PreferencesHelper

class SongsFragment : Fragment(), ISongsRVAdapter {

    private var _binding: FragmentSongsBinding? = null
    private val binding: FragmentSongsBinding
        get() = _binding!!
    private lateinit var viewModel: MainViewModel
    private val songsAdapter = SongsRVAdapter(this)
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

        val isReadPermissionAvailable = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val isWritePermissionAvailable = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        val minSDK29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        readPermissionGranted = isReadPermissionAvailable
        writePermissionGranted = isWritePermissionAvailable || minSDK29

        setUpRecyclerView()

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        if (readPermissionGranted) {
            if (writePermissionGranted) {
                viewModel.loadSongs()

                viewModel.songs.observe(viewLifecycleOwner) {
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

        findNavController().navigate(R.id.action_songsFragment_to_nowPlayingFragment)
    }
}