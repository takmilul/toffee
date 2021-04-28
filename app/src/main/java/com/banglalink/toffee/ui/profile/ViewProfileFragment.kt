package com.banglalink.toffee.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentViewProfileBinding
import com.banglalink.toffee.extension.launchActivity
import com.banglalink.toffee.extension.loadProfileImage
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.splash.SplashScreenActivity
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.unsafeLazy
import net.gotev.uploadservice.UploadService

class ViewProfileFragment : BaseFragment() {

    private var _binding: FragmentViewProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<ViewProfileViewModel>()

    private val progressDialog by unsafeLazy {
        VelBoxProgressDialog(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentViewProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!mPref.isVerifiedUser) {
            return
        }

        binding.data = EditProfileForm().apply {
            fullName = mPref.customerName
            phoneNo = mPref.phoneNumber
            photoUrl = mPref.userImageUrl ?: ""
        }

        loadProfile()
        observe(mPref.profileImageUrlLiveData) {
            binding.profileIv.loadProfileImage(it)
        }
        binding.editProfile.setOnClickListener {
            onClickEditProfile()
        }
    }

    private fun loadProfile() {
        progressDialog.show()
        observe(viewModel.loadCustomerProfile()) {
            progressDialog.dismiss()
            when (it) {
                is Resource.Success -> {
                    binding.data = it.data
                }
                is Resource.Failure -> {
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }

    fun onClickEditProfile() {
        if (!mPref.isVerifiedUser) {
            handleVerficationApp()
            return
        }

        if (findNavController().currentDestination?.id != R.id.thumbnailSelectionMethodFragment && findNavController().currentDestination?.id == R.id.profileFragment) {
            val action =
                ViewProfileFragmentDirections.actionProfileFragmentToEditProfileFragment(binding.data)
            findNavController().navigate(action)
        }

    }

    fun handleVerficationApp() {
        mPref.clear()
        mPref.logout = "1"
        UploadService.stopAllUploads()
        requireActivity().launchActivity<SplashScreenActivity>()
        requireActivity().finish()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}