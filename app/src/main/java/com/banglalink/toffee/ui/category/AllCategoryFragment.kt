package com.banglalink.toffee.ui.category

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import com.banglalink.toffee.model.NavCategory
import com.banglalink.toffee.ui.common.SingleListFragmentV2
import com.banglalink.toffee.ui.common.SingleListItemCallback

class AllCategoryFragment: SingleListFragmentV2<NavCategory>(),
    SingleListItemCallback<NavCategory> {

    override val shouldLoadMore: Boolean = false

    @SuppressLint("UseRequireInsteadOfGet")
    override fun initAdapter() {
        mAdapter = CategoryListAdapter(this)
        mViewModel = ViewModelProvider(activity!!)[CategoryViewModel::class.java]
    }

    override fun onItemClicked(item: NavCategory) {
        parentFragment?.findNavController()?.
        navigate(R.id.action_allCategoriesFragment_to_categoryDetailsFragment, Bundle().apply {
            putParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM, item)
        })
    }

    override fun getRecyclerLayoutManager(): RecyclerView.LayoutManager {
        return GridLayoutManager(context, 3)
    }
}