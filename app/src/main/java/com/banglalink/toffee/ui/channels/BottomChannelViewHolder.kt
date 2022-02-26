package com.banglalink.toffee.ui.channels

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R

class BottomChannelViewHolder(view: View)
    :RecyclerView.ViewHolder(view) {

    val imageView = itemView.findViewById<ImageView>(R.id.icon)!!
}