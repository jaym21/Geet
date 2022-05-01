package dev.jaym21.geet.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import dev.jaym21.geet.R
import dev.jaym21.geet.models.Album

class AlbumsRVAdapter(private val listener: IAlbumsRVAdapter): ListAdapter<Album, AlbumsRVAdapter.AlbumsViewHolder>(AlbumsDiffUtil()) {

    class AlbumsDiffUtil: DiffUtil.ItemCallback<Album>() {
        override fun areItemsTheSame(oldItem: Album, newItem: Album): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean {
            return oldItem.id == newItem.id
        }

    }

    inner class AlbumsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val albumName: TextView = itemView.findViewById(R.id.tvAlbumName)
        val albumArtwork: ImageView = itemView.findViewById(R.id.ivAlbumArtwork)
        val artistName: TextView = itemView.findViewById(R.id.tvArtistName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumsViewHolder {
        return AlbumsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_album_layout, parent, false))
    }

    override fun onBindViewHolder(holder: AlbumsViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.albumName.text = currentItem.albumTitle
        holder.artistName.text = currentItem.artist
        Glide.with(holder.itemView.context).load(currentItem.id).transform(RoundedCorners(12)).into(holder.albumArtwork)
    }
}

interface IAlbumsRVAdapter {
    fun onAlbumClick(album: Album)
}