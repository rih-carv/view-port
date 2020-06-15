package com.github.globocom.viewport.commons

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

internal object LiveDataTransformations {
    fun <T> filteringOldItems(liveData: LiveData<List<T>>): LiveData<List<T>> =
        MediatorLiveData<List<T>>().apply {
            var previousVisibleItems = listOf<T>()
            addSource(liveData) { items ->
                value = items.subtract(previousVisibleItems).toList().also {
                    previousVisibleItems = items
                }
            }
        }
}