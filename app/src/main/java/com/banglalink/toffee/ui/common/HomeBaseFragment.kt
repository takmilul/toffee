package com.banglalink.toffee.ui.common

import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.banglalink.toffee.R
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.listeners.OptionCallBack
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.report.ReportPopupFragment
import com.banglalink.toffee.ui.widget.MyPopupWindow

abstract class HomeBaseFragment:BaseFragment(), OptionCallBack {

    val homeViewModel by activityViewModels<HomeViewModel>()

    override fun onOptionClicked(anchor: View, channelInfo: ChannelInfo) {
        val popupMenu = MyPopupWindow(requireContext(), anchor)
        popupMenu.inflate(R.menu.menu_catchup_item)

        if (channelInfo.favorite == null || channelInfo.favorite == "0") {
            popupMenu.menu.getItem(0).title = "Add to Favorites"
        } else {
            popupMenu.menu.getItem(0).title = "Remove from Favorites"
        }
        if(hideNotInterestedMenuItem(channelInfo)){//we are checking if that could be shown or not
            popupMenu.menu.getItem(2).isVisible = false
        }
        popupMenu.menu.findItem(R.id.menu_share).isVisible = hideShareMenuItem() && channelInfo.isApproved == 1
        popupMenu.menu.findItem(R.id.menu_report).isVisible = mPref.customerId != channelInfo.channel_owner_id
        popupMenu.setOnMenuItemClickListener{
            when(it?.itemId){
                R.id.menu_share->{
                    if (!mPref.isVerifiedUser) {
                        (activity as HomeActivity).handleVerficationApp()
                        return@setOnMenuItemClickListener true
                    }
                    homeViewModel.shareContentLiveData.value = channelInfo
                }
                R.id.menu_fav->{
                    if (!mPref.isVerifiedUser) {
                        (activity as HomeActivity).handleVerficationApp()
                        return@setOnMenuItemClickListener true
                    }
                    homeViewModel.updateFavorite(channelInfo).observe(viewLifecycleOwner, Observer {
                        handleFavoriteResponse(it)
                    })
                }
                R.id.menu_report -> {
                    if (!mPref.isVerifiedUser) {
                        (activity as HomeActivity).handleVerficationApp()
                        return@setOnMenuItemClickListener true
                    }
                    val fragment =
                        channelInfo.duration?.let { durations ->
                            ReportPopupFragment.newInstance(- 1,
                                durations, channelInfo.id
                            )
                        }
                    fragment?.show(requireActivity().supportFragmentManager, "report_video")
                }
                R.id.menu_not_interested->{
                    if (!mPref.isVerifiedUser) {
                        (activity as HomeActivity).handleVerficationApp()
                        return@setOnMenuItemClickListener true
                    }
                    removeItemNotInterestedItem(channelInfo)
                }
            }
            return@setOnMenuItemClickListener true
        }
        popupMenu.show()
    }

    open fun hideShareMenuItem(hide: Boolean = false): Boolean {
        return hide
    }

    //hook for subclass for to hide Not Interested Menu Item.
    //In catchup details fragment we need to hide this menu from popup menu for first item
    open fun hideNotInterestedMenuItem(channelInfo: ChannelInfo):Boolean{
        return false
    }

    fun handleFavoriteResponse(it:Resource<ChannelInfo>){
        when(it){
            is Resource.Success->{
                val channelInfo = it.data
                when(channelInfo.favorite){
                    "0"->{
                        context?.showToast("Content successfully removed from favorite list")
                        handleFavoriteRemovedSuccessFully(channelInfo)
                    }
                    "1"->{
                        handleFavoriteAddedSuccessfully(channelInfo)
                        context?.showToast("Content successfully added to favorite list")
                    }
                }
            }
            is Resource.Failure->{
                context?.showToast(it.error.msg)
            }
        }
    }

    open fun handleFavoriteAddedSuccessfully(channelInfo: ChannelInfo){
        //subclass can hook here
    }

    open fun handleFavoriteRemovedSuccessFully(channelInfo: ChannelInfo){
        //subclass can hook here
    }

    override fun viewAllVideoClick() {
        homeViewModel.viewAllVideoLiveData.postValue(true)
    }

    abstract fun removeItemNotInterestedItem(channelInfo: ChannelInfo)
}