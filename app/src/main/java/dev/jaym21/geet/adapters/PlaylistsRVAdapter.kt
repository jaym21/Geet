package dev.jaym21.geet.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.jaym21.geet.R
import dev.jaym21.geet.models.Playlist
import dev.jaym21.geet.utils.SongUtils

class PlaylistsRVAdapter(private val listener: IPlaylistsRVAdapter): ListAdapter<Playlist, PlaylistsRVAdapter.PlaylistsViewHolder>(PlaylistsDiffUtil()) {

    class PlaylistsDiffUtil: DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem.id == newItem.id
        }

    }

    inner class PlaylistsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val playlistName: TextView = itemView.findViewById(R.id.tvPlaylistName)
        val noOfSongs: TextView = itemView.findViewById(R.id.tvNoOfSongsPlaylist)
        val artwork1: ImageView = itemView.findViewById(R.id.ivAlbumsArtwork1)
        val artwork2: ImageView = itemView.findViewById(R.id.ivAlbumsArtwork2)
        val artwork3: ImageView = itemView.findViewById(R.id.ivAlbumsArtwork3)
        val artwork4: ImageView = itemView.findViewById(R.id.ivAlbumsArtwork4)
        val topLayer: LinearLayout = itemView.findViewById(R.id.llArtworkTopLayer)
        val bottomLayer: LinearLayout = itemView.findViewById(R.id.llArtworkBottomLayer)
        val root: ConstraintLayout = itemView.findViewById(R.id.clPlaylistItemRoot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistsViewHolder {
        return PlaylistsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_playlist_layout, parent, false))
    }

    override fun onBindViewHolder(holder: PlaylistsViewHolder, position: Int) {
        val currentItem = getItem(position)
        var noOfSongsText = "Song"
        if (currentItem.noOfSongs > 1) {
            noOfSongsText = "Songs"
        }

        holder.playlistName.text = currentItem.name
        holder.noOfSongs.text = "${currentItem.noOfSongs} $noOfSongsText"

        val albumIds = currentItem.albumIds
        val images = mutableListOf<Bitmap>()
        for (i in albumIds) {
            val bitmap = SongUtils.getAlbumArtBitmap(holder.itemView.context, i)
            if (bitmap != null) {
                images.add(bitmap)
            }
        }

        holder.topLayer.visibility = View.VISIBLE
        holder.bottomLayer.visibility = View.VISIBLE
        holder.artwork1.visibility = View.VISIBLE
        holder.artwork2.visibility = View.VISIBLE
        holder.artwork3.visibility = View.VISIBLE
        holder.artwork4.visibility = View.VISIBLE

        when (images.size) {
            0 -> {
                holder.bottomLayer.visibility = View.GONE
                holder.artwork2.visibility = View.GONE
                Glide.with(holder.itemView.context).load(R.drawable.ic_album_disk).into(holder.artwork1)
                holder.artwork1.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.colorAccent))
            }
            1 -> {
                holder.bottomLayer.visibility = View.GONE
                holder.artwork2.visibility = View.GONE
                Glide.with(holder.itemView.context).load(images[0]).into(holder.artwork1)
            }
            2 -> {
                holder.bottomLayer.visibility = View.GONE
                Glide.with(holder.itemView.context).load(images[0]).into(holder.artwork1)
                Glide.with(holder.itemView.context).load(images[1]).into(holder.artwork2)
            }
            3 -> {
                holder.artwork4.visibility = View.GONE
                Glide.with(holder.itemView.context).load(images[0]).into(holder.artwork1)
                Glide.with(holder.itemView.context).load(images[1]).into(holder.artwork2)
                Glide.with(holder.itemView.context).load(images[2]).into(holder.artwork3)
            }
            4 -> {
                Glide.with(holder.itemView.context).load(images[0]).into(holder.artwork1)
                Glide.with(holder.itemView.context).load(images[1]).into(holder.artwork2)
                Glide.with(holder.itemView.context).load(images[2]).into(holder.artwork3)
                Glide.with(holder.itemView.context).load(images[3]).into(holder.artwork4)
            }
            else -> {
                Glide.with(holder.itemView.context).load(images[0]).into(holder.artwork1)
                Glide.with(holder.itemView.context).load(images[1]).into(holder.artwork2)
                Glide.with(holder.itemView.context).load(images[2]).into(holder.artwork3)
                Glide.with(holder.itemView.context).load(images[3]).into(holder.artwork4)
            }
        }

        holder.root.setOnClickListener {
            listener.onPlaylistClicked(currentItem)
        }
    }
}

interface IPlaylistsRVAdapter {
    fun onPlaylistClicked(playlist: Playlist)
}