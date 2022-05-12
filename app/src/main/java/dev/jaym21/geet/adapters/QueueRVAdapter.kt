package dev.jaym21.geet.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
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
import dev.jaym21.geet.extensions.moveElement
import dev.jaym21.geet.models.Song
import dev.jaym21.geet.utils.SongUtils

class QueueRVAdapter(private val listener: IQueueRVAdapter): RecyclerView.Adapter<QueueRVAdapter.QueueViewHolder>() {

    var songs: List<Song> = emptyList()

    inner class QueueViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tvSongTitleQueue)
        val artist: TextView = itemView.findViewById(R.id.tvArtistNameQueue)
        val artwork: ImageView = itemView.findViewById(R.id.ivSongArtworkQueue)
        val dragHandle: ImageView = itemView.findViewById(R.id.ivDragHandle)
        val root: ConstraintLayout = itemView.findViewById(R.id.clQueueItemRoot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueueViewHolder {
        return QueueViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_queue_layout, parent, false))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: QueueViewHolder, position: Int) {
        val currentItem = songs[position]
        holder.title.text = currentItem.title
        holder.artist.text = currentItem.artist

        val albumArtUri = SongUtils.getAlbumArtUri(currentItem.albumId)

        Glide.with(holder.itemView.context).load(albumArtUri).transform(RoundedCorners(12)).into(holder.artwork)

        holder.dragHandle.setOnTouchListener { _, motionEvent ->
            holder.dragHandle.performClick()
            if (motionEvent.actionMasked == MotionEvent.ACTION_DOWN) {
                listener.onPickUp(holder)
                true
            } else false
        }

        holder.root.setOnLongClickListener {
            listener.onPickUp(holder)
            true
        }

        holder.root.setOnClickListener {
            listener.onSongClicked(currentItem)
        }
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    fun updateData(songs: List<Song>) {
        this.songs = songs
        notifyDataSetChanged()
    }

    fun reorderSong(from: Int, to: Int) {
        songs.moveElement(from, to)
        notifyItemMoved(from, to)
    }

    fun getSongIdForPosition(position: Int): Long {
        val song = songs[position]
        return song.id
    }
}

interface IQueueRVAdapter {
    fun onSongClicked(song: Song)
    fun onPickUp(viewHolder: RecyclerView.ViewHolder)
}