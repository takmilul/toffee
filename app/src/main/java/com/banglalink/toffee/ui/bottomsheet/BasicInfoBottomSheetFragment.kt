package com.banglalink.toffee.ui.bottomsheet

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.BottomSheetBasicInfoBinding
import com.banglalink.toffee.enums.InputType
import com.banglalink.toffee.extension.hide
import com.banglalink.toffee.extension.isValid
import com.banglalink.toffee.extension.safeClick
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.ui.common.BaseFragment
import java.util.*

class BasicInfoBottomSheetFragment : BaseFragment() {
    private var _binding: BottomSheetBasicInfoBinding? = null
    private val binding get() = _binding!!

    //
    var c = Calendar.getInstance()
    var selected_year = c.get(Calendar.YEAR)
    var selected_month = c.get(Calendar.MONTH)
    var selected_day = c.get(Calendar.DAY_OF_MONTH)

    lateinit var selectedDate: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetBasicInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            termsAndConditionsTv.safeClick({ showTermsAndConditionDialog() })
            dateOfBirthTv.safeClick({ openDatePicker() })
            saveBtn.safeClick({ handleSubmitButton() })
            termsAndConditionsCheckbox.setOnClickListener {
                saveBtn.isEnabled = binding.termsAndConditionsCheckbox.isChecked
            }
        }
    }


    private fun handleSubmitButton() {

        if (binding.nameEt.text.isBlank()) {
            binding.errorNameTv.show()
        } else {
            binding.errorNameTv.hide()
        }

        if (binding.addressEt.text.isBlank()) {
            binding.errorAddressTv.show()
        } else {
            binding.errorAddressTv.hide()
        }

        if (binding.dateOfBirthTv.text.isBlank()) {
            binding.errorDateTv.show()
        } else {
            val age =getAge(selected_year,selected_month,selected_day)
            if (age<18)
            {
                binding.errorDateTv.text=getString(R.string.Date_of_birth_must_be_match)
            }
            else {
                binding.errorDateTv.hide()
            }
        }



        val emailText = binding.emailEt.text.toString()
        val notValidEmail = emailText.isNotBlank() and !emailText.isValid(InputType.EMAIL)

        if (binding.emailEt.text.isBlank()) {
            binding.errorEmailTv.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.pink_to_accent_color
                )
            )
            binding.errorEmailTv.text = getString(R.string.email_null_error_text)
        }

        if (notValidEmail) {
            binding.errorEmailTv.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.pink_to_accent_color
                )
            )
            binding.errorEmailTv.text = getString(R.string.email_error_text)
        }

        if (binding.nidEt.text.isBlank()) {
            binding.nidWarningTv.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.pink_to_accent_color
                )
            )
            binding.nidWarningTv.text = getString(R.string.nid_null_error_text)
        }


    }


    private fun openDatePicker() {
        val dpd = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                // Display Selected date in TextView
                selectedDate = ("" + dayOfMonth + "/" + monthOfYear + "/" + year)
                selected_year = year
                selected_month=monthOfYear
                selected_day = dayOfMonth
                binding.dateOfBirthTv.text = selectedDate
            },
            selected_year,
            selected_month,
            selected_day
        )
        dpd.show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }

    private fun showTermsAndConditionDialog() {
        val args = Bundle().apply {
            putString("myTitle", "Terms & Conditions")
            putString("url", mPref.termsAndConditionUrl)
        }
        parentFragment?.parentFragment?.findNavController()
            ?.navigate(R.id.termsAndConditionFragment, args)
    }

    private fun getAge(year: Int, month: Int, day: Int): Int {
        val dob = Calendar.getInstance()
        val today = Calendar.getInstance()
        dob[year, month] = day
        var age = today[Calendar.YEAR] - dob[Calendar.YEAR]
        if (today[Calendar.DAY_OF_YEAR] < dob[Calendar.DAY_OF_YEAR]) {
            age--
        }

        return age
    }

}