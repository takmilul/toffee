package com.banglalink.toffee.ui.upload

import android.app.Dialog
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.banglalink.toffee.R
import com.banglalink.toffee.data.network.retrofit.CacheManager
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.databinding.UploadBottomSheetBinding
import com.banglalink.toffee.extension.dp
import com.banglalink.toffee.ui.profile.ViewProfileViewModel
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BottomSheetUploadFragment : BottomSheetDialogFragment() {

    @Inject lateinit var mPref: SessionPreference
    @Inject lateinit var cacheManager: CacheManager
    private var channelName: String = ""
    private var channelLogoUrl: String = ""
    private var profileImageBase64: String? = null
    private var _binding: UploadBottomSheetBinding ? = null
    private val binding get() = _binding!!
    private lateinit var progressDialog: VelBoxProgressDialog
    private val viewModel by viewModels<ViewProfileViewModel>()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    private lateinit var navController: NavController

    companion object {
        const val TAG = "BottomSheetDialog"
    }
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
//        _binding = UploadBottomSheetBinding.inflate(layoutInflater)
//        return binding.root
//    }
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//
////        progressDialog = VelBoxProgressDialog(requireContext())
////        binding.channelLogoIv.isClickable = true
////        observeEditChannel()
////
////        channelName = mPref.channelName
////        channelLogoUrl = mPref.channelLogo
////        if (channelName.isNotBlank()) {
////            binding.channelNameEt.setText(channelName)
////        }
////        if (channelLogoUrl.isNotBlank()) {
////            loadImage()
////        }
////
////        with(binding) {
////            editIv.safeClick({ showImagePickerDialog() })
////            saveBtn.safeClick({ saveChannelInfo() })
////            cancelButton.safeClick({ dialog?.dismiss() })
////            channelLogoIv.safeClick({ showImagePickerDialog() })
////            termsAndConditionsTv.safeClick({ showTermsAndConditionDialog() })
////            channelNameEt.addTextChangedListener(this@BottomSheetUploadFragment)
////            termsAndConditionsCheckbox.setOnCheckedChangeListener { _, _ -> saveButtonStateChange() }
////        }
//    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val dialogBinding = UploadBottomSheetBinding.inflate(layoutInflater)

        val navHostFragment = childFragmentManager.findFragmentById(R.id.bottomSheetFragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        val parent = dialogBinding.root.parent as View
        bottomSheetBehavior = BottomSheetBehavior.from(parent)
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val value = height - parent.layoutParams.height + 300
        bottomSheetBehavior.peekHeight = value.dp

        return dialog
    }

//    private fun showTermsAndConditionDialog() {
//        val args = Bundle().apply {
//            putString("myTitle", "Terms & Conditions")
//            putString("url", mPref.termsAndConditionUrl)
//        }
//        findNavController().navigate(R.id.termsAndConditionFragment, args)
//    }

//    private fun showImagePickerDialog() {
//        if (findNavController().currentDestination?.id != R.id.thumbnailSelectionMethodFragment && findNavController().currentDestination?.id == R.id.bottomSheetUploadFragment) {
//            val action = BottomSheetUploadFragmentDirections.actionBottomSheetUploadFragmentToThumbnailSelectionMethodFragment(
//                "Update Your Channel Logo",
//                true
//            )
//            findNavController().navigate(action)
//        }
//    }

//    private fun saveChannelInfo() {
//        binding.textViewFillUp.visibility = View.INVISIBLE
//        progressDialog.show()
//        try {
//            profileImageBase64 = imagePathToBase64(requireContext(), channelLogoUrl)
//            val ugcEditMyChannelRequest = MyChannelEditRequest(
//                mPref.customerId,
//                mPref.password,
//                mPref.customerId,
//                1,
//                channelName,
//                "",
//                "NULL",
//                "NULL",
//                "NULL",
//                profileImageBase64 ?: "NULL"
//            )
//
//            viewModel.editChannel(ugcEditMyChannelRequest)
//        }
//        catch (e: Exception) {
//            Log.e(TAG, "saveChannelInfo: ${e.message}")
//        }
//    }
//
//    private fun observeEditChannel() {
//        observe(viewModel.editChannelResult) {
//            when (it) {
//                is Resource.Success -> {
//                    if (channelLogoUrl.isNotBlank()) {
//                        mPref.channelLogo = channelLogoUrl
//                    }
//                    if (channelName.isNotBlank()) {
//                        mPref.channelName = channelName
//                    }
//                    progressDialog.dismiss()
//                    requireContext().showToast(it.data.message)
//                    cacheManager.clearCacheByUrl(GET_MY_CHANNEL_DETAILS)
//                    findNavController().popBackStack().let {
//                        findNavController().navigate(R.id.newUploadMethodFragment)
//                    }
//                }
//                is Resource.Failure -> {
//                    Log.e("data", "data" + it.error.msg)
//                    requireContext().showToast(it.error.msg)
//                    progressDialog.dismiss()
//                }
//            }
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        loadNumber()
//        observeLogoChange()
//        saveButtonStateChange()
//    }
//
//    private fun saveButtonStateChange() {
//        binding.saveBtn.isEnabled = channelLogoUrl.isNotBlank() && binding.termsAndConditionsCheckbox.isChecked && channelName.isNotBlank()
//        binding.textViewFillUp.visibility = if(binding.saveBtn.isEnabled) View.INVISIBLE else View.VISIBLE
//    }
//
//    private fun loadNumber() {
//        if (mPref.phoneNumber.length > 13) {
//            val mobile = mPref.phoneNumber.substring(3, 14)
//            binding.mobileTv.text = mobile
//        }
//        else {
//            binding.mobileTv.text = mPref.phoneNumber
//        }
//    }
//
//    private fun observeLogoChange() {
//        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String?>(ThumbnailSelectionMethodFragment.THUMB_URI)
//            ?.observe(this, {
//                it?.let {
//                    channelLogoUrl = it
//                    loadImage()
//                    saveButtonStateChange()
//                }
//            })
//    }
//
//    private fun loadImage() {
//        binding.channelLogoIv.load(channelLogoUrl) {
//            memoryCachePolicy(CachePolicy.DISABLED)
//            diskCachePolicy(CachePolicy.ENABLED)
//            crossfade(false)
//            error(R.drawable.ic_channel_logo)
//        }
//        binding.editIv.show()
//        binding.channelLogoIv.isClickable = false
//    }

    override fun getTheme(): Int = R.style.SheetDialog
}