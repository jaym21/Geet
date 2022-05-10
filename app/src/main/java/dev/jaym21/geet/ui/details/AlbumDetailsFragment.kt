package dev.jaym21.geet.ui.details

import android.os.Bundle
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
import dev.jaym21.geet.extensions.getExtraBundle
import dev.jaym21.geet.extensions.toSongIds
import dev.jaym21.geet.models.Album
import dev.jaym21.geet.models.Song
import dev.jaym21.geet.ui.BaseFragment
import dev.jaym21.geet.ui.SongBottomSheetFragment
import dev.jaym21.geet.utils.Constants
import dev.jaym21.geet.utils.SongUtils

class AlbumDetailsFragment : BaseFragment(), ISongsRVAdapter {

    private var _binding: FragmentAlbumDetailsBinding? = null
    private val binding: FragmentAlbumDetailsBinding
        get() = _binding!!
    private var album: Album? = null
    private var caller: String? = null
    private var songs = listOf<Song>()
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
        caller = arguments?.getString(Constants.MEDIA_CALLER)

        if (album != null && caller != null) {
            binding.tvAlbumName.text = album?.albumTitle
            binding.tvArtistName.text = album?.artist
            Glide.with(requireContext()).load(SongUtils.getAlbumArtBitmap(requireContext(), album?.id)).into(binding.ivAlbumArtwork)

            var songText = "song"
            if (album?.noOfSongs!! > 1) {
                songText = "songs"
            }

            songsAdapter = SongsRVAdapter(this, this, nowPlayingViewModel)

            setUpRecyclerView()

            mainViewModel.getAlbumSongs(caller!!, album?.id!!)

            mainViewModel.albumSongs.observe(viewLifecycleOwner) {
                songs = it
                songsAdapter?.submitList(it)

                val totalTime = SongUtils.getTotalDuration(songs)
                if (album?.year == 0) {
                    binding.tvAlbumInfo.text = context?.getString(
                        R.string.format_two,
                        "${album?.noOfSongs} $songText",
                        totalTime
                    )
                } else {
                    binding.tvAlbumInfo.text = context?.getString(
                        R.string.format_three,
                        album?.year?.toString() ?: getString(R.string.no_year),
                        "${album?.noOfSongs} $songText",
                        totalTime
                    )
                }
            }

            playbackSessionViewModel?.mediaItems
                ?.filter { it.isNotEmpty() }
                ?.observe(this) {
                    songsAdapter?.submitList(it as List<Song>)
                }

            binding.ivBackButton.setOnClickListener {
                findNavController().popBackStack()
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
        val extras = getExtraBundle(songs.toSongIds(), album?.albumTitle!!)
        mainViewModel.mediaItemClicked(song, extras)
    }

    override fun onMoreMenuClicked(song: Song) {
        val songBottomSheetFragment = SongBottomSheetFragment(mainViewModel, viewLifecycleOwner, findNavController())
        val bundle = Bundle().apply {
            putParcelable(Constants.SONG_BOTTOM_SHEET_ARG, song)
        }
        songBottomSheetFragment.arguments = bundle
        songBottomSheetFragment.show(requireActivity().supportFragmentManager, "SongBottomSheetFragment")
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

}