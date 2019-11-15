package com.banglalink.toffee.ui.home

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.CommonSingleListFragment

class CatchupFragment : CommonSingleListFragment() {
    override fun onFavoriteItemRemoved(channelInfo: ChannelInfo) {
        //do nothing
    }


    private val viewModel by lazy {
        ViewModelProviders.of(this).get(CatchupViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel.parseBundle(arguments!!)
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = arguments?.getString("title")
        activity?.title = title

        viewModel.contentLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Resource.Success -> {
                    hideProgress()
                    mAdapter?.addAll(it.data)
                }
                is Resource.Failure -> {
                    hideProgress()
                    Log.e("TAG", "Failure")
                }
            }
        })
    }

    override fun loadItems(offset: Int) {
        viewModel.getContent(offset)
    }

    companion object {
        fun createInstance(
            categoryID: Int,
            subCategoryID: Int,
            subCategory: String,
            category: String,
            title: String,
            type: String
        ): CatchupFragment {
            val catchupFragment = CatchupFragment()
            val bundle = Bundle()
            bundle.putInt("category-id", categoryID)
            bundle.putInt("sub-category-id", subCategoryID)
            bundle.putString("sub-category", subCategory)
            bundle.putString("category", category)
            bundle.putString("title", title)
            bundle.putString("type", type)
            catchupFragment.arguments = bundle
            return catchupFragment
        }
    }

    fun updateInfo(
        categoryId: Int,
        subCategoryID: Int,
        subCategory: String,
        category: String,
        title: String,
        type: String
    ) {
        activity?.title = title
        binding.progressBar.visibility = View.VISIBLE
        mAdapter?.removeAll()
        mAdapter?.clearOffset()
        mAdapter?.notifyDataSetChanged()
        viewModel.updateInfo(category, categoryId, subCategory, subCategoryID, type)
    }

}