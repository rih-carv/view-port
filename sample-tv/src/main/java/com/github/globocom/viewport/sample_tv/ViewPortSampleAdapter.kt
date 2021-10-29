package com.github.globocom.viewport.sample_tv

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.globocom.viewport.sample_tv.databinding.ViewHolderViewPortSampleBinding

class ViewPortSampleAdapter(private val listItems: List<Int>) :
    RecyclerView.Adapter<ViewPortSampleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewPortSampleViewHolder(
        ViewHolderViewPortSampleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
    )

    override fun onBindViewHolder(holder: ViewPortSampleViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount() = listItems.size
}