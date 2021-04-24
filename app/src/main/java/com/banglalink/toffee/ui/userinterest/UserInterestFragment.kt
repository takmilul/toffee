package com.banglalink.toffee.ui.userinterest

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentUserInterestBinding
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.onTransitionCompletedListener
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.model.CustomerInfoSignIn
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.mychannel.MyChannelVideosEditViewModel
import com.google.android.material.chip.Chip

class UserInterestFragment : BaseFragment() {
    private var verifiedUserData: CustomerInfoSignIn? = null
    private var _binding: FragmentUserInterestBinding? = null
    private val binding get() = _binding !!
    private val selectedInterestList: ArrayList<Int> = arrayListOf()
    private val homeViewModel: HomeViewModel by activityViewModels()
    private val viewModel: MyChannelVideosEditViewModel by activityViewModels()
    private val args by navArgs<UserInterestFragmentArgs>()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUserInterestBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    companion object {
        @JvmStatic
        fun newInstance() = UserInterestFragment()
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        verifiedUserData = args.verifiedUserData
        selectedInterestList.clear()
        binding.interestChipGroup.removeAllViews()
        
        binding.confirmBtn.safeClick({
            homeViewModel.sendUserInterestData(selectedInterestList)
            signIn()
        })
        binding.skipButton.safeClick({ signIn() })
        
        observeCategory()
    }
    
    private fun signIn() {
        val signInMotionLayout = parentFragment?.parentFragment?.view
        signInMotionLayout?.let {
            if (it is MotionLayout) {
                it.onTransitionCompletedListener { onLoginSuccessAnimationCompletion() }
                it.setTransition(R.id.firstEndAnim, R.id.secondEndAmin)
                it.transitionToEnd()
            }
        }
    }
    
    private fun observeCategory() {
        observe(viewModel.categories){
            if(it.isNotEmpty()){
                val categoryList = it.sortedBy { category -> category.id }
                categoryList.let { list ->
                    list.forEachIndexed { _, category ->
                        val newChip = addChip(category.categoryName).apply {
                            tag = category.id
                        }
                        binding.interestChipGroup.addView(newChip)
                    }
                }
                binding.interestChipGroup.forEach { 
                    val selectedChip = it as Chip
                    selectedChip.setOnCheckedChangeListener { buttonView, isChecked ->
                        selectedInterestList.add(buttonView.tag.toString().toInt())
                    }
                }
            }
        }
    }
    
    private fun onLoginSuccessAnimationCompletion(){
        verifiedUserData?.let {
            requireActivity().launchActivity<HomeActivity> {
                if (it.referralStatus == "Valid") {
                    putExtra(
                        HomeActivity.INTENT_REFERRAL_REDEEM_MSG,
                        it.referralStatusMessage
                    )
                }
            }
            requireActivity().finish()
        }
    }
    
    private fun addChip(name: String): Chip {
        val intColor = ContextCompat.getColor(requireContext(), R.color.hashtag_chip_color)
        val textColor = ContextCompat.getColor(requireContext(), R.color.main_text_color)
        val chipColor = createStateColor(intColor)
        val chip = layoutInflater.inflate(R.layout.interest_chip_layout, binding.interestChipGroup, false) as Chip
        chip.text = name
        chip.id = View.generateViewId()
        chip.chipBackgroundColor = chipColor
        chip.rippleColor = chipColor
        chip.setTextColor(createStateColor(textColor, textColor))
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