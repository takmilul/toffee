package com.banglalink.toffee.ui.upload

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.ui.home.HomeActivity
import com.github.florent37.runtimepermission.kotlin.PermissionException
import com.github.florent37.runtimepermission.kotlin.coroutines.experimental.askPermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.upload_method_fragment.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class UploadMethodFragment : DialogFragment() {

    @Inject
    lateinit var mUploadInfoRepository: UploadInfoRepository

    private var videoUri: Uri? = null

    companion object {
        private const val REQUEST_CAPTURE_VIDEO = 0x220
        private const val REQUEST_PICK_VIDEO = 0x230

        fun newInstance(): UploadMethodFragment {
            return UploadMethodFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(
            STYLE_NORMAL,
            R.style.FullScreenDialogStyle
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.upload_method_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setOnClickListener {
            findNavController().popBackStack()
        }

        upload_method_card.setOnClickListener {
            
        }

        learn_more_button.setOnClickListener {

        }

        open_camera_button.setOnClickListener {
            checkCameraPermissions()
        }

        open_gallery_button.setOnClickListener {
            checkFileSystemPermission()
        }
    }

    private fun checkFileSystemPermission() {
        lifecycleScope.launch {
            try {
                if (askPermission(Manifest.permission.READ_EXTERNAL_STORAGE).isAccepted) {
                    startActivityForResult(
                        Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        ).setType("video/*"),
                        REQUEST_PICK_VIDEO
                    )
                }
            }
            catch (e: PermissionException) {
                ToffeeAnalytics.logBreadCrumb("Storage permission denied")
                requireContext().showToast(getString(R.string.grant_storage_permission))
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
        }
    }

    private var videoFile: File? = null
    private fun openCameraIntent() {
        val videoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        if (videoIntent.resolveActivity(requireActivity().packageManager) != null) {

//            val videoFile: File?
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
            startActivityForResult(videoIntent, REQUEST_CAPTURE_VIDEO)
            ToffeeAnalytics.logBreadCrumb("Camera activity started")

        }
    }

    @Throws(IOException::class)
    fun createVideoFile(): File {

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val videoFileName = "VIDEO_" + timeStamp + "_"
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_MOVIES)

        return File.createTempFile(videoFileName, ".mp4", storageDir)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PICK_VIDEO -> {
                if (resultCode == Activity.RESULT_OK && data != null && data.dataString != null) {
                    openEditUpload(data.dataString!!)
                }
                else {
                    ToffeeAnalytics.logBreadCrumb("Camera/video picker returned without any data")
                }
            }
            REQUEST_CAPTURE_VIDEO -> {
                if (resultCode == Activity.RESULT_OK && videoFile != null) {
                    println("CaptureAbsolutePath${videoFile!!.absolutePath}")
                    println("CapturePath${videoFile!!.path}")
                    openEditUpload(videoFile!!.absolutePath)
                }
                else {
                    ToffeeAnalytics.logBreadCrumb("Camera/video capture result not returned")
                }
            }
        }
        /*if(resultCode == Activity.RESULT_OK
            && requestCode == REQUEST_CAPTURE_VIDEO
            && data != null) {
            data.dataString?.let {
                uploadUri(it)
            }
        } else {
            ToffeeAnalytics.logBreadCrumb("Camera/video picker returned without any data")
        }*/
    }

    private fun openEditUpload(uri: String) {
        activity?.findNavController(R.id.home_nav_host)?.navigate(R.id.action_uploadMethodFragment_to_editUploadInfoFragment, Bundle().apply {
            putString(EditUploadInfoFragment.UPLOAD_FILE_URI, uri)
        })
    }

//    private fun uploadUri3(uri: String) {
//
//        lifecycleScope.launch {
//            val dialog = VelBoxProgressDialog(requireContext())
//            dialog.show()
//
//            val accessToken = withContext(Dispatchers.IO) {
//                val credential = GoogleCredential.fromStream(
//                    requireContext().assets.open("toffee-261507-60ca3e5405df.json")
//                ).createScoped(listOf("https://www.googleapis.com/auth/devstorage.read_write"))
//                credential.refreshToken()
//                credential.accessToken
//            }
//
//            if (accessToken.isNullOrEmpty()) {
//                open_gallery_button.snack("Error uploading file. Please try again later.") {}
//                return@launch
//            }
//
//            val fn = withContext(Dispatchers.IO + Job()) {
//                UtilsKt.fileNameFromContentUri(requireContext(), Uri.parse(uri))
//            }
//            val idx = fn.lastIndexOf(".")
//            val ext = if (idx >= 0) {
//                fn.substring(idx)
//            }
//            else ""
//
//            val fileName = mPref.customerId.toString() + "_" + UUID.randomUUID().toString() + ext
//            val upInfo = UploadInfo(serverContentId = 0L, fileUri = uri, fileName = fileName)
//
//            val contentType = withContext(Dispatchers.IO + Job()) {
//                UtilsKt.contentTypeFromContentUri(requireContext(), Uri.parse(uri))
//            }
//
//            Log.e("UPLOAD", "$fileName, $contentType")
//
//            val upId = mUploadInfoRepository.insertUploadInfo(upInfo)
//            val uploadIdStr =
//                withContext(Dispatchers.IO + Job()) {
//                    BinaryUploadRequest(
//                        requireContext(),
//                        "https://storage.googleapis.com/upload/storage/v1/b/ugc-content-storage/o?uploadType=media&name=${fileName}"
//                    )
//                        .setUploadID(UtilsKt.uploadIdToString(upId))
//                        .setMethod("POST")
//                        .addHeader("Content-Type", contentType)
//                        .setFileToUpload(uri)
//                        .setBearerAuth(accessToken)
//                        .startUpload()
//                }
//
////            mPref.uploadId = uploadIdStr
//            dialog.dismiss()
//        }
//    }
//
//    private fun uploadUri2(uri: String) {
//
//        lifecycleScope.launch {
//            val upInfo = UploadInfo(serverContentId = 0L, fileUri = uri, fileName = "fileName")
//            val upId = mUploadInfoRepository.insertUploadInfo(upInfo)
//            val uploadIdStr =
//                withContext(Dispatchers.IO + Job()) {
//                    MultipartUploadRequest(
//                        requireContext(),
//                        "http://23.94.70.184:25478/upload?token=1148123456789"
//                    )
//                        .setUploadID(UtilsKt.uploadIdToString(upId))
//                        .setMethod("POST")
//                        .addFileToUpload(uri, "file")
//                        .startUpload()
//                }
//
////            mPref.uploadId = uploadIdStr
//            activity?.findNavController(R.id.home_nav_host)?.navigate(R.id.action_uploadMethodFragment_to_editUploadInfoFragment)
//        }
//    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is HomeActivity) {
            context.rotateFab(true)
        }
    }

    override fun onDetach() {
        requireActivity().let {
            if (it is HomeActivity) it.rotateFab(false)
        }
        super.onDetach()
    }
}