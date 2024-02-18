package com.banglalink.toffee.ui.payment_methods

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.fragment.app.activityViewModels
import com.banglalink.toffee.data.network.request.TokenizedPaymentMethodsApiRequest
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.compose_theme.ScreenBackground
import com.banglalink.toffee.ui.compose_theme.ScreenBackgroundDark
import com.banglalink.toffee.ui.premium.PremiumViewModel
import com.banglalink.toffee.R
import timber.log.Timber

class ManagePaymentMethodsFragment : BaseFragment() {
    private val viewModel by activityViewModels<PremiumViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        _binding = FragmentManagePaymentMethodsBinding.inflate(inflater, container, false)
//        return binding.root
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ManagePaymentMethodsScreen(viewModel)
            }
        }
    }

    @Composable
    fun ManagePaymentMethodsScreen(viewModel: PremiumViewModel) {
        LaunchedEffect(key1 = true, block = {
            viewModel.getTokenizedPaymentMethods(
                TokenizedPaymentMethodsApiRequest(
                    customerId = mPref.customerId,
                    password = mPref.password
                )
            )
        })
        val data = viewModel.tokenizedPaymentMethodsResponseCompose.observeAsState()
        Timber.tag("tokenizedPaymentMethodsResponse").d(data.value.toString())
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
                it.nagadBean?.let {nagadBean -> // Tokenized Payment is available for Nagad

                    nagadBean.nagadAccountInfo?.let {nagadAccountInfo-> // Saved account found for Nagad, Showing Account info
                        SavedPaymentMethods(nagadAccountInfo, viewModel, mPref, requireContext())

                    }?: run {// No saved account found, Showing Add account section
                        AddPaymentMethods(
                            onClickNagad = {
                                // TODO API 02
                                Toast.makeText(requireContext(), "Adding Account", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}