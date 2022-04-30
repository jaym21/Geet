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
import dev.jaym21.geet.models.Artist

class ArtistsRVAdapter(private val listener: IArtistsRVAdapter): ListAdapter<Artist, ArtistsRVAdapter.ArtistsViewHolder>(ArtistsDiffUtil()) {

    class ArtistsDiffUtil: DiffUtil.ItemCallback<Artist>() {
        override fun areItemsTheSame(oldItem: Artist, newItem: Artist): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Artist, newItem: Artist): Boolean {
            return oldItem.id == newItem.id
        }

    }

    inner class ArtistsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val artistName: TextView = itemView.findViewById(R.id.tvArtistName)
        val artistArtworkMatrix: ImageView = itemView.findViewById(R.id.ivArtistArtworkMatrix)
        val noOfSongsAndAlbums: TextView = itemView.findViewById(R.id.tvNoOfSongsAndAlbums)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistsViewHolder {
        return ArtistsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_artist_layout, parent, false))
    }

    override fun onBindViewHolder(holder: ArtistsViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.artistName.text = currentItem.name
        var noOfSongsText = "Song"
        var noOfAlbumsText = "Album"
        if (currentItem.noOfSongs > 1) {
            noOfSongsText = "Songs"
        }
        if (currentItem.noOfAlbums > 1) {
            noOfAlbumsText = "Albums"
        }
        holder.noOfSongsAndAlbums.text = "${currentItem.noOfSongs} $noOfSongsText, ${currentItem.noOfAlbums} $noOfAlbumsText"
        Glide.with(holder.itemView.context).load(currentItem.id).transform(RoundedCorners(12)).into(holder.artistArtworkMatrix)
    }
}

interface IArtistsRVAdapter {
    fun onAlbumClick(album: Album)
}