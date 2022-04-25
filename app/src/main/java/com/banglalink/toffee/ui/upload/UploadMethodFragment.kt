package com.banglalink.toffee.ui.upload

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import com.banglalink.toffee.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.databinding.UploadMethodFragmentBinding
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.ui.widget.ToffeeAlertDialogBuilder
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
class UploadMethodFragment : DialogFragment() {

    private var videoUri: Uri? = null
    @Inject lateinit var mUploadInfoRepository: UploadInfoRepository
    private var _binding: UploadMethodFragmentBinding ? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.FullScreenDialogStyle
        )
        activity?.title = "Upload"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = UploadMethodFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (activity is HomeActivity) {
            (activity as HomeActivity).rotateFab(true)
        }

        view.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.openCameraButton.setOnClickListener {
            checkCameraPermissions()
        }
        binding.openGalleryButton.setOnClickListener {
            checkFileSystemPermission()
        }
        binding.uploadMethodCard.setOnClickListener { }
        binding.learnMoreButton.setOnClickListener { }
    }

    private fun checkFileSystemPermission() {
        lifecycleScope.launch {
            try {
                if (askPermission(Manifest.permission.READ_EXTERNAL_STORAGE).isAccepted) {
                    galleryResultLauncher.launch(Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI).setType("video/mp4"),)
                }
            }
            catch (e: PermissionException) {
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
            }
            catch (e: PermissionException) {
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
            }
            catch (e: IOException) {
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
        if (storageDir?.exists() == false){
            storageDir.mkdirs()
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
            lifecycleScope.launch {
                println("CaptureAbsolutePath${videoFile!!.absolutePath}")
                println("CapturePath${videoFile!!.path}")
                if (Utils.getVideoUploadLimit(Utils.getVideoDuration(requireContext(), videoUri.toString()))){

                    ToffeeAlertDialogBuilder(requireContext()).apply {
                        setTitle(R.string.txt_video_length)
                        setText(R.string.txt_video_length_msg)
                        setPositiveButtonListener(getString(R.string.btn_got_it)) {
                            it?.dismiss()
                        }
                    }.create().show()
                }
                else{
                    openEditUpload(videoFile!!.absolutePath)
                }
            }

        } else {
            ToffeeAnalytics.logBreadCrumb("Camera/video capture result not returned")
        }
    }
    
    private fun checkAndOpenUpload(videoUri: Uri) {
        lifecycleScope.launch {
            val contentType = Utils.contentTypeFromContentUri(requireContext(), videoUri)
            val fileName = Utils.fileNameFromContentUri(requireContext(), videoUri)

            Log.i("UPLOAD_T", "Type ->> $contentType, Name ->> $fileName")

            if(contentType == "video/mp4" && fileName.substringAfterLast(".", "mp4") == "mp4") {

                if (Utils.getVideoUploadLimit(Utils.getVideoDuration(requireContext(), videoUri.toString()))){

                    ToffeeAlertDialogBuilder(requireContext()).apply {
                        setTitle(R.string.txt_video_length)
                        setText(R.string.txt_video_length_msg)
                        setPositiveButtonListener(getString(R.string.btn_got_it)) {
                            it?.dismiss()
                        }
                    }.create().show()
                }
                else{
                    openEditUpload(videoUri.toString())
                }

            } else {
                ToffeeAlertDialogBuilder(requireContext()).apply {
                    setTitle(R.string.txt_video_format)
                    setText(R.string.txt_video_format_msg)
                    setPositiveButtonListener(getString(R.string.btn_got_it)) {
                        it?.dismiss()
                    }
                }.create().show()
            }
        }
    }

    private fun openEditUpload(uri: String) {
        activity?.findNavController(R.id.home_nav_host)?.navigate(R.id.action_uploadMethodFragment_to_editUploadInfoFragment, Bundle().apply {
            putString(EditUploadInfoFragment.UPLOAD_FILE_URI, uri)
        })
    }

    override fun onDestroyView() {
        requireActivity().let {
            if (it is HomeActivity) it.rotateFab(false)
        }
        _binding = null
        super.onDestroyView()
    }
}