package dev.jaym21.geet.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import dev.jaym21.geet.R
import dev.jaym21.geet.models.Album
import dev.jaym21.geet.models.Artist
import dev.jaym21.geet.utils.SongUtils

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
        val artwork1: ImageView = itemView.findViewById(R.id.ivAlbumsArtwork1)
        val artwork2: ImageView = itemView.findViewById(R.id.ivAlbumsArtwork2)
        val artwork3: ImageView = itemView.findViewById(R.id.ivAlbumsArtwork3)
        val artwork4: ImageView = itemView.findViewById(R.id.ivAlbumsArtwork4)
        val topLayer: LinearLayout = itemView.findViewById(R.id.llArtworkTopLayer)
        val bottomLayer: LinearLayout = itemView.findViewById(R.id.llArtworkBottomLayer)
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
    }
}

interface IArtistsRVAdapter {
    fun onArtistClick(artist: Artist)
}