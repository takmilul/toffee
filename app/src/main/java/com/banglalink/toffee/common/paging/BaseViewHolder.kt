package com.banglalink.toffee.common.paging

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.BR

class BaseViewHolder(val binding: ViewDataBinding)
    :RecyclerView.ViewHolder(binding.root) {

    fun bind(obj: Any, callback: Any?, position: Int) {
        binding.setVariable(BR.callback, callback)
        binding.setVariable(BR.position, position)
        binding.setVariable(BR.data, obj)

        binding.executePendingBindings()
    }
}