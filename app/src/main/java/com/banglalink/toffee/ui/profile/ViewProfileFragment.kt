package com.banglalink.toffee.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentViewProfileBinding
import com.banglalink.toffee.extension.checkVerification
import com.banglalink.toffee.extension.loadProfileImage
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.EditProfileForm
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.unsafeLazy

class ViewProfileFragment : BaseFragment() {

    private var _binding: FragmentViewProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<ViewProfileViewModel>()

    private val progressDialog by unsafeLazy {
        VelBoxProgressDialog(requireContext())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentViewProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (mPref.isVerifiedUser) {
            binding.data = EditProfileForm().apply {
                fullName = mPref.customerName
                phoneNo = mPref.phoneNumber
                photoUrl = mPref.userImageUrl ?: ""
            }
            observe(mPref.profileImageUrlLiveData) {
                binding.profileIv.loadProfileImage(it)
            }
            loadProfile()
        }

        binding.editProfile.setOnClickListener {
            requireActivity().checkVerification { onClickEditProfile() }
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
        if (findNavController().currentDestination?.id != R.id.thumbnailSelectionMethodFragment && findNavController().currentDestination?.id == R.id.profileFragment) {
            val action =
                ViewProfileFragmentDirections.actionProfileFragmentToEditProfileFragment(binding.data)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}