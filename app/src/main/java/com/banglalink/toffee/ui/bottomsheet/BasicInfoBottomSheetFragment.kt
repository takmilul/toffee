package com.banglalink.toffee.ui.bottomsheet

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.apiservice.GET_MY_CHANNEL_DETAILS
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
import com.banglalink.toffee.ui.profile.ViewProfileViewModel
import com.banglalink.toffee.ui.upload.BottomSheetUploadFragment
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.UtilsKt
import com.banglalink.toffee.util.unsafeLazy
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import java.util.Calendar.*
import javax.inject.Inject

@AndroidEntryPoint
class BasicInfoBottomSheetFragment : BaseFragment() {
    private var age: Int = 0
    private var userNID= ""
    private var userDOB = ""
    private var userName = ""
    private var userEmail= ""
    private var userAddress = ""
    private var channelName: String = ""
//    private var selectedDate: String = ""
    private var newChannelLogoUrl: String = "NULL"
//    private var calendar = getInstance()
    @Inject lateinit var cacheManager: CacheManager
    private var profileForm: EditProfileForm? = null
    private var myChannelDetail: MyChannelDetail? = null
    private var _binding: BottomSheetBasicInfoBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel by activityViewModels<HomeViewModel>()
    private val viewModel by activityViewModels<ViewProfileViewModel>()
    private val profileViewModel by activityViewModels<ViewProfileViewModel>()
    private val progressDialog by unsafeLazy { VelBoxProgressDialog(requireContext()) }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileForm = profileViewModel.profileForm.value
        myChannelDetail = homeViewModel.myChannelDetailLiveData.value ?: MyChannelDetail(0)
        channelName = arguments?.getString("channelName") ?: ""
        newChannelLogoUrl = arguments?.getString("newChannelLogoUrl") ?: ""
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetBasicInfoBinding.inflate(inflater, container, false)
        binding.myChannelDetail = myChannelDetail?.apply {
            if (name.isNullOrBlank()) name = profileForm?.fullName
            if (email.isNullOrBlank()) email = profileForm?.email
            if (address.isNullOrBlank()) address = profileForm?.address
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
        userName = binding.nameEt.text.toString().trim()
        userAddress = binding.addressEt.text.toString().trim()
        userDOB = binding.dateOfBirthTv.text.toString().trim()
        userEmail=binding.emailEt.text.toString().trim()
        userNID=binding.nidEt.text.toString().trim()
        
        if (userName.isBlank()) {
            binding.errorNameTv.show()
        } else {
            binding.errorNameTv.hide()
        }

        if (userAddress.isBlank()) {
            binding.errorAddressTv.show()
        } else {
            binding.errorAddressTv.hide()
        }

        val isDobValid =validateDOB()
        val notValidEmail = userEmail.isNotBlank() and !userEmail.isValid(InputType.EMAIL)

        if (userEmail.isBlank()) {
            binding.errorEmailTv.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.pink_to_accent_color
                )
            )
            binding.errorEmailTv.text = getString(R.string.email_null_error_text)
        }

        if (notValidEmail) {
            binding.errorEmailTv.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.pink_to_accent_color
                )
            )
            binding.errorEmailTv.text = getString(R.string.email_error_text)
        } else{
            binding.errorEmailTv.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.main_text_color
                )
            )
            binding.errorEmailTv.text = getString(R.string.verification_email_sent)
        }

        var validNID =false
        if (userNID.isBlank()) {
            binding.nidWarningTv.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.pink_to_accent_color
                )
            )
            binding.nidWarningTv.text = getString(R.string.nid_null_error_text)
        } else {

            val nidLength = userNID.length
            validNID = nidLength == 10 || nidLength == 13 || nidLength == 17
            if (!validNID) {
                binding.nidWarningTv.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.pink_to_accent_color
                    )
                )
                binding.nidWarningTv.text = getString(R.string.invalid_nid_number)
            } else{
                binding.nidWarningTv.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.main_text_color
                    )
                )
            binding.nidWarningTv.text = getString(R.string.your_nid_must_match)
            }
        }
    
        if (userName.isNotBlank()
            && userAddress.isNotBlank()
            && isDobValid
            && !notValidEmail
            && userNID.isNotBlank()
            && !mPref.phoneNumber.isNullOrBlank()
            && validNID) {
            saveChannelInfo()
        }
        else {
            progressDialog.dismiss()
        }
    }
    
    private fun observeEditChannel() {
        observe(viewModel.editChannelResult) {
            when (it) {
                is Resource.Success -> {
                    mPref.isChannelDetailChecked = true

                    mPref.channelLogo = if (newChannelLogoUrl != "NULL") newChannelLogoUrl else myChannelDetail?.profileUrl ?: ""
                    mPref.channelName = channelName
                    mPref.customerName = userName
                    mPref.customerEmail = userEmail
                    mPref.customerAddress = userAddress
                    mPref.customerDOB = userDOB
                    mPref.customerNID = userNID
                    progressDialog.dismiss()
                    requireContext().showToast(it.data.message)
                    cacheManager.clearCacheByUrl(GET_MY_CHANNEL_DETAILS)
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
        val selectedDate=UtilsKt.dateToStr(UtilsKt.strToDate(binding.dateOfBirthTv.text.toString(),"dd/MM/yyyy"),"yyyy-MM-dd")
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
                myChannelDetail?.profileUrl ?: "NULL",
                newChannelLogoUrl,
                userName,
                userEmail,
                userAddress,
                selectedDate ?: "",
                userNID,
                mPref.phoneNumber,
                0,
                !myChannelDetail?.nationalIdNo.isNullOrBlank(),
                !(myChannelDetail?.channelName.isNullOrBlank() && myChannelDetail?.profileUrl.isNullOrBlank())
            )
        
            viewModel.editChannel(ugcEditMyChannelRequest)
        } catch (e: Exception) {
            Log.e(BottomSheetUploadFragment.TAG, "saveChannelInfo: ${e.message}")
        }
    }

    private fun showDatePicker() {
        val date = UtilsKt.strToDate(binding.dateOfBirthTv.text.toString(),"dd/MM/yyyy") ?: Date()
        val calendar = Calendar.getInstance()
        calendar.time=date
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { view, year, monthOfYear, dayOfMonth ->

                val calendarTwo= Calendar.getInstance()
                calendarTwo.set(year, monthOfYear, dayOfMonth)

                binding.dateOfBirthTv.text = UtilsKt.dateToStr(calendarTwo.time,"dd/MM/yyyy")
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

    private fun ageCalculate( date : Date):Int
    {

        val dob = Calendar.getInstance()
        dob.time=date
        val today = Calendar.getInstance()
//        dob[year, monthOfYear-1] = dayOfMonth
        var userAge = today[Calendar.YEAR] - dob[Calendar.YEAR]
        if (today[Calendar.MONTH] <= dob[Calendar.MONTH] && today[Calendar.DAY_OF_YEAR] < dob[Calendar.DAY_OF_YEAR]) {
            userAge--
        }
        return userAge
    }



    private fun validateDOB(): Boolean {
        var isDobValid = false
        if (binding.dateOfBirthTv.text.isBlank()) {
            binding.errorDateTv.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.pink_to_accent_color
                )
            )
            binding.errorDateTv.text = getString(R.string.date_error_text)
        } else {
            val date =UtilsKt.strToDate(binding.dateOfBirthTv.text.toString(),"dd/MM/yyyy") ?: Date()

            val userAge= ageCalculate(date)
            // selectedDate = "$year-$month-$day"

            if (userAge < 18)
            {
                binding.errorDateTv.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.pink_to_accent_color
                    )
                )
                binding.errorDateTv.text = getString(R.string.Date_of_birth_must_be_match)
//                binding.errorDateTv.text=getString(R.string.Date_of_birth_must_be_match)
//                binding.errorDateTv.show()
            }
            else {
                isDobValid = true
                binding.errorDateTv.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.main_text_color
                    )
                )
                binding.errorDateTv.text = getString(R.string.Date_of_birth_must_be_match)
                // binding.errorDateTv.hide()
            }
        }

        return isDobValid
    }
    
