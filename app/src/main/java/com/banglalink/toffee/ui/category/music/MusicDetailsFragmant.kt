package com.banglalink.toffee.ui.category.music

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.FirebaseParams
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.enums.PageType
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.LandingPageViewModel
import java.util.*

class MusicDetailsFragmant : BaseFragment() {

    lateinit var category: Category
    private val landingViewModel by activityViewModels<LandingPageViewModel>()

    companion object {
        const val ARG_CATEGORY_ITEM = "ARG_CATEGORY_ITEM"
        const val ARG_SUBCATEGORY_ITEM = "ARG_CATEGORY_ITEM"
        const val ARG_TITLE = "title"

        fun newInstance(category: Category): MusicDetailsFragmant {
            return MusicDetailsFragmant().apply {
                arguments = Bundle().also {
                    it.putParcelable(ARG_CATEGORY_ITEM, category)
                    it.putString(ARG_TITLE, category.categoryName)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_music_details, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        category = requireArguments().getParcelable(ARG_CATEGORY_ITEM)!!
        landingViewModel.pageType.value = PageType.Category
        landingViewModel.pageName.value = category.categoryName.uppercase(Locale.getDefault()) + "CATEGORY_PAGE"
        landingViewModel.featuredPageName.value = category.categoryName + " Page"
        landingViewModel.checkedSubCategoryChipId.value = 0
        landingViewModel.categoryId.value = category.id.toInt()
        mPref.categoryId.value = category.id.toInt()
        mPref.categoryName.value = category.categoryName
        landingViewModel.subCategoryId.value = 0
        landingViewModel.isDramaSeries.value = false
        ToffeeAnalytics.logEvent(
            ToffeeEvents.SCREEN_VIEW,  bundleOf(
                FirebaseParams.BROWSER_SCREEN to "category",
            "category_type" to category.categoryName)
        )
    }
}