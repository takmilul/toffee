package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.banglalink.toffee.R
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.home.CategoriesListAdapter
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.ui.home.OptionCallBack
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.android.synthetic.main.fragment_landing_categories.*

class AllCategoriesFragment: Fragment(R.layout.fragment_landing_categories) {
    private lateinit var mAdapter: CategoriesListAdapter

    val viewModel by unsafeLazy {
        ViewModelProvider(activity!!)[LandingPageViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoriesHeader.visibility = View.GONE
        viewAllButton.visibility = View.GONE

        mAdapter = CategoriesListAdapter(object: OptionCallBack{
            override fun onOptionClicked(anchor: View, channelInfo: ChannelInfo) {

            }

            override fun viewAllVideoClick() {

            }
        }) {
            val args = Bundle().apply {
                putParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM, it)
            }
            parentFragment?.
            findNavController()?.
            navigate(R.id.action_allCategoriesFragment_to_categoryDetailsFragment, args)
        }

        with(categoriesList) {
            layoutManager = GridLayoutManager(context, 3)
            adapter = mAdapter
        }

//        viewModel.loadCategories()

        observeList()
    }

    private fun observeList() {
        viewModel.categoryInfoLiveData.observe(viewLifecycleOwner, Observer {
            when(it) {
                is Resource.Success -> {
                    mAdapter.addAll(it.data)
                }
                is Resource.Failure -> {
                    // Log error
                }
            }
        })
    }
}