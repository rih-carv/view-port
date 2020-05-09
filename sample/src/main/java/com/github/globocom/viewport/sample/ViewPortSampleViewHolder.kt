package com.github.globocom.viewport.sample

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.view_holder_view_port_sample.view.*

class ViewPortSampleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val headline = view.view_holder_view_port_text_view_headline

    fun bind() {
        with(headline) {
            text = context.getString(R.string.view_port_title_with_position, adapterPosition)
        }
    }
}
