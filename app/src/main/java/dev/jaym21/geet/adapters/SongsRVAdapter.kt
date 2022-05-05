package dev.jaym21.geet.adapters

import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.RoundedCorner
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import dev.jaym21.geet.R
import dev.jaym21.geet.models.Song
import dev.jaym21.geet.utils.SongUtils
import dev.jaym21.geet.viewmodels.NowPlayingViewModel

private const val INVALID_POSITION = -1

class SongsRVAdapter(private val listener: ISongsRVAdapter, private val lifecycleOwner: LifecycleOwner, private val nowPlayingViewModel: NowPlayingViewModel): ListAdapter<Song, SongsRVAdapter.SongsViewHolder>(SongsDiffUtil()) {

    private var nowPlayingPosition = INVALID_POSITION

    init {
        //attach observer for updating now playing indicator on songs
        nowPlayingViewModel.currentData.observe(lifecycleOwner) {
            val previousPlayingPosition = nowPlayingPosition

            if (!it.mediaId.isNullOrEmpty() && it.state == PlaybackStateCompat.STATE_PLAYING) {
                nowPlayingPosition = getPositionForSong(it.mediaId!!.toLong())
            } else {
                nowPlayingPosition = INVALID_POSITION
            }

            //removing playing indicator from previous playing song position
            if (previousPlayingPosition != INVALID_POSITION)
                notifyItemChanged(previousPlayingPosition)

            //adding playing indicator on now playing position
            if (nowPlayingPosition != INVALID_POSITION)
                notifyItemChanged(nowPlayingPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongsViewHolder {
        return SongsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_song_layout, parent, false))
    }

    override fun onBindViewHolder(holder: SongsViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.title.text = currentItem.title
        holder.artist.text = currentItem.artist

        val albumArtUri = SongUtils.getAlbumArtUri(currentItem.albumId)
        Glide.with(holder.itemView.context).load(albumArtUri).transform(RoundedCorners(12)).into(holder.artwork)

        if (position == nowPlayingPosition) {
            holder.title.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorAccent))
        } else {
            holder.title.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
        }

        if (position == nowPlayingPosition) {
            holder.artist.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorAccent))
        } else {
            holder.artist.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white_alpha_85))
        }

        holder.root.setOnClickListener {
            listener.onSongClicked(currentItem)
        }

        holder.moreMenu.setOnClickListener {
            listener.onMoreMenuClicked(currentItem)
        }
    }

    inner class SongsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvSongTitle)
        val artist: TextView = itemView.findViewById(R.id.tvArtistName)
        val artwork: ImageView = itemView.findViewById(R.id.ivSongArtwork)
        val moreMenu: ImageView = itemView.findViewById(R.id.ivMoreMenu)
        val root: ConstraintLayout = itemView.findViewById(R.id.clSongRoot)
    }

    class SongsDiffUtil: DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.id == newItem.id
        }
    }

    private fun getPositionForSong(songId: Long): Int {
        val songs = currentList
        return songs.indexOf(songs.find { it.id == songId })
    }
}

interface ISongsRVAdapter {
    fun onSongClicked(song: Song)
    fun onMoreMenuClicked(song: Song)
}