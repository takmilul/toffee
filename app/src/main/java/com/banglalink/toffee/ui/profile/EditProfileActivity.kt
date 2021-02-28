package com.banglalink.toffee.ui.profile

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import coil.load
import coil.transform.CircleCropTransformation
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.apiservice.EditProfileViewModel
import com.banglalink.toffee.databinding.ActivityEditProfileBinding
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseAppCompatActivity
import com.banglalink.toffee.ui.widget.VelBoxFieldTextWatcher
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.github.florent37.runtimepermission.kotlin.PermissionException
import com.github.florent37.runtimepermission.kotlin.coroutines.experimental.askPermission
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class EditProfileActivity : BaseAppCompatActivity() {

    var photoUri: Uri? = null
    private lateinit var progressDialog: VelBoxProgressDialog
    lateinit var binding:ActivityEditProfileBinding
    private val REQUEST_IMAGE = 1729
    private val TAG = "EditProfileActivity"

    companion object{
        const val PROFILE_INFO = "Profile"
    }
    private val viewModel by viewModels<EditProfileViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_edit_profile)
        binding.profileForm = intent.getSerializableExtra(PROFILE_INFO) as EditProfileForm

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
            checkCameraPermissions()
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

    fun String.isValidEmail() =
        isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()

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
            startActivityForResult(pictureIntent, REQUEST_IMAGE)
            ToffeeAnalytics.logBreadCrumb("Camera activity started")

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode,data)

        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                ToffeeAnalytics.logBreadCrumb("Got result from camera")
                photoUri?.let {
                    startCrop(it)
                }
            } else if (resultCode == RESULT_CANCELED) {
               showToast("You cancelled the operation")
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            ToffeeAnalytics.logBreadCrumb("Got result from crop lib")
            data?.let { intent ->
                val uri = UCrop.getOutput(intent)
                uri?.let {
                    ToffeeAnalytics.logBreadCrumb("Handling crop image")
                    binding.profileEditLayout.profileIv.load(it){
                        transformations(CircleCropTransformation())
                    }
                    handleUploadImage(it)
                }
            }
        }
    }

    private fun startCrop(uri: Uri) {

        var uCrop = UCrop.of(
            uri,
            Uri.fromFile(createImageFile())
        )

        val options = UCrop.Options().apply {
            setHideBottomControls(true)
            setFreeStyleCropEnabled(false)
            setCircleDimmedLayer(true)
        }

        uCrop = uCrop.withOptions(options)

        uCrop.start(this)
        ToffeeAnalytics.logBreadCrumb("Crop started")

    }

    private fun handleSaveButton(){
        
        /*val isValid = binding.profileForm?.fullName?.isValid(TITLE)
        println("VALIDATION: $isValid")
        Log.e("VALIDATION", "handleSaveButton: $isValid")
        return*/

        progressDialog.show()
        binding.profileForm?.let {

            if (it.fullName.isBlank()){
                progressDialog.hide()
               // Toast.makeText(this, "All fields are blank", Toast.LENGTH_SHORT).show()
                binding.nameEt.setBackgroundResource(R.drawable.error_single_line_input_text_bg)
                binding.errorNameTv.show()

            }
            else{
                binding.nameEt.setBackgroundResource(R.drawable.single_line_input_text_bg)
                binding.errorNameTv.hide()
            }
            val validEmail=!it.email.isBlank() and !it.email.isValidEmail()
             if(validEmail){
                progressDialog.hide()
                binding.emailEt.setBackgroundResource(R.drawable.error_single_line_input_text_bg)
                binding.errorEmailTv.show()
            }
            else{
                 binding.emailEt.setBackgroundResource(R.drawable.single_line_input_text_bg)
                 binding.errorEmailTv.hide()
            }

            if(!it.fullName.isBlank()) {
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

    private fun handleUploadImage(photoUri:Uri){
        try {
            progressDialog.show()
            observe( viewModel.uploadProfileImage(photoUri)){
                progressDialog.dismiss()
                when (it) {
                    is Resource.Success -> {
                        showToast(
                            getString(R.string.photo_update_success)
                        )
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

    @Throws(IOException::class)
    fun createImageFile(): File {

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

}
