package com.github.globocom.viewport.mobile

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Parcelable
import android.util.AttributeSet
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.globocom.viewport.commons.ViewPortLiveData

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
        const val INSTANCE_STATE_SUPER_STATE = "instanceStateSuperState"
        const val INSTANCE_STATE_IS_HEAR_BEAT_STARTED = "instanceStateIsHearBeatStarted"
        const val INSTANCE_STATE_IS_LIB_STARTED = "instanceStateIsLibStarted"
        const val INSTANCE_STATE_CURRENT_VISIBLE_ITEMS_LIST = "instanceStateCurrentVisibleItemsList"
        const val INSTANCE_STATE_PREVIOUSLY_VISIBLE_ITEMS_LIST = "instanceStatePreviouslyVisibleItemsList"
        const val INSTANCE_STATE_OLD_ITEMS_LIST = "instanceStateOldItemsList"
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

    /**
     * A [LiveData] that emits only the newest visible items, ignoring items that remained visible since the last emission.
     * The main use case of this is to send impression events for metrics of those items.
     */
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
        val myState = Bundle()
        myState.putParcelable(INSTANCE_STATE_SUPER_STATE, superState)

        viewPortManager?.let {
            myState.putBoolean(INSTANCE_STATE_IS_HEAR_BEAT_STARTED, it.isHearBeatStarted)
            myState.putBoolean(INSTANCE_STATE_IS_LIB_STARTED, it.isLibStarted)
            myState.putIntArray(INSTANCE_STATE_CURRENT_VISIBLE_ITEMS_LIST, it.currentVisibleItemsList.toIntArray())
            myState.putIntArray(INSTANCE_STATE_PREVIOUSLY_VISIBLE_ITEMS_LIST, it.previouslyVisibleItemsList.toIntArray())
            myState.putIntArray(INSTANCE_STATE_OLD_ITEMS_LIST, it.oldItemsList.toIntArray())
        }

        return myState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            super.onRestoreInstanceState(state.getParcelable(INSTANCE_STATE_SUPER_STATE))
            viewPortManager?.apply {
                isHearBeatStarted = state.getBoolean(INSTANCE_STATE_IS_HEAR_BEAT_STARTED)
                isLibStarted = state.getBoolean(INSTANCE_STATE_IS_LIB_STARTED)
                currentVisibleItemsList = state.getIntArray(INSTANCE_STATE_CURRENT_VISIBLE_ITEMS_LIST)?.toMutableList() ?: mutableListOf()
                previouslyVisibleItemsList = state.getIntArray(INSTANCE_STATE_PREVIOUSLY_VISIBLE_ITEMS_LIST)?.toMutableList() ?: mutableListOf()
                oldItemsList = state.getIntArray(INSTANCE_STATE_OLD_ITEMS_LIST)?.toMutableList() ?: mutableListOf()
            }
        } else {
            super.onRestoreInstanceState(state)
        }
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

    /**
     * A Manager to listen to elements of a [RecyclerView] that remains visible in some view port
     * for more than a given delayed time ([HEART_BEAT_TIME]).
     *
     * @param firstAndLastVisibleItemsLiveData -> [LiveData] to handle first and last visible items
     * when client interacts with supported [RecyclerView]'s kinds.
     *
     * @param lifecycleOwner -> Client should set this value with a [LifecycleOwner] to pause/return
     * sending values by [viewedItemsLiveData] when your activity/fragment pauses or resumes.
     */
    private class ViewPortManager(
        private val firstAndLastVisibleItemsLiveData: LiveData<Pair<Int, Int>>,
        private val lifecycleOwner: LifecycleOwner?
    ) {
        private companion object {
            const val HEART_BEAT_TIME = 250L
        }

        var currentVisibleItemsList = mutableListOf<Int>()
        var previouslyVisibleItemsList = mutableListOf<Int>()
        var oldItemsList = mutableListOf<Int>()

        var isHearBeatStarted = false
        var isLibStarted = false

        /**
         * [MutableLiveData] to be manipulated in that class. Client should access it's value by using
         * [viewedItemsLiveData].
         */
        private val viewPortLiveData = ViewPortLiveData<List<Int>>()
        private val onlyNewViewPortItemsLiveData = ViewPortLiveData<List<Int>>()

        /**
         * A [CountDownTimer] to simulate 'heart beating'. It starts with [Long.MAX_VALUE] and calls
         * a 'pulse' [CountDownTimer.onTick] ever 250 milliseconds.
         */
        private val heartBeat: CountDownTimer =
            object : CountDownTimer(
                Long.MAX_VALUE,
                HEART_BEAT_TIME
            ) {
                override fun onFinish() {}
                override fun onTick(millisUntilFinished: Long) {

                    // Retrieves elements that continues visible.
                    val continueVisibleItemsList =
                        oldItemsList.intersect(currentVisibleItemsList).toMutableList()

                    // Updates oldItemsList.
                    oldItemsList = currentVisibleItemsList

                    // If there are 'continue visible' items since last pulse, updates list
                    // avoiding repeat already viewed items.
                    val valueWillChange = viewPortLiveData.value != continueVisibleItemsList
                    if (!continueVisibleItemsList.none() && valueWillChange) {
                        viewPortLiveData.value = continueVisibleItemsList

                        // Get only new items by removing previous items from current visible ones
                        val onlyNewItems = continueVisibleItemsList.subtract(previouslyVisibleItemsList)
                        previouslyVisibleItemsList = continueVisibleItemsList

                        // Updates onlyNewViewPortItemsLiveData value with only the newly visible items
                        onlyNewViewPortItemsLiveData.value = onlyNewItems.toList()
                    }
                }
            }

        private val firstAndLastVisibleItemsObserver = Observer<Pair<Int, Int>> {
            val firstItemPosition = it.first
            val lastItemPosition = it.second

            // Gets range of current completely visible items positions.
            currentVisibleItemsList = (firstItemPosition..lastItemPosition).toMutableList()

            // Updates old items viewed.
            if (oldItemsList.none()) {
                oldItemsList = currentVisibleItemsList
            }
        }

        /**
         * Protected [LiveData] to avoid client from overrides this [ViewPortLiveData].
         */
        val viewedItemsLiveData: LiveData<List<Int>> get() = viewPortLiveData
        val onlyNewViewedItemsLiveData: LiveData<List<Int>> get() = onlyNewViewPortItemsLiveData

        private fun stopHeartBeat() {
            heartBeat.cancel()
            isHearBeatStarted = false
        }

        private fun startHeartBeat() {
            if (!isHearBeatStarted) {
                isHearBeatStarted = true
                heartBeat.start()
            }
        }

        fun pauseLib() {
            stopHeartBeat()
            firstAndLastVisibleItemsLiveData.removeObserver(firstAndLastVisibleItemsObserver)
        }

        fun resumeLib() {
            startHeartBeat()
            lifecycleOwner?.let {
                firstAndLastVisibleItemsLiveData.observe(it, firstAndLastVisibleItemsObserver)
            }
        }

        fun stopLib() {
            pauseLib()
            oldItemsList.clear()
            currentVisibleItemsList.clear()
            previouslyVisibleItemsList.clear()
        }

        /**
         * Call this method to initialize this module.
         *
         * It verifies if module is already started, thus client don't need to worry about twice
         * initialization.
         */
        fun startLib() {
            stopLib()
            if (!isLibStarted) {
                isLibStarted = true
                resumeLib()
            }
        }
    }
}