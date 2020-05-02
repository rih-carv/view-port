package com.viewport.tv

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.leanback.widget.OnChildViewHolderSelectedListener
import androidx.leanback.widget.VerticalGridView
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import com.viewport.commons.ViewPortGridViewHelper
import com.viewport.commons.ViewPortLiveData
import com.viewport.commons.ViewPortManager

class ViewPortVerticalGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : VerticalGridView(context, attrs, defStyleAttr), LifecycleObserver {
    init {
        isSaveEnabled = true
    }

    companion object {
        private const val INSTANCE_STATE_KEY = "instanceState"
    }

    private var viewPortManager: ViewPortManager? = null

    /**
     * [ViewPortLiveData] used to handle first and last visible items positions.
     *
     * Send this live data to [ViewPortManager] in order to obtain viewed items interval
     * between first and last visible items.
     */
    private var firstAndLastVisibleItemsLiveData = ViewPortLiveData<Pair<Int, Int>>()

    private val childSelectedListener by lazy {
        object : OnChildViewHolderSelectedListener() {
            override fun onChildViewHolderSelected(
                parent: RecyclerView,
                child: ViewHolder,
                position: Int,
                subposition: Int
            ) {
                super.onChildViewHolderSelected(parent, child, position, subposition)

                val firstVisibleItemPosition =
                    ViewPortGridViewHelper.findFirstVisibleItemPosition(parent)

                val lastVisibleItemPosition =
                    ViewPortGridViewHelper.findLastCompletelyVisibleItemPosition(parent)

                firstAndLastVisibleItemsLiveData.value =
                    Pair(firstVisibleItemPosition, lastVisibleItemPosition)
            }
        }
    }

    /**
     * [MutableLiveData] to be manipulated in that class. Client should access it's value by using
     * [viewedItemsLiveData].
     */
    val viewedItemsLiveData = ViewPortLiveData<List<Int>>()

    /**
     * Client should set this value with a [LifecycleOwner] to pause/return sending values by
     * [viewedItemsLiveData] when your activity/fragment pauses or resumes.
     *
     * * Attempt to, if client not provides a valid LifecycleOwner, it could not receives updates
     * about viewed items through [viewedItemsLiveData]
     */
    var lifecycleOwner: LifecycleOwner? = null
        set(value) {
            field = value
            field?.lifecycle?.addObserver(this)

            viewPortManager = ViewPortManager(firstAndLastVisibleItemsLiveData, field)

            field?.let {
                viewPortManager?.viewedItemsLiveData?.observe(it, Observer { viewedItems ->
                    this.viewedItemsLiveData.value = viewedItems
                })
            }

            // Attach and listen a ChildViewHolderSelectedListener.
            addOnChildViewHolderSelectedListener(childSelectedListener)

            // Starts view port manager.
            viewPortManager?.startLib()
        }

    override fun onChildAttachedToWindow(child: View) {
        super.onChildAttachedToWindow(child)
        windowAlignment = WINDOW_ALIGN_BOTH_EDGE
    }

    override fun onChildDetachedFromWindow(child: View) {
        super.onChildDetachedFromWindow(child)
        super.onDetachedFromWindow()
    }

    override fun onSaveInstanceState(): Parcelable? = Bundle().apply {
        putParcelable(INSTANCE_STATE_KEY, super.onSaveInstanceState())

    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState((state as Bundle).apply {

        }.getParcelable(INSTANCE_STATE_KEY))
    }

    /**
     * [Lifecycle.Event.ON_PAUSE] client events are triggered.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    internal fun onPause() {
        viewPortManager?.pauseLib()
    }

    /**
     * [Lifecycle.Event.ON_RESUME] client events are triggered.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    internal fun onResume() {
        viewPortManager?.resumeLib()
    }

    /**
     * [Lifecycle.Event.ON_DESTROY] client events are triggered.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    internal fun onDestroy() {
        viewPortManager?.stopLib()
        lifecycleOwner?.lifecycle?.removeObserver(this)
        removeOnChildViewHolderSelectedListener(childSelectedListener)
    }
}