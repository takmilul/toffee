package com.banglalink.toffee.ui.audiobook

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.util.Utils
import com.banglalink.toffee.util.Utils.formatPackExpiryDate
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.math.log

class AudioBookLandingFragment<T : Any> : BaseFragment(), ProviderIconCallback<T> {
    private val viewModel by viewModels<AudioBookViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_audio_book_landing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "Kabbik - Audio Book"

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
                    Log.d("KabbikApi", "observeHomeApiResponse: ${it.data.toString()}")
                }
                is Resource.Failure ->{

                }
            }
        }
    }
}