package com.banglalink.toffee.ui.profile

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.databinding.ActivityEditProfileBinding
import com.banglalink.toffee.databinding.DialogueProfileImageSelectionBinding
import com.banglalink.toffee.enums.InputType
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseAppCompatActivity
import com.banglalink.toffee.ui.widget.VelBoxFieldTextWatcher
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.UtilsKt
import com.github.florent37.runtimepermission.kotlin.PermissionException
import com.github.florent37.runtimepermission.kotlin.coroutines.experimental.askPermission
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class EditProfileActivity : BaseAppCompatActivity() {

    var photoUri: Uri? = null
    private val TAG = "EditProfileActivity"
    private var alertDialog: AlertDialog? = null
    lateinit var binding:ActivityEditProfileBinding
    private lateinit var progressDialog: VelBoxProgressDialog

    companion object{
        const val PROFILE_INFO = "Profile"
    }
    
    private val viewModel by viewModels<EditProfileViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_profile)
        binding.profileForm = intent.getSerializableExtra(PROFILE_INFO) as EditProfileForm
        binding.container.setOnClickListener {
            UtilsKt.hideSoftKeyboard(this)
        }

        progressDialog = VelBoxProgressDialog(this)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.nameEt.onFocusChangeListener = VelBoxFieldTextWatcher(
            binding.nameEt,
            VelBoxFieldTextWatcher.FieldType.NAME_FIELD
        )
        binding.emailEt.onFocusChangeListener = VelBoxFieldTextWatcher(
            binding.emailEt,
            VelBoxFieldTextWatcher.FieldType.EMAIL_FIELD
        )
        binding.addressEt.onFocusChangeListener = VelBoxFieldTextWatcher(
            binding.addressEt,
            VelBoxFieldTextWatcher.FieldType.ADDRESS_FIELD
        )

        binding.profileEditLayout.editIv.setOnClickListener{
            openUploadOption()
        }

        binding.saveButton.setOnClickListener{
            handleSaveButton()
        }

        binding.cancelBtn.setOnClickListener{
            finish()
        }

        observe(mPref.profileImageUrlLiveData){
            binding.profileEditLayout.profileIv.loadProfileImage(it)
        }
    }

    private fun openUploadOption()
    {
        val dialogBinding = DialogueProfileImageSelectionBinding.inflate(layoutInflater, null, false)

        alertDialog=  AlertDialog
            .Builder(this)
            .setView(dialogBinding.root).create()
            .apply {
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }

        alertDialog?.show()

        with(dialogBinding){
            openGalleryButton.setOnClickListener {
                checkFileSystemPermission()
            }
            openCameraButton.setOnClickListener {
                checkCameraPermissions()
            }
        }
    }

    private fun checkFileSystemPermission() {
        lifecycleScope.launch {
            try{
                if(askPermission(Manifest.permission.READ_EXTERNAL_STORAGE).isAccepted) {
                    galleryResultLauncher.launch(
                        Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        )
                    )
                }
            }
            catch (e: PermissionException) {
                ToffeeAnalytics.logBreadCrumb("Storage permission denied")
                showToast(getString(R.string.grant_storage_permission))
            }
        }
    }

    private val galleryResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == Activity.RESULT_OK && it.data != null && it.data?.data != null) {
            startCrop(it.data!!.data!!)
        } else {
            ToffeeAnalytics.logBreadCrumb("Camera/video picker returned without any data")
        }
    }

    private val cameraResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if (it.resultCode == Activity.RESULT_OK && photoUri != null) {
            ToffeeAnalytics.logBreadCrumb("Got result from camera")
            startCrop(photoUri!!)
        } else {
            ToffeeAnalytics.logBreadCrumb("Camera/video capture result not returned")
        }
    }

    private fun startCrop(uri: Uri) {
        var uCrop = UCrop.of(
            uri,
            Uri.fromFile(createImageFile())
        )

        val options = UCrop.Options().apply {
            setHideBottomControls(true)
            withAspectRatio(4f, 4f)
            setCircleDimmedLayer(true)
            withMaxResultSize(1280, 720)
            setFreeStyleCropEnabled(false)
        }

        uCrop = uCrop.withOptions(options)
        uCrop.start(this)
        ToffeeAnalytics.logBreadCrumb("Crop started")
    }

    @Throws(IOException::class)
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == UCrop.REQUEST_CROP && data != null) {
                val uri = UCrop.getOutput(data)
                uri?.let {
                    alertDialog?.dismiss()
                    ToffeeAnalytics.logBreadCrumb("Got result from crop lib")
                    ToffeeAnalytics.logBreadCrumb("Handling crop image")
                    binding.profileEditLayout.profileIv.load(it){
                        transformations(CircleCropTransformation())
                    }
                    handleUploadImage(it)
                }
            }
        }
        else {
            ToffeeAnalytics.logBreadCrumb("Camera/image picker returned without any data")
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
                showToast(getString(R.string.grant_camera_permission))
            }
        }
    }

    private fun openCameraIntent() {
        val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (pictureIntent.resolveActivity(packageManager) != null) {

            val photoFile: File?
            try {
                photoFile = createImageFile()
                ToffeeAnalytics.logBreadCrumb("Photo file created")
            } catch (e: IOException) {
                e.printStackTrace()
                return
            }

            photoUri = FileProvider.getUriForFile(this, "$packageName.provider", photoFile)
            ToffeeAnalytics.logBreadCrumb("Photo uri set")
            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            cameraResultLauncher.launch(pictureIntent)
            ToffeeAnalytics.logBreadCrumb("Camera activity started")
        }
    }

    private fun handleSaveButton(){
        progressDialog.show()
        binding.profileForm?.let {

            if (it.fullName.isBlank()){
                progressDialog.hide()
                binding.nameEt.setBackgroundResource(R.drawable.error_single_line_input_text_bg)
                binding.errorNameTv.show()
            }
            else{
                binding.nameEt.setBackgroundResource(R.drawable.single_line_input_text_bg)
                binding.errorNameTv.hide()
            }
            
            val notValidEmail= it.email.isNotBlank() and !it.email.isValid(InputType.EMAIL)
            
            if(notValidEmail){
                progressDialog.hide()
                binding.emailEt.setBackgroundResource(R.drawable.error_single_line_input_text_bg)
                binding.errorEmailTv.show()
            }
            else{
                 binding.emailEt.setBackgroundResource(R.drawable.single_line_input_text_bg)
                 binding.errorEmailTv.hide()
            }

            if(it.fullName.isNotBlank()) {
                it.apply{
                    fullName = fullName.trim()
                    email = email.trim()
                    address = address.trim()
                }

                observe(viewModel.updateProfile(it)) {
                    progressDialog.dismiss()
                    when (it) {
                        is Resource.Success -> {
                            showToast("Profile updated successfully")
                            val intent = intent
                            intent.putExtra(PROFILE_INFO, binding.profileForm)
                            setResult(RESULT_OK, intent)
                            finish()
                        }
                        is Resource.Failure -> {
                            showToast(it.error.msg)
                        }
                    }
                }
            }
        }
    }

    private fun handleUploadImage(photoUri: Uri){
        try {
            progressDialog.show()
            observe(viewModel.uploadProfileImage(photoUri)){
                progressDialog.dismiss()
                when (it) {
                    is Resource.Success -> {
                        showToast(getString(R.string.photo_update_success))
                    }
                    is Resource.Failure -> {
                        showToast(it.error.msg)
                    }
                }
            }

        } catch (e: Exception) {
            progressDialog.dismiss()
            ToffeeAnalytics.logException(e)
            Log.e(TAG, e.message, e)
        }
    }
}
