package com.github.globocom.viewport.commons

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

object ViewPortPartialHelper {

    fun findFirstPartiallyVisibleItemPosition(
        recyclerView: RecyclerView,
        linearLayoutManager: LinearLayoutManager,
        tolerance: Float = 1f
    ): Int {
        findOneVisibleChild(
            linearLayoutManager,
            0,
            linearLayoutManager.childCount,
            tolerance
        )?.let { childView ->
            return recyclerView.getChildAdapterPosition(childView)
        }
        return RecyclerView.NO_POSITION
    }

    fun findLastPartiallyVisibleItemPosition(
        recyclerView: RecyclerView,
        linearLayoutManager: LinearLayoutManager,
        tolerance: Float = 1f
    ): Int {
        findOneVisibleChild(
            linearLayoutManager,
            linearLayoutManager.childCount - 1,
            -1,
            tolerance
        )?.let { childView ->
            return recyclerView.getChildAdapterPosition(childView)
        }
        return RecyclerView.NO_POSITION
    }

    private fun findOneVisibleChild(
        layoutManager: RecyclerView.LayoutManager,
        fromIndex: Int,
        toIndex: Int,
        tolerance: Float
    ): View? {
        val helper: OrientationHelper = if (layoutManager.canScrollVertically()) {
            OrientationHelper.createVerticalHelper(layoutManager)
        } else {
            OrientationHelper.createHorizontalHelper(layoutManager)
        }

        val start: Int = helper.startAfterPadding
        val end: Int = helper.endAfterPadding
        val next = if (toIndex > fromIndex) 1 else -1
        var index = fromIndex

        while (index != toIndex) {
            val child: View? = layoutManager.getChildAt(index)
            val childStart: Int = helper.getDecoratedStart(child)
            val childEnd: Int = helper.getDecoratedEnd(child)
            if (childStart < end && childEnd > start) {
                if (childStart >= start && childEnd <= end) {
                    return child
                } else {
                    if (checkTolerance(tolerance, childStart, childEnd, end, next == 1)) {
                        return child
                    }
                }
            }
            index += next
        }
        return null
    }

    private fun checkTolerance(
        tolerance: Float,
        childStart: Int,
        childEnd: Int,
        end: Int,
        start: Boolean
    ): Boolean {
        return if (start) checkToleranceAtStart(
            tolerance,
            childStart,
            childEnd
        ) else checkToleranceAtEnd(tolerance, childStart, childEnd, end)
    }

    private fun checkToleranceAtStart(
        tolerance: Float,
        childStart: Int,
        childEnd: Int
    ): Boolean {
        val viewSize = abs(childStart) + abs(childEnd)
        val visibleViewSize: Float = if (childStart < 0) {
            childEnd.toFloat()
        } else {
            childStart.toFloat()
        }
        val visibleProportion = visibleViewSize / viewSize
        return visibleProportion >= tolerance
    }

    private fun checkToleranceAtEnd(
        tolerance: Float,
        childStart: Int,
        childEnd: Int,
        end: Int
    ): Boolean {
        val viewSize = childEnd - childStart
        val visibleViewSize = end - childStart
        val visibleProportion: Float = visibleViewSize.toFloat() / viewSize.toFloat()
        return visibleProportion >= tolerance
    }
}