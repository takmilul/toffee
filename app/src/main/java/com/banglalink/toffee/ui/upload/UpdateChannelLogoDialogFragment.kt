package com.banglalink.toffee.ui.upload

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.extension.showToast
import com.github.florent37.runtimepermission.kotlin.PermissionException
import com.github.florent37.runtimepermission.kotlin.coroutines.experimental.askPermission
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_update_channel_logo_dailog.view.*

import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class UpdateChannelLogoDialogFragment : DialogFragment() {
    private var imageUri: Uri? = null
    private var isProfile:Boolean = false
    private lateinit var title: String
    private var alertDialog: AlertDialog? = null
    companion object {
        private const val REQUEST_IMAGE = 0x225
        private const val REQUEST_IMAGE_FROM_FILE = 0x226
        const val THUMB_URI = "thumb-uri"
        const val TITLE_ARG = "thumb_arg_key"
        fun newInstance(): UpdateChannelLogoDialogFragment {
            return UpdateChannelLogoDialogFragment()
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        title = UpdateChannelLogoDialogFragmentArgs.fromBundle(requireArguments()).title
        isProfile = UpdateChannelLogoDialogFragmentArgs.fromBundle(requireArguments()).isProfile
        val dialogView = layoutInflater.inflate(R.layout.fragment_update_channel_logo_dailog, null, false)
        with(dialogView){
            open_gallery_button.setOnClickListener {
                checkFileSystemPermission()
            }
            open_camera_button.setOnClickListener {
                checkCameraPermissions()
            }
            close_iv?.setOnClickListener {
                dismiss()
            }
        }
        alertDialog = AlertDialog
            .Builder(requireContext())
            .setView(dialogView).create()
            .apply {
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        return alertDialog!!
    }

    private fun checkFileSystemPermission() {
        lifecycleScope.launch {
            try{
                if(askPermission(Manifest.permission.READ_EXTERNAL_STORAGE).isAccepted) {
                    startActivityForResult(
                            Intent(
                                    Intent.ACTION_PICK,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
                            REQUEST_IMAGE_FROM_FILE
                    )
                }
            }
            catch(e: PermissionException) {
                ToffeeAnalytics.logBreadCrumb("Storage permission denied")
                requireContext().showToast(getString(R.string.grant_storage_permission))
            }
        }
    }

    private fun checkCameraPermissions() {
        lifecycleScope.launch{
            try {
                if(askPermission(Manifest.permission.CAMERA).isAccepted){
                    openCameraIntent()
                }
            }
            catch (e: PermissionException){
                ToffeeAnalytics.logBreadCrumb("Camera permission denied")
                requireContext().showToast(getString(R.string.grant_camera_permission))
            }
        }
    }

    private fun openCameraIntent() {
        val imageIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (imageIntent.resolveActivity(requireActivity().packageManager) != null) {

            val imageFile: File?
            try {
                imageFile = createImageFile()
                ToffeeAnalytics.logBreadCrumb("Thumbnail file created")
            } catch (e: IOException) {
                e.printStackTrace()
                return
            }

            imageUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", imageFile)
            ToffeeAnalytics.logBreadCrumb("Thumbnail uri set")
            imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            startActivityForResult(imageIntent, REQUEST_IMAGE)
            ToffeeAnalytics.logBreadCrumb("Camera activity started")

        }
    }

    @Throws(IOException::class)
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "IMAGE_" + timeStamp + "_"
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    private fun startCrop(uri: Uri) {
        var uCrop = UCrop.of(
                uri,
                Uri.fromFile(createImageFile())
        )

        val options = UCrop.Options().apply {
            setHideBottomControls(true)
            if (isProfile){
                withAspectRatio(4f, 4f)
                setCircleDimmedLayer(true)
            } else {
                withAspectRatio(16f, 9f)
            }
            withMaxResultSize(1280, 720)
            setFreeStyleCropEnabled(false)
        }

        uCrop = uCrop.withOptions(options)

        uCrop.start(requireContext(), this)
        ToffeeAnalytics.logBreadCrumb("Crop started")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == REQUEST_IMAGE) {
                ToffeeAnalytics.logBreadCrumb("Got result from camera")
                imageUri?.let {
                    startCrop(it)
                }
            }
            else if(requestCode == REQUEST_IMAGE_FROM_FILE) {
                data?.data?.let {
                    startCrop(it)
                }
            }
            else if(requestCode == UCrop.REQUEST_CROP && data != null) {
                val uri = UCrop.getOutput(data)
                if(uri != null) {
                    BottomSheetUploadFragment.UPLOAD_FILE_URI =uri.toString()
                    findNavController().let {
                        it.previousBackStackEntry?.savedStateHandle?.set(
                            THUMB_URI, uri.toString())
                        it.popBackStack()
                    }

                }
            }
        }
        else {
            ToffeeAnalytics.logBreadCrumb("Camera/image picker returned without any data")
        }
    }
}