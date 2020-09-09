package com.banglalink.toffee.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.listeners.EndlessRecyclerViewScrollListener
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.HomeBaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import com.banglalink.toffee.util.unsafeLazy
import com.daimajia.slider.library.SliderTypes.BaseSliderView
import com.daimajia.slider.library.SliderTypes.DefaultSliderView

class CategoryDetailsFragment : HomeBaseFragment() {
    lateinit var category: Category

    companion object {
        const val ARG_CATEGORY_ITEM = "ARG_CATEGORY_ITEM"

        fun newInstance(category: Category): CategoryDetailsFragment {
            return CategoryDetailsFragment().apply {
                arguments = Bundle().also {
                    it.putParcelable(ARG_CATEGORY_ITEM, category)
                }
            }
        }
    }

    override fun removeItemNotInterestedItem(channelInfo: ChannelInfo) {

    }

    val viewModel by unsafeLazy {
        ViewModelProviders.of(this).get(LandingPageViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        category = requireArguments().getParcelable(ARG_CATEGORY_ITEM)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_category_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = category.name
    }

    fun onBackPressed(): Boolean {
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }
}