package com.github.globocom.viewport.commons

import android.view.View
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView

object ViewPortGridViewHelper {

    // =============================================================================================
    // Public methods.
    // =============================================================================================
    fun findFirstVisibleItemPosition(recyclerView: RecyclerView) =
        recyclerView.layoutManager?.let { layoutManager ->
            findOneVisibleChild(
                layoutManager,
                0,
                layoutManager.childCount,
                completelyVisible = false,
                acceptPartiallyVisible = true
            )?.let { childView ->
                recyclerView.getChildAdapterPosition(childView)
            }
        } ?: RecyclerView.NO_POSITION

    fun findLastCompletelyVisibleItemPosition(recyclerView: RecyclerView) =
        recyclerView.layoutManager?.let { layoutManager ->
            findOneVisibleChild(
                layoutManager,
                layoutManager.childCount - 1,
                -1,
                completelyVisible = true,
                acceptPartiallyVisible = false
            )?.let { childView ->
                recyclerView.getChildAdapterPosition(childView)
            }
        } ?: RecyclerView.NO_POSITION

    // =============================================================================================
    // Private methods.
    // =============================================================================================
    private fun findOneVisibleChild(
        layoutManager: RecyclerView.LayoutManager,
        fromIndex: Int,
        toIndex: Int,
        completelyVisible: Boolean,
        acceptPartiallyVisible: Boolean
    ): View? {
        val helper: OrientationHelper = if (layoutManager.canScrollVertically()) {
            OrientationHelper.createVerticalHelper(layoutManager)
        } else {
            OrientationHelper.createHorizontalHelper(layoutManager)
        }

        val start: Int = helper.startAfterPadding
        val end: Int = helper.endAfterPadding
        val next = if (toIndex > fromIndex) 1 else -1
        var partiallyVisible: View? = null
        var index = fromIndex

        while (index != toIndex) {
            val child: View? = layoutManager.getChildAt(index)
            val childStart: Int = helper.getDecoratedStart(child)
            val childEnd: Int = helper.getDecoratedEnd(child)
            if (childStart < end && childEnd > start) {
                if (completelyVisible) {
                    if (childStart >= start && childEnd <= end) {
                        return child
                    } else if (acceptPartiallyVisible && partiallyVisible == null) {
                        partiallyVisible = child
                    }
                } else {
                    return child
                }
            }

            index += next
        }

        return partiallyVisible
    }
}