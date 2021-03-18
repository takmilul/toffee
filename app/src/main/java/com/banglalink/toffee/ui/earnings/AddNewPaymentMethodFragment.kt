package com.banglalink.toffee.ui.earnings

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.R.layout
import com.banglalink.toffee.databinding.AlertDialogSuccessBinding
import com.banglalink.toffee.databinding.FragmentAddNewPaymentMethodBinding
import com.banglalink.toffee.extension.observe

class AddNewPaymentMethodFragment : Fragment(), OnClickListener {
    
    private lateinit var binding: FragmentAddNewPaymentMethodBinding
    private val viewModel by viewModels<AddNewPaymentMethodViewModel>()

    companion object {
        @JvmStatic
        fun newInstance() : AddNewPaymentMethodFragment {
            return AddNewPaymentMethodFragment()
        }
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentAddNewPaymentMethodBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        observeData()
        viewModel.getAccountTypeList()
        viewModel.getBankNameList()
        viewModel.getDistrictNameList()
        viewModel.getBranchNameList()
        
        binding.addButton.setOnClickListener(this)
    }

    private fun observeData() {
        observe(viewModel.accountTypesLiveData){}
        observe(viewModel.bankNamesLiveData){}
        observe(viewModel.districtNamesLiveData){}
        observe(viewModel.branchNamesLiveData){}
    }

    override fun onClick(v: View?) {
        when(v) {
            binding.addButton -> {
                findNavController().popBackStack()
                showRatingDialog()
            }
        }
    }

    private fun showRatingDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val dialogBinding = AlertDialogSuccessBinding.inflate(LayoutInflater.from(requireContext()))
        dialogBuilder.setView(dialogBinding.root)

        dialogBinding.successMsgTextView.text = "Your new account \n has been submitted successfully! \n You will be notified once its ready to use."

        val alertDialog: AlertDialog = dialogBuilder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
    }
}