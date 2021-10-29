package com.github.globocom.viewport.sample_tv

import androidx.recyclerview.widget.RecyclerView
import com.github.globocom.viewport.sample_tv.databinding.ViewHolderViewPortSampleBinding

class ViewPortSampleViewHolder(
    binding: ViewHolderViewPortSampleBinding
) : RecyclerView.ViewHolder(binding.root) {
    private val headline = binding.viewHolderViewPortTextViewHeadline

    fun bind() {
        with(headline) {
            text = context.getString(R.string.view_port_title_with_position, adapterPosition)
        }
    }
}
