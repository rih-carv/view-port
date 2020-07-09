package com.github.globocom.viewport.tv

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.leanback.widget.OnChildViewHolderSelectedListener
import androidx.leanback.widget.VerticalGridView
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import com.github.globocom.viewport.commons.ViewPortGridViewHelper
import com.github.globocom.viewport.commons.ViewPortLiveData
import com.github.globocom.viewport.commons.ViewPortManager
import com.github.globocom.viewport.commons.ViewPortSavedState

open class ViewPortVerticalGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : VerticalGridView(context, attrs, defStyleAttr), LifecycleObserver {
    init {
        isSaveEnabled = true
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
     * A [LiveData] that emits only the newest visible items, ignoring items that remained visible since the last emission.
     * The main use case of this is to send impression events for metrics of those items.
     */
    val onlyNewViewedItemsLiveData = ViewPortLiveData<List<Int>>()

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
                viewPortManager?.onlyNewViewedItemsLiveData?.observe(it, Observer { viewedItems ->
                    this.onlyNewViewedItemsLiveData.value = viewedItems
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

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val myState = ViewPortSavedState(superState)

        return viewPortManager?.onSaveInstanceState(myState) ?: superState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is ViewPortSavedState) {
            super.onRestoreInstanceState(state.superState)
            viewPortManager?.onRestoreInstanceState(state)
            requestLayout()
        } else {
            super.onRestoreInstanceState(state)
        }
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