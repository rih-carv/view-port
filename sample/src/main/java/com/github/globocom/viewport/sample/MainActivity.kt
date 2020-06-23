package com.github.globocom.viewport.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    companion object {
        private const val LINEAR_LAYOUT_MANAGER = 0
        private const val GRID_LAYOUT_MANAGER = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setRecyclerView(LINEAR_LAYOUT_MANAGER)

        activity_main_view_port_recycler_view.apply {
            viewedItemsLiveData.observe(this@MainActivity, Observer {
                activity_main_text_view_items_viewed_tv.text =
                    getString(R.string.view_port_viewed_items, it.toString())
            })
            onlyNewViewedItemsLiveData.observe(this@MainActivity, Observer {
                activity_main_text_view_new_viewed_items.text =
                    getString(R.string.view_port_new_visible_items, it.toString())
            })
            lifecycleOwner = this@MainActivity
        }

        activity_main_button_linear_layout.setOnClickListener {
            setRecyclerView(LINEAR_LAYOUT_MANAGER)
            activity_main_view_port_recycler_view.invalidate()
        }

        activity_main_button_grid_layout.setOnClickListener {
            setRecyclerView(GRID_LAYOUT_MANAGER)
            activity_main_view_port_recycler_view.invalidate()
        }
    }

    private fun setRecyclerView(layoutManagerOption: Int) {
        activity_main_view_port_recycler_view.apply {
            setHasFixedSize(true)

            layoutManager =
                if (layoutManagerOption == GRID_LAYOUT_MANAGER) GridLayoutManager(context, 4)
                else LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

            adapter = ViewPortSampleAdapter((0..100).toList())
        }
    }
}
