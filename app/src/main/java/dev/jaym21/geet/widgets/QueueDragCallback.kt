package dev.jaym21.geet.widgets

import android.graphics.Canvas
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.view.isInvisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.shape.MaterialShapeDrawable
import dev.jaym21.geet.adapters.QueueRVAdapter
import dev.jaym21.geet.extensions.getDimensionSafely
import dev.jaym21.geet.utils.Constants
import dev.jaym21.geet.viewmodels.MainViewModel
import dev.jaym21.geet.viewmodels.NowPlayingViewModel
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

class QueueDragCallback(private val mainViewModel: MainViewModel, private val nowPlayingViewModel: NowPlayingViewModel, private val lifecycleOwner: LifecycleOwner): ItemTouchHelper.Callback() {

    private var shouldElevate = true

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.UP or ItemTouchHelper.DOWN) or
                makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE, ItemTouchHelper.START)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        val from = viewHolder.adapterPosition
        val to = target.adapterPosition

        val extras = Bundle().apply {
            putInt(Constants.QUEUE_FROM, from)
            putInt(Constants.QUEUE_TO, to)
        }
        mainViewModel.transportControls().sendCustomAction(Constants.ACTION_QUEUE_REORDER, extras)

        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val extras = Bundle()
        nowPlayingViewModel.queueData.observe(lifecycleOwner) { queueData ->
            val id = queueData.queue[viewHolder.adapterPosition]
            extras.putLong(Constants.SONG, id)
        }
    }


    //making QueueFragment scroll slower when an item is scrolled out of bounds
    override fun interpolateOutOfBoundsScroll(
        recyclerView: RecyclerView,
        viewSize: Int,
        viewSizeOutOfBounds: Int,
        totalSize: Int,
        msSinceStartScroll: Long
    ): Int {

        val standardSpeed = super.interpolateOutOfBoundsScroll(recyclerView, viewSize, viewSizeOutOfBounds, totalSize, msSinceStartScroll)

        val holdAbsVelocity = max(Constants.MINIMUM_INITIAL_DRAG_VELOCITY, min(abs(standardSpeed), Constants.MAXIMUM_INITIAL_DRAG_VELOCITY))

        return holdAbsVelocity * sign(viewSizeOutOfBounds.toDouble()).toInt()
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val holder = viewHolder as QueueRVAdapter.QueueViewHolder

        if (isCurrentlyActive && shouldElevate && actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            val bg = holder.body.background as MaterialShapeDrawable
            val elevation = recyclerView.context.getDimensionSafely(2)
            holder.itemView.animate()
                .translationZ(elevation)
                .setDuration(100)
                .setUpdateListener { bg.elevation = holder.itemView.translationZ }
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()

            shouldElevate = false
        }

        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            holder.background.isInvisible = dX == 0f
        }

        holder.body.translationX = dX
        holder.itemView.translationY = dY
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        val holder = viewHolder as QueueRVAdapter.QueueViewHolder
        val bg = holder.body.background as MaterialShapeDrawable

        if (holder.itemView.translationZ != 0f) {
            holder.itemView.animate()
                .translationZ(0.0f)
                .setDuration(100)
                .setUpdateListener { bg.elevation = holder.itemView.translationZ }
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }

        shouldElevate = true

        holder.body.translationX = 0f
        holder.itemView.translationY = 0f
    }
    override fun isLongPressDragEnabled(): Boolean {
        return false
    }
}