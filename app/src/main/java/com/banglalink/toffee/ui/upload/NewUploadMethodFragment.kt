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
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.databinding.FragmentNewUploadMethodBinding
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.widget.VelBoxAlertDialogBuilder
import com.banglalink.toffee.util.UtilsKt
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
    
    @Inject lateinit var mpref: SessionPreference
    @Inject lateinit var mUploadInfoRepository: UploadInfoRepository
    private val homeViewModel by activityViewModels<HomeViewModel>()
    private var videoUri: Uri? = null
    private var alertDialog: AlertDialog? = null
    private var _binding: FragmentNewUploadMethodBinding ? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = FragmentNewUploadMethodBinding.inflate(layoutInflater)
        binding.myChannelButton.setOnClickListener {
            openMyChannelFragment()
        }
        binding.imageView11.setOnClickListener {
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
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun checkFileSystemPermission() {
        lifecycleScope.launch {
            try {
                if (askPermission(Manifest.permission.READ_EXTERNAL_STORAGE).isAccepted) {
                    galleryResultLauncher.launch(Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI).setType("video/mp4"))
                }
            } catch (e: PermissionException) {
                ToffeeAnalytics.logBreadCrumb("Storage permission denied")
                requireContext().showToast(getString(R.string.grant_storage_permission))
            }
            catch (e: NoActivityException){
                ToffeeAnalytics.logBreadCrumb("Activity Not Found - filesystem(gallery)")
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
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val videoFileName = "VIDEO_" + timeStamp + "_"
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES)
        if (storageDir?.exists() == true){
            storageDir.mkdir()
        }else{
            storageDir?.mkdirs()
        }
        return File.createTempFile(videoFileName, ".mp4", storageDir)
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
            val contentType = UtilsKt.contentTypeFromContentUri(requireContext(), videoUri)
            val fileName = UtilsKt.fileNameFromContentUri(requireContext(), videoUri)

            Log.e("UPLOAD_T", "Type ->> $contentType, Name ->> $fileName")

            if(contentType == "video/mp4" || fileName.substringAfterLast(".", "") == "mp4") {
                openEditUpload(videoUri.toString())
            } else {
                VelBoxAlertDialogBuilder(requireContext()).apply {
                    setTitle("Select mp4 file")
                    setText("Only mp4 file uploading is supported.")
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
    
    override fun onDestroy() {
        cameraResultLauncher.unregister()
        galleryResultLauncher.unregister()
        super.onDestroy()
    }
}