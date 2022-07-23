package com.github.globocom.viewport.sample_tv

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.recyclerview.widget.RecyclerView
import com.github.globocom.viewport.commons.ViewPortLiveData
import com.github.globocom.viewport.sample_tv.ViewPortSampleNestedItemsAdapter.ViewPortNestedSampleViewHolder
import com.github.globocom.viewport.sample_tv.databinding.ViewHolderNestedViewPortSampleBinding

class ViewPortSampleNestedItemsAdapter(
    private val listItems: List<Int>,
    private val lifecycleOwner: LifecycleOwner,
    private val mainViewedItemsLiveData: ViewPortLiveData<List<Int>>,
    private val mainOnlyNewViewedItemsLiveData: ViewPortLiveData<List<Int>>
) : RecyclerView.Adapter<ViewPortNestedSampleViewHolder>() {
    private val sampleItemsAdapter = ViewPortSampleItemsAdapter((0..100).toList())
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
            ViewHolderNestedViewPortSampleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), sampleItemsAdapter
        ).also { observe(it) }

    override fun onBindViewHolder(holder: ViewPortNestedSampleViewHolder, position: Int) =
        holder.bind(lifecycleOwner)

    private fun observe(viewHolder: ViewPortNestedSampleViewHolder) = with(viewHolder.binding) {
        indexedNestedViewedItems.apply {
            addSource(horizontalRecyclerView.viewedItemsLiveData) {
                value = value.orEmpty() + (viewHolder.absoluteAdapterPosition to it)
            }
        }
        indexedNestedOnlyNewViewedItems.apply {
            addSource(horizontalRecyclerView.onlyNewViewedItemsLiveData) {
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
        internal val binding: ViewHolderNestedViewPortSampleBinding,
        private val sampleItemsAdapter: ViewPortSampleItemsAdapter
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(lifecycle: LifecycleOwner): Unit = with(binding) {
            headlineTextView.apply {
                text = context.getString(
                    R.string.view_port_title_with_position,
                    absoluteAdapterPosition
                )
            }
            horizontalRecyclerView.apply {
                lifecycleOwner = lifecycle
                setHasFixedSize(true)
                adapter = sampleItemsAdapter
            }
        }
    }
}