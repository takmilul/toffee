package com.banglalink.toffee.ui.profile

import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.core.view.forEach
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.FirebaseParams
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.apiservice.ApiRoutes.GET_MY_CHANNEL_DETAILS
import com.banglalink.toffee.apiservice.BrowsingScreens
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.databinding.FragmentEditProfileBinding
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.navigateTo
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.px
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.EditProfileForm
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.upload.ThumbnailSelectionMethodFragment
import com.banglalink.toffee.ui.widget.ToffeeFieldTextWatcher
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.util.BindingUtil
import com.banglalink.toffee.util.Log
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.unsafeLazy
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val TAG = "EditProfileActivity"
@AndroidEntryPoint
class EditProfileFragment : BaseFragment() {
    
    private var previousEmail: String = ""
    private var previousAddress: String = ""
    @Inject lateinit var bindingUtil: BindingUtil
    @Inject lateinit var cacheManager: CacheManager
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val progressDialog by unsafeLazy {
        ToffeeProgressDialog(requireContext())
    }
    private val viewModel by viewModels<EditProfileViewModel>()
    private val userInterestList: MutableMap<String, Int> = mutableMapOf()
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = "Edit Profile"
        
        observeCategory()
        with(binding) {
            profileForm = arguments?.getParcelable("data")
            previousEmail = profileForm?.email ?: ""
            previousAddress = profileForm?.address ?: ""
            container.setOnClickListener {
                Utils.hideSoftKeyboard(requireActivity())
            }
            saveButton.setOnClickListener { handleSaveButton() }
            cancelBtn.setOnClickListener { findNavController().popBackStack() }
            nameEt.onFocusChangeListener = ToffeeFieldTextWatcher(binding.nameEt, ToffeeFieldTextWatcher.FieldType.NAME_FIELD)
//            emailEt.onFocusChangeListener = ToffeeFieldTextWatcher(binding.emailEt, ToffeeFieldTextWatcher.FieldType.EMAIL_FIELD)
            addressEt.onFocusChangeListener = ToffeeFieldTextWatcher(binding.addressEt, ToffeeFieldTextWatcher.FieldType.ADDRESS_FIELD)
            editIv.setOnClickListener {
                if (findNavController().currentDestination?.id != R.id.thumbnailSelectionMethodFragment && findNavController().currentDestination?.id == R.id.editProfileFragment) {
                    val action =
                        EditProfileFragmentDirections.actionEditProfileToThumbnailSelectionMethodFragment(
                            "Set Profile Photo",
                            true
                        )
                    findNavController().navigate(action)
                }
            }
        }
        observe(mPref.profileImageUrlLiveData) {
            when (it) {
                is String -> bindingUtil.bindRoundImage(binding.profileIv, it)
                is Int -> bindingUtil.loadImageFromResource(binding.profileIv, it)
            }
        }
        
        if (mPref.isVerifiedUser) {
            binding.accountDelete.visibility = View.VISIBLE
        }
        
        binding.accountDelete.setOnClickListener {
            findNavController().navigateTo(R.id.bottomSheetDeleteFragment)
        }
        
