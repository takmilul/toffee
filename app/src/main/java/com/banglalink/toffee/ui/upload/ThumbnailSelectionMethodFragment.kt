package com.banglalink.toffee.ui.upload

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.extension.showToast
import com.github.florent37.runtimepermission.kotlin.PermissionException
import com.github.florent37.runtimepermission.kotlin.coroutines.experimental.askPermission
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.fragment_thumb_selection_method.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ThumbnailSelectionMethodFragment: Fragment() {
    private var imageUri: Uri? = null

    companion object {
        private const val REQUEST_IMAGE = 0x225

        fun newInstance(): ThumbnailSelectionMethodFragment {
            return ThumbnailSelectionMethodFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_thumb_selection_method, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        open_gallery_button.setOnClickListener {
            checkFileSystemPermission()
        }

        open_camera_button.setOnClickListener {
            checkCameraPermissions()
        }
    }

    private fun checkFileSystemPermission() {
        lifecycleScope.launch {
            try{
                if(askPermission(Manifest.permission.READ_EXTERNAL_STORAGE).isAccepted) {
                    startActivityForResult(
                        Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
                        REQUEST_IMAGE
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
            setFreeStyleCropEnabled(true)
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
            else if(requestCode == UCrop.REQUEST_CROP && data != null) {
                val uri = UCrop.getOutput(data)
                if(uri != null) {
                    // TODO: Process the output uri
                }
            }
        }
        else {
            ToffeeAnalytics.logBreadCrumb("Camera/image picker returned without any data")
        }
    }
}