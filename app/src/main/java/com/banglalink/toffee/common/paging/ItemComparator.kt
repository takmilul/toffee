package com.banglalink.toffee.common.paging

import android.annotation.SuppressLint
import androidx.recyclerview.widget.DiffUtil

class ItemComparator<T: Any>: DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean {
        return oldItem == newItem
    }
}