package com.banglalink.toffee.ui.profile

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import coil.transform.CircleCropTransformation
import com.banglalink.toffee.R
import com.banglalink.toffee.analytics.ToffeeAnalytics
import com.banglalink.toffee.databinding.FragmentEditProfileBinding
import com.banglalink.toffee.enums.InputType
import com.banglalink.toffee.extension.*
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.upload.ThumbnailSelectionMethodFragment
import com.banglalink.toffee.ui.widget.VelBoxFieldTextWatcher
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.UtilsKt
import com.banglalink.toffee.util.unsafeLazy
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class EditProfileFragment : BaseFragment() {

    private val TAG = "EditProfileActivity"
    private val progressDialog by unsafeLazy {
        VelBoxProgressDialog(requireContext())
    }
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val args by navArgs<EditProfileFragmentArgs>()
    private val viewModel by viewModels<EditProfileViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            profileForm = args.data
            container.setOnClickListener {
                UtilsKt.hideSoftKeyboard(requireActivity())
            }
            saveButton.setOnClickListener { handleSaveButton() }
            cancelBtn.setOnClickListener { findNavController().popBackStack() }
            nameEt.onFocusChangeListener = VelBoxFieldTextWatcher(binding.nameEt, VelBoxFieldTextWatcher.FieldType.NAME_FIELD)
            emailEt.onFocusChangeListener = VelBoxFieldTextWatcher(binding.emailEt, VelBoxFieldTextWatcher.FieldType.EMAIL_FIELD)
            addressEt.onFocusChangeListener = VelBoxFieldTextWatcher(binding.addressEt, VelBoxFieldTextWatcher.FieldType.ADDRESS_FIELD)
            editIv.setOnClickListener {
                if (findNavController().currentDestination?.id != R.id.thumbnailSelectionMethodFragment && findNavController().currentDestination?.id == R.id.EditProfileFragment) {
                    val action =
                        EditProfileFragmentDirections.actionEditProfileToThumbnailSelectionMethodFragment(
                            "Set Profile Photo",
                            true
                        )
                    findNavController().navigate(action)
                }
            }
        }
        observe(mPref.profileImageUrlLiveData) {
            binding.profileIv.loadProfileImage(it)
        }
        observeThumbnailChange()
    }

    @Throws(IOException::class)
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (storageDir?.exists() == false) {
            storageDir.mkdirs()
        }
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    private fun observeThumbnailChange() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String?>(
            ThumbnailSelectionMethodFragment.THUMB_URI
        )
            ?.observe(viewLifecycleOwner, {
                it?.let { photoData ->
                    ToffeeAnalytics.logBreadCrumb("Got result from crop lib")
                    ToffeeAnalytics.logBreadCrumb("Handling crop image")
                    binding.profileIv.load(photoData) {
                        transformations(CircleCropTransformation())
                    }
                    handleUploadImage(photoData.toUri())

                }
            })
    }

    private fun handleSaveButton() {
        progressDialog.show()
        binding.profileForm?.let {

            if (it.fullName.isBlank()) {
                progressDialog.hide()
                binding.nameEt.setBackgroundResource(R.drawable.error_single_line_input_text_bg)
                binding.errorNameTv.show()
            } else {
                binding.nameEt.setBackgroundResource(R.drawable.single_line_input_text_bg)
                binding.errorNameTv.hide()
            }

            val notValidEmail = it.email.isNotBlank() and !it.email.isValid(InputType.EMAIL)

            if (notValidEmail) {
                progressDialog.hide()
                binding.emailEt.setBackgroundResource(R.drawable.error_single_line_input_text_bg)
                binding.errorEmailTv.show()
            } else {
                binding.emailEt.setBackgroundResource(R.drawable.single_line_input_text_bg)
                binding.errorEmailTv.hide()
            }

            if (it.fullName.isNotBlank()) {
                it.apply {
                    fullName = fullName.trim()
                    email = email.trim()
                    address = address.trim()
                }

                observe(viewModel.updateProfile(it)) {
                    progressDialog.dismiss()
                    when (it) {
                        is Resource.Success -> {
                            requireContext().showToast("Profile updated successfully")
                            findNavController().popBackStack()
                        }
                        is Resource.Failure -> {
                            requireContext().showToast(it.error.msg)
                        }
                    }
                }
            }
        }
    }
    
    private fun handleUploadImage(photoUri: Uri) {
        try {
            progressDialog.show()
            observe(viewModel.uploadProfileImage(photoUri)) {
                progressDialog.dismiss()
                when (it) {
                    is Resource.Success -> {
                        requireContext().showToast(getString(R.string.photo_update_success))
                    }
                    is Resource.Failure -> {
                        requireContext().showToast(it.error.msg)
                    }
                }
            }

        } catch (e: Exception) {
            progressDialog.dismiss()
            ToffeeAnalytics.logException(e)
            Log.e(TAG, e.message, e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}