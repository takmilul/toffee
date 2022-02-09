package com.banglalink.toffee.ui.channels

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import de.hdodenhof.circleimageview.CircleImageView

class BottomChannelViewHolder(view: View)
    :RecyclerView.ViewHolder(view) {

    val imageView = itemView.findViewById<CircleImageView>(R.id.icon)!!
}