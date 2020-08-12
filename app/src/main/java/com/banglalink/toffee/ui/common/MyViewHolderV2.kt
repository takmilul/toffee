package com.banglalink.toffee.ui.common

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.BR


class MyViewHolderV2(val binding: ViewDataBinding)
    :RecyclerView.ViewHolder(binding.root) {

    fun bind(obj: Any) {
        binding.setVariable(BR.data, obj)
//        binding.executePendingBindings()
    }

    fun bindCallBack(obj: Any?){
        binding.setVariable(BR.callback, obj)
//        binding.executePendingBindings()
    }

    fun bindPosition(pos: Int) {
        binding.setVariable(BR.position, pos)
    }
}