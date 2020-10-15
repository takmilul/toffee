package com.banglalink.toffee.ui.upload

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.database.entities.UploadInfo
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.extension.snack
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.util.UtilsKt
import com.github.florent37.runtimepermission.kotlin.PermissionException
import com.github.florent37.runtimepermission.kotlin.coroutines.experimental.askPermission
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.services.pubsub.PubsubScopes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.upload_method_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.gotev.uploadservice.protocols.binary.BinaryUploadRequest
import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Logger
import javax.inject.Inject

@AndroidEntryPoint
class UploadMethodFragment: BaseFragment() {

    @Inject
    lateinit var mUploadInfoRepository: UploadInfoRepository

    private var videoUri: Uri? = null

    companion object {
        private const val REQUEST_VIDEO = 0x220

        fun newInstance(): UploadMethodFragment {
            return UploadMethodFragment()
        }
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
            requireActivity().supportFragmentManager.popBackStack()
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
            try{
                if(askPermission(Manifest.permission.READ_EXTERNAL_STORAGE).isAccepted) {
                    startActivityForResult(
                        Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        ),
                        REQUEST_VIDEO
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
        val videoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        if (videoIntent.resolveActivity(requireActivity().packageManager) != null) {

            val videoFile: File?
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
                videoFile
            )
            ToffeeAnalytics.logBreadCrumb("Video uri set")
            videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
            startActivityForResult(videoIntent, REQUEST_VIDEO)
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
        if(resultCode == Activity.RESULT_OK
            && requestCode == REQUEST_VIDEO
            && data != null) {
            data.dataString?.let {
                uploadUri(it)
            }
        } else {
            ToffeeAnalytics.logBreadCrumb("Camera/video picker returned without any data")
        }
    }

    private fun uploadUri(uri: String) {

        lifecycleScope.launch {
            val accessToken = withContext(Dispatchers.IO) {
                val credential = GoogleCredential.fromStream(
                    requireContext().assets.open("toffee-261507-60ca3e5405df.json")
                ).createScoped(listOf("https://www.googleapis.com/auth/devstorage.read_write"))
                credential.refreshToken()
                credential.accessToken
            }

            if(accessToken.isNullOrEmpty()) {
                open_gallery_button.snack("Error uploading file. Please try again later."){}
                return@launch
            }
            Log.e("TOKEN", accessToken)
            val upInfo = UploadInfo(fileUri = uri)
            val fileName = UUID.randomUUID()
            Log.e("FILENAME", fileName.toString())
            val upId = mUploadInfoRepository.insertUploadInfo(upInfo)
            val uploadIdStr =
                withContext(Dispatchers.IO + Job()) {
                    BinaryUploadRequest(
                        requireContext(),
                        "https://storage.googleapis.com/upload/storage/v1/b/ugc-content-storage/o?uploadType=media&name=${fileName}"
                    )
                        .setUploadID(UtilsKt.uploadIdToString(upId))
                        .setMethod("POST")
                        .addHeader("Content-Type", "video/mp4")
                        .setFileToUpload(uri)
                        .setBearerAuth(accessToken)
                        .startUpload()
                }

            mPref.uploadId = uploadIdStr
            activity?.findNavController(R.id.home_nav_host)?.navigate(R.id.action_uploadMethodFragment_to_editUploadInfoFragment)
        }
    }
}