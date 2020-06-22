package com.github.globocom.viewport.mobile

import android.content.Context
import android.os.Handler
import android.os.Parcelable
import android.util.AttributeSet
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.globocom.viewport.commons.ViewPortLiveData
import com.github.globocom.viewport.commons.ViewPortManager
import com.github.globocom.viewport.commons.ViewPortSavedState

open class ViewPortRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr), LifecycleObserver {
    init {
        isSaveEnabled = true
    }

    private companion object {
        const val SCROLL_IDLE_TIME = 500L
    }

    /**
     * [MutableLiveData] to be manipulated in that class. Client should access it's value by using
     * [viewedItemsLiveData].
     */
    private val viewPortLiveData = ViewPortLiveData<List<Int>>()
    private val onlyNewItemsViewPortLiveData = ViewPortLiveData<List<Int>>()
    private var viewPortManager: ViewPortManager? = null
    private val scrollIdleTimeoutHandler = Handler()
    private var firstAndLastVisibleItemsLiveData = ViewPortLiveData<Pair<Int, Int>>()

    /**
     * [Runnable] to run when [recyclerView] scroll turns [RecyclerView.SCROLL_STATE_IDLE].
     *
     * This is necessary because when that state is reached. We need to cancel [heartBeat], but
     * may exists unprocessed items viewed that must to stay visible for at least [HEART_BEAT_TIME].
     */
    private val scrollIdleTimeoutRunnable = Runnable { viewPortManager?.pauseLib() }

    private val onScrollListener = object : OnScrollListener() {
        /**
         * During scroll, updates a [List] of [Int] with current visible items into [recyclerView],
         * to be matched in future on [heartBeat] implementation.
         */
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            (recyclerView.layoutManager as? LinearLayoutManager)?.let {
                // Gets first item completely visible position.
                val firstItemPosition = it.findFirstCompletelyVisibleItemPosition()

                // Gets last item completely visible position.
                val lastItemPosition = it.findLastCompletelyVisibleItemPosition()

                firstAndLastVisibleItemsLiveData.value = Pair(firstItemPosition, lastItemPosition)
            }
        }

        /**
         * Listen to scroll states of [recyclerView], in order to stop/start listeners.
         *
         * Attempt to when in [SCROLL_STATE_IDLE] we should stop listener only after a delayed
         * time, bigger than [HEART_BEAT_TIME]. We need this behavior to guarantee that a last
         * 'heart beat' will occurs, thus a last match may happen even if the scroll ends.
         */
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            when (newState) {
                SCROLL_STATE_IDLE -> {
                    scrollIdleTimeoutHandler.postDelayed(
                        scrollIdleTimeoutRunnable,
                        SCROLL_IDLE_TIME
                    )
                }

                SCROLL_STATE_SETTLING,
                SCROLL_STATE_DRAGGING -> {
                    scrollIdleTimeoutHandler.removeCallbacks(scrollIdleTimeoutRunnable)
                    viewPortManager?.resumeLib()
                }
            }
        }
    }

    /**
     * Protected [LiveData] to avoid client from overrides this [ViewPortLiveData].
     */
    val viewedItemsLiveData: LiveData<List<Int>> get() = viewPortLiveData
    val onlyNewViewedItemsLiveData: LiveData<List<Int>> get() = onlyNewItemsViewPortLiveData

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
                    this.viewPortLiveData.value = viewedItems
                })
                viewPortManager?.onlyNewViewedItemsLiveData?.observe(it, Observer { viewedItems ->
                    this.onlyNewItemsViewPortLiveData.value = viewedItems
                })
            }

            addOnScrollListener(onScrollListener)

            viewPortManager?.startLib()
        }


    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val myState = ViewPortSavedState(superState)

        return viewPortManager?.onSaveInstanceState(myState)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as? ViewPortSavedState
        super.onRestoreInstanceState(savedState?.superState)

        viewPortManager?.onRestoreInstanceState(savedState)

        requestLayout()
    }

    /**
     * [Lifecycle.Event.ON_PAUSE] client events are triggered.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onPause() {
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
        removeOnScrollListener(onScrollListener)
    }
}