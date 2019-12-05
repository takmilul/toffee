package com.banglalink.toffee.ui.profile

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.ActivityEditProfileBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.widget.VelBoxFieldTextWatcher
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import androidx.lifecycle.lifecycleScope
import coil.api.load
import coil.transform.CircleCropTransformation
import com.banglalink.toffee.data.storage.Preference
import com.banglalink.toffee.extension.loadProfileImage
import com.banglalink.toffee.ui.common.BaseAppCompatActivity
import com.banglalink.toffee.util.unsafeLazy
import com.github.florent37.runtimepermission.kotlin.PermissionException
import com.github.florent37.runtimepermission.kotlin.coroutines.experimental.askPermission
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : BaseAppCompatActivity() {

    var photoUri: Uri? = null
    private lateinit var progressDialog: VelBoxProgressDialog
    lateinit var binding:ActivityEditProfileBinding
    private val REQUEST_IMAGE = 3
    private val GALLERY_IMAGE_REQUEST = 4
    private val TAG = "EditProfileActivity"

    companion object{
        const val PROFILE_INFO = "Profile"
    }
    private val viewModel by unsafeLazy {
        ViewModelProviders.of(this).get(EditProfileViewModel::class.java)
    }

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

        observe(Preference.getInstance().profileImageUrlLiveData){
            binding.profileEditLayout.profileIv.loadProfileImage(it)
        }
    }

    private fun checkCameraPermissions() {
        lifecycleScope.launch{
            try {
                if(askPermission(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE).isAccepted){
                    openCameraIntent()
                }
            }
            catch (e: PermissionException){
                showToast("Please grant permission ${e.denied[0]}")
            }
        }
    }

    private fun openCameraIntent() {
        val pictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (pictureIntent.resolveActivity(packageManager) != null) {

            val photoFile: File?
            try {
                photoFile = createImageFile()
            } catch (e: IOException) {
                e.printStackTrace()
                return
            }

            photoUri = FileProvider.getUriForFile(this, "$packageName.provider", photoFile)
            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            startActivityForResult(pictureIntent, REQUEST_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode,data)

        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                startCrop(photoUri!!)
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "You cancelled the operation", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == UCrop.REQUEST_CROP) {
            if (resultCode == RESULT_OK) {
                photoUri = UCrop.getOutput(data!!)
                binding.profileEditLayout.profileIv.load(photoUri){
                    transformations(CircleCropTransformation())
                    placeholder(R.drawable.ic_profile_default)
                }
                photoUri?.let {
                    handleUploadImage(it)
                }


            }
        } else if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            startCrop(data.data!!)
        }
    }

    private fun startCrop(uri: Uri) {
        val destinationFileName = "PersonImage"

        var uCrop = UCrop.of(
            uri,
            Uri.fromFile(File(cacheDir, destinationFileName + System.currentTimeMillis()))
        )

        val options = UCrop.Options().apply {
            setHideBottomControls(true)
            setFreeStyleCropEnabled(true)
            setActiveWidgetColor(ContextCompat.getColor(this@EditProfileActivity,R.color.colorAccent))
        }

        uCrop = uCrop.withOptions(options)

        uCrop.start(this)

    }

    private fun handleSaveButton(){
        progressDialog.show()
        observe(viewModel.updateProfile(binding.profileForm!!)){
            progressDialog.dismiss()
            when(it){
                is Resource.Success->{
                    val intent = intent
                    intent.putExtra(PROFILE_INFO,binding.profileForm)
                    setResult(RESULT_OK, intent)
                    finish()
                }
                is Resource.Failure->{
                    showToast(it.error.msg)
                }
            }
        }
    }

    private fun handleUploadImage(photoUri:Uri){
        try {
            observe( viewModel.uploadProfileImage(photoUri)){
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
            Log.e(TAG, e.message, e)
        }
    }

    @Throws(IOException::class)
    fun Context.createImageFile(): File {

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

}
