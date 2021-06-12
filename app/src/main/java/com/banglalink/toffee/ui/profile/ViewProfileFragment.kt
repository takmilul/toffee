package com.banglalink.toffee.ui.profile

import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentViewProfileBinding
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.EditProfileForm
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.unsafeLazy
import com.google.android.material.chip.Chip

class ViewProfileFragment : BaseFragment() {

    private var _binding: FragmentViewProfileBinding? = null
    private val binding get() = _binding!!
    private val userInterestList: MutableMap<String, Int> = mutableMapOf()
    private val viewModel by activityViewModels<ViewProfileViewModel>()
    private val progressDialog by unsafeLazy { VelBoxProgressDialog(requireContext()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentViewProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeCategory()
        if (mPref.isVerifiedUser) {
            val phoneNumber = if (mPref.phoneNumber.length == 11) mPref.phoneNumber else mPref.phoneNumber.substring(3)
            binding.data = EditProfileForm().apply {
                phoneNo = phoneNumber
                photoUrl = mPref.userImageUrl ?: ""
            }
            observe(mPref.profileImageUrlLiveData) {
                binding.profileIv.loadProfileImage(it)
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
                    binding.data = it.data.apply { phoneNo = if (phoneNo.length == 11) phoneNo else phoneNo.substring(3) }
                }
                is Resource.Failure -> {
                    requireContext().showToast(it.error.msg)
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
        val selectedTextColor = ContextCompat.getColor(requireContext(), R.color.main_text_color)
        val unSelectedTextColor = ContextCompat.getColor(requireContext(), R.color.cardTitleColor)
        val chipColor = createStateColor(intColor,intColor)
        val strokeColor = createStateColor( unSelectedTextColor,unSelectedTextColor)
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
      //  progressDialog.show()
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
               // progressDialog.hide()
            }
        }
    }
}