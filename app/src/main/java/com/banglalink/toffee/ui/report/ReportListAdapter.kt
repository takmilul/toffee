package com.banglalink.toffee.ui.report

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.banglalink.toffee.BR
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BasePagingDataAdapter
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.common.paging.ItemComparator
import com.banglalink.toffee.model.OffenseType

class ReportListAdapter (private val cb: BaseListItemCallback<OffenseType>)
    : BasePagingDataAdapter<OffenseType>(cb, ItemComparator())  {

    var selectedPosition = -1
        private set

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val obj = getItem(position)
        holder.binding.setVariable(BR.selectedPosition, selectedPosition)
        obj?.let {
            holder.bind(obj, callback, position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            layoutInflater, viewType, parent, false
        )
        return BaseViewHolder(binding)
    }



    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_report_fragment_popup
    }

    fun setSelectedItemPosition(position: Int){
        selectedPosition = position
    }

}