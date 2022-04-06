package com.banglalink.toffee.ui.bottomsheet

import android.os.Bundle
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.BottomSheetUploadPhotoBinding
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.ui.common.ChildDialogFragment
import com.banglalink.toffee.ui.upload.ThumbnailSelectionMethodFragment
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.BindingUtil
import com.banglalink.toffee.util.imagePathToBase64
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PhotoUploadBottomSheetFragment : ChildDialogFragment() {

    private var channelName: String = ""
    private var channelLogoUrl: String = ""
    private var newChannelLogoUrl: String = ""
    private var isNewChannelLogo: Boolean = false
    @Inject lateinit var bindingUtil: BindingUtil
    private var channelNameTextWatcher: TextWatcher? = null
    private lateinit var progressDialog: VelBoxProgressDialog
    private var _binding: BottomSheetUploadPhotoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetUploadPhotoBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressDialog = VelBoxProgressDialog(requireContext())
        binding.channelLogoIv.isClickable = true
        channelName = mPref.channelName
        channelLogoUrl = mPref.channelLogo
        if (channelName.isNotBlank()) {
            binding.channelNameEt.setText(channelName)
        }
        if (channelLogoUrl.isNotBlank()) {
            loadImage(channelLogoUrl)
        }

        with(binding) {
            skipButton.safeClick({closeDialog()})
            editIv.safeClick({ showImagePickerDialog() })
            channelLogoIv.safeClick({ showImagePickerDialog() })
            nextBtn.safeClick({navigateToBasicInfoBottomSheet()})
            channelNameTextWatcher = channelNameEt.doAfterTextChanged {
                channelName = it.toString().trim()
                binding.channelNameCountTv.text = getString(R.string.channel_name_limit, it.toString().length)
                saveButtonStateChange()
            }
            channelNameCountTv.text = getString(R.string.channel_name_limit, 0)
        }
    }
    
    private fun navigateToBasicInfoBottomSheet() {
        findNavController().navigate(R.id.basicInfoBottomSheetFragment, bundleOf(
            "channelName" to channelName,
            "newChannelLogoUrl" to newChannelLogoUrl
        ))
    }

    private fun showImagePickerDialog() {
        findNavController().navigate(R.id.thumbnailSelectionMethodFragment, bundleOf(
            ThumbnailSelectionMethodFragment.TITLE to getString(R.string.update_channel_photo),
            ThumbnailSelectionMethodFragment.IS_PROFILE_IMAGE to true
        ))
        isNewChannelLogo = true
    }

    override fun onResume() {
        super.onResume()
        observeLogoChange()
        saveButtonStateChange()
    }

    private fun saveButtonStateChange() {
        binding.nextBtn.isEnabled = (channelLogoUrl.isNotBlank() || newChannelLogoUrl.isNotBlank()) && channelName.isNotBlank()
    }

    private fun observeLogoChange() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String?>(ThumbnailSelectionMethodFragment.THUMB_URI)
            ?.observe(this) {
                if (isNewChannelLogo) {
                    it?.let {
                        loadImage(it)
                        saveButtonStateChange()
                        newChannelLogoUrl = imagePathToBase64(requireContext(), it)
                    }
                }
            }
    }

    private fun loadImage(logoUrl: String) {
        bindingUtil.bindRoundImage(binding.channelLogoIv, logoUrl)
        binding.editIv.show()
        binding.channelLogoIv.isClickable = false
    }

    override fun onDestroyView() {
        binding.channelNameEt.removeTextChangedListener(channelNameTextWatcher)
        channelNameTextWatcher = null
        super.onDestroyView()
        _binding = null
    }
}