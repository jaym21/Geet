package dev.jaym21.geet.adapters

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
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import dev.jaym21.geet.R
import dev.jaym21.geet.models.Album
import dev.jaym21.geet.utils.SongUtils

class ArtistAlbumsRVAdapter(private val listener: IArtistAlbumsRVAdapter): ListAdapter<Album,ArtistAlbumsRVAdapter.ArtistAlbumsViewHolder>(ArtistAlbumsDiffUtil()) {

    class ArtistAlbumsDiffUtil: DiffUtil.ItemCallback<Album>() {
        override fun areItemsTheSame(oldItem: Album, newItem: Album): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean {
            return oldItem.id == newItem.id
        }
    }

    inner class ArtistAlbumsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvAlbumTitle)
        val year: TextView = itemView.findViewById(R.id.tvAlbumYear)
        val artwork: ImageView = itemView.findViewById(R.id.ivArtistAlbumArtwork)
        val root: ConstraintLayout = itemView.findViewById(R.id.clArtistAlbumRoot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistAlbumsViewHolder {
        return ArtistAlbumsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_artist_album_layout, parent, false))
    }

    override fun onBindViewHolder(holder: ArtistAlbumsViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.name.text = currentItem.albumTitle
        holder.year.text = currentItem.year.toString()
        val albumArtwork = SongUtils.getAlbumArtBitmap(holder.itemView.context, currentItem.id)
        Glide.with(holder.itemView.context).load(albumArtwork).transform(RoundedCorners(12)).into(holder.artwork)

        holder.root.setOnClickListener {
            listener.onArtistAlbumClick(currentItem)
        }
    }
}

interface IArtistAlbumsRVAdapter {
    fun onArtistAlbumClick(album:Album)
}