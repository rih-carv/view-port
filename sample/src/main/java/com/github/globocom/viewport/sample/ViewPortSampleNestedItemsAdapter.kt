package com.github.globocom.viewport.sample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.globocom.viewport.sample.ViewPortSampleNestedItemsAdapter.ViewPortNestedSampleViewHolder
import kotlinx.android.synthetic.main.view_holder_nested_view_port_sample.view.*

class ViewPortSampleNestedItemsAdapter(
    private val listItems: List<Int>,
    private val lifecycleOwner: LifecycleOwner,
    private val mainViewedItemsLiveData: LiveData<List<Int>>,
    private val mainOnlyNewViewedItemsLiveData: LiveData<List<Int>>
) : RecyclerView.Adapter<ViewPortNestedSampleViewHolder>() {
    private val sampleItemsAdapter = ViewPortSampleAdapter((0..100).toList())
    private val indexedNestedViewedItems = MediatorLiveData<Map<Int, List<Int>>>()
    private val indexedNestedOnlyNewViewedItems = MediatorLiveData<Map<Int, List<Int>>>()
    val viewedItemsLiveData = MediatorLiveData<Map<Int, List<Int>>>().apply {
        combine(mainViewedItemsLiveData, indexedNestedViewedItems)
    } as LiveData<Map<Int, List<Int>>>
    val onlyNewViewedItemsLiveData = MediatorLiveData<Map<Int, List<Int>>>().apply {
        combine(mainOnlyNewViewedItemsLiveData, indexedNestedOnlyNewViewedItems)
    } as LiveData<Map<Int, List<Int>>>

    override fun getItemCount() = listItems.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewPortNestedSampleViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.view_holder_nested_view_port_sample,
                parent,
                false
            ), sampleItemsAdapter
        ).also { observe(it) }

    override fun onBindViewHolder(holder: ViewPortNestedSampleViewHolder, position: Int) =
        holder.bind(lifecycleOwner)

    private fun observe(viewHolder: ViewPortNestedSampleViewHolder) = with(viewHolder.view) {
        indexedNestedViewedItems.apply {
            addSource(horizontal_recycler_view.viewedItemsLiveData) {
                value = value.orEmpty() + (viewHolder.absoluteAdapterPosition to it)
            }
        }
        indexedNestedOnlyNewViewedItems.apply {
            addSource(horizontal_recycler_view.onlyNewViewedItemsLiveData) {
                value = value.orEmpty() + (viewHolder.absoluteAdapterPosition to it)
            }
        }
    }

    private fun MediatorLiveData<Map<Int, List<Int>>>.combine(
        mainViewport: LiveData<List<Int>>,
        indexedNestedViewport: LiveData<Map<Int, List<Int>>>
    ) {
        fun Map<Int, List<Int>>.coerceIn(main: List<Int>) { value = filterKeys { it in main } }
        addSource(mainViewport) { indexedNestedViewport.value.orEmpty().coerceIn(it) }
        addSource(indexedNestedViewport) { it.coerceIn(mainViewport.value.orEmpty()) }
    }

    class ViewPortNestedSampleViewHolder(
        internal val view: View,
        private val sampleItemsAdapter: ViewPortSampleAdapter
    ) : RecyclerView.ViewHolder(view) {
        fun bind(lifecycle: LifecycleOwner): Unit = with(view) {
            headline_text_view.apply {
                text = context.getString(
                    R.string.view_port_title_with_position,
                    absoluteAdapterPosition
                )
            }
            horizontal_recycler_view.apply {
                lifecycleOwner = lifecycle
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                adapter = sampleItemsAdapter
            }
        }
    }
}