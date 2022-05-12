package dev.jaym21.geet.widgets

import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.shape.MaterialShapeDrawable
import dev.jaym21.geet.R
import dev.jaym21.geet.adapters.QueueRVAdapter
import dev.jaym21.geet.extensions.getDimensionSafely
import dev.jaym21.geet.utils.Constants
import dev.jaym21.geet.viewmodels.MainViewModel
import dev.jaym21.geet.viewmodels.NowPlayingViewModel
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

class QueueDragCallback(private val mainViewModel: MainViewModel, private val queueAdapter: QueueRVAdapter): ItemTouchHelper.Callback() {

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
        val from = viewHolder.bindingAdapterPosition
        val to = target.bindingAdapterPosition

        Log.d("TAGYOYO", "onMove: from $from to $to")
        queueAdapter.reorderSong(from, to)
        val extras = Bundle().apply {
            putInt(Constants.QUEUE_FROM, from)
            putInt(Constants.QUEUE_TO, to)
        }
        mainViewModel.transportControls().sendCustomAction(Constants.ACTION_QUEUE_REORDER, extras)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val extras = Bundle()
        val songId = queueAdapter.getSongIdForPosition(viewHolder.bindingAdapterPosition)
        extras.putLong(Constants.SONG, songId)
        mainViewModel.transportControls().sendCustomAction(Constants.ACTION_SONG_DELETED, extras)
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
        RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            .addBackgroundColor(ContextCompat.getColor(viewHolder.itemView.context, R.color.swipe_red_bg))
            .addActionIcon(R.drawable.ic_delete)
            .create()
            .decorate()

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
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

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }
}