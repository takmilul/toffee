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
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.apiservice.GET_MY_CHANNEL_DETAILS
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
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.BindingUtil
import com.banglalink.toffee.util.UtilsKt
import com.banglalink.toffee.util.imagePathToBase64
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MyChannelEditDetailFragment : Fragment(), OnClickListener {
    private var isPosterClicked = false
    private var newBannerUrl: String? = null
    @Inject lateinit var bindingUtil: BindingUtil
    @Inject lateinit var mPref: SessionPreference
    private var newProfileImageUrl: String? = null
    @Inject lateinit var cacheManager: CacheManager
    private var myChannelDetail: MyChannelDetail? = null
    private lateinit var progressDialog: VelBoxProgressDialog
    private var _binding: FragmentMyChannelEditDetailBinding ? = null
    private val binding get() = _binding!!
    @Inject lateinit var viewModelAssistedFactory: MyChannelEditDetailViewModel.AssistedFactory
    private val profileViewModel by activityViewModels<ViewProfileViewModel>()
    private val viewModel by viewModels<MyChannelEditDetailViewModel> { MyChannelEditDetailViewModel.provideFactory(viewModelAssistedFactory, myChannelDetail) }

    private var nameWatcher: TextWatcher? = null
    private var descWatcher: TextWatcher? = null

    private var channelName = ""
    private var userName = ""
    private var userAddress = ""
    private var userDOB = ""
    private var userEmail=""
    private var userNID=""
    
    companion object {
        fun newInstance(): MyChannelEditDetailFragment {
            return MyChannelEditDetailFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        progressDialog = VelBoxProgressDialog(requireContext())
        val args = MyChannelEditDetailFragmentArgs.fromBundle(requireArguments())
        myChannelDetail = args.myChannelDetail ?: MyChannelDetail(0)
        val profileForm = profileViewModel.profileForm.value
        myChannelDetail?.apply { 
            if (name.isNullOrBlank()) name = profileForm?.fullName
            if (email.isNullOrBlank()) email = profileForm?.email
            if (address.isNullOrBlank()) address = profileForm?.address
            paymentPhoneNo = if (mPref.phoneNumber.length == 11) mPref.phoneNumber else mPref.phoneNumber.substring(3)
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
            dateOfBirthTv.safeClick ({ showDatePicker() })
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
        val paymentCategoryAdapter = ToffeeSpinnerAdapter<Payment>(requireContext(), "Select Payment Option")
        binding.categoryPaymentSpinner.adapter = paymentCategoryAdapter
        binding.categoryPaymentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(position != 0 && viewModel.selectedPaymentPosition.value != position) {
                    viewModel.selectedPaymentMethod = viewModel.paymentMethodList.value?.get(position - 1)
                    viewModel.selectedPaymentPosition.value = position
                }
                else {
                    binding.categoryPaymentSpinner.setSelection(viewModel.selectedPaymentPosition.value ?: 0)
                }
                binding.container.requestFocus()
                UtilsKt.hideSoftKeyboard(requireActivity())
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        observe(viewModel.paymentMethodList) { paymentMethodList ->
            progressDialog.dismiss()
            if(!paymentMethodList.isNullOrEmpty()) {
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
        val categoryAdapter = ToffeeSpinnerAdapter<Category>(requireContext(), "Select Category")
        binding.categorySpinner.adapter = categoryAdapter
        binding.categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(position != 0 && viewModel.selectedCategoryPosition.value != position) {
                    viewModel.selectedCategory = viewModel.categoryList.value?.get(position-1)
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
            if(!categories.isNullOrEmpty()) {
                categoryAdapter.setData(categories)
                viewModel.selectedCategory =
                    categories.find { it.id == myChannelDetail?.categoryId } ?: categories.first()
                viewModel.selectedCategoryPosition.value =
                    (categories.indexOf(viewModel.selectedCategory).takeIf { it > 0 } ?: 0) + 1
            }
        }

        observe(viewModel.selectedCategoryPosition) {
            categoryAdapter.selectedItemPosition = it
            binding.categorySpinner.setSelection(it)
        }
    }

    private fun observeThumbnailChange() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String?>(ThumbnailSelectionMethodFragment.THUMB_URI)
            ?.observe(viewLifecycleOwner, {
                it?.let {
                    if (isPosterClicked) {
                        newBannerUrl = it
                        loadImage()
                    }
                    else {
                        newProfileImageUrl = it
                        loadImage()
                    }
                }
            })
    }

    private fun loadImage(){
        newBannerUrl?.let {
            bindingUtil.bindImageFromUrl(binding.bannerImageView, it, 720, 405)
        }
        newProfileImageUrl?.let {
            bindingUtil.bindRoundImage(binding.profileImageView, it)
        }
    }
    
    private fun observeEditChannel() {
        observe(viewModel.exitFragment) {
            //requireContext().showToast("Unable to load data!")
            requireContext().showToast("Oops! Something went wrong.")
            findNavController().popBackStack()
        }
        
        observe(viewModel.editDetailLiveData) {
            when (it) {
                is Success -> {
                    mPref.isChannelDetailChecked = true
                    mPref.channelLogo = newProfileImageUrl ?: (myChannelDetail?.profileUrl ?: "")
                    mPref.channelName = channelName
                    mPref.customerName = userName
                    mPref.customerEmail = userEmail
                    mPref.customerAddress = userAddress
                    mPref.customerDOB = userDOB
                    mPref.customerNID = userNID

                    binding.saveButton.isClickable = true
                    progressDialog.dismiss()
                    cacheManager.clearCacheByUrl(GET_MY_CHANNEL_DETAILS)
                    findNavController().navigateUp()
                    requireContext().showToast(it.data.message)
                }
                is Failure -> {
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
            binding.container -> UtilsKt.hideSoftKeyboard(requireActivity())
            binding.cancelButton -> {
                UtilsKt.hideSoftKeyboard(requireActivity())
                findNavController().navigateUp()
            }
            binding.saveButton -> {
                UtilsKt.hideSoftKeyboard(requireActivity())
                updateChannelInfo()
            }
            binding.bannerEditButton -> {
                if(findNavController().currentDestination?.id != R.id.thumbnailSelectionMethodFragment && findNavController().currentDestination?.id ==R.id.myChannelEditDetailFragment) {
                    isPosterClicked = true
                    val action =
                        MyChannelEditDetailFragmentDirections.actionMyChannelEditFragmentToThumbnailSelectionMethodFragment(
                            "Set Channel Cover Photo",
                            false
                        )
                    findNavController().navigate(action)
                }
            }
            binding.profileImageEditButton -> {
                if (findNavController().currentDestination?.id != R.id.thumbnailSelectionMethodFragment && findNavController().currentDestination?.id == R.id.myChannelEditDetailFragment) {
                    isPosterClicked = false
                    val action =
                        MyChannelEditDetailFragmentDirections.actionMyChannelEditFragmentToThumbnailSelectionMethodFragment(
                            "Set Channel Photo",
                            true
                        )
                    findNavController().navigate(action)
                }
            }
        }
    }

    private fun updateChannelInfo() {
        binding.saveButton.isClickable = false
        progressDialog.show()
        
        var bannerBase64: String? = null
        try {
            if (!newBannerUrl.isNullOrEmpty()) {
                bannerBase64 = imagePathToBase64(requireContext(), newBannerUrl!!)
            }
        }
        catch (e: Exception) {
            bannerBase64 = null
        }
        
        var profileImageBase64: String? = null
        try {
            if (!newProfileImageUrl.isNullOrEmpty()) {
                profileImageBase64 = imagePathToBase64(requireContext(), newProfileImageUrl!!)
            }
        }
        catch (e: Exception) {
            profileImageBase64 = null
        }

        channelName = binding.channelName.text.toString().trim()
        val description = binding.description.text.toString().trim()
        val isChannelLogoAvailable= !myChannelDetail?.profileUrl.isNullOrEmpty() or !profileImageBase64.isNullOrEmpty()

        userName = binding.nameEt.text.toString().trim()
        userAddress = binding.addressEt.text.toString().trim()
        userDOB = binding.dateOfBirthTv.text.toString().trim()
        userEmail = binding.emailEt.text.toString().trim()
        userNID = binding.nidEt.text.toString().trim()
        
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
            binding.emailEt.setBackgroundResource(R.drawable.error_single_line_input_text_bg)
        }
        else {
            if (notValidEmail) {
                binding.errorEmailTv.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.pink_to_accent_color
                    )
                )
                binding.errorEmailTv.text = getString(R.string.email_error_text)
                binding.emailEt.setBackgroundResource(R.drawable.error_single_line_input_text_bg)
            } else {
                binding.errorEmailTv.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.main_text_color
                    )
                )
                binding.errorEmailTv.text = getString(R.string.verification_email_sent)
                binding.emailEt.setBackgroundResource(R.drawable.single_line_input_text_bg)
            }
        }
        var validNID = false
        if (userNID.isBlank()) {
            binding.nidErrorTv.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.pink_to_accent_color
                )
            )
            binding.nidErrorTv.text = getString(R.string.nid_null_error_text)
            binding.nidEt.setBackgroundResource(R.drawable.error_single_line_input_text_bg)
        } else{
            val nidLength = userNID.length
            validNID = nidLength == 10 || nidLength == 13 || nidLength == 17
            if (!validNID) {
                binding.nidErrorTv.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.pink_to_accent_color
                    )
                )
                binding.nidErrorTv.text = getString(R.string.invalid_nid_number)
                binding.nidEt.setBackgroundResource(R.drawable.error_single_line_input_text_bg)
            } else{
                binding.nidErrorTv.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.main_text_color
                    )
                )
                binding.nidErrorTv.text = getString(R.string.your_nid_must_match)
                binding.nidEt.setBackgroundResource(R.drawable.single_line_input_text_bg)
            }
        }
        
        if(channelName.isNotBlank() and isChannelLogoAvailable
            && userName.isNotBlank()
            && !notValidEmail
            && userAddress.isNotBlank()
            && isDobValid
            && userNID.isNotBlank()
            && validNID){

            val selectedDate=UtilsKt.dateToStr(UtilsKt.strToDate(binding.dateOfBirthTv.text.toString(),"dd/MM/yyyy"),"yyyy-MM-dd")

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
                selectedDate ?: "",
                userNID,
                mPref.phoneNumber,
                viewModel.selectedPaymentMethod?.id?.toInt() ?: 0,
                !myChannelDetail?.nationalIdNo.isNullOrBlank(),
                !(myChannelDetail?.channelName.isNullOrBlank() && myChannelDetail?.profileUrl.isNullOrBlank())
            )
            viewModel.editChannel(ugcEditMyChannelRequest)
        }
        else {
            progressDialog.dismiss()
            binding.saveButton.isClickable = true
        }
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
            binding.dateOfBirthTv.setBackgroundResource(R.drawable.error_single_line_input_text_bg)
        } else {
            val date =UtilsKt.strToDate(binding.dateOfBirthTv.text.toString(),"dd/MM/yyyy") ?: Date()
            val userAge= ageCalculate(date)

            if (userAge < 18) {
                binding.errorDateTv.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.pink_to_accent_color
                    )
                )
                binding.errorDateTv.text = getString(R.string.Date_of_birth_must_be_match)
                binding.dateOfBirthTv.setBackgroundResource(R.drawable.error_single_line_input_text_bg)
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
                binding.dateOfBirthTv.setBackgroundResource(R.drawable.single_line_input_text_bg)
            }
        }

        return isDobValid
    }

    /*private fun convertImageFileToBase64(imageFile: File): String {
        return FileInputStream(imageFile).use { inputStream ->
            ByteArrayOutputStream().use { outputStream ->
                Base64OutputStream(outputStream, Base64.NO_WRAP).use { base64FilterStream ->
                    inputStream.copyTo(base64FilterStream)
                    base64FilterStream.close()
                    outputStream.toString()
                }
            }
        }
    }*/
    
    private fun showDatePicker() {
        binding.emailEt.requestFocus()
        binding.emailEt.setSelection(binding.emailEt.length())
        val date = UtilsKt.strToDate(binding.dateOfBirthTv.text.toString(),"dd/MM/yyyy") ?: Date()
        val calendar = Calendar.getInstance()
        calendar.time = date
        val datePickerDialog = DatePickerDialog(requireContext(),
            { view, year, monthOfYear, dayOfMonth ->
                val calendarTwo=Calendar.getInstance()
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

    private fun ageCalculate( date :Date):Int {
        val dob = Calendar.getInstance()
        dob.time=date
        val today = Calendar.getInstance()
        var userAge = today[Calendar.YEAR] - dob[Calendar.YEAR]
        if (today[Calendar.MONTH] <= dob[Calendar.MONTH] && today[Calendar.DAY_OF_YEAR] < dob[Calendar.DAY_OF_YEAR]) {
            userAge--
        }
        return userAge
    }
}