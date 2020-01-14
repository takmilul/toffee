package com.banglalink.toffee.ui.common

import android.content.Intent
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.R
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.widget.MyPopupWindow
import com.banglalink.toffee.ui.home.OptionCallBack
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.util.unsafeLazy

abstract class HomeBaseFragment:Fragment(), OptionCallBack {
    val homeViewModel by unsafeLazy {
        ViewModelProviders.of(activity!!).get(HomeViewModel::class.java)
    }

    override fun onOptionClicked(anchor: View, channelInfo: ChannelInfo) {
        val popupMenu = MyPopupWindow(context!!, anchor)
        popupMenu.inflate(R.menu.menu_catchup_item)

        if (channelInfo.favorite == null || channelInfo.favorite == "0") {
            popupMenu.menu.getItem(0).title = "Add to Favorites"
        } else {
            popupMenu.menu.getItem(0).title = "Remove from Favorites"
        }
        popupMenu.setOnMenuItemClickListener{
            when(it?.itemId){
                R.id.menu_share->{
                    val sharingIntent = Intent(Intent.ACTION_SEND)
                    sharingIntent.type = "text/plain"
                    sharingIntent.putExtra(
                        Intent.EXTRA_TEXT,
                        channelInfo.video_share_url
                    )
                    activity?.startActivity(Intent.createChooser(sharingIntent, "Share via"))
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_fav->{
                    homeViewModel.updateFavorite(channelInfo).observe(viewLifecycleOwner, Observer {
                        handleFavoriteResponse(it)
                    })
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_not_interested->{
                    removeItemNotInterestedItem(channelInfo)
                    return@setOnMenuItemClickListener true
                }
                else->{
                    return@setOnMenuItemClickListener false
                }
            }
        }
        popupMenu.show()
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