package com.banglalink.toffee.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.banglalink.toffee.R
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.category.CategoryDetailsFragment
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.CategoriesListAdapter
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.util.unsafeLazy
import kotlinx.android.synthetic.main.fragment_landing_categories.*

class LandingCategoriesFragment: HomeBaseFragment() {
    private lateinit var mAdapter: CategoriesListAdapter

    val viewModel by unsafeLazy {
        ViewModelProvider(activity!!)[LandingPageViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_landing_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = CategoriesListAdapter(this) {
            parentFragment?.
            findNavController()?.
            navigate(R.id.action_landingPageFragment_to_categoryDetailsFragment,
            Bundle().apply {
                putParcelable(CategoryDetailsFragment.ARG_CATEGORY_ITEM, it)
            })
        }

        viewAllButton.setOnClickListener {
            parentFragment?.findNavController()?.navigate(R.id.action_landingPageFragment_to_allCategoriesFragment)
        }

        with(categoriesList) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = mAdapter
        }

        viewModel.loadCategories()

        observeList()
    }

    private fun observeList() {
        viewModel.categoryInfoLiveData.observe(viewLifecycleOwner, Observer {
            when(it) {
                is Resource.Success -> {
                    mAdapter.addAll(it.data)
                }
            }
        })
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }
}