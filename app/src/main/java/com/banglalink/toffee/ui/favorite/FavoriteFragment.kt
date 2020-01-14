package com.banglalink.toffee.ui.favorite

import android.os.Bundle
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.CommonSingleListFragment

class FavoriteFragment : CommonSingleListFragment() {
    private val viewModel by lazy {
        ViewModelProviders.of(this).get(FavoriteViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "Favorites"

    }

    override fun loadItems():LiveData<Resource<List<ChannelInfo>>> {
        return viewModel.loadFavoriteContents()
    }

    override fun removeUnFavoriteItemFromList(): Boolean {
        return true
    }
}