package com.banglalink.toffee.ui.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.banglalink.toffee.R
import com.banglalink.toffee.model.UgcCategory
import com.banglalink.toffee.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryDetailsFragment : BaseFragment() {
    lateinit var category: UgcCategory

    companion object {
        const val ARG_CATEGORY_ITEM = "ARG_CATEGORY_ITEM"
        const val ARG_SUBCATEGORY_ITEM = "ARG_CATEGORY_ITEM"

        fun newInstance(category: UgcCategory): CategoryDetailsFragment {
            return CategoryDetailsFragment().apply {
                arguments = Bundle().also {
                    it.putParcelable(ARG_CATEGORY_ITEM, category)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_category_details, container, false)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        category = requireArguments().getParcelable(ARG_CATEGORY_ITEM)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = category.categoryName
    }
}