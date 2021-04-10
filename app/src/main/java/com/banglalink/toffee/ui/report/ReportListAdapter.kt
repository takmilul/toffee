package com.banglalink.toffee.ui.report

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.BR
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.common.paging.BaseViewHolder
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.ReportListModel

class ReportListAdapter (private val callback: BaseListItemCallback<Category>,
                         private val dataList:List<Category>)
    :RecyclerView.Adapter<BaseViewHolder>(

) {

    var selectedPosition = -1
        private set

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val obj = dataList.get(position)
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

    override fun getItemCount(): Int {
      return  dataList.size

    }

    override fun getItemViewType(position: Int): Int {
        return R.layout.list_item_report_fragment_popup
    }

    fun setSelectedItemPosition(position: Int){
        selectedPosition = position
    }

}