package com.banglalink.toffee.ui.bottomsheet

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import coil.request.CachePolicy
import com.banglalink.toffee.R
import com.banglalink.toffee.apiservice.GET_MY_CHANNEL_DETAILS
import com.banglalink.toffee.data.network.request.MyChannelEditRequest
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.databinding.BottomSheetUploadPhotoBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.profile.ViewProfileViewModel
import com.banglalink.toffee.ui.upload.BottomSheetUploadFragment
import com.banglalink.toffee.ui.upload.BottomSheetUploadFragmentDirections
import com.banglalink.toffee.ui.upload.ThumbnailSelectionMethodFragment
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.imagePathToBase64
import com.google.android.material.bottomsheet.BottomSheetBehavior
import javax.inject.Inject

class PhotoUploadBottomSheetFragment : BaseFragment(), TextWatcher {

    @Inject
    lateinit var cacheManager: CacheManager
    private var channelName: String = ""
    private var channelLogoUrl: String = ""
    private var profileImageBase64: String? = null
    private var _binding: BottomSheetUploadPhotoBinding? = null
    private val binding get() = _binding!!
    private lateinit var progressDialog: VelBoxProgressDialog
    private val viewModel by viewModels<ViewProfileViewModel>()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetUploadPhotoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressDialog = VelBoxProgressDialog(requireContext())
        binding.channelLogoIv.isClickable = true
        observeEditChannel()

        channelName = mPref.channelName
        channelLogoUrl = mPref.channelLogo
        if (channelName.isNotBlank()) {
            binding.channelNameEt.setText(channelName)
        }
        if (channelLogoUrl.isNotBlank()) {
            loadImage()
        }

        with(binding) {
            editIv.safeClick({ showImagePickerDialog() })
            //  saveBtn.safeClick({ saveChannelInfo() })
            //   cancelButton.safeClick({ dialog?.dismiss() })
            nextBtn.safeClick({navigateToBasicInfoBottomSheet()})
            channelLogoIv.safeClick({ showImagePickerDialog() })
//            termsAndConditionsTv.safeClick({ showTermsAndConditionDialog() })
            channelNameEt.addTextChangedListener(this@PhotoUploadBottomSheetFragment)
            //  termsAndConditionsCheckbox.setOnCheckedChangeListener { _, _ -> saveButtonStateChange() }
        }
    }


    private fun navigateToBasicInfoBottomSheet() {
        if (findNavController().currentDestination?.id != R.id.basicInfoBottomSheetFragment && findNavController().currentDestination?.id == R.id.photoUploadBottomSheetFragment) {
            val action =
                PhotoUploadBottomSheetFragmentDirections.actionPhotoUploadBottomSheetFragmentToBasicInfoBottomSheetFragment(
                    channelLogoUrl,
                    channelName
                )
            findNavController().navigate(action)
        }

    }

    private fun showTermsAndConditionDialog() {
        val args = Bundle().apply {
            putString("myTitle", "Terms & Conditions")
            putString("url", mPref.termsAndConditionUrl)
        }
        findNavController().navigate(R.id.termsAndConditionFragment, args)
    }

    private fun showImagePickerDialog() {
        if (findNavController().currentDestination?.id != R.id.thumbnailSelectionMethodFragment && findNavController().currentDestination?.id == R.id.photoUploadBottomSheetFragment) {
            val action =
                PhotoUploadBottomSheetFragmentDirections.actionPhotoUploadBottomSheetFragmentToThumbnailSelectionMethodFragment(
                    "Update Your Channel Logo",
                    true
                )
            findNavController().navigate(action)
        }
    }

    private fun saveChannelInfo() {
        //   binding.textViewFillUp.visibility = View.INVISIBLE
        progressDialog.show()
        try {
            profileImageBase64 = imagePathToBase64(requireContext(), channelLogoUrl)
            val ugcEditMyChannelRequest = MyChannelEditRequest(
                mPref.customerId,
                mPref.password,
                mPref.customerId,
                1,
                channelName,
                "",
                "NULL",
                "NULL",
                "NULL",
                profileImageBase64 ?: "NULL"
            )

            viewModel.editChannel(ugcEditMyChannelRequest)
        } catch (e: Exception) {
            Log.e(BottomSheetUploadFragment.TAG, "saveChannelInfo: ${e.message}")
        }
    }

    private fun observeEditChannel() {
        observe(viewModel.editChannelResult) {
            when (it) {
                is Resource.Success -> {
                    if (channelLogoUrl.isNotBlank()) {
                        mPref.channelLogo = channelLogoUrl
                    }
                    if (channelName.isNotBlank()) {
                        mPref.channelName = channelName
                    }
                    progressDialog.dismiss()
                    requireContext().showToast(it.data.message)
                    cacheManager.clearCacheByUrl(GET_MY_CHANNEL_DETAILS)
                    findNavController().popBackStack().let {
                        findNavController().navigate(R.id.newUploadMethodFragment)
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

    override fun onResume() {
        super.onResume()
        loadNumber()
        observeLogoChange()
        saveButtonStateChange()
    }

    private fun saveButtonStateChange() {
        binding.nextBtn.isEnabled = channelLogoUrl.isNotBlank() && channelName.isNotBlank()
//        binding.textViewFillUp.visibility = if(binding.saveBtn.isEnabled) View.INVISIBLE else View.VISIBLE
    }

    private fun loadNumber() {
        if (mPref.phoneNumber.length > 13) {
            val mobile = mPref.phoneNumber.substring(3, 14)
            binding.mobileTv.text = mobile
        } else {
            binding.mobileTv.text = mPref.phoneNumber
        }
    }

    private fun observeLogoChange() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String?>(
            ThumbnailSelectionMethodFragment.THUMB_URI
        )
            ?.observe(this, {
                it?.let {
                    channelLogoUrl = it
                    loadImage()
                    saveButtonStateChange()
                }
            })
    }

    private fun loadImage() {
        binding.channelLogoIv.load(channelLogoUrl) {
            memoryCachePolicy(CachePolicy.DISABLED)
            diskCachePolicy(CachePolicy.ENABLED)
            crossfade(false)
            error(R.drawable.ic_channel_logo)
        }
        binding.editIv.show()
        binding.channelLogoIv.isClickable = false
    }

//    override fun getTheme(): Int = R.style.SheetDialog

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        channelName = s.toString().trim()
        saveButtonStateChange()
    }

}