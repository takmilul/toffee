package com.banglalink.toffee.ui.payment_methods

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Observer
import com.banglalink.toffee.ui.compose_theme.CardBgColor
import com.banglalink.toffee.ui.compose_theme.CardBgColorDark
import com.banglalink.toffee.ui.compose_theme.CardTitleColor
import com.banglalink.toffee.ui.compose_theme.CardTitleColorDark
import com.banglalink.toffee.ui.compose_theme.Fonts
import com.banglalink.toffee.ui.compose_theme.MainTextColor
import com.banglalink.toffee.ui.compose_theme.MainTextColorDark
import com.banglalink.toffee.R
import com.banglalink.toffee.data.network.request.RemoveTokenizedAccountApiRequest
import com.banglalink.toffee.data.network.request.TokenizedPaymentMethodsApiRequest
import com.banglalink.toffee.data.network.response.NagadAccountInfo
import com.banglalink.toffee.data.storage.SessionPreference
import com.banglalink.toffee.ui.compose_theme.PinkFilledButton
import com.banglalink.toffee.ui.compose_theme.PinkOutlinedButton
import com.banglalink.toffee.ui.compose_theme.RedFilledButton
import com.banglalink.toffee.ui.compose_theme.ColorAccent2
import com.banglalink.toffee.ui.premium.PremiumViewModel
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import timber.log.Timber

