package com.banglalink.toffee.ui.upload

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeActivity
import com.github.florent37.runtimepermission.kotlin.PermissionException
import com.github.florent37.runtimepermission.kotlin.coroutines.experimental.askPermission
import kotlinx.android.synthetic.main.upload_method_fragment.*
import kotlinx.coroutines.launch
import net.gotev.uploadservice.protocols.multipart.MultipartUploadRequest
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class UploadMethodFragment: BaseFragment(R.layout.upload_method_fragment) {

    private var videoUri: Uri? = null

    companion object {
        private const val REQUEST_VIDEO = 0x220

        fun newInstance(): UploadMethodFragment {
            return UploadMethodFragment()
        }
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
        val upId = MultipartUploadRequest(
            requireContext(),
            "http://23.94.70.184:25478/upload?token=1148123456789"
        )
            .setMethod("POST")
            .addFileToUpload(uri, "file")
            .startUpload()

        mPref.uploadId = upId
        mPref.uploadStatus = 1

        activity?.
            findNavController(R.id.home_nav_host)?.let {
            it.navigate(R.id.action_uploadMethodFragment_to_editUploadInfoFragment,
            Bundle().apply {
                putString(EditUploadInfoFragment.ARG_UPLOAD_ID, upId)
                putString(EditUploadInfoFragment.ARG_UPLOAD_URI, uri)
            })
        }
    }
}