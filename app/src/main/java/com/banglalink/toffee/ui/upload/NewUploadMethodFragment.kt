package com.banglalink.toffee.ui.upload

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.data.repository.UploadInfoRepository
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.MyChannelNavParams
import com.banglalink.toffee.ui.home.HomeActivity
import com.banglalink.toffee.ui.home.HomeViewModel
import com.github.florent37.runtimepermission.kotlin.PermissionException
import com.github.florent37.runtimepermission.kotlin.coroutines.experimental.askPermission
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_new_upload_method.view.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class NewUploadMethodFragment : DialogFragment() {
    @Inject
    lateinit var mpref: Preference

    @Inject
    lateinit var mUploadInfoRepository: UploadInfoRepository
    private val homeViewModel by activityViewModels<HomeViewModel>()
    private var videoUri: Uri? = null
    private var alertDialog: AlertDialog? = null

    companion object {
        private const val REQUEST_CAPTURE_VIDEO = 0x220
        private const val REQUEST_PICK_VIDEO = 0x230
        fun newInstance(): UploadMethodFragment {
            return UploadMethodFragment()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogView = layoutInflater.inflate(R.layout.fragment_new_upload_method, null, false)
        with(dialogView) {
            myChannelButton.setOnClickListener {
                openMyChannelFragment()
            }
            imageView11?.setOnClickListener {
                findNavController().popBackStack(R.id.bottomSheetUploadFragment, true)
            }
            open_camera_button.setOnClickListener {
                checkCameraPermissions()
            }
            open_gallery_button.setOnClickListener {
                checkFileSystemPermission()
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


    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        findNavController().popBackStack(R.id.bottomSheetUploadFragment, true)
    }

    private fun checkFileSystemPermission() {
        lifecycleScope.launch {
            try {
                if (askPermission(Manifest.permission.READ_EXTERNAL_STORAGE).isAccepted) {
                    startActivityForResult(
                        Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        ).setType("video/mp4"),
                        REQUEST_PICK_VIDEO
                    )
                }
            } catch (e: PermissionException) {
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
            } catch (e: PermissionException) {
                ToffeeAnalytics.logBreadCrumb("Camera permission denied")
                requireContext().showToast(getString(R.string.grant_camera_permission))
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
                } else {
                    ToffeeAnalytics.logBreadCrumb("Camera/video picker returned without any data")
                }
            }
            REQUEST_CAPTURE_VIDEO -> {
                if (resultCode == Activity.RESULT_OK && videoFile != null) {
                    println("CaptureAbsolutePath${videoFile!!.absolutePath}")
                    println("CapturePath${videoFile!!.path}")
                    openEditUpload(videoFile!!.absolutePath)
                } else {
                    ToffeeAnalytics.logBreadCrumb("Camera/video capture result not returned")
                }
            }
        }

    }

    private fun openMyChannelFragment() {
        homeViewModel.myChannelNavLiveData.value =
            MyChannelNavParams(mpref.channelId, mpref.customerId, 0)
    }

    private fun openEditUpload(uri: String) {
        findNavController()?.navigate(
            R.id.action_newUploadMethodFragment_to_editUploadInfoFragment,
            Bundle().apply {
                putString(EditUploadInfoFragment.UPLOAD_FILE_URI, uri)
            })

    }

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