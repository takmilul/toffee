package com.banglalink.toffee.ui.premium

import android.widget.RadioButton
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.data.network.response.PackPaymentMethod
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.MyBaseAdapter
import com.banglalink.toffee.ui.common.MyViewHolder

class DataPackAdapter(
    cb: BaseListItemCallback<PackPaymentMethod>,
) : MyBaseAdapter<PackPaymentMethod>(cb) {
    private var selectedItem: PackPaymentMethod? = null
    
    override fun getLayoutIdForPosition(position: Int): Int {
        return if (values[position].listTitle!=null) {
            R.layout.item_data_pack_title
        }else{
            R.layout.item_data_pack
        }
        
    }
    
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val obj = getItem(position)
        if(obj.listTitle!=null){
            return
        }else{
            obj.let {
                val radioButton = holder.itemView.findViewById<RadioButton>(R.id.prepaidRadioButtonOne)
                radioButton.isChecked = obj.dataPackId==selectedItem?.dataPackId
            }
        }
        
    }
    
    fun setSelectedItem(item: PackPaymentMethod?) {
        selectedItem = item
        notifyDataSetChanged()
    }
    
}