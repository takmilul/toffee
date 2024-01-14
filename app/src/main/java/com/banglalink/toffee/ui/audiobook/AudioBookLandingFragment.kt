package com.banglalink.toffee.ui.audiobook

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import coil.load
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.databinding.FragmentAudioBookLandingBinding
import com.banglalink.toffee.databinding.FragmentFireworkBinding
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.audiobook.category.AudioBookCategoryView
import com.banglalink.toffee.ui.widget.FireworkCardView
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.Utils.formatPackExpiryDate
import com.banglalink.toffee.util.unsafeLazy
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.math.log

class AudioBookLandingFragment<T : Any> : BaseFragment(), ProviderIconCallback<T> {
    private val viewModel by viewModels<AudioBookViewModel>()
    private var _binding: FragmentAudioBookLandingBinding? = null
    private val binding get() = _binding!!
    private val progressDialog by unsafeLazy { ToffeeProgressDialog(requireContext()) }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAudioBookLandingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "Kabbik - Audio Book"

        progressDialog.show()

        observeLoginResponse()
        observeHomeApiResponse()

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val date = dateFormat.format(calendar.getTime())

        val result = compareDates(
            fromDate = date,
            toDate = mPref.kabbikTokenExpiryTime
        )

        when (result){
            DateComparisonResult.EARLIER->{
                Log.d("KabbikApi", "EARLIER: ${mPref.kabbikTokenExpiryTime}")
                requireActivity().showToast("Kabbik token isn't expired")

                viewModel.homeApi(mPref.kabbikAccessToken)
            }
            else -> {
                viewModel.login()
            }
        }

    }
    enum class DateComparisonResult {
        EARLIER, LATER, SAME
    }
    private fun compareDates(fromDate: String, toDate: String): DateComparisonResult {
        val parsedFrom = formatPackExpiryDate(fromDate)
        val parsedTo = formatPackExpiryDate(toDate)

        val result = parsedFrom?.compareTo(parsedTo ?: "") ?: 0

        return when {
            result < 0 -> DateComparisonResult.EARLIER
            result > 0 -> DateComparisonResult.LATER
            else -> DateComparisonResult.SAME
        }
    }

    private fun observeLoginResponse() {
        observe(viewModel.loginResponse) {
            when (it) {
                is Resource.Success -> {
                    mPref.kabbikAccessToken = it.data.token.toString()
                    val systemDate =
                        Utils.dateToStr(Utils.getDate(Date().toString(), "yyyy-MM-dd")).toString()
                    mPref.kabbikTokenExpiryTime = systemDate + " " + it.data.expiry
                    Log.d("KabbikApi", "systemDate: $systemDate")
                    Log.d(
                        "KabbikApi",
                        "mPref.kabbikTokenExpiryTime: ${mPref.kabbikTokenExpiryTime}"
                    )
                    Log.d("KabbikApi", "observeLoginResponse: ${it.data.toString()}")
                    requireActivity().showToast("Kabbik API Called")

                    it.data.token?.let { token ->
                        viewModel.homeApi(token)
                    }
                }

                is Resource.Failure -> {
                    Log.d("KabbikApi", "observeLoginResponse: ${it.toString()}")
                }
            }
        }
    }

    private fun observeHomeApiResponse(){
        observe(viewModel.homeApiResponse){
            when(it) {
                is Resource.Success ->{
                    binding.audioBookFragmentContainer.removeAllViews()
                    Log.d("KabbikApi", "observeHomeApiResponse: ${it.data.toString()}")
                    binding.audioBookFragmentContainer.removeAllViews()
                    it.data.data.forEach { kabbikCategory ->
                        if (kabbikCategory.name == "কাব্যিক অরিজিনালস"){
                            // TODO
                        } else {
                            binding.audioBookFragmentContainer.addView(AudioBookCategoryView(requireContext()).apply {
                                setConfiguration(
                                    cardTitle = kabbikCategory.name ?: "",
                                    items = kabbikCategory.itemsData,
                                    onSeeAllClick = {
                                        val bundle = bundleOf(
                                            "myTitle" to kabbikCategory.name
                                        )
                                        findNavController().navigate(R.id.audioBookCategoryDetails, args = bundle)
                                        requireContext().showToast(kabbikCategory.name ?: "")
                                    }
                                )
                            })
                        }
                    }.also {
                        progressDialog.dismiss()
                    }
                }
                is Resource.Failure ->{
                    progressDialog.dismiss()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        progressDialog.dismiss()
    }
}