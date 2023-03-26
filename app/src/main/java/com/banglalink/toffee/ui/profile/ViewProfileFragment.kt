package com.banglalink.toffee.ui.profile

import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.forEach
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.FirebaseParams
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.databinding.FragmentViewProfileBinding
import com.banglalink.toffee.extension.checkVerification
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.px
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.EditProfileForm
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.util.BindingUtil
import com.banglalink.toffee.util.unsafeLazy
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
@AndroidEntryPoint
class ViewProfileFragment : BaseFragment() {
    
    private lateinit var phoneNumber: String
    @Inject lateinit var bindingUtil: BindingUtil
    private var _binding: FragmentViewProfileBinding? = null
    private val binding get() = _binding!!
    private val userInterestList: MutableMap<String, Int> = mutableMapOf()
    private val viewModel by activityViewModels<ViewProfileViewModel>()
    private val progressDialog by unsafeLazy { ToffeeProgressDialog(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentViewProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = "Profile"
        observeCategory()
        if (mPref.isVerifiedUser) {
            phoneNumber = if (mPref.phoneNumber.length > 11) mPref.phoneNumber.substring(3) else mPref.phoneNumber
            binding.data = EditProfileForm().apply {
                phoneNo = phoneNumber
                photoUrl = mPref.userImageUrl ?: ""
            }
            observe(mPref.profileImageUrlLiveData) {
                bindingUtil.bindRoundImage(binding.profileIv, it)
            }
            loadProfile()
        }

        binding.editProfile.setOnClickListener {
            requireActivity().checkVerification { onClickEditProfile() }
        }

       // binding.emailTv.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_not_verified,0)
    }

    private fun loadProfile() {
        progressDialog.show()
        observe(viewModel.loadCustomerProfile()) {
            progressDialog.dismiss()
            when (it) {
                is Resource.Success -> {
                    viewModel.profileForm.value = it.data
                    binding.data = it.data.apply { phoneNo = phoneNumber }
                }
                is Resource.Failure -> {
                    requireContext().showToast(it.error.msg)
                    ToffeeAnalytics.logEvent(
                        ToffeeEvents.EXCEPTION,
                        bundleOf(
                            "api_name" to ApiNames.GET_USER_PROFILE,
                            FirebaseParams.BROWSER_SCREEN to "Profile Screen",
                            "error_code" to it.error.code,
                            "error_description" to it.error.msg
                        )
                    )
                }
            }
        }
    }

    private fun onClickEditProfile() {
        if (findNavController().currentDestination?.id != R.id.thumbnailSelectionMethodFragment && findNavController().currentDestination?.id == R.id.profileFragment) {
            val action =
                ViewProfileFragmentDirections.actionProfileFragmentToEditProfileFragment(binding.data)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun addChip(name: String, width:Int): Chip {
        val intColor = ContextCompat.getColor(requireContext(), R.color.colorSecondaryDark)
        val chipColor = createStateColor(intColor,intColor)
        val chip = layoutInflater.inflate(R.layout.interest_chip_layout, binding.interestChipGroup, false) as Chip
        chip.layoutParams.width = width
        chip.text = name
        chip.id = View.generateViewId()
        chip.chipBackgroundColor = chipColor
        chip.rippleColor =createStateColor(Color.TRANSPARENT)
        chip.chipStrokeColor = chipColor
        chip.setTextColor(Color.WHITE)
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
    
    private fun observeCategory() {
        observe(viewModel.categories){
            if(it.isNotEmpty()){
                val width = (Resources.getSystem().displayMetrics.widthPixels - 64.px) / 3
                val categoryList = it.sortedBy { category -> category.id }
                categoryList.let { list ->
                    list.forEachIndexed { _, category ->
                        val newChip = addChip(category.categoryName,width).apply {
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
            }
        }
    }
}