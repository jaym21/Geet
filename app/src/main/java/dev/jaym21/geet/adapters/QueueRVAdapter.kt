package dev.jaym21.geet.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.jaym21.geet.R
import dev.jaym21.geet.models.Song

class QueueRVAdapter: ListAdapter<Song, QueueRVAdapter.QueueViewHolder>(QueueDiffUtil()) {

    class QueueDiffUtil: DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.id == newItem.id
        }

    }

    inner class QueueViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueueViewHolder {
        return QueueViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_queue_layout, parent, false))
    }

    override fun onBindViewHolder(holder: QueueViewHolder, position: Int) {

    }
}