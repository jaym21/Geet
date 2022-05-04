package dev.jaym21.geet.ui.details

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import dev.jaym21.geet.R
import dev.jaym21.geet.adapters.ISongsRVAdapter
import dev.jaym21.geet.adapters.SongsRVAdapter
import dev.jaym21.geet.databinding.FragmentAlbumDetailsBinding
import dev.jaym21.geet.extensions.filter
import dev.jaym21.geet.models.Album
import dev.jaym21.geet.models.Song
import dev.jaym21.geet.ui.BaseFragment
import dev.jaym21.geet.utils.Constants
import dev.jaym21.geet.utils.SongUtils


class AlbumDetailsFragment : BaseFragment(), ISongsRVAdapter {

    private var _binding: FragmentAlbumDetailsBinding? = null
    private val binding: FragmentAlbumDetailsBinding
        get() = _binding!!
    private var album: Album? = null
    private var songsAdapter: SongsRVAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAlbumDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        album = arguments?.getParcelable(Constants.ALBUM)

        if (album != null) {
            binding.tvAlbumName.text = album?.albumTitle
            binding.tvArtistName.text = album?.artist
            Glide.with(requireContext()).load(SongUtils.getAlbumArtBitmap(requireContext(), album?.id)).into(binding.ivBackButton)
            binding.tvAlbumInfo.text = context?.getString(R.string.format_two, album?.year?.toString() ?: getString(R.string.no_year), album?.noOfSongs.toString())


            songsAdapter = SongsRVAdapter(this, this, nowPlayingViewModel)

            setUpRecyclerView()

            playbackSessionViewModel?.mediaItems
                ?.filter { it.isNotEmpty() }
                ?.observe(this) {
                    songsAdapter?.submitList(it as List<Song>)
                }

        } else {
            findNavController().popBackStack()
        }
    }

    private fun setUpRecyclerView() {
        binding.apply {
            rvAlbumSongs.adapter = songsAdapter
            rvAlbumSongs.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
    }

    override fun onSongClicked(song: Song) {

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}