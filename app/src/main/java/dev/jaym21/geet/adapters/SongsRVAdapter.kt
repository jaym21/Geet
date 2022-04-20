package dev.jaym21.geet.adapters

import android.content.ContentUris
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.jaym21.geet.R
import dev.jaym21.geet.databinding.ItemSongLayoutBinding
import dev.jaym21.geet.models.Song
import dev.jaym21.geet.utils.SongUtils

class SongsRVAdapter(private val listener: ISongsRVAdapter): ListAdapter<Song, SongsRVAdapter.SongsViewHolder>(SongsDiffUtil()) {

    class SongsDiffUtil: DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.id == newItem.id
        }
    }

    inner class SongsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvSongTitle)
        val artist: TextView = itemView.findViewById(R.id.tvArtistName)
        val artwork: ImageView = itemView.findViewById(R.id.ivSongArtwork)
        val root: ConstraintLayout = itemView.findViewById(R.id.clSongRoot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongsViewHolder {
        return SongsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_song_layout, parent, false))
    }

    override fun onBindViewHolder(holder: SongsViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.title.text = currentItem.title
        holder.artist.text = currentItem.artist

        val albumArtUri = SongUtils.getAlbumArtUri(currentItem.albumId)

        Glide.with(holder.itemView.context).load(albumArtUri).into(holder.artwork)

        holder.root.setOnClickListener {
            listener.onSongClicked(currentItem)
        }
    }
}

interface ISongsRVAdapter {
    fun onSongClicked(song: Song)
}