package com.github.globocom.viewport.sample

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ViewPortSampleAdapter(private val listItems: List<Int>) :
    RecyclerView.Adapter<ViewPortSampleViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ) = ViewPortSampleViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.view_holder_view_port_sample, parent, false)
    )

    override fun onBindViewHolder(holder: ViewPortSampleViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount() = listItems.size
}