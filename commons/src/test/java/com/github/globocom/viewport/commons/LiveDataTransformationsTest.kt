package com.github.globocom.viewport.commons

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.globocom.viewport.commons.LiveDataTransformations.filteringOldItems
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class LiveDataTransformationsTest {
    @Rule
    @JvmField
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private fun <T> LiveData<T>.triggeringUpdates() = this.apply { observeForever {} }

    @Test
    fun testFilteringOldItemsWithoutValue() {
        val visibleItems = MutableLiveData<List<Int>>()
        val result = filteringOldItems(visibleItems).triggeringUpdates()

        assertEquals(null, result.value)
    }

    @Test
    fun testFilteringOldItemsWithFirstValue() {
        val visibleItems = MutableLiveData<List<Int>>()
        val result = filteringOldItems(visibleItems).triggeringUpdates()

        visibleItems.value = listOf(0, 1, 2)

        assertEquals(listOf(0, 1, 2), result.value)
    }

    @Test
    fun testFilteringOldItemsWithDifferentItems() {
        val visibleItems = MutableLiveData<List<Int>>()
        val result = filteringOldItems(visibleItems).triggeringUpdates()

        visibleItems.value = listOf(0, 1, 2)
        visibleItems.value = listOf(3, 4, 5)

        assertEquals(listOf(3, 4, 5), result.value)
    }

    @Test
    fun testFilteringOldItemsWithDifferentLengths() {
        val visibleItems = MutableLiveData<List<Int>>()
        val result = filteringOldItems(visibleItems).triggeringUpdates()

        visibleItems.value = listOf(0, 1, 2)
        visibleItems.value = listOf(3, 4, 5, 6)
        assertEquals(listOf(3, 4, 5, 6), result.value)

        visibleItems.value = listOf(0, 1, 2)
        assertEquals(listOf(0, 1, 2), result.value)
    }

    @Test
    fun testFilteringOldItemsWithRepeatedItems() {
        val visibleItems = MutableLiveData<List<Int>>()
        val result = filteringOldItems(visibleItems).triggeringUpdates()

        visibleItems.value = listOf(0, 1, 2)
        visibleItems.value = listOf(1, 2, 3)

        assertEquals(listOf(3), result.value)
    }

    @Test
    fun testFilteringOldItemsWithRepeatedItemsInDifferentOrder() {
        val visibleItems = MutableLiveData<List<Int>>()
        val result = filteringOldItems(visibleItems).triggeringUpdates()

        visibleItems.value = listOf(0, 1, 6, 4)
        visibleItems.value = listOf(4, 3, 1, 5)

        assertEquals(listOf(3, 5), result.value)
    }

    @Test
    fun testFilteringOldItemsWithRepeatedItemsPreservesOrder() {
        val visibleItems = MutableLiveData<List<Int>>()
        val result = filteringOldItems(visibleItems).triggeringUpdates()

        visibleItems.value = listOf(1, 2, 3)
        visibleItems.value = listOf(4, 3, 1, 0)

        assertEquals(listOf(4, 0), result.value)
    }

    @Test
    fun testFilteringOldItemsWithEqualItems() {
        val visibleItems = MutableLiveData<List<Int>>()
        val result = filteringOldItems(visibleItems).triggeringUpdates()

        visibleItems.value = listOf(0, 1, 2)
        visibleItems.value = listOf(0, 1, 2)

        assertEquals(emptyList<Int>(), result.value)
    }
}