package com.banglalink.toffee.ui.favorite

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.CommonSingleListFragment

class FavoriteFragment : CommonSingleListFragment() {
    override fun onFavoriteItemRemoved(channelInfo: ChannelInfo) {
        mAdapter?.remove(channelInfo)
    }

    override fun loadItems() {
        viewModel.loadFavoriteContents()
    }

    private val viewModel by lazy {
        ViewModelProviders.of(this).get(FavoriteViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "Favorites"

        viewModel.favoriteListLiveData.observe(viewLifecycleOwner, Observer {
            hideProgress()
            when (it) {
                is Resource.Success -> {

                    mAdapter?.addAll(it.data)
                }
                is Resource.Failure -> {
                    activity?.showToast(it.error.msg)
                }
            }
        })

    }
}