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
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.VideoOnly
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.R.string
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.databinding.FragmentNewUploadMethodBinding
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.widget.ToffeeAlertDialogBuilder
import com.banglalink.toffee.util.Log
import com.banglalink.toffee.util.Utils
import com.github.florent37.runtimepermission.kotlin.NoActivityException
import com.github.florent37.runtimepermission.kotlin.PermissionException
import com.github.florent37.runtimepermission.kotlin.coroutines.experimental.askPermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class NewUploadMethodFragment : DialogFragment() {
    
    private var videoUri: Uri? = null
    private var alertDialog: AlertDialog? = null
    @Inject lateinit var mpref: SessionPreference
    private var _binding: FragmentNewUploadMethodBinding ? = null
    private val binding get() = _binding!!
    @Inject lateinit var mUploadInfoRepository: UploadInfoRepository
    private val homeViewModel by activityViewModels<HomeViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentNewUploadMethodBinding.inflate(layoutInflater)
        binding.myChannelButton.setOnClickListener {
            openMyChannelFragment()
        }
        binding.closeButton.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.openCameraButton.setOnClickListener {
            checkCameraPermissions()
        }
        binding.openGalleryButton.setOnClickListener {
            checkFileSystemPermission()
        }
        alertDialog = AlertDialog
            .Builder(requireContext())
            .setView(binding.root).create()
            .apply {
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
        return alertDialog!!
    }
    
    private fun checkFileSystemPermission() {
        lifecycleScope.launch {
            try {
                if (PickVisualMedia.isPhotoPickerAvailable(requireContext())) {
                    newGalleryResultLauncher.launch(PickVisualMediaRequest(VideoOnly))
                } else {
                    if (askPermission(if (Build.VERSION.SDK_INT < 33) Manifest.permission.READ_EXTERNAL_STORAGE else Manifest.permission.READ_MEDIA_VIDEO).isAccepted) {
                        galleryResultLauncher.launch(
                            Intent(
                                Intent.ACTION_PICK,
                                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            ).setTypeAndNormalize("video/mp4")
                        )
                    }
                }
            } catch (e: PermissionException) {
                ToffeeAnalytics.logBreadCrumb("Storage permission denied")
                requireContext().showToast(getString(R.string.grant_storage_permission))
            }
            catch (e: NoActivityException){
                ToffeeAnalytics.logBreadCrumb("Activity Not Found - filesystem(gallery)")
                requireContext().showToast(getString(R.string.no_activity_msg))
            }
            catch (e: ActivityNotFoundException){
                ToffeeAnalytics.logBreadCrumb("Activity Not Found - filesystem(gallery)")
                requireContext().showToast(getString(R.string.no_activity_msg))
            }
            catch (e: Exception) {
                ToffeeAnalytics.logBreadCrumb(e.message ?: "")
                requireContext().showToast(getString(R.string.no_activity_msg))
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
                requireContext().showToast(getString(R.string.grant_camera_permission))
            }
            catch (e: NoActivityException){
                ToffeeAnalytics.logBreadCrumb("Activity Not Found - filesystem(camera)")
                requireContext().showToast(getString(R.string.no_activity_msg))
            }
            catch (e: ActivityNotFoundException){
                ToffeeAnalytics.logBreadCrumb("Activity Not Found - filesystem(camera)")
                requireContext().showToast(getString(R.string.no_activity_msg))
            }
            catch (e: Exception) {
                ToffeeAnalytics.logBreadCrumb(e.message ?: "")
                requireContext().showToast(getString(R.string.no_activity_msg))
            }
        }
    }

    private var videoFile: File? = null
    
    private fun openCameraIntent() {
        val videoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        if (videoIntent.resolveActivity(requireActivity().packageManager) != null) {
            try {
                videoFile = createVideoFile()
                ToffeeAnalytics.logBreadCrumb("Video file created")
            } catch (e: IOException) {
                e.printStackTrace()
                return
            }

            videoUri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                videoFile!!
            )
            ToffeeAnalytics.logBreadCrumb("Video uri set")
            videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
            cameraResultLauncher.launch(videoIntent)
            ToffeeAnalytics.logBreadCrumb("Camera activity started")
        }
    }

    @Throws(IOException::class)
    fun createVideoFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
        val videoFileName = "VIDEO_" + timeStamp + "_"
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        if (storageDir?.exists() == false){
            storageDir.mkdirs()
        }
        return File.createTempFile(videoFileName, ".mp4", storageDir)
    }
    
    private val newGalleryResultLauncher = registerForActivityResult(PickVisualMedia()) { uri ->
        if (uri != null) {
            checkAndOpenUpload(uri)
        } else {
            ToffeeAnalytics.logBreadCrumb("Camera/video picker returned without any data")
        }
    }
    
    private val galleryResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == Activity.RESULT_OK && it.data != null && it.data?.data != null) {
            checkAndOpenUpload(it.data!!.data!!)
        } else {
            ToffeeAnalytics.logBreadCrumb("Camera/video picker returned without any data")
        }
    }

    private val cameraResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == Activity.RESULT_OK && videoFile != null) {
            println("CaptureAbsolutePath${videoFile!!.absolutePath}")
            println("CapturePath${videoFile!!.path}")
            openEditUpload(videoFile!!.absolutePath)
        } else {
            ToffeeAnalytics.logBreadCrumb("Camera/video capture result not returned")
        }
    }

    private fun checkAndOpenUpload(videoUri: Uri) {
        lifecycleScope.launch {
            val contentType = Utils.contentTypeFromContentUri(requireContext(), videoUri)
            val fileName = Utils.fileNameFromContentUri(requireContext(), videoUri)

            Log.i("UPLOAD_T", "Type ->> $contentType, Name ->> $fileName")

            if(contentType == "video/mp4" || fileName.substringAfterLast(".", "") == "mp4") {
                openEditUpload(videoUri.toString())
            } else {
                ToffeeAlertDialogBuilder(requireContext()).apply {
                    setTitle(getString(string.upload_vod_requirement_title))
                    setText(getString(string.upload_vod_requirement_description))
                    setPositiveButtonListener("Got It!") {
                        it?.dismiss()
                    }
                }.create().show()
            }
        }
    }

    private fun openMyChannelFragment() {
        homeViewModel.myChannelNavLiveData.value = MyChannelNavParams(mpref.customerId)
    }

    private fun openEditUpload(uri: String) {
        findNavController().popBackStack().let {
            findNavController().navigate(R.id.editUploadInfoFragment, Bundle().apply { putString(EditUploadInfoFragment.UPLOAD_FILE_URI, uri) })
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    override fun onDestroy() {
        cameraResultLauncher.unregister()
        galleryResultLauncher.unregister()
        super.onDestroy()
    }
}