@Composable
fun AddPaymentMethods(
    onClickNagad: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp)
    ) {
        item {
            Text(
                modifier = Modifier.padding(bottom = 12.dp),
                text = "Add Payment Methods".uppercase(),
                fontSize = 12.sp,
                fontFamily = Fonts.roboto,
                fontWeight = FontWeight.Bold,
                color = if (isSystemInDarkTheme()) {
                    MainTextColorDark
                } else {
                    MainTextColor
                }
            )
        }
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(52.dp)
                    .fillMaxWidth()
                    .clickable { onClickNagad.invoke() },
                backgroundColor = if (isSystemInDarkTheme()) {
                    CardBgColorDark
                } else {
                    CardBgColor
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Image(
                        modifier = Modifier
                            .width(56.dp)
                            .height(24.dp),
                        painter = painterResource(id = R.drawable.ic_nagad_logo),
                        contentDescription = ""
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_forward_new),
                        contentDescription = "",
                        tint = if (isSystemInDarkTheme()) {
                            MainTextColorDark
                        } else {
                            MainTextColor
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SavedPaymentMethods(
    nagadAccountInfo: NagadAccountInfo,
    viewModel: PremiumViewModel,
    mPref: SessionPreference,
//    appContext: Context,
    progressDialog: ToffeeProgressDialog,
    nagadPaymentInit: () -> Unit? = {}
) {
    var openDialog by remember {
        mutableStateOf(false)
    }

    if (openDialog) {
        val removeResponse = viewModel.removeTokenizeAccountResponse.observeAsState()

        Dialog(
            onDismissRequest = {
                openDialog = false
                removeResponse.value?.status?.let {
                    viewModel.getTokenizedPaymentMethods(
                        TokenizedPaymentMethodsApiRequest(
                            customerId = mPref.customerId,
                            password = mPref.password
                        )
                    )
                    viewModel.removeTokenizeAccountResponse.value = null
                }
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth().padding(16.dp),
                backgroundColor = if (isSystemInDarkTheme()) {
                    CardBgColorDark
                } else {
                    CardBgColor
                }
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 40.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    Icon(
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                openDialog = false
                                removeResponse.value?.status?.let {
                                    viewModel.getTokenizedPaymentMethods(
                                        TokenizedPaymentMethodsApiRequest(
                                            customerId = mPref.customerId,
                                            password = mPref.password
                                        )
                                    )
                                    viewModel.removeTokenizeAccountResponse.value = null
                                }
                            },
                        painter = painterResource(id = R.drawable.ic_close_small),
                        contentDescription = "",
                        tint = if (isSystemInDarkTheme()) {
                            MainTextColorDark
                        } else {
                            MainTextColor
                        }
                    )
                }
                when (removeResponse.value?.status) {
                    true -> {
                        RemoveAccountSuccessfulCard(
                            onAddAccountClick = {
                                openDialog = false
                                removeResponse.value?.status?.let {
                                    viewModel.getTokenizedPaymentMethods(
                                        TokenizedPaymentMethodsApiRequest(
                                            customerId = mPref.customerId,
                                            password = mPref.password
                                        )
                                    )
                                    viewModel.removeTokenizeAccountResponse.value = null
                                }
                                nagadPaymentInit.invoke()
                            },
                            onCancelClick = {
                                openDialog = false
                                removeResponse.value?.status?.let {
                                    viewModel.getTokenizedPaymentMethods(
                                        TokenizedPaymentMethodsApiRequest(
                                            customerId = mPref.customerId,
                                            password = mPref.password
                                        )
                                    )
                                    viewModel.removeTokenizeAccountResponse.value = null
                                }
                            }
                        )

                    }

                    false -> {
                        RemoveAccountFailureCard(
//                            onTryAgainClick = {
//                                progressDialog.show()
//                                nagadAccountInfo.paymentMethodId?.let {
//                                    viewModel.removeTokenizeAccount(
//                                        paymentMethodId = it,
//                                        body = RemoveTokenizedAccountApiRequest(
//                                            customerId = mPref.customerId,
//                                            password = mPref.password,
//                                            isPrepaid = if (mPref.isPrepaid) 1 else 0,
//                                            clientType = "MOBILE_APP",
//                                            walletNumber = nagadAccountInfo.walletNumber,
//                                            paymentToken = nagadAccountInfo.paymentToken,
//                                            paymentCusId = nagadAccountInfo.paymentCusId,
//                                        ),
//                                        onFailure = { progressDialog.dismiss() },
//                                        onSuccess = { progressDialog.dismiss() }
//                                    )
//                                }
//                            }
                        )
                    }

                    else -> {
                        RemoveAccountCard(
                            walletNumber = nagadAccountInfo.walletNumber,
                            onRemoveClick = {
                                progressDialog.show()
                                nagadAccountInfo.paymentMethodId?.let {
                                    viewModel.removeTokenizeAccount(
                                        paymentMethodId = it,
                                        body = RemoveTokenizedAccountApiRequest(
                                            customerId = mPref.customerId,
                                            password = mPref.password,
                                            isPrepaid = if (mPref.isPrepaid) 1 else 0,
                                            clientType = "MOBILE_APP",
                                            walletNumber = nagadAccountInfo.walletNumber,
                                            paymentToken = nagadAccountInfo.paymentToken,
                                            paymentCusId = nagadAccountInfo.paymentCusId,
                                        ),
                                        onFailure = { progressDialog.dismiss() },
                                        onSuccess = { progressDialog.dismiss() }
                                    )
                                }
                            },
                            onCancelClick = {
                                openDialog = false
                                removeResponse.value?.status?.let {
                                    viewModel.removeTokenizeAccountResponse.value = null
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    LazyColumn(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 16.dp)
    ) {
        item {
            Text(
                modifier = Modifier.padding(bottom = 12.dp),
                text = "SAVED METHODS",
                fontSize = 12.sp,
                fontFamily = Fonts.roboto,
                fontWeight = FontWeight.Bold,
                color = if (isSystemInDarkTheme()) {
                    MainTextColorDark
                } else {
                    MainTextColor
                }
            )
        }
        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(52.dp)
                    .fillMaxWidth(),
                backgroundColor = if (isSystemInDarkTheme()) {
                    CardBgColorDark
                } else {
                    CardBgColor
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Image(
                        modifier = Modifier
                            .width(26.dp)
                            .height(24.dp),
                        painter = painterResource(id = R.drawable.ic_nagad_icon),
                        contentDescription = ""
                    )
                    Text(
                        modifier = Modifier
                            .weight(1F)
                            .padding(start = 12.dp),
                        text = nagadAccountInfo.walletNumber ?: "Unknown",
                        fontSize = 14.sp,
                        fontFamily = Fonts.roboto,
                        fontWeight = FontWeight.Medium,
                        color = if (isSystemInDarkTheme()) {
                            MainTextColorDark
                        } else {
                            MainTextColor
                        }
                    )
                    Text(
                        modifier = Modifier
                            .padding(start = 48.dp)
                            .clickable { openDialog = true },
                        text = "REMOVE",
                        fontSize = 14.sp,
                        fontFamily = Fonts.roboto,
                        fontWeight = FontWeight.Bold,
                        color = ColorAccent2
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun RemoveAccountCard(
    walletNumber: String? = null,
    onRemoveClick: () -> Unit? = {},
    onCancelClick: () -> Unit? = {}
) {
    Column(
        modifier = Modifier
            .padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 32.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            modifier = Modifier
                .width(88.dp)
                .height(88.dp),
            painter = painterResource(id = R.drawable.ic_purchase_warning),
            contentDescription = ""
        )
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = "Remove Saved Account",
            fontSize = 20.sp,
            fontFamily = Fonts.roboto,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = if (isSystemInDarkTheme()) CardTitleColorDark else CardTitleColor
        )
        val annotatedText = buildAnnotatedString {
            withStyle(style = SpanStyle(color = if (isSystemInDarkTheme()) MainTextColorDark else MainTextColor)) {
                append("Are you sure you want to remove the saved Nagad account ")
            }
            withStyle(style = SpanStyle(color = if (isSystemInDarkTheme()) CardTitleColorDark else CardTitleColor)) {
                append("$walletNumber?")
            }
        }

        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = annotatedText,
            fontSize = 14.sp,
            fontFamily = Fonts.roboto,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            color = if (isSystemInDarkTheme()) {
                MainTextColorDark
            } else {
                MainTextColor
            }
        )
        Row(
            modifier = Modifier
                .padding(top = 32.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PinkOutlinedButton(
                modifier = Modifier.weight(1F),
                text = "CANCEL",
                onClick = {
                    onCancelClick.invoke()
                }
            )
            RedFilledButton(
                modifier = Modifier
                    .weight(1F)
                    .padding(start = 16.dp),
                text = "REMOVE",
                onClick = {
                    onRemoveClick.invoke()
                }
            )
        }

    }
}

@Preview
@Composable
fun RemoveAccountSuccessfulCard(
    onAddAccountClick: () -> Unit? = {},
    onCancelClick: () -> Unit? = {}
) {
    Column(
        modifier = Modifier
            .padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 32.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            modifier = Modifier
                .width(88.dp)
                .height(88.dp),
            painter = painterResource(id = R.drawable.ic_complete_delete),
            contentDescription = ""
        )
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = "Account Removed Successfully",
            fontSize = 20.sp,
            fontFamily = Fonts.roboto,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = if (isSystemInDarkTheme()) CardTitleColorDark else CardTitleColor
        )

        Row(
            modifier = Modifier
                .padding(top = 32.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PinkOutlinedButton(
                modifier = Modifier.weight(1F).padding(end = 16.dp),
                text = "CANCEL",
                onClick = {
                    onCancelClick.invoke()
                }
            )
            PinkFilledButton(
                modifier = Modifier
                    .weight(1F),
                text = "ADD NEW ACCOUNT",
                onClick = {
                    onAddAccountClick.invoke()
                }
            )
        }

    }
}

@Preview
@Composable
fun RemoveAccountFailureCard(
    onTryAgainClick: () -> Unit? = {}
) {
    Column(
        modifier = Modifier
            .padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 32.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            modifier = Modifier
                .width(88.dp)
                .height(88.dp),
            painter = painterResource(id = R.drawable.ic_purchase_warning),
            contentDescription = ""
        )
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = "Failed to Remove Nagad Account",
            fontSize = 20.sp,
            fontFamily = Fonts.roboto,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = if (isSystemInDarkTheme()) CardTitleColorDark else CardTitleColor
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "Your Nagad account could not be removed due to a technical error. Please try again.",
            fontSize = 14.sp,
            fontFamily = Fonts.roboto,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            color = if (isSystemInDarkTheme()) {
                MainTextColorDark
            } else {
                MainTextColor
            }
        )

        Row(
            modifier = Modifier
                .padding(top = 32.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PinkFilledButton(
                modifier = Modifier.defaultMinSize(minWidth = 135.dp),
                text = "TRY AGAIN",
                onClick = {
                    onTryAgainClick.invoke()
                }
            )
        }

    }
}


@Composable
fun SaveAccountFailureDialog(
    viewModel: PremiumViewModel,
    onTryAgainClick: () -> Unit = {},
    onDismissClick: () -> Unit = {}
) {
    var openDialog by remember {
        mutableStateOf(false)
    }
    DisposableEffect(key1 = viewModel.isTokenizedAccountInitFailed.value) {
        val observer = Observer<Boolean?> { isFailed ->
            Timber.tag("DILG").d("isFailedAddingAccount changed: $isFailed")
            isFailed?.let {
                openDialog = isFailed
            }
        }
        viewModel.isTokenizedAccountInitFailed.observeForever(observer)

        onDispose {
            openDialog = false
            viewModel.isTokenizedAccountInitFailed.removeObserver(observer)
        }
    }

    if (openDialog){
        Dialog(
            onDismissRequest = {
                openDialog = false
                onDismissClick.invoke()
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth().padding(16.dp),
                backgroundColor = if (isSystemInDarkTheme()) {
                    CardBgColorDark
                } else {
                    CardBgColor
                }
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 40.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    Icon(
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                openDialog = false
                                onDismissClick.invoke()
                            },
                        painter = painterResource(id = R.drawable.ic_close_small),
                        contentDescription = "",
                        tint = if (isSystemInDarkTheme()) {
                            MainTextColorDark
                        } else {
                            MainTextColor
                        }
                    )
                }
                Column(
                    modifier = Modifier
                        .padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 32.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Image(
                        modifier = Modifier
                            .width(88.dp)
                            .height(88.dp),
                        painter = painterResource(id = R.drawable.ic_purchase_warning),
                        contentDescription = ""
                    )
                    Text(
                        modifier = Modifier.padding(top = 16.dp),
                        text = "Failed to Save Nagad Account",
                        fontSize = 20.sp,
                        fontFamily = Fonts.roboto,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        color = if (isSystemInDarkTheme()) CardTitleColorDark else CardTitleColor
                    )
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = "Your Nagad account could not be saved due to a technical error. Please try again.",
                        fontSize = 14.sp,
                        fontFamily = Fonts.roboto,
                        fontWeight = FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        color = if (isSystemInDarkTheme()) {
                            MainTextColorDark
                        } else {
                            MainTextColor
                        }
                    )

                    Row(
                        modifier = Modifier
                            .padding(top = 32.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PinkFilledButton(
                            modifier = Modifier.defaultMinSize(minWidth = 135.dp),
                            text = "TRY AGAIN",
                            onClick = {
                                openDialog = false
                                onDismissClick.invoke()
                                onTryAgainClick.invoke()
                            }
                        )
                    }

                }
            }
        }
    }
}