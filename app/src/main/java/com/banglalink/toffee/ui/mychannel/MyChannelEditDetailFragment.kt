package com.banglalink.toffee.ui.mychannel

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.FirebaseParams
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.apiservice.ApiNames
import com.banglalink.toffee.apiservice.ApiRoutes
import com.banglalink.toffee.data.network.request.MyChannelEditRequest
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.databinding.FragmentMyChannelEditDetailBinding
import com.banglalink.toffee.enums.InputType
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.MyChannelDetail
import com.banglalink.toffee.model.Payment
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.profile.ViewProfileViewModel
import com.banglalink.toffee.ui.upload.ThumbnailSelectionMethodFragment
import com.banglalink.toffee.ui.widget.ToffeeSpinnerAdapter
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.util.BindingUtil
import com.banglalink.toffee.util.Utils
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MyChannelEditDetailFragment : Fragment(), OnClickListener {
    private var userNID: String = ""
    private var userDOB: String? = ""
    private var userEmail: String = ""
    private var userName: String = ""
    private var channelName: String = ""
    private var userAddress: String = ""
    private var newBannerUrl: String? = null
    private var nameWatcher: TextWatcher? = null
    private var descWatcher: TextWatcher? = null
    private var isPosterClicked: Boolean = false
    @Inject lateinit var bindingUtil: BindingUtil
    @Inject lateinit var mPref: SessionPreference
    private var newProfileImageUrl: String? = null
    @Inject lateinit var cacheManager: CacheManager
    private var myChannelDetail: MyChannelDetail? = null
    private lateinit var progressDialog: ToffeeProgressDialog
    private var _binding: FragmentMyChannelEditDetailBinding? = null
    private val binding get() = _binding!!
    private val profileViewModel by activityViewModels<ViewProfileViewModel>()
    @Inject lateinit var viewModelAssistedFactory: MyChannelEditDetailViewModel.AssistedFactory
    private val viewModel by viewModels<MyChannelEditDetailViewModel> {
        MyChannelEditDetailViewModel.provideFactory(viewModelAssistedFactory, myChannelDetail)
    }
    
    companion object {
        fun newInstance(): MyChannelEditDetailFragment {
            return MyChannelEditDetailFragment()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressDialog = ToffeeProgressDialog(requireContext())
        val args = MyChannelEditDetailFragmentArgs.fromBundle(requireArguments())
        myChannelDetail = args.myChannelDetail ?: MyChannelDetail(0)
        val profileForm = profileViewModel.profileForm.value
        myChannelDetail?.apply {
            if (name.isNullOrBlank()) name = profileForm?.fullName
            if (email.isNullOrBlank()) email = profileForm?.email
            if (address.isNullOrBlank()) address = profileForm?.address
            paymentPhoneNo = if (mPref.phoneNumber.length > 11) mPref.phoneNumber.substring(3) else mPref.phoneNumber
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyChannelEditDetailBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }
    
    override fun onDestroyView() {
        binding.categorySpinner.adapter = null
        binding.categoryPaymentSpinner.adapter = null
        binding.channelName.removeTextChangedListener(nameWatcher)
        binding.description.removeTextChangedListener(descWatcher)
        nameWatcher = null
        descWatcher = null
        super.onDestroyView()
        _binding = null
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.container.setOnClickListener(this)
        progressDialog.show()
        observeEditChannel()
        setupCategorySpinner()
        observeThumbnailChange()
        setupPaymentCategorySpinner()
        channelNameWatcher()
        channelDesWatcher()
        with(binding) {
            channelNameCountTv.text = getString(R.string.channel_name_limit, 0)
            channelDesCountTv.text = getString(R.string.channel_description_limit, 0)
            dateOfBirthTv.safeClick({ showDatePicker() })
            saveButton.safeClick(this@MyChannelEditDetailFragment)
            cancelButton.safeClick(this@MyChannelEditDetailFragment)
            bannerEditButton.safeClick(this@MyChannelEditDetailFragment)
            profileImageEditButton.safeClick(this@MyChannelEditDetailFragment)
        }
    }
    
    private fun channelNameWatcher() {
        nameWatcher = binding.channelName.doAfterTextChanged { s -> binding.channelNameCountTv.text = getString(R.string.channel_name_limit, s?.length ?: 0) }
    }
    
    private fun channelDesWatcher() {
        descWatcher = binding.description.doAfterTextChanged { s -> binding.channelDesCountTv.text = getString(R.string.channel_description_limit, s?.length ?: 0) }
    }
    
    private fun setupPaymentCategorySpinner() {
        val paymentCategoryAdapter = ToffeeSpinnerAdapter<Payment>(requireContext(), getString(R.string.select_payment_option))
        binding.categoryPaymentSpinner.adapter = paymentCategoryAdapter
        binding.categoryPaymentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position != 0 && viewModel.selectedPaymentPosition.value != position) {
                    viewModel.selectedPaymentMethod = viewModel.paymentMethodList.value?.get(position - 1)
                    viewModel.selectedPaymentPosition.value = position
                } else {
                    binding.categoryPaymentSpinner.setSelection(viewModel.selectedPaymentPosition.value ?: 0)
                }
                binding.container.requestFocus()
                Utils.hideSoftKeyboard(requireActivity())
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        observe(viewModel.paymentMethodList) { paymentMethodList ->
            progressDialog.dismiss()
            if (!paymentMethodList.isNullOrEmpty()) {
                paymentCategoryAdapter.setData(paymentMethodList)
                viewModel.selectedPaymentMethod = paymentMethodList.find { it.id == myChannelDetail?.paymentMethodId }
                viewModel.selectedPaymentPosition.value = paymentMethodList.indexOf(viewModel.selectedPaymentMethod) + 1
            }
        }
        
        observe(viewModel.selectedPaymentPosition) {
            paymentCategoryAdapter.selectedItemPosition = it
            binding.categoryPaymentSpinner.setSelection(it)
        }
    }
    
    private fun setupCategorySpinner() {
        val categoryAdapter = ToffeeSpinnerAdapter<Category>(requireContext(), getString(R.string.select_category))
        binding.categorySpinner.adapter = categoryAdapter
        binding.categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position != 0 && viewModel.selectedCategoryPosition.value != position) {
                    viewModel.selectedCategory = viewModel.categoryList.value?.get(position - 1)
                    viewModel.selectedCategoryPosition.value = position
                } else {
                    binding.categorySpinner.setSelection(viewModel.selectedCategoryPosition.value ?: 1)
                }
                binding.nameEt.requestFocus()
                binding.nameEt.setSelection(binding.nameEt.length())
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        observe(viewModel.categoryList) { categories ->
            if (!categories.isNullOrEmpty()) {
                categoryAdapter.setData(categories)
                viewModel.selectedCategory = categories.find { it.id == myChannelDetail?.categoryId } ?: categories.first()
                viewModel.selectedCategoryPosition.value = (categories.indexOf(viewModel.selectedCategory).takeIf { it > 0 } ?: 0) + 1
            }
        }
        
        observe(viewModel.selectedCategoryPosition) {
            categoryAdapter.selectedItemPosition = it
            binding.categorySpinner.setSelection(it)
        }
    }
    
    private fun observeThumbnailChange() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String?>(ThumbnailSelectionMethodFragment.THUMB_URI)
            ?.observe(viewLifecycleOwner) {
                it?.let {
                    if (isPosterClicked) {
                        newBannerUrl = it
                        loadImage()
                    } else {
                        newProfileImageUrl = it
                        loadImage()
                    }
                }
            }
    }
    
    private fun loadImage() {
        newBannerUrl?.let {
            bindingUtil.bindImageFromUrl(binding.bannerImageView, it)
        }
        newProfileImageUrl?.let {
            bindingUtil.bindRoundImage(binding.profileImageView, it)
        }
    }
    
    private fun observeEditChannel() {
        observe(viewModel.exitFragment) {
            cacheManager.clearCacheByUrl(ApiRoutes.GET_ALL_USER_CHANNEL)
            requireContext().showToast(getString(R.string.unable_to_load_data))
            findNavController().popBackStack()
        }
        
        observe(viewModel.editDetailLiveData) {
            when (it) {
                is Success -> {
                    mPref.isChannelDetailChecked = true
                    mPref.channelLogo = it.data.profileImage ?: myChannelDetail?.profileUrl.orEmpty()
                    mPref.channelName = channelName
                    mPref.customerName = userName
                    mPref.customerEmail = userEmail
                    mPref.customerAddress = userAddress
                    mPref.customerDOB = userDOB!!
                    mPref.customerNID = userNID
                    
                    binding.saveButton.isClickable = true
                    progressDialog.dismiss()
                    cacheManager.clearCacheByUrl(ApiRoutes.GET_MY_CHANNEL_DETAILS)
                    requireContext().showToast(it.data.message)
                    findNavController().navigateUp()
                }
                is Failure -> {
                    ToffeeAnalytics.logEvent(
                        ToffeeEvents.EXCEPTION,
                        bundleOf(
                            "api_name" to ApiNames.EDIT_MY_CHANNEL,
                            FirebaseParams.BROWSER_SCREEN to "Edit Details",
                            "error_code" to it.error.code,
                            "error_description" to it.error.msg)
                    )
                    binding.saveButton.isClickable = true
                    progressDialog.dismiss()
                    println(it.error)
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }
    
    override fun onClick(v: View?) {
        when (v) {
            binding.container -> Utils.hideSoftKeyboard(requireActivity())
            binding.cancelButton -> {
                Utils.hideSoftKeyboard(requireActivity())
                findNavController().navigateUp()
            }
            binding.saveButton -> {
                Utils.hideSoftKeyboard(requireActivity())
                updateChannelInfo()
            }
            binding.bannerEditButton -> {
                isPosterClicked = true
                findNavController().navigate(R.id.thumbnailSelectionMethodFragment, bundleOf(
                    ThumbnailSelectionMethodFragment.TITLE to getString(R.string.set_channel_cover_photo),
                    ThumbnailSelectionMethodFragment.IS_PROFILE_IMAGE to false,
                    ThumbnailSelectionMethodFragment.IS_CHANNEL_BANNER to true
                ))
            }
            binding.profileImageEditButton -> {
                isPosterClicked = false
                findNavController().navigate(R.id.thumbnailSelectionMethodFragment, bundleOf(
                    ThumbnailSelectionMethodFragment.TITLE to getString(R.string.set_channel_photo),
                    ThumbnailSelectionMethodFragment.IS_PROFILE_IMAGE to true
                ))
            }
        }
    }
    
    private fun updateChannelInfo() {
        binding.saveButton.isClickable = false
        progressDialog.show()
        
        userNID = binding.nidEt.text.toString().trim()
        userName = binding.nameEt.text.toString().trim()
        userEmail = binding.emailEt.text.toString().trim()
        userAddress = binding.addressEt.text.toString().trim()
        channelName = binding.channelName.text.toString().trim()
        val description = binding.description.text.toString().trim()
        
        var bannerBase64: String? = null
        try {
            if (!newBannerUrl.isNullOrEmpty()) {
                bannerBase64 = Utils.imagePathToBase64(requireContext(), newBannerUrl!!)
            }
        } catch (e: Exception) {
            bannerBase64 = null
        }
        
        var profileImageBase64: String? = null
        try {
            if (!newProfileImageUrl.isNullOrEmpty()) {
                profileImageBase64 = Utils.imagePathToBase64(requireContext(), newProfileImageUrl!!)
            }
        } catch (e: Exception) {
            profileImageBase64 = null
        }
        val isChannelLogoAvailable = !myChannelDetail?.profileUrl.isNullOrEmpty() or !profileImageBase64.isNullOrEmpty()
        
        if (channelName.isNotBlank()) {
            binding.channelName.setBackgroundResource(R.drawable.single_line_input_text_bg)
            binding.errorChannelNameTv.hide()
        } else {
            binding.channelName.setBackgroundResource(R.drawable.error_single_line_input_text_bg)
            binding.errorChannelNameTv.show()
        }
        if (isChannelLogoAvailable) {
            binding.errorThumTv.hide()
        } else {
            binding.saveButton.isClickable = true
            binding.errorThumTv.show()
        }
        
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
            binding.emailEt.validateInput(
                binding.errorEmailTv,
                R.string.email_null_error_text,
                R.color.pink_to_accent_color,
                R.drawable.error_single_line_input_text_bg
            )
        } else {
            if (notValidEmail) {
                binding.emailEt.validateInput(
                    binding.errorEmailTv,
                    R.string.email_error_text,
                    R.color.pink_to_accent_color,
                    R.drawable.error_single_line_input_text_bg
                )
            } else {
                binding.emailEt.validateInput(
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
                binding.nidErrorTv,
                R.string.nid_null_error_text,
                R.color.pink_to_accent_color,
                R.drawable.error_single_line_input_text_bg
            )
        } else {
            val nidLength = userNID.length
            validNID = nidLength == 10 || nidLength == 13 || nidLength == 17
            if (!validNID) {
                binding.nidEt.validateInput(
                    binding.nidErrorTv,
                    R.string.invalid_nid_number,
                    R.color.pink_to_accent_color,
                    R.drawable.error_single_line_input_text_bg
                )
            } else {
                binding.nidEt.validateInput(
                    binding.nidErrorTv,
                    R.string.your_nid_must_match,
                    R.color.main_text_color,
                    R.drawable.single_line_input_text_bg
                )
            }
        }
        
        if (channelName.isNotBlank() and isChannelLogoAvailable && userName.isNotBlank() && !notValidEmail && userAddress.isNotBlank() && isDobValid && userNID.isNotBlank() && validNID) {
            
            val ugcEditMyChannelRequest = MyChannelEditRequest(
                mPref.customerId,
                mPref.password,
                mPref.customerId,
                viewModel.selectedCategory?.id ?: 1,
                channelName,
                description,
                myChannelDetail?.bannerUrl ?: "NULL",
                bannerBase64 ?: "NULL",
                myChannelDetail?.profileUrl ?: "NULL",
                profileImageBase64 ?: "NULL",
                userName,
                userEmail,
                userAddress,
                userDOB!!,
                userNID,
                mPref.phoneNumber,
                viewModel.selectedPaymentMethod?.id?.toInt() ?: 0,
                !myChannelDetail?.nationalIdNo.isNullOrBlank(),
                !(myChannelDetail?.channelName.isNullOrBlank() && myChannelDetail?.profileUrl.isNullOrBlank())
            )
            viewModel.editChannel(ugcEditMyChannelRequest)
        } else {
            progressDialog.dismiss()
            binding.saveButton.isClickable = true
        }
    }
    
    private fun validateDOB(): Boolean {
        var isDobValid = false
        userDOB = Utils.dateToStr(Utils.strToDate(binding.dateOfBirthTv.text.toString(), "dd/MM/yyyy"), "yyyy-MM-dd")
        if (binding.dateOfBirthTv.text.isBlank() || userDOB.isNullOrBlank()) {
            binding.emailEt.validateInput(
                binding.errorDateTv,
                R.string.date_error_text,
                R.color.pink_to_accent_color,
                R.drawable.error_single_line_input_text_bg
            )
        } else {
            val date = Utils.strToDate(binding.dateOfBirthTv.text.toString(), "dd/MM/yyyy") ?: Date()
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
    
    private fun showDatePicker() {
        binding.emailEt.requestFocus()
        binding.emailEt.setSelection(binding.emailEt.length())
        val date = Utils.strToDate(binding.dateOfBirthTv.text.toString(), "dd/MM/yyyy") ?: Date()
        val calendar = Calendar.getInstance()
        calendar.time = date
        val datePickerDialog = DatePickerDialog(requireContext(),
            { _, year, monthOfYear, dayOfMonth ->
                val calendarTwo = Calendar.getInstance()
                calendarTwo.set(year, monthOfYear, dayOfMonth)
                binding.dateOfBirthTv.text = Utils.dateToStr(calendarTwo.time, "dd/MM/yyyy")
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
}