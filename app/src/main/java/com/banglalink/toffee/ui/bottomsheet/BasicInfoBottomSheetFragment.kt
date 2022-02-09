package com.banglalink.toffee.ui.bottomsheet

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.apiservice.ApiRoutes
import com.banglalink.toffee.data.network.request.MyChannelEditRequest
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.databinding.BottomSheetBasicInfoBinding
import com.banglalink.toffee.enums.InputType
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.EditProfileForm
import com.banglalink.toffee.model.MyChannelDetail
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.mychannel.MyChannelHomeViewModel
import com.banglalink.toffee.ui.profile.ViewProfileViewModel
import com.banglalink.toffee.ui.upload.BottomSheetUploadFragment
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.UtilsKt
import com.banglalink.toffee.util.unsafeLazy
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class BasicInfoBottomSheetFragment : BaseFragment() {
    private var userNID: String = ""
    private var userDOB: String? = ""
    private var userName: String = ""
    private var userEmail: String = ""
    private var userAddress: String = ""
    private var channelName: String = ""
    private var newChannelLogoUrl: String? = "NULL"
    private var oldChannelLogoUrl: String? = "NULL"
    @Inject lateinit var cacheManager: CacheManager
    private var profileForm: EditProfileForm? = null
    private var myChannelDetail: MyChannelDetail? = null
    private var _binding: BottomSheetBasicInfoBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel by activityViewModels<HomeViewModel>()
    private val profileViewModel by activityViewModels<ViewProfileViewModel>()
    private val progressDialog by unsafeLazy { VelBoxProgressDialog(requireContext()) }
    private val myChannelHomeViewModel by activityViewModels<MyChannelHomeViewModel>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileForm = profileViewModel.profileForm.value
        myChannelDetail = homeViewModel.myChannelDetailLiveData.value ?: MyChannelDetail(0)
        channelName = arguments?.getString("channelName").orEmpty()
        newChannelLogoUrl = arguments?.getString("newChannelLogoUrl").orEmpty()
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetBasicInfoBinding.inflate(inflater, container, false)
        binding.myChannelDetail = myChannelDetail?.apply {
            if (name.isNullOrBlank()) name = profileForm?.fullName
            if (email.isNullOrBlank()) email = profileForm?.email
            if (address.isNullOrBlank()) address = profileForm?.address
            oldChannelLogoUrl = if (profileUrl.isNullOrBlank() || profileUrl == "NULL") mPref.channelLogo else profileUrl!!
        }
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeEditChannel()
        with(binding) {
            saveBtn.safeClick({ handleSubmitButton() })
            dateOfBirthTv.safeClick({ showDatePicker() })
            termsAndConditionsTv.safeClick({ showTermsAndConditionDialog() })
            termsAndConditionsCheckbox.setOnClickListener {
                saveBtn.isEnabled = termsAndConditionsCheckbox.isChecked
            }
        }
    }
    
    private fun handleSubmitButton() {
        progressDialog.show()
        userNID = binding.nidEt.text.toString().trim()
        userName = binding.nameEt.text.toString().trim()
        userEmail = binding.emailEt.text.toString().trim()
        userAddress = binding.addressEt.text.toString().trim()
        
        if (userName.isBlank()) {
            binding.errorNameTv.show()
            binding.nameEt.setBackgroundResource(R.drawable.error_single_line_input_text_bg)
        } else {
            binding.errorNameTv.hide()
            binding.nameEt.setBackgroundResource(R.drawable.single_line_input_text_bg)
        }
        
        if (userAddress.isBlank()) {
            binding.errorAddressTv.show()
            binding.addressEt.setBackgroundResource(R.drawable.error_single_line_input_text_bg)
        } else {
            binding.errorAddressTv.hide()
            binding.addressEt.setBackgroundResource(R.drawable.single_line_input_text_bg)
        }
        
        val isDobValid = validateDOB()
        val notValidEmail = userEmail.isNotBlank() and !userEmail.isValid(InputType.EMAIL)
        
        if (userEmail.isBlank()) {
            binding.nidEt.validateInput(
                binding.errorEmailTv,
                R.string.email_null_error_text,
                R.color.pink_to_accent_color,
                R.drawable.error_single_line_input_text_bg
            )
        } else {
            if (notValidEmail) {
                binding.nidEt.validateInput(
                    binding.errorEmailTv,
                    R.string.email_error_text,
                    R.color.pink_to_accent_color,
                    R.drawable.error_single_line_input_text_bg
                )
            } else {
                binding.nidEt.validateInput(
                    binding.errorEmailTv,
                    R.string.verification_email_sent,
                    R.color.main_text_color,
                    R.drawable.single_line_input_text_bg
                )
            }
        }
        var validNID = false
        if (userNID.isBlank()) {
            binding.nidEt.validateInput(
                binding.nidWarningTv,
                R.string.nid_null_error_text,
                R.color.pink_to_accent_color,
                R.drawable.error_single_line_input_text_bg
            )
        } else {
            val nidLength = userNID.length
            validNID = nidLength == 10 || nidLength == 13 || nidLength == 17
            if (!validNID) {
                binding.nidEt.validateInput(
                    binding.nidWarningTv,
                    R.string.invalid_nid_number,
                    R.color.pink_to_accent_color,
                    R.drawable.error_single_line_input_text_bg
                )
            } else {
                binding.nidEt.validateInput(
                    binding.nidWarningTv,
                    R.string.your_nid_must_match,
                    R.color.main_text_color,
                    R.drawable.single_line_input_text_bg
                )
            }
        }
        
        if (userName.isNotBlank()
            && userAddress.isNotBlank()
            && isDobValid
            && !notValidEmail
            && userNID.isNotBlank()
            && mPref.phoneNumber.isNotBlank()
            && validNID) {
            saveChannelInfo()

            ToffeeAnalytics.logEvent(
                ToffeeEvents.UGC_UPLOAD,
                bundleOf("tnc_status" to 1)
            )

        } else {
            progressDialog.dismiss()
        }
    }
    
    private fun observeEditChannel() {
        observe(profileViewModel.editChannelResult) {
            when (it) {
                is Resource.Success -> {
                    mPref.isChannelDetailChecked = true
                    mPref.channelLogo = it.data.profileImage ?: oldChannelLogoUrl.orEmpty()
                    mPref.channelName = channelName
                    mPref.customerName = userName
                    mPref.customerEmail = userEmail
                    mPref.customerAddress = userAddress
                    mPref.customerDOB = userDOB!!
                    mPref.customerNID = userNID
                    progressDialog.dismiss()
                    requireContext().showToast(it.data.message)
                    cacheManager.clearCacheByUrl(ApiRoutes.GET_MY_CHANNEL_DETAILS)
                    myChannelHomeViewModel.getChannelDetail(mPref.customerId)
                    parentFragment?.parentFragment?.let {
                        if (it is BottomSheetDialogFragment) {
                            it.findNavController().popBackStack().let { _ ->
                                it.findNavController().navigate(R.id.newUploadMethodFragment)
                            }
                        }
                    }
                }
                is Resource.Failure -> {
                    Log.e("data", "data" + it.error.msg)
                    requireContext().showToast(it.error.msg)
                    progressDialog.dismiss()
                }
            }
        }
    }
    
    private fun saveChannelInfo() {
        try {
            val ugcEditMyChannelRequest = MyChannelEditRequest(
                mPref.customerId,
                mPref.password,
                mPref.customerId,
                1,
                channelName,
                myChannelDetail?.description ?: "",
                myChannelDetail?.bannerUrl ?: "NULL",
                "NULL",
                oldChannelLogoUrl ?: "NULL",
                newChannelLogoUrl ?: "NULL",
                userName,
                userEmail,
                userAddress,
                userDOB!!,
                userNID,
                mPref.phoneNumber,
                0,
                !myChannelDetail?.nationalIdNo.isNullOrBlank(),
                !(myChannelDetail?.channelName.isNullOrBlank() && myChannelDetail?.profileUrl.isNullOrBlank())
            )
            
            profileViewModel.editChannel(ugcEditMyChannelRequest)
        } catch (e: Exception) {
            Log.e(BottomSheetUploadFragment.TAG, "saveChannelInfo: ${e.message}")
        }
    }
    
    private fun showDatePicker() {
        binding.emailEt.requestFocus()
        binding.emailEt.setSelection(binding.emailEt.length())
        val date = UtilsKt.strToDate(binding.dateOfBirthTv.text.toString().trim(), "dd/MM/yyyy") ?: Date()
        val calendar = Calendar.getInstance()
        calendar.time = date
        val datePickerDialog = DatePickerDialog(
            requireContext(), { _, year, monthOfYear, dayOfMonth ->
                val calendarTwo = Calendar.getInstance()
                calendarTwo.set(year, monthOfYear, dayOfMonth)
                binding.dateOfBirthTv.text = UtilsKt.dateToStr(calendarTwo.time, "dd/MM/yyyy")
                validateDOB()
            },
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        )
        datePickerDialog.show()
        datePickerDialog.apply {
            datePicker.maxDate = System.currentTimeMillis()
            val buttonColor = ContextCompat.getColor(requireContext(), R.color.main_text_color)
            getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(buttonColor)
            getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(buttonColor)
        }
    }
    
    private fun ageCalculate(date: Date): Int {
        val dob = Calendar.getInstance()
        dob.time = date
        val today = Calendar.getInstance()
        var userAge = today[Calendar.YEAR] - dob[Calendar.YEAR]
        if (today[Calendar.MONTH] <= dob[Calendar.MONTH] && today[Calendar.DAY_OF_YEAR] < dob[Calendar.DAY_OF_YEAR]) {
            userAge--
        }
        return userAge
    }
    
    private fun validateDOB(): Boolean {
        var isDobValid = false
        userDOB = UtilsKt.dateToStr(UtilsKt.strToDate(binding.dateOfBirthTv.text.toString(), "dd/MM/yyyy"), "yyyy-MM-dd")
        if (binding.dateOfBirthTv.text.isBlank() || userDOB.isNullOrBlank()) {
            binding.dateOfBirthTv.validateInput(
                binding.errorDateTv,
                R.string.date_error_text,
                R.color.pink_to_accent_color,
                R.drawable.error_single_line_input_text_bg
            )
        } else {
            val date = UtilsKt.strToDate(binding.dateOfBirthTv.text.toString().trim(), "dd/MM/yyyy") ?: Date()
            val userAge = ageCalculate(date)
            
            if (userAge < 18) {
                binding.dateOfBirthTv.validateInput(
                    binding.errorDateTv,
                    R.string.Date_of_birth_must_be_match,
                    R.color.pink_to_accent_color,
                    R.drawable.error_single_line_input_text_bg
                )
            } else {
                isDobValid = true
                binding.dateOfBirthTv.validateInput(
                    binding.errorDateTv,
                    R.string.Date_of_birth_must_be_match,
                    R.color.main_text_color,
                    R.drawable.single_line_input_text_bg
                )
            }
        }
        return isDobValid
    }
    
    private fun showTermsAndConditionDialog() {
        val args = Bundle().apply {
            putString("myTitle", "Terms & Conditions")
            putString("url", mPref.termsAndConditionUrl)
        }
        findNavController().navigate(R.id.htmlPageViewDialog, args)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}