        observeThumbnailChange()
    }
    
    private fun observeThumbnailChange() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String?>(ThumbnailSelectionMethodFragment.THUMB_URI)
            ?.observe(viewLifecycleOwner) {
                it?.let { photoData ->
                    ToffeeAnalytics.logBreadCrumb("Got result from crop lib")
                    ToffeeAnalytics.logBreadCrumb("Handling crop image")
                    handleUploadImage(photoData.toUri())
                }
            }
    }
    
    private fun handleSaveButton() {
        progressDialog.show()
        binding.profileForm?.let { form ->
            if (form.fullName.isBlank()) {
                progressDialog.dismiss()
                binding.nameEt.setBackgroundResource(R.drawable.error_single_line_input_text_bg)
                binding.errorNameTv.show()
            } else {
                binding.nameEt.setBackgroundResource(R.drawable.single_line_input_text_bg)
                binding.errorNameTv.hide()
            }
            
//            val notValidEmail = form.email.isNotBlank() and !form.email.isValid(InputType.EMAIL)
//            if (notValidEmail) {
//                progressDialog.dismiss()
//                binding.emailEt.setBackgroundResource(R.drawable.error_single_line_input_text_bg)
//                binding.errorEmailTv.show()
//            } else {
//                binding.emailEt.setBackgroundResource(R.drawable.single_line_input_text_bg)
//                binding.errorEmailTv.hide()
//            }
            
            if (form.fullName.isNotBlank() /*&& !notValidEmail*/) {
                form.apply {
                    fullName = fullName.trim()
                    email = email.trim()
                    address = address.trim()
                }
                observeEditProfile(form)
                viewModel.updateProfile(form)
            }
        }
    }
    
    private fun observeEditProfile(form: EditProfileForm) {
        observe(viewModel.editProfileLiveData) {
            progressDialog.dismiss()
            when (it) {
                is Success -> {
                    mPref.customerName = form.fullName
                    if (previousEmail != form.email) ToffeeAnalytics.logEvent(ToffeeEvents.EMAIL_ADDED)
                    if (form.address.isNotBlank() && previousAddress != form.address) {
                        ToffeeAnalytics.logEvent(ToffeeEvents.ADDRESS_ADDED)
                    }
                    cacheManager.clearCacheByUrl(GET_MY_CHANNEL_DETAILS)
                    requireContext().showToast("Profile updated successfully")
                    findNavController().popBackStack()
                }
                is Failure -> {
                    ToffeeAnalytics.logEvent(
                        ToffeeEvents.EXCEPTION,
                        bundleOf(
                            "api_name" to ApiNames.UPDATE_USER_PROFILE,
                            FirebaseParams.BROWSER_SCREEN to BrowsingScreens.PROFILE,
                            "error_code" to it.error.code,
                            "error_description" to it.error.msg
                        )
                    )
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }
    
    private fun handleUploadImage(photoUri: Uri) {
        try {
            progressDialog.show()
            observe(viewModel.uploadProfileImage(photoUri)) {
                progressDialog.dismiss()
                when (it) {
                    is Success -> {
                        if (it.data == null) {
                            requireContext().showToast(getString(R.string.try_again_message))
                        } else {
                            it.data?.userPhoto?.let { url ->
                                bindingUtil.bindRoundImage(binding.profileIv, url)
                            }
                            requireContext().showToast(getString(R.string.photo_update_success))
                        }
                    }
                    is Failure -> {
                        ToffeeAnalytics.logEvent(
                            ToffeeEvents.EXCEPTION,
                            bundleOf(
                                "api_name" to ApiNames.UPDATE_USER_PROFILE_PHOTO,
                                FirebaseParams.BROWSER_SCREEN to BrowsingScreens.PROFILE,
                                "error_code" to it.error.code,
                                "error_description" to it.error.msg
                            )
                        )
                        requireContext().showToast(it.error.msg)
                    }
                }
            }
        } catch (e: Exception) {
            progressDialog.dismiss()
            ToffeeAnalytics.logException(e)
            Log.e(TAG, e.message, e)
        }
    }
    
    private fun addChip(name: String, width: Int): Chip {
        val intColor = ContextCompat.getColor(requireContext(), R.color.colorSecondaryDark)
        val chipColor = createStateColor(intColor, intColor)
        val chip = layoutInflater.inflate(R.layout.interest_chip_layout, binding.interestChipGroup, false) as Chip
        with(chip) {
            layoutParams.width = width
            text = name
            id = View.generateViewId()
            chipBackgroundColor = chipColor
            rippleColor = createStateColor(Color.TRANSPARENT)
            chipStrokeColor = chipColor
            setTextColor(Color.WHITE)
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
    
    private fun observeCategory() {
        //  progressDialog.show()
        observe(viewModel.categories) {
            if (it.isNotEmpty()) {
                val width = (Resources.getSystem().displayMetrics.widthPixels - 64.px) / 3
                val categoryList = it.sortedBy { category -> category.id }
                categoryList.let { list ->
                    list.forEachIndexed { _, category ->
                        val newChip = addChip(category.categoryName, width).apply {
                            tag = category.categoryName
                        }
                        binding.interestChipGroup.addView(newChip)
                        userInterestList[category.categoryName] = 0
                    }
                }
                binding.interestChipGroup.addView(addChip("   +   ", width).apply {
                    tag = "+"
                })
                binding.interestChipGroup.forEach {
                    val selectedChip = it as Chip
                    selectedChip.setOnCheckedChangeListener { buttonView, isChecked ->
                        userInterestList[buttonView.tag.toString()] = if (isChecked) 1 else 0
                        if (buttonView.tag.toString().equals("+")) {
                            requireContext().showToast("clicked")
                        }
                    }
                }
                // progressDialog.dismiss()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}