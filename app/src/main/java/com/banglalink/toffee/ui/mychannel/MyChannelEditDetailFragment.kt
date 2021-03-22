package com.banglalink.toffee.ui.mychannel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.apiservice.GET_MY_CHANNEL_DETAILS
import com.banglalink.toffee.data.network.request.MyChannelEditRequest
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.databinding.FragmentMyChannelEditDetailBinding
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.model.Category
import com.banglalink.toffee.model.MyChannelDetail
import com.banglalink.toffee.model.Resource.Failure
import com.banglalink.toffee.model.Resource.Success
import com.banglalink.toffee.ui.upload.ThumbnailSelectionMethodFragment
import com.banglalink.toffee.ui.widget.ToffeeSpinnerAdapter
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.UtilsKt
import com.banglalink.toffee.util.bindImageFromUrl
import com.banglalink.toffee.util.bindRoundImage
import com.banglalink.toffee.util.imagePathToBase64
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyChannelEditDetailFragment : Fragment(), OnClickListener {
    @Inject lateinit var mPref: SessionPreference
    @Inject lateinit var cacheManager: CacheManager
    private var isPosterClicked = false
    private var myChannelDetail: MyChannelDetail? = null
    private var newBannerUrl: String? = null
    private var newProfileImageUrl: String? = null
    private lateinit var progressDialog: VelBoxProgressDialog
    private var _binding: FragmentMyChannelEditDetailBinding ? = null
    private val binding get() = _binding!!

    @Inject lateinit var viewModelAssistedFactory: MyChannelEditDetailViewModel.AssistedFactory
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
        myChannelDetail = args.myChannelDetail
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentMyChannelEditDetailBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.container.setOnClickListener(this)
        progressDialog.show()
        observeEditChannel()
        observeThumbnailChange()
        setupCategorySpinner()
        binding.bannerEditButton.safeClick(this)
        binding.profileImageEditButton.safeClick(this)
        binding.cancelButton.safeClick(this)
        binding.saveButton.safeClick(this)
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

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        observe(viewModel.categoryList) { categories ->
            categoryAdapter.setData(categories)
            progressDialog.dismiss()
            viewModel.selectedCategory = categories?.find { it.id == myChannelDetail?.categoryId } ?: categories?.first()
            viewModel.selectedCategoryPosition.value = (categories.indexOf(categories.find { it.id == myChannelDetail?.categoryId }).takeIf { it > 0 } ?: 0) + 1
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
            bindImageFromUrl(binding.bannerImageView, it)
        }
        newProfileImageUrl?.let {
            bindRoundImage(binding.profileImageView, it)
        }
    }
    
    private fun observeEditChannel() {
        observe(viewModel.exitFragment) {
            Toast.makeText(requireContext(), "Unable to load data!", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }
        observe(viewModel.editDetailLiveData) {
            when (it) {
                is Success -> {
                    binding.saveButton.isClickable = true
                    progressDialog.dismiss()
                    cacheManager.clearCacheByUrl(GET_MY_CHANNEL_DETAILS)
                    findNavController().navigateUp()
                    Toast.makeText(requireContext(), it.data.message, Toast.LENGTH_SHORT).show()
                }
                is Failure -> {
                    binding.saveButton.isClickable = true
                    progressDialog.dismiss()
                    println(it.error)
                    Toast.makeText(requireContext(), it.error.msg, Toast.LENGTH_SHORT).show()
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
        
        if (channelName.isNotBlank()) {

            binding.saveButton.isClickable = true
            progressDialog.dismiss()
            binding.channelName.setBackgroundResource(R.drawable.single_line_input_text_bg)
            binding.errorChannelNameTv.hide()
        }
        else {
            binding.saveButton.isClickable = true
            progressDialog.dismiss()
            binding.channelName.setBackgroundResource(R.drawable.error_single_line_input_text_bg)
            binding.errorChannelNameTv.show()
            //   binding.errorDescriptionTv.hide()
        }
        if (isChannelLogoAvailable) {
            binding.saveButton.isClickable = true
            progressDialog.dismiss()
            binding.errorThumTv.hide()
        }
        else {
            binding.saveButton.isClickable = true
            progressDialog.dismiss()
            binding.errorThumTv.show()
        }

        if(channelName.isNotBlank() and isChannelLogoAvailable){
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
                profileImageBase64 ?: "NULL"
            )

            viewModel.editChannel(ugcEditMyChannelRequest)
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
}