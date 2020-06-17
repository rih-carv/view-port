package com.github.globocom.viewport.commons

import android.os.CountDownTimer
import android.os.Parcelable
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.globocom.viewport.commons.ViewPortManager.Companion.HEART_BEAT_TIME

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
class ViewPortManager(
    private val firstAndLastVisibleItemsLiveData: LiveData<Pair<Int, Int>>,
    private val lifecycleOwner: LifecycleOwner?
) {
    private companion object {
        const val HEART_BEAT_TIME = 250L
    }

    private var currentVisibleItemsList = mutableListOf<Int>()
    private var previouslyVisibleItemsList = mutableListOf<Int>()
    private var oldItemsList = mutableListOf<Int>()

    private var isHearBeatStarted = false
    private var isLibStarted = false

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
                    oldItemsList.intersect(currentVisibleItemsList).toList()

                // Updates oldItemsList.
                oldItemsList = currentVisibleItemsList

                // If there are 'continue visible' items since last pulse, updates list
                // avoiding repeat already viewed items.
                val valueWillChange = viewPortLiveData.value != continueVisibleItemsList
                if (!continueVisibleItemsList.none() && valueWillChange) {
                    viewPortLiveData.value = continueVisibleItemsList

                    // Get only new items by removing previous items from current visible ones
                    val onlyNewItems = continueVisibleItemsList.subtract(previouslyVisibleItemsList)
                    previouslyVisibleItemsList = continueVisibleItemsList.toMutableList()

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


    fun onSaveInstanceState(myState: ViewPortSavedState): Parcelable? {
        myState.isHearBeatStarted = this.isHearBeatStarted
        myState.isLibStarted = this.isLibStarted
        myState.currentVisibleItemsList = this.currentVisibleItemsList
        myState.previouslyVisibleItemsList = this.previouslyVisibleItemsList
        myState.oldItemsList = this.oldItemsList
        return myState
    }

    fun onRestoreInstanceState(state: Parcelable?) {
        (state as? ViewPortSavedState)?.let {
            this.isHearBeatStarted = it.isHearBeatStarted
            this.isLibStarted = it.isLibStarted
            this.currentVisibleItemsList = it.currentVisibleItemsList
            this.previouslyVisibleItemsList = it.previouslyVisibleItemsList
            this.oldItemsList = it.oldItemsList
        }
    }

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
     *
     * @throws [ExceptionInInitializerError] in case of [recyclerView] has not [LinearLayoutManager]
     * or [GridLayoutManager] as type of it's [RecyclerView.LayoutManager] (see [verifyPremises]).
     */
    @Throws(ExceptionInInitializerError::class)
    fun startLib() {
        stopLib()
        if (!isLibStarted) {
            isLibStarted = true
            resumeLib()
        }
    }
}