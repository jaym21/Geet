package dev.jaym21.geet.extensions

import android.graphics.Insets
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.WindowInsets
import androidx.core.content.ContextCompat

fun View.disableDropShadowCompat() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val transparent = ContextCompat.getColor(context, android.R.color.transparent)
        outlineAmbientShadowColor = transparent
        outlineSpotShadowColor = transparent
    }
}

/**
 * Resolve system bar insets in a version-aware manner. This can be used to apply padding to a view
 * that properly follows all the frustrating changes that were made between 8-11.
 */
val WindowInsets.systemBarInsetsCompat: Rect
    get() {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                getInsets(WindowInsets.Type.systemBars()).run { Rect(left, top, right, bottom) }
            }
            else -> {
                @Suppress("DEPRECATION")
                (Rect(
        systemWindowInsetLeft,
        systemWindowInsetTop,
        systemWindowInsetRight,
        systemWindowInsetBottom
    ))
            }
        }
    }

/**
 * Replaces the system bar insets in a version-aware manner. This can be used to modify the insets
 * for child views in a way that follows all of the frustrating changes that were made between 8-11.
 */
fun WindowInsets.replaceSystemBarInsetsCompat(
    left: Int,
    top: Int,
    right: Int,
    bottom: Int
): WindowInsets {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
            WindowInsets.Builder(this)
                .setInsets(WindowInsets.Type.systemBars(), Insets.of(left, top, right, bottom))
                .build()
        }
        else -> {
            @Suppress("DEPRECATION") replaceSystemWindowInsets(left, top, right, bottom)
        }
    }
}

/**
 * Determines if the point given by [x] and [y] falls within this view.
 * @param minTouchTargetSize The minimum touch size, independent of the view's size (Optional)
 */
fun View.isUnder(x: Float, y: Float, minTouchTargetSize: Int = 0): Boolean {
    return isUnderImpl(x, left, right, (parent as View).width, minTouchTargetSize) &&
            isUnderImpl(y, top, bottom, (parent as View).height, minTouchTargetSize)
}

private fun isUnderImpl(
    position: Float,
    viewStart: Int,
    viewEnd: Int,
    parentEnd: Int,
    minTouchTargetSize: Int
): Boolean {
    val viewSize = viewEnd - viewStart

    if (viewSize >= minTouchTargetSize) {
        return position >= viewStart && position < viewEnd
    }
    var touchTargetStart = viewStart - (minTouchTargetSize - viewSize) / 2

    if (touchTargetStart < 0) {
        touchTargetStart = 0
    }

    var touchTargetEnd = touchTargetStart + minTouchTargetSize
    if (touchTargetEnd > parentEnd) {
        touchTargetEnd = parentEnd
        touchTargetStart = touchTargetEnd - minTouchTargetSize

        if (touchTargetStart < 0) {
            touchTargetStart = 0
        }
    }

    return position >= touchTargetStart && position < touchTargetEnd
}
