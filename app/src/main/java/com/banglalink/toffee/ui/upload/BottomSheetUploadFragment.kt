package com.banglalink.toffee.ui.upload

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import coil.load
import coil.request.CachePolicy
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentEditUploadInfoBinding
import com.banglalink.toffee.ui.mychannel.MyChannelEditDetailFragmentDirections
import com.banglalink.toffee.util.Utils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.upload_bottom_sheet.*
import kotlinx.android.synthetic.main.upload_bottom_sheet.view.*
import kotlinx.android.synthetic.main.upload_method_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class BottomSheetUploadFragment : BottomSheetDialogFragment() {
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    var cancelButton:Button?=null
    private var newProfileImageUrl: String? = null
    private var channel_logo_iv: ImageView? = null
    private  var uploadFileUri: String = ""
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val view = View.inflate(context, R.layout.upload_bottom_sheet, null)
        cancelButton=view?.findViewById(R.id.cancelButton)
        channel_logo_iv=view?.findViewById(R.id.channel_logo_iv)
        dialog.setContentView(view)
        val parent = view.parent as View
        bottomSheetBehavior = BottomSheetBehavior.from(parent)
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val value = height - parent.layoutParams.height + 80
        bottomSheetBehavior.peekHeight = Utils.pxToDp(value)
        cancelButton?.setOnClickListener {
            //openEditUpload("")
            Log.e("SDFf,","SDFs")
            val action = BottomSheetUploadFragmentDirections.actionBottomSheetUploadFragmentToThumbnailSelectionMethodFragment("Set Your Channel Logo",false)
            findNavController().navigate(action)
        }
        try {
            uploadFileUri = requireArguments().getString(EditUploadInfoFragment.UPLOAD_FILE_URI, "")
        } catch (e: Exception) {
        }
        loadImage()
        return dialog
    }
    private fun loadImage(){
        Log.e("SDFf,","SDFs"+UPLOAD_FILE_URI)
        uploadFileUri?.let {
            channel_logo_iv?.load(it) {
                memoryCachePolicy(CachePolicy.DISABLED)
                diskCachePolicy(CachePolicy.ENABLED)
                crossfade(false)
            }
        }
    }


    companion object {
        const val TAG = "CustomBottomSheetDialogFragment"
        const val UPLOAD_FILE_URI = "UPLOAD_FILE_URI"
        fun newInstance(): BottomSheetUploadFragment {
            return BottomSheetUploadFragment()
        }
    }

    override fun getTheme(): Int = R.style.BottomSheetMenuTheme
    private fun openEditUpload(uri: String) {
        activity?.findNavController(R.id.home_nav_host)?.navigate(
            R.id.action_bottomSheetUploadFragment_to_uploadMethodFragment,
            Bundle().apply {
                putString(UploadMethodFragment.UPLOAD_FILE_URI, uri)
            })
    }
}