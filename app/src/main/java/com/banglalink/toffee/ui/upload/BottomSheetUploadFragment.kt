package com.banglalink.toffee.ui.upload

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import coil.request.CachePolicy
import com.banglalink.toffee.R
import com.banglalink.toffee.data.network.request.MyChannelEditRequest
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.databinding.UploadBottomSheetBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.profile.ViewProfileViewModel
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.imagePathToBase64
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.list_item_live.*
import javax.inject.Inject

@AndroidEntryPoint
class BottomSheetUploadFragment : BottomSheetDialogFragment(), TextWatcher {

    @Inject lateinit var mPref: Preference
    private var channelName: String = ""
    private var channelLogoUrl: String = ""
    private var profileImageBase64: String? = null
    private lateinit var binding: UploadBottomSheetBinding
    private lateinit var progressDialog: VelBoxProgressDialog
    private val viewModel by viewModels<ViewProfileViewModel>()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>

    companion object {
        const val TAG = "BottomSheetDialog"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = UploadBottomSheetBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressDialog = VelBoxProgressDialog(requireContext())
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
            saveBtn.safeClick({ saveChannelInfo() })
            cancelButton.safeClick({ dialog?.dismiss() })
            channelLogoIv.safeClick({ showImagePickerDialog() })
            termsAndConditionsTv.safeClick({ showTermsAndConditionDialog() })
            channelNameEt.addTextChangedListener(this@BottomSheetUploadFragment)
            termsAndConditionsCheckbox.setOnCheckedChangeListener { _, _ -> saveButtonStateChange() }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val view = View.inflate(context, R.layout.upload_bottom_sheet, null)

        dialog.setContentView(view)
        val parent = view.parent as View
        bottomSheetBehavior = BottomSheetBehavior.from(parent)
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val value = height - parent.layoutParams.height + 80
        bottomSheetBehavior.peekHeight = Utils.pxToDp(value)

        return dialog
    }

    private fun showTermsAndConditionDialog() {
        val action = BottomSheetUploadFragmentDirections.actionBottomSheetUploadFragmentToTermsConditionFragment()
        findNavController().navigate(action)
    }

    private fun showImagePickerDialog() {
        val action = BottomSheetUploadFragmentDirections.actionBottomSheetUploadFragmentToThumbnailSelectionMethodFragment(
            "Update Your Channel Logo",
            true
        )
        findNavController().navigate(action)
    }

    private fun saveChannelInfo() {
        binding.textViewFillUp.visibility = View.INVISIBLE
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
        }
        catch (e: Exception) {
            Log.e(TAG, "saveChannelInfo: ${e.message}")
        }
    }

    private fun observeEditChannel() {
        observe(viewModel.editChannelResult) {
            when (it) {
                is Resource.Success -> {
                    mPref.channelLogo = channelLogoUrl
                    mPref.channelName = channelName
                    Toast.makeText(requireContext(), it.data.message, Toast.LENGTH_SHORT).show()
                    val action = BottomSheetUploadFragmentDirections.actionBottomSheetUploadFragmentToNewUploadMethodFragment()
                    findNavController().navigate(action)
                    progressDialog.dismiss()
                }
                is Resource.Failure -> {
                    Log.e("data", "data" + it.error.msg)
                    Toast.makeText(requireContext(), it.error.msg, Toast.LENGTH_SHORT).show()
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
        binding.saveBtn.isEnabled = channelLogoUrl.isNotBlank() && binding.termsAndConditionsCheckbox.isChecked && channelName.isNotBlank()
    }

    private fun loadNumber() {
        if (mPref.phoneNumber.length > 13) {
            val mobile = mPref.phoneNumber.substring(3, 14)
            binding.mobileTv.text = mobile
        }
        else {
            binding.mobileTv.text = mPref.phoneNumber
        }
    }

    private fun observeLogoChange() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String?>(ThumbnailSelectionMethodFragment.THUMB_URI)
            ?.observe(viewLifecycleOwner, {
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

    override fun getTheme(): Int = R.style.SheetDialog

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(s: Editable?) {
        channelName = s.toString().trim()
        if (channelName.isBlank()) {
            binding.textViewFillUp.show()
        }
        else {
            binding.textViewFillUp.visibility = View.INVISIBLE
        }
        saveButtonStateChange()
    }
}