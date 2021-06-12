package com.banglalink.toffee.ui.mychannel

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.content.ContextCompat
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
    private var age: Int = 0
    private var isPosterClicked = false
    private var selectedDate: String = ""
    private var newBannerUrl: String? = null
    @Inject lateinit var bindingUtil: BindingUtil
    @Inject lateinit var mPref: SessionPreference
    private var newProfileImageUrl: String? = null
    @Inject lateinit var cacheManager: CacheManager
    private var myChannelDetail: MyChannelDetail? = null
    private var calendar = Calendar.getInstance()
    private lateinit var progressDialog: VelBoxProgressDialog
    private var _binding: FragmentMyChannelEditDetailBinding ? = null
    private val binding get() = _binding!!
    @Inject lateinit var viewModelAssistedFactory: MyChannelEditDetailViewModel.AssistedFactory
    private val profileViewModel by activityViewModels<ViewProfileViewModel>()
    private val viewModel by viewModels<MyChannelEditDetailViewModel> { MyChannelEditDetailViewModel.provideFactory(viewModelAssistedFactory, myChannelDetail) }
    
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
            if (paymentPhoneNo.isNullOrBlank()) paymentPhoneNo = if (mPref.phoneNumber.length == 11) mPref.phoneNumber else mPref.phoneNumber.substring(3)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMyChannelEditDetailBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }
    
    override fun onDestroyView() {
        binding.categorySpinner.adapter = null
        binding.categoryPaymentSpinner.adapter = null
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
        binding.saveButton.safeClick(this)
        binding.cancelButton.safeClick(this)
        binding.bannerEditButton.safeClick(this)
        binding.profileImageEditButton.safeClick(this)
        binding.dateOfBirthTv.safeClick ({ showDatePicker() })
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
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        observe(viewModel.paymentMethodList) { paymentMethodList ->
            progressDialog.dismiss()
            if(!paymentMethodList.isNullOrEmpty()) {
                paymentCategoryAdapter.setData(paymentMethodList)
                viewModel.selectedPaymentMethod =
                    paymentMethodList.find { it.id == myChannelDetail?.paymentMethodId }
                viewModel.selectedPaymentPosition.value =
                    (paymentMethodList.indexOf(viewModel.selectedPaymentMethod).takeIf { it > 0 } ?: 0) + 1
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
                }
                else {
                    binding.categorySpinner.setSelection(viewModel.selectedCategoryPosition.value ?: 1)
                }
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
            requireContext().showToast("Unable to load data!")
            findNavController().popBackStack()
        }
        observe(viewModel.editDetailLiveData) {
            when (it) {
                is Success -> {
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

        val channelName = binding.channelName.text.toString().trim()
        val description = binding.description.text.toString().trim()
        val isChannelLogoAvailable= !myChannelDetail?.profileUrl.isNullOrEmpty() or !profileImageBase64.isNullOrEmpty()

        val userName = binding.nameEt.text.toString().trim()
        val userAddress = binding.addressEt.text.toString().trim()
        val userDOB = binding.dateOfBirthTv.text.toString().trim()
        val userEmail=binding.emailEt.text.toString().trim()
        val userNID=binding.nidEt.text.toString().trim()
        var paymentPhoneNumber=binding.mobileTv.text.toString().trim()
        
        if (channelName.isNotBlank()) {
            binding.channelName.setBackgroundResource(R.drawable.single_line_input_text_bg)
            binding.errorChannelNameTv.hide()
        }
        else {
            binding.channelName.setBackgroundResource(R.drawable.error_single_line_input_text_bg)
            binding.errorChannelNameTv.show()
        }
        if (isChannelLogoAvailable) {
            binding.errorThumTv.hide()
        }
        else {
            binding.saveButton.isClickable = true
            binding.errorThumTv.show()
        }
        
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
    
        var isDobValid = false
        if (userDOB.isBlank()) {
            binding.errorDateTv.show()
        } else {
            if (age < 18)
            {
                binding.errorDateTv.text=getString(R.string.Date_of_birth_must_be_match)
                binding.errorDateTv.show()
            }
            else {
                isDobValid = true
                binding.errorDateTv.hide()
            }
        }
        
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

        if (userNID.isBlank()) {
            binding.nidErrorTv.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.pink_to_accent_color
                )
            )
            binding.nidErrorTv.text = getString(R.string.nid_null_error_text)
        } else{
            binding.nidErrorTv.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.main_text_color
                )
            )
            binding.nidErrorTv.text = getString(R.string.your_nid_must_match)
        }
        
        if(viewModel.selectedPaymentMethod?.id?:0 > 0) {
            binding.errorPaymentOption.hide()
        } else{
            binding.errorPaymentOption.show()
        }
    
        if (paymentPhoneNumber.startsWith("0")) {
            paymentPhoneNumber = "+88$paymentPhoneNumber"
        }
    
        if (! paymentPhoneNumber.startsWith("+")) {
            paymentPhoneNumber = "+$paymentPhoneNumber"
        }
    
        if(channelName.isNotBlank() and isChannelLogoAvailable && userName.isNotBlank() && !notValidEmail && userAddress.isNotBlank() && isDobValid && 
            userNID.isNotBlank() && paymentPhoneNumber.isNotBlank() && viewModel.selectedPaymentMethod?.id?:0 > 0){
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
                userDOB,
                userNID,
                paymentPhoneNumber,
                viewModel.selectedPaymentMethod?.id?.toInt() ?: 0
            )
            viewModel.editChannel(ugcEditMyChannelRequest)
        }
        else {
            progressDialog.dismiss()
            binding.saveButton.isClickable = true
        }
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
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { view, year, monthOfYear, dayOfMonth ->
                selectedDate = "$dayOfMonth/${monthOfYear + 1}/$year"
                binding.dateOfBirthTv.text = selectedDate
                calendar.set(year, monthOfYear, dayOfMonth)
                val dob = Calendar.getInstance()
                val today = Calendar.getInstance()
                dob[year, monthOfYear] = dayOfMonth
                age = today[Calendar.YEAR] - dob[Calendar.YEAR]
                if (today[Calendar.MONTH] <= dob[Calendar.MONTH] && today[Calendar.DAY_OF_YEAR] < dob[Calendar.DAY_OF_YEAR]) {
                    age--
                }
            },
            calendar[Calendar.YEAR],
            calendar[Calendar.MONTH],
            calendar[Calendar.DAY_OF_MONTH]
        )
        datePickerDialog.show()
        datePickerDialog.apply {
            val buttonColor = ContextCompat.getColor(requireContext(), R.color.main_text_color)
            getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(buttonColor)
            getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(buttonColor)
        }
    }
}