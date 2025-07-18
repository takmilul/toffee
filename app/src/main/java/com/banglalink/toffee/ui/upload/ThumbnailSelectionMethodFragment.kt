package com.banglalink.toffee.ui.upload

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.VideoOnly
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.analytics.ToffeeEvents
import com.banglalink.toffee.databinding.FragmentThumbSelectionMethodBinding
import com.banglalink.toffee.extension.checkIfFragmentAttached
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.util.Utils
import com.github.florent37.runtimepermission.kotlin.NoActivityException
import com.github.florent37.runtimepermission.kotlin.PermissionException
import com.github.florent37.runtimepermission.kotlin.coroutines.experimental.askPermission
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ThumbnailSelectionMethodFragment: DialogFragment() {
    
    private var imageUri: Uri? = null
    private lateinit var title: String
    private val binding get() = _binding!!
    private var isProfileImage: Boolean = false
    private var isChannelBanner: Boolean = false
    private var alertDialog: AlertDialog? = null
    private var _binding: FragmentThumbSelectionMethodBinding? = null
    
    companion object {
        const val THUMB_URI = "thumb-uri"
        const val TITLE = "title"
        const val IS_PROFILE_IMAGE = "isProfileImage"
        const val IS_CHANNEL_BANNER = "isChannelBanner"
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        title = ThumbnailSelectionMethodFragmentArgs.fromBundle(requireArguments()).title
        isProfileImage = ThumbnailSelectionMethodFragmentArgs.fromBundle(requireArguments()).isProfileImage
        isChannelBanner = ThumbnailSelectionMethodFragmentArgs.fromBundle(requireArguments()).isChannelBanner
        _binding = FragmentThumbSelectionMethodBinding.inflate(layoutInflater)
        binding.heading.text = title
        binding.openGalleryButton.setOnClickListener { checkFileSystemPermission() }
        binding.openCameraButton.setOnClickListener { checkCameraPermissions() }
        alertDialog = checkIfFragmentAttached {
            AlertDialog.Builder(requireContext())
                .setView(binding.root)
                .create().apply {
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }
        }
        return alertDialog!!
    }
    
    private fun checkFileSystemPermission() {
        lifecycleScope.launch {
            try {
                if (PickVisualMedia.isPhotoPickerAvailable(requireContext())) {
                    newGalleryResultLauncher.launch(PickVisualMediaRequest(ImageOnly))
                } else {
                    if (askPermission(if (Build.VERSION.SDK_INT < 33) Manifest.permission.READ_EXTERNAL_STORAGE else Manifest.permission.READ_MEDIA_IMAGES).isAccepted) {
                        galleryResultLauncher.launch(
                            Intent(
                                Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            )
                        )
                    }
                }
            } catch (e: PermissionException) {
                ToffeeAnalytics.logBreadCrumb("Storage permission denied")
                checkIfFragmentAttached {
                    requireContext().showToast(getString(R.string.grant_storage_permission))
                }
            } catch (e: NoActivityException) {
                ToffeeAnalytics.logBreadCrumb("Activity Not Found - filesystem(gallery)")
                checkIfFragmentAttached {
                    requireContext().showToast(getString(R.string.no_activity_msg))
                }
            } catch (e: ActivityNotFoundException) {
                ToffeeAnalytics.logBreadCrumb("Activity Not Found - filesystem(gallery)")
                checkIfFragmentAttached {
                    requireContext().showToast(getString(R.string.no_activity_msg))
                }
            } catch (e: Exception) {
                ToffeeAnalytics.logBreadCrumb(e.message ?: "")
                checkIfFragmentAttached {
                    requireContext().showToast(getString(R.string.no_activity_msg))
                }
            }
        }
    }
    
    private fun checkCameraPermissions() {
        lifecycleScope.launch {
            try {
                if (askPermission(Manifest.permission.CAMERA).isAccepted) {
                    openCameraIntent()
                }
            } catch (e: PermissionException) {
                ToffeeAnalytics.logBreadCrumb("Camera permission denied")
                checkIfFragmentAttached {
                    requireContext().showToast(getString(R.string.grant_camera_permission))
                }
            } catch (e: NoActivityException) {
                ToffeeAnalytics.logBreadCrumb("Activity Not Found - filesystem(camera)")
                checkIfFragmentAttached {
                    requireContext().showToast(getString(R.string.no_activity_msg))
                }
            } catch (e: ActivityNotFoundException) {
                ToffeeAnalytics.logBreadCrumb("Activity Not Found - filesystem(camera)")
                checkIfFragmentAttached {
                    requireContext().showToast(getString(R.string.no_activity_msg))
                }
            } catch (e: Exception) {
                ToffeeAnalytics.logBreadCrumb(e.message ?: "")
                checkIfFragmentAttached {
                    requireContext().showToast(getString(R.string.no_activity_msg))
                }
            }
        }
    }
    
    private fun openCameraIntent() {
        checkIfFragmentAttached {
            val imageIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (imageIntent.resolveActivity(requireContext().packageManager) != null) {
                val imageFile: File?
                try {
                    imageFile = createImageFile()
                    ToffeeAnalytics.logBreadCrumb("Thumbnail file created")
                } catch (e: IOException) {
                    e.printStackTrace()
                    return@checkIfFragmentAttached
                }
                
                imageUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", imageFile)
                ToffeeAnalytics.logBreadCrumb("Thumbnail uri set")
                imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                cameraResultLauncher.launch(imageIntent)
                ToffeeAnalytics.logBreadCrumb("Camera activity started")
            }
        }
    }
    
    @Throws(IOException::class)
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
        val imageFileName = "IMAGE_" + timeStamp + "_"
        val storageDir = checkIfFragmentAttached { requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES) }
        if (storageDir?.exists() == false) {
            storageDir.mkdirs()
        }
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }
    
    private fun startCrop(uri: Uri) {
        checkIfFragmentAttached {
            try {
                var uCrop = UCrop.of(
                    uri, Uri.fromFile(createImageFile())
                )
                val options = UCrop.Options().apply {
                    setHideBottomControls(true)
                    when {
                        isChannelBanner -> {
                            val width = Utils.getScreenWidth().toFloat()
                            val height = resources.getDimension(R.dimen.channel_banner_height)
                            withAspectRatio(width, height)
                        }
                        
                        isProfileImage -> {
                            withAspectRatio(4f, 4f)
                            setCircleDimmedLayer(true)
                        }
                        
                        else -> {
                            withAspectRatio(16f, 9f)
                        }
                    }
                    withMaxResultSize(1280, 720)
                    setFreeStyleCropEnabled(false)
                }
                
                uCrop = uCrop.withOptions(options)
                uCrop.start(requireContext(), this@ThumbnailSelectionMethodFragment)
                ToffeeAnalytics.logBreadCrumb("Crop started")
            } catch (e: Exception) {
                ToffeeAnalytics.logBreadCrumb("Failed to crop image")
            }
        }
    }
    
    private val newGalleryResultLauncher = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            startCrop(uri)
        } else {
            ToffeeAnalytics.logBreadCrumb("Camera/video picker returned without any data")
        }
    }
    
    private val galleryResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK && it.data != null && it.data?.data != null) {
            if (title.equals(getString(R.string.set_channel_photo))) {
                ToffeeAnalytics.logEvent(
                    ToffeeEvents.UGC_CHANNEL_IMAGE, bundleOf("image_upload_type" to "Gallery")
                )
            }
            startCrop(it.data!!.data!!)
        } else {
            ToffeeAnalytics.logBreadCrumb("Camera/video picker returned without any data")
        }
    }
    
    private val cameraResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK && imageUri != null) {
            if (title.equals(getString(R.string.set_channel_photo))) {
                ToffeeAnalytics.logEvent(
                    ToffeeEvents.UGC_CHANNEL_IMAGE, bundleOf("image_upload_type" to "Camera")
                )
            }
            ToffeeAnalytics.logBreadCrumb("Got result from camera")
            startCrop(imageUri!!)
        } else {
            ToffeeAnalytics.logBreadCrumb("Camera/video capture result not returned")
        }
    }
    
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == UCrop.REQUEST_CROP && data != null) {
                val uri = UCrop.getOutput(data)
                if (uri != null) {
                    findNavController().let {
                        it.previousBackStackEntry?.savedStateHandle?.set(THUMB_URI, uri.toString())
                        alertDialog?.dismiss()
                    }
                }
            }
        } else {
            ToffeeAnalytics.logBreadCrumb("Camera/image picker returned without any data")
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}