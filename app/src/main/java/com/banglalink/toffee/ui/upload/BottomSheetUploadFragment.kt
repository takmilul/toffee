package com.banglalink.toffee.ui.upload

import android.app.Dialog
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import coil.load
import coil.request.CachePolicy
import com.banglalink.toffee.R
import com.banglalink.toffee.util.Utils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BottomSheetUploadFragment : BottomSheetDialogFragment() {
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    var cancelButton:Button?=null
    private var newProfileImageUrl: String? = null
    private var channel_logo_iv: ImageView? = null
    private var edit_iv: ImageView? = null
    private  var uploadFileUri: String = ""
    private  var bool: Boolean = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val view = View.inflate(context, R.layout.upload_bottom_sheet, null)
        cancelButton=view?.findViewById(R.id.cancelButton)
        channel_logo_iv=view?.findViewById(R.id.channel_logo_iv)
        edit_iv=view?.findViewById(R.id.edit_iv)
        dialog.setContentView(view)
        val parent = view.parent as View
        bottomSheetBehavior = BottomSheetBehavior.from(parent)
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val value = height - parent.layoutParams.height + 80
        bottomSheetBehavior.peekHeight = Utils.pxToDp(value)
        channel_logo_iv?.setOnClickListener {
            //openEditUpload("")
            Log.e("SDFf,","SDFs")
            val action = BottomSheetUploadFragmentDirections.actionBottomSheetUploadFragmentToUpdateChannelLogoDialogFragment("Set Your Channel Logo",true)
            findNavController().navigate(action)
            bool=true
        }
        val args: Bundle? = arguments
        if (args != null) {
            uploadFileUri = requireArguments().getString(UPLOAD_FILE_URI, "")
            edit_iv?.visibility=View.VISIBLE
            loadImage()
        }
        return dialog
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

    override fun onResume() {
        super.onResume()
        loadImage()
    }
    private fun loadImage(){
        UPLOAD_FILE_URI.let {
            channel_logo_iv?.load(it) {
                memoryCachePolicy(CachePolicy.DISABLED)
                diskCachePolicy(CachePolicy.ENABLED)
                crossfade(false)
                error(R.drawable.ic_channel_logo)
            }
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