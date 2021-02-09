package com.banglalink.toffee.ui.upload

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import coil.load
import coil.request.CachePolicy
import com.banglalink.toffee.R
import com.banglalink.toffee.data.network.request.MyChannelEditRequest
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.mychannel.MyChannelEditDetailViewModel
import com.banglalink.toffee.ui.profile.ViewProfileViewModel
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.imagePathToBase64
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BottomSheetUploadFragment : BottomSheetDialogFragment() {
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    var cancelButton: Button? = null
    private var channel_logo_iv: ImageView? = null
    private var mobile_tv: TextView? = null
    private var text_view_fill_up: TextView? = null
    private var edit_iv: ImageView? = null
    private var channel_name_et: EditText? = null
    private var save_btn: Button? = null
    private lateinit var progressDialog: VelBoxProgressDialog
    private var terms_and_conditions_checkbox: AppCompatCheckBox? = null
    var profileImageBase64 = "NULL"
    private val viewModel by viewModels<ViewProfileViewModel>()

    @Inject
    lateinit var mpref: Preference
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val view = View.inflate(context, R.layout.upload_bottom_sheet, null)
        cancelButton = view?.findViewById(R.id.cancelButton)
        channel_logo_iv = view?.findViewById(R.id.channel_logo_iv)
        text_view_fill_up = view?.findViewById(R.id.text_view_fill_up)
        edit_iv = view?.findViewById(R.id.edit_iv)
        mobile_tv = view?.findViewById(R.id.mobile_tv)
        progressDialog = VelBoxProgressDialog(requireContext())
        channel_name_et = view?.findViewById(R.id.channel_name_et)
        terms_and_conditions_checkbox = view?.findViewById(R.id.terms_and_conditions_checkbox)
        save_btn = view?.findViewById(R.id.save_btn)
        dialog.setContentView(view)
        val parent = view.parent as View
        bottomSheetBehavior = BottomSheetBehavior.from(parent)
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val value = height - parent.layoutParams.height + 80
        bottomSheetBehavior.peekHeight = Utils.pxToDp(value)
        channel_logo_iv?.setOnClickListener {
            val action =
                BottomSheetUploadFragmentDirections.actionBottomSheetUploadFragmentToUpdateChannelLogoDialogFragment(
                    "Update Your Channel Logo",
                    true
                )
            findNavController().navigate(action)
        }
        cancelButton?.setOnClickListener {
            dismiss()
        }
        observeEditChannel()
        save_btn?.setOnClickListener {
            if (terms_and_conditions_checkbox?.isChecked!! && UPLOAD_FILE_URI != "UPLOAD_FILE_URI") {
                text_view_fill_up?.visibility = View.INVISIBLE
                progressDialog.show()
                try {
                    if (!UPLOAD_FILE_URI.isNullOrEmpty()) {
                        profileImageBase64 = imagePathToBase64(requireContext(), UPLOAD_FILE_URI!!)
                    }
                } catch (e: Exception) {
                    profileImageBase64 = "NULL"
                }
                val ugcEditMyChannelRequest = MyChannelEditRequest(
                    0,
                    "",
                    0,
                    1,
                    channel_name_et?.text.toString().trim(),
                    "",
                    "NULL",
                    "NULL",
                    "NULL",

                    profileImageBase64
                )

                viewModel.editChannel(ugcEditMyChannelRequest)
            } else {
                text_view_fill_up?.visibility = View.VISIBLE
            }
        }
        channel_name_et?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {

                save_btn?.isEnabled = s.toString() != ""
                if (s.toString() == "") {
                    text_view_fill_up?.visibility = View.VISIBLE
                } else {
                    text_view_fill_up?.visibility = View.INVISIBLE
                }

            }
        })
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    private fun observeEditChannel() {

        observe(viewModel.liveData) {
            when (it) {
                is Resource.Success -> {
                    mpref.channelLogo = profileImageBase64
                    mpref.channelName = channel_name_et?.text.toString().trim()
                    dismiss()
                    Toast.makeText(requireContext(), it.data.message, Toast.LENGTH_SHORT).show()
                    val action =
                        BottomSheetUploadFragmentDirections.actionBottomSheetUploadFragmentToNewUploadMethodFragment()
                    findNavController().navigate(action)
                    progressDialog.dismiss()
                }
                is Resource.Failure -> {
                    println(it.error)
                    Log.e("data", "data" + it.error.additionalMsg)
                    Log.e("data", "data" + it.error.code)
                    Log.e("data", "data" + it.error.msg)
                    Toast.makeText(requireContext(), it.error.msg, Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadImage()
        loadNumber()
        Log.e("onResume", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.e("onResume", "onResume")
    }

    private fun loadNumber() {
        if (mpref.phoneNumber.length > 13) {
            val mobile = mpref.phoneNumber.substring(3, 14)
            mobile_tv?.text = mobile
        } else {
            mobile_tv?.text = mpref.phoneNumber
        }
    }

    private fun loadImage() {
        UPLOAD_FILE_URI.let {
            channel_logo_iv?.load(it) {
                memoryCachePolicy(CachePolicy.DISABLED)
                diskCachePolicy(CachePolicy.ENABLED)
                crossfade(false)
                error(R.drawable.ic_channel_logo)
            }
        }
        if (UPLOAD_FILE_URI == "UPLOAD_FILE_URI") {
            edit_iv?.visibility = View.GONE
        } else {
            edit_iv?.visibility = View.VISIBLE
        }
    }

    companion object {
        const val TAG = "CustomBottomSheetDialogFragment"
        var UPLOAD_FILE_URI = "UPLOAD_FILE_URI"
        fun newInstance(): BottomSheetUploadFragment {
            return BottomSheetUploadFragment()
        }
    }

    override fun getTheme(): Int = R.style.BottomSheetMenuTheme
}