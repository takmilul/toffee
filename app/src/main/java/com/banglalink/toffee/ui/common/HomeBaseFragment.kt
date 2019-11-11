package com.banglalink.toffee.ui.common

import android.os.Bundle
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
import com.banglalink.toffee.ui.player.ChannelInfo

abstract class HomeBaseFragment:Fragment(), OptionCallBack {
    val homeViewModel by lazy {
        ViewModelProviders.of(activity!!).get(HomeViewModel::class.java)
    }

    protected val baseViewModel by lazy {
        ViewModelProviders.of(this).get(BaseViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeFavoriteLiveData()

    }

    protected open fun observeFavoriteLiveData(){
        baseViewModel.favoriteLiveData.observe(viewLifecycleOwner, Observer {
            when(it){
                is Resource.Success->{
                    val channelInfo = it.data
                    when(channelInfo.favorite){
                        "0"->context?.showToast("Content successfully removed from favorite list")
                        "1"->context?.showToast("Content successfully added to favorite list")
                    }
                }
                is Resource.Failure->{
                    context?.showToast(it.error.msg)
                }
            }
        })
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
                    activity?.showToast("Share clicked ${channelInfo.program_name} ")
                    return@setOnMenuItemClickListener true
                }
                R.id.menu_fav->{
                    baseViewModel.updateFavorite(channelInfo)
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

    abstract fun removeItemNotInterestedItem(channelInfo: ChannelInfo)
}