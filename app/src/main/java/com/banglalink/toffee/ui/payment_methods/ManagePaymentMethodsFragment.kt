package com.banglalink.toffee.ui.payment_methods

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.data.network.request.AddTokenizedAccountInitRequest
import com.banglalink.toffee.data.network.request.TokenizedPaymentMethodsApiRequest
import com.banglalink.toffee.extension.navigateTo
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.compose_theme.ContentLoader
import com.banglalink.toffee.ui.compose_theme.ScreenBackground
import com.banglalink.toffee.ui.compose_theme.ScreenBackgroundDark
import com.banglalink.toffee.ui.premium.PremiumViewModel
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.usecase.PaymentLogFromDeviceData
import com.banglalink.toffee.util.unsafeLazy
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@AndroidEntryPoint
class ManagePaymentMethodsFragment : BaseFragment() {
    @Inject lateinit var json: Json
    private var paymentName: String = "nagad"
    private var paymentMethodId: Int? = null
    private var transactionIdentifier: String? = null
    private var statusCode: String? = null
    private var statusMessage: String? = null
    private val viewModel by activityViewModels<PremiumViewModel>()
    private val progressDialog by unsafeLazy { ToffeeProgressDialog(requireContext()) }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val isApiResponded = viewModel.isTokenizedPaymentMethodApiRespond.collectAsState(false)
                ManagePaymentMethodsScreen(viewModel, progressDialog, isApiResponded)
            }
        }
    }

    @Composable
    fun ManagePaymentMethodsScreen(
        viewModel: PremiumViewModel,
        progressDialog: ToffeeProgressDialog,
        isApiResponded: State<Boolean?>,
    ) {
        LaunchedEffect(key1 = true, block = {
            observeAddTokenizedAccountInit()
            observeManagePaymentPageReloaded()
            viewModel.getTokenizedPaymentMethods(
                TokenizedPaymentMethodsApiRequest(
                    customerId = mPref.customerId,
                    password = mPref.password
                )
            )
        })
        val data = viewModel.tokenizedPaymentMethodsResponseCompose.observeAsState()

        SaveAccountFailureDialog (
            viewModel = viewModel,
            onTryAgainClick = {
//                        data.value?.nagadBean?.paymentMethodId.let {id->
//                            paymentMethodId = id
//                            addTokenizedAccountInit(paymentMethodId)
//                        }
            },
            onDismissClick = {
                viewModel.isTokenizedAccountInitFailed.value = false
            }
        )

        if(isApiResponded.value == true) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isSystemInDarkTheme()) {
                            ScreenBackgroundDark
                        } else {
                            ScreenBackground
                        }
                    )
            ) {
                data.value?.let {
                    it.nagadBean?.let { nagadBean -> // Tokenized Payment is available for Nagad

                        nagadBean.nagadAccountInfo?.let { nagadAccountInfo -> // Saved account found for Nagad, Showing Account info
                            SavedPaymentMethods(
                                nagadAccountInfo = nagadAccountInfo,
                                viewModel = viewModel,
                                mPref = mPref,
//                                appContext = requireContext(),
                                progressDialog = progressDialog,
                                nagadPaymentInit = {
                                    paymentMethodId = nagadAccountInfo.paymentMethodId
                                    addTokenizedAccountInit(paymentMethodId)
                                }
                            )
                        } ?: run {// No saved account found, Showing Add account section
                            AddPaymentMethods {
                                paymentMethodId = nagadBean.paymentMethodId
                                addTokenizedAccountInit(paymentMethodId)
                            }
                        }
                    }
                }
            }
        } else {
            ContentLoader(
                Modifier
                    .fillMaxWidth()
                    .background(
                        if (isSystemInDarkTheme()) {
                            ScreenBackgroundDark
                        } else {
                            ScreenBackground
                        }
                    )
            )
        }
    }

    private fun addTokenizedAccountInit(paymentMethodId: Int?) {
        val request = AddTokenizedAccountInitRequest(
            customerId = mPref.customerId,
            password = mPref.password,
            is_Bl_Number = if (mPref.isBanglalinkNumber == "true") 1 else 0,
            isPrepaid = if (mPref.isPrepaid) 1 else 0,
            paymentMethodId = paymentMethodId,
            clientType = "MOBILE_APP",
            paymentPurpose = "ECOM_TOKEN_GEN",
        )
        viewModel.getAddTokenizedAccountInit(paymentName, request)
    }

    private fun observeAddTokenizedAccountInit() {
        observe(viewModel.addTokenizedAccountInitLiveData) { it ->
            progressDialog.dismiss() // Dismiss the progress dialog
            when (it) {
                is Resource.Success -> {
                    it.data?.let {
                        transactionIdentifier = it.transactionIdentifierId
                        statusCode = it.statusCode.toString()
                        statusMessage = it.message
                        //Send Log to the Pub/Sub
                        viewModel.sendPaymentLogFromDeviceData(
                            PaymentLogFromDeviceData(
                                id = System.currentTimeMillis() + mPref.customerId,
                                callingApiName = "${paymentName}AddTokenizedAccountInitFromAndroid",
                                paymentMethodId = paymentMethodId!!,
                                paymentMsisdn = null,
                                paymentPurpose = "ECOM_TOKEN_GEN",
                                paymentRefId = if (paymentName == "nagad") transactionIdentifier else null,
                                transactionStatus = statusCode,
                                rawResponse = json.encodeToString(it)
                            )
                        )

                        if (it.statusCode != 200) {
                            requireContext().showToast(it.message.toString())
                            return@observe
                        }
                        val args = bundleOf(
                            "myTitle" to "Manage Payment Methods",
                            "url" to it.webViewUrl,
                            "paymentType" to "nagadAddAccount",
                            "paymentMethodId" to paymentMethodId,
                            "paymentPurpose" to "ECOM_TOKEN_GEN",
                            "isHideBackIcon" to false,
                            "isHideCloseIcon" to true,
                            "isVisibilityHideCloseIcon" to true,
                            "isBkashBlRecharge" to false,
                        )
                        // Navigate to the payment WebView dialog
                        findNavController().navigateTo(R.id.paymentWebViewDialog, args)
                    } ?: requireContext().showToast(getString(R.string.try_again_message))
                }

                is Resource.Failure -> {
                    //Send Log to the Pub/Sub
                    viewModel.sendPaymentLogFromDeviceData(
                        PaymentLogFromDeviceData(
                            id = System.currentTimeMillis() + mPref.customerId,
                            callingApiName = "${paymentName}AddTokenizedAccountInitFromAndroid",
                            paymentMethodId = paymentMethodId!!,
                            paymentMsisdn = null,
                            paymentPurpose = "ECOM_TOKEN_GEN",
                            paymentRefId = if (paymentName == "nagad") transactionIdentifier else null,
                            transactionStatus = statusCode,
                            rawResponse = json.encodeToString(it.error.msg)
                        )
                    )
                    requireContext().showToast(it.error.msg)
                }
            }
        }
    }

    private fun observeManagePaymentPageReloaded() {
        observe(mPref.isManagePaymentPageReloaded) { isReloaded ->
            if (isReloaded == true) {
                viewModel.getTokenizedPaymentMethods(
                    TokenizedPaymentMethodsApiRequest(
                        customerId = mPref.customerId,
                        password = mPref.password
                    )
                )
                mPref.isManagePaymentPageReloaded.value = false
            }
        }
    }

    override fun onDestroyView() {
        viewModel.isTokenizedPaymentMethodApiRespond.value = false
        viewModel.tokenizedPaymentMethodsResponseCompose.value = null
        super.onDestroyView()
    }
}