//    private fun showDatePicker() {
//        val datePickerDialog = DatePickerDialog(
//            requireContext(),
//            { view, year, monthOfYear, dayOfMonth ->
//                selectedDate = "$year-${monthOfYear + 1}-$dayOfMonth"
////                selectedDate = "$dayOfMonth/${monthOfYear + 1}/$year"
//                val selectedDateForTextView = "$dayOfMonth/${monthOfYear + 1}/$year"
////                val selectedDateForTextView = "$year-${monthOfYear + 1}-$dayOfMonth"
//                binding.dateOfBirthTv.text = selectedDateForTextView
//                calendar.set(year, monthOfYear, dayOfMonth)
//                val dob = getInstance()
//                val today = getInstance()
//                dob[year, monthOfYear] = dayOfMonth
//                age = today[YEAR] - dob[YEAR]
//                if (today[MONTH] <= dob[MONTH] && today[DAY_OF_YEAR] < dob[DAY_OF_YEAR]) {
//                    age--
//                }
//            },
//            calendar[YEAR],
//            calendar[MONTH],
//            calendar[DAY_OF_MONTH]
//        )
//        datePickerDialog.show()
//        datePickerDialog.apply {
//            val buttonColor = ContextCompat.getColor(requireContext(), R.color.main_text_color)
//            getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(buttonColor)
//            getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(buttonColor)
//        }
//    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showTermsAndConditionDialog() {
        val args = Bundle().apply {
            putString("myTitle", "Terms & Conditions")
            putString("url", mPref.termsAndConditionUrl)
        }
        findNavController().navigate(R.id.htmlPageViewDialog, args)
    }
}