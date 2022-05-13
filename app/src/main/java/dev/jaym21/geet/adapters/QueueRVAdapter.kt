package dev.jaym21.geet.adapters

import android.annotation.SuppressLint
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
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
import dev.jaym21.geet.extensions.moveElement
import dev.jaym21.geet.models.Song
import dev.jaym21.geet.utils.SongUtils
import dev.jaym21.geet.viewmodels.NowPlayingViewModel

private const val INVALID_POSITION = -1

class QueueRVAdapter(private val listener: IQueueRVAdapter, private val lifecycleOwner: LifecycleOwner, private val nowPlayingViewModel: NowPlayingViewModel): RecyclerView.Adapter<QueueRVAdapter.QueueViewHolder>() {

    var songs: List<Song> = emptyList()
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

        if (position == nowPlayingPosition) {
            holder.title.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorAccent))
        } else {
            holder.title.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
        }

        if (position == nowPlayingPosition) {
            holder.artist.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.colorAccent))
        } else {
            holder.artist.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white_alpha_70))
        }

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

    private fun getPositionForSong(songId: Long): Int {
        return songs.indexOf(songs.find { it.id == songId })
    }
}

interface IQueueRVAdapter {
    fun onSongClicked(song: Song)
    fun onPickUp(viewHolder: RecyclerView.ViewHolder)
}