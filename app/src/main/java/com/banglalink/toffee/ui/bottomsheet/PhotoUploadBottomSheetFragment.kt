package com.banglalink.toffee.ui.bottomsheet

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import coil.load
import coil.request.CachePolicy
import com.banglalink.toffee.R
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.databinding.BottomSheetUploadPhotoBinding
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.ui.common.ChildDialogFragment
import com.banglalink.toffee.ui.upload.ThumbnailSelectionMethodFragment
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.imagePathToBase64
import javax.inject.Inject

class PhotoUploadBottomSheetFragment : ChildDialogFragment(), TextWatcher {

    private var channelName: String = ""
    private var channelLogoUrl: String = ""
    private var isNewChannelLogo: Boolean = false
    private var newChannelLogoUrl: String = ""
    @Inject lateinit var cacheManager: CacheManager
    private lateinit var progressDialog: VelBoxProgressDialog
    private var _binding: BottomSheetUploadPhotoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
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
            channelNameEt.addTextChangedListener(this@PhotoUploadBottomSheetFragment)
        }
        binding.channelNameCountTv.text = getString(R.string.channel_name_limit, "0")
    }
    
    private fun navigateToBasicInfoBottomSheet() {
        if (findNavController().currentDestination?.id != R.id.basicInfoBottomSheetFragment && findNavController().currentDestination?.id == R.id.photoUploadBottomSheetFragment) {
            findNavController().navigate(R.id.basicInfoBottomSheetFragment, Bundle().apply { 
                arguments.apply { 
                    putString("channelName", channelName)
                    putString("newChannelLogoUrl", newChannelLogoUrl)
                }
            })
        }
    }

    private fun showImagePickerDialog() {
        if (findNavController().currentDestination?.id != R.id.thumbnailSelectionMethodFragment && findNavController().currentDestination?.id == R.id.photoUploadBottomSheetFragment) {
            val action =
                PhotoUploadBottomSheetFragmentDirections.actionPhotoUploadBottomSheetFragmentToThumbnailSelectionMethodFragment(
                    "Update Your Channel Logo",
                    true
                )
            findNavController().navigate(action)
            isNewChannelLogo = true
        }
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
            ?.observe(this, {
                if (isNewChannelLogo) {
                    it?.let {
                        loadImage(it)
                        saveButtonStateChange()
                        newChannelLogoUrl = imagePathToBase64(requireContext(), it)
                    }
                }
            })
    }

    private fun loadImage(logoUrl: String) {
        binding.channelLogoIv.load(logoUrl) {
            memoryCachePolicy(CachePolicy.DISABLED)
            diskCachePolicy(CachePolicy.ENABLED)
            crossfade(false)
            error(R.drawable.ic_channel_logo)
        }
        binding.editIv.show()
        binding.channelLogoIv.isClickable = false
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        channelName = s.toString().trim()
        binding.channelNameCountTv.text = getString(R.string.channel_name_limit, s.toString().length)
        saveButtonStateChange()
    }
}