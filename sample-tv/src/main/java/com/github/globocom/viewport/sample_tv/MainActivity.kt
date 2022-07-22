package com.github.globocom.viewport.sample_tv

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentActivity
import androidx.leanback.widget.BaseGridView
import androidx.lifecycle.LiveData
import com.github.globocom.viewport.sample_tv.databinding.ActivityMainBinding

class MainActivity : FragmentActivity() {
    enum class Layout(val activeGridView: ActivityMainBinding.() -> BaseGridView) {
        VERTICAL(ActivityMainBinding::verticalRecyclerView),
        HORIZONTAL(ActivityMainBinding::horizontalRecyclerView),
        NESTED(ActivityMainBinding::nestedRecyclerView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivityMainBinding.inflate(layoutInflater).setupView().root)
    }

    private fun ActivityMainBinding.setupView() = apply {
        infix fun Button.sets(layoutOption: Layout) = setOnClickListener {
            setRecyclerViewLayout(layoutOption)
        }

        verticalLayoutButton sets Layout.VERTICAL
        horizontalLayoutButton sets Layout.HORIZONTAL
        nestedLayoutButton sets Layout.NESTED

        setupRecyclerViews()
        setRecyclerViewLayout(Layout.VERTICAL)
    }

    private fun ActivityMainBinding.setRecyclerViewLayout(layoutOption: Layout) {
        Layout.values().forEach { it.activeGridView(this).visibility = View.GONE }
        layoutOption.activeGridView(this).visibility = View.VISIBLE
    }

    private fun ActivityMainBinding.setupRecyclerViews() {
        val sampleItemsAdapter = ViewPortSampleItemsAdapter((0..100).toList())

        verticalRecyclerView.apply {
            setHasFixedSize(true)
            adapter = sampleItemsAdapter
            lifecycleOwner = this@MainActivity
            viewedItemsTextView with R.string.view_port_viewed_items bindOn viewedItemsLiveData
            newViewedItemsTextView with R.string.view_port_new_visible_items bindOn onlyNewViewedItemsLiveData
        }

        horizontalRecyclerView.apply {
            setHasFixedSize(true)
            adapter = sampleItemsAdapter
            lifecycleOwner = this@MainActivity
            viewedItemsTextView with R.string.view_port_viewed_items bindOn viewedItemsLiveData
            newViewedItemsTextView with R.string.view_port_new_visible_items bindOn onlyNewViewedItemsLiveData
        }

        nestedRecyclerView.apply {
            val nestedAdapter = ViewPortSampleNestedItemsAdapter(
                (0..100).toList(),
                this@MainActivity,
                viewedItemsLiveData,
                onlyNewViewedItemsLiveData
            )
            setHasFixedSize(true)
            adapter = nestedAdapter
            lifecycleOwner = this@MainActivity
            viewedItemsTextView with R.string.view_port_viewed_items bindOn nestedAdapter.viewedItemsLiveData
            newViewedItemsTextView with R.string.view_port_new_visible_items bindOn nestedAdapter.onlyNewViewedItemsLiveData
        }
    }

    private infix fun TextView.with(@StringRes stringId: Int) = Text(this, stringId)

    private infix fun Text.bindOn(liveData: LiveData<*>) = liveData.observe(this@MainActivity) {
        textView.text = getString(stringId, it.toString())
    }

    private class Text(
        val textView: TextView,
        @StringRes val stringId: Int
    )
}