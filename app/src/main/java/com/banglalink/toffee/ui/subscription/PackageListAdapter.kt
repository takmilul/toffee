package com.banglalink.toffee.ui.subscription

import com.banglalink.toffee.R
import com.banglalink.toffee.model.Package
import com.foxrentacar.foxpress.ui.common.MyBaseAdapter
import com.foxrentacar.foxpress.ui.common.MyViewHolder

class PackageListAdapter(private val packageCallBack: PackageCallBack,channelCallback:(Package)->Unit):MyBaseAdapter<Package>(channelCallback){
    override fun getLayoutIdForPosition(position: Int): Int {
       val mPackage = values[position]
        return when {
            mPackage.isBasePackage == 1 -> R.layout.free_subscription_trial_layout
            mPackage.isFree == 1 -> R.layout.free_subscription_layout
            mPackage.isSubscribed -> R.layout.already_subscribed_pack_layout
            else -> R.layout.subscribe_pack_layout
        }
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.bindCallBack(packageCallBack)
    }

    fun updatePackage(mPackage: Package) {
        val index = values.indexOf(mPackage)
        if(index != -1){
            notifyItemChanged(index)
        }
    }
}