package com.banglalink.toffee.ui.login

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.forEach
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.data.repository.TVChannelRepository
import com.banglalink.toffee.databinding.AlertDialogUserInterestBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.px
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.ui.common.ChildDialogFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.mychannel.MyChannelVideosEditViewModel
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.util.unsafeLazy
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UserInterestFragment : ChildDialogFragment() {
    private var chipWidth: Int = 0
    @Inject lateinit var cacheManager: CacheManager
    private var _binding: AlertDialogUserInterestBinding? = null
    @Inject lateinit var tVChannelRepository: TVChannelRepository
    private val homeViewModel: HomeViewModel by activityViewModels()
    private val binding get() = _binding !!
    private val userInterestList: MutableMap<String, Int> = mutableMapOf()
    private val viewModel: MyChannelVideosEditViewModel by activityViewModels()
    private val progressDialog by unsafeLazy { ToffeeProgressDialog(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = AlertDialogUserInterestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userInterestList.clear()
        with(binding) {
            interestChipGroup.removeAllViews()
            skipButton.safeClick({ reloadContent() })
            doneButton.safeClick({
                val userInterestCount = userInterestList.values.count { it == 1 }
                if (userInterestCount >= 3) {
                    homeViewModel.sendUserInterestData(userInterestList)
                    cPref.setUserInterestSubmitted(mPref.phoneNumber)
                    reloadContent()

                    val interestListAnalytics = userInterestList.filterValues {
                        it == 1
                    }.keys.joinToString("|")

                    ToffeeAnalytics.logEvent(ToffeeEvents.SELECT_INTEREST,  bundleOf("interest" to interestListAnalytics))
                }
                else {
                   // requireContext().showToast("Please select at least 3 interest or press skip to sign in")
                    requireContext().showToast("Please select at least 3 interests or skip to sign-in")
                }
            })
            addButton.safeClick({
                val interest = binding.otherEt.text.toString().trim()
                if (interest.isNotBlank()) {
                    val newChip = addOthersChip(interest).apply {
                        tag = interest
                    }
                    binding.interestOthersChipGroup.addView(newChip)
                    userInterestList[interest] = 1
                    binding.otherEt.text.clear()
                }
            })
        }
        observeCategory()
    }
    
    private fun observeCategory() {
        progressDialog.show()
        observe(viewModel.categories){
            chipWidth = (binding.root.measuredWidth - 48.px) / 3
            if(it.isNotEmpty()){
                val categoryList = it.sortedBy { category -> category.id }
                categoryList.let { list ->
                    list.forEachIndexed { _, category ->
                        val newChip = addChip(category.categoryName).apply {
                            tag = category.categoryName
                        }
                        binding.interestChipGroup.addView(newChip)
                        userInterestList[category.categoryName] = 0
                    }
                }
                binding.interestChipGroup.forEach { 
                    val selectedChip = it as Chip
                    selectedChip.setOnCheckedChangeListener { buttonView, isChecked ->
                        userInterestList[buttonView.tag.toString()] = if (isChecked) 1 else 0
                    }
                }
                progressDialog.hide()
            }
            else {
                progressDialog.hide()
                closeDialog()
                requireActivity().recreate()
                //requireContext().showToast("Unable to load data!")
                requireContext().showToast("Oops! Something went wrong.")
            }
        }
    }

    private fun reloadContent() {
        closeDialog()
        cacheManager.clearAllCache()
        viewLifecycleOwner.lifecycleScope.launch {
            tVChannelRepository.deleteAllRecentItems()
        }
        requireActivity().showToast(getString(R.string.verify_success), Toast.LENGTH_LONG).also {
            requireActivity().recreate()
        }
    }

    private fun addOthersChip(name: String): Chip {
        val intColor = ContextCompat.getColor(requireContext(), R.color.interest_chip_color)
        val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.fixedStrokeColor)
        val chipColor = createStateColor(intColor,intColor)
        val chip = layoutInflater.inflate(R.layout.interest_other_chip_layout, binding.interestChipGroup, false) as Chip
        with(chip) {
            text = name
            id = View.generateViewId()
            chipStrokeColor = chipColor
            layoutParams.width = chipWidth
            chipBackgroundColor = chipColor
            setTextColor(selectedTextColor)
            setOnCloseIconClickListener {
                val tag = it.tag.toString()
                binding.interestOthersChipGroup.removeView(it)
                userInterestList[tag] = 0
            }
            rippleColor =createStateColor(Color.TRANSPARENT)
        }
        return chip
    }
    
    private fun addChip(name: String): Chip {
        val intColor = ContextCompat.getColor(requireContext(), R.color.interest_chip_color)
        val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.fixedStrokeColor)
        val unSelectedTextColor = ContextCompat.getColor(requireContext(), R.color.cardTitleColor)
        val chipColor = createStateColor(intColor)
        val strokeColor = createStateColor(intColor, unSelectedTextColor)
        val chip = layoutInflater.inflate(R.layout.interest_chip_layout, binding.interestChipGroup, false) as Chip
        with(chip) {
            id = View.generateViewId()
            layoutParams.width = chipWidth
            text = name
            rippleColor = chipColor
            chipStrokeColor = strokeColor
            chipBackgroundColor = chipColor
            setTextColor(createStateColor(selectedTextColor, unSelectedTextColor))
        }
        return chip
    }
    
    private fun createStateColor(selectedColor: Int, unSelectedColor: Int = Color.TRANSPARENT): ColorStateList {
        return ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf()
            ),
            intArrayOf(
                selectedColor,
                unSelectedColor
            )
        )
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}