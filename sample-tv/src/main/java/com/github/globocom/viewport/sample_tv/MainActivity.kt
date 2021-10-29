package com.github.globocom.viewport.sample_tv

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.leanback.widget.BaseGridView
import androidx.lifecycle.Observer
import com.github.globocom.viewport.sample_tv.databinding.ActivityMainBinding

class MainActivity : FragmentActivity() {
    enum class Layout(
        val active: ActivityMainBinding.() -> BaseGridView,
        val inactive: ActivityMainBinding.() -> BaseGridView
    ) {
        VERTICAL(
            ActivityMainBinding::activityMainViewPortVerticalRecyclerView,
            ActivityMainBinding::activityMainViewPortHorizontalRecyclerView
        ),
        HORIZONTAL(
            ActivityMainBinding::activityMainViewPortHorizontalRecyclerView,
            ActivityMainBinding::activityMainViewPortVerticalRecyclerView
        )
    }

    private val sampleAdapter = ViewPortSampleAdapter((0..100).toList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(ActivityMainBinding.inflate(layoutInflater).setupView().root)
    }

    private fun ActivityMainBinding.setupView() = apply {
        setRecyclerViewLayout(Layout.VERTICAL)

        activityMainViewPortVerticalRecyclerView.apply {
            viewedItemsLiveData.observe(this@MainActivity, Observer {
                activityMainTextViewItemsViewedTv.text =
                    getString(R.string.view_port_viewed_items, it.toString())
            })
            onlyNewViewedItemsLiveData.observe(this@MainActivity, Observer {
                activityMainTextViewNewViewedItems.text =
                    getString(R.string.view_port_new_visible_items, it.toString())
            })
            lifecycleOwner = this@MainActivity
        }

        activityMainViewPortHorizontalRecyclerView.apply {
            viewedItemsLiveData.observe(this@MainActivity, Observer {
                activityMainTextViewItemsViewedTv.text =
                    getString(R.string.view_port_viewed_items, it.toString())
            })
            onlyNewViewedItemsLiveData.observe(this@MainActivity, Observer {
                activityMainTextViewNewViewedItems.text =
                    getString(R.string.view_port_new_visible_items, it.toString())
            })
            lifecycleOwner = this@MainActivity
        }

        activityMainButtonVerticalLayout.setOnClickListener {
            setRecyclerViewLayout(Layout.VERTICAL)
        }

        activityMainButtonHorizontalLayout.setOnClickListener {
            setRecyclerViewLayout(Layout.HORIZONTAL)
        }
    }

    private fun ActivityMainBinding.setRecyclerViewLayout(layoutOption: Layout) {
        layoutOption.inactive(this).visibility = View.GONE
        layoutOption.active(this).apply {
            setHasFixedSize(true)
            visibility = View.VISIBLE
            adapter = sampleAdapter
            invalidate()
        }
    }
}