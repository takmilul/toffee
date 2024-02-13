package com.banglalink.toffee.ui.payment_methods

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.banglalink.toffee.ui.compose_theme.CardBgColor
import com.banglalink.toffee.ui.compose_theme.CardBgColorDark
import com.banglalink.toffee.ui.compose_theme.CardTitleColor
import com.banglalink.toffee.ui.compose_theme.CardTitleColorDark
import com.banglalink.toffee.ui.compose_theme.Fonts
import com.banglalink.toffee.ui.compose_theme.MainTextColor
import com.banglalink.toffee.ui.compose_theme.MainTextColorDark
import com.banglalink.toffee.ui.compose_theme.ScreenBackground
import com.banglalink.toffee.ui.compose_theme.ScreenBackgroundDark
import com.banglalink.toffee.R
import com.banglalink.toffee.ui.compose_theme.ColorAccent2
import com.banglalink.toffee.ui.compose_theme.RedButtonColor

@Composable
@Preview
fun ManagePaymentMethodsScreen() {
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
        SavedPaymentMethods()
        AddPaymentMethods()
    }
}

@Composable
fun SavedPaymentMethods() {
    var openDialog by remember {
        mutableStateOf(false)
    }
    if (openDialog){
        RemoveAccountDialog(
            onDismissRequest = {
                openDialog = false
            }
        )
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
                        text = "01671109898",
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

@Composable
fun AddPaymentMethods() {
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
@Preview
fun RemoveAccountDialog(onDismissRequest: () -> Unit = {}) {
    var removedSuccess by remember {
        mutableStateOf(false)
    }
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth(),
            backgroundColor = if (isSystemInDarkTheme()) {
                CardBgColorDark
            } else {
                CardBgColor
            }
        ){
            Column (
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 40.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ){
                Icon(
                    modifier = Modifier
                        .size(13 .dp)
                        .clickable {
                            onDismissRequest.invoke()
                        },
                    painter = painterResource(id = R.drawable.ic_close),
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
                    painter = if (removedSuccess){
                        painterResource(id = R.drawable.ic_complete_delete)
                    } else {
                        painterResource(id = R.drawable.ic_purchase_warning)
                    },
                    contentDescription = ""
                )
                Text(
                    modifier = Modifier.padding(top = 16.dp),
                    text = if (removedSuccess){"Account Removed Successfully"} else {"Remove Saved Account"},
                    fontSize = 20.sp,
                    fontFamily = Fonts.roboto,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = if (isSystemInDarkTheme()) CardTitleColorDark else CardTitleColor
                )
                if (!removedSuccess){
                    val annotatedText = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = if (isSystemInDarkTheme()) MainTextColorDark else MainTextColor)) {
                            append("Are you sure you want to remove the saved Nagad account ")
                        }
                        withStyle(style = SpanStyle(color = if (isSystemInDarkTheme()) CardTitleColorDark else CardTitleColor)) {
                            append("01671109898?")
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
                }
                Row(
                    modifier = Modifier
                        .padding(top = 32.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (removedSuccess){
                        PinkOutlinedButton(
                            modifier = Modifier.wrapContentWidth(Alignment.Start),
                            text = "CANCEL",
                            onClick = {
                                onDismissRequest.invoke()
                            }
                        )
                        PinkFilledButton(
                            modifier = Modifier
                                .weight(1F)
                                .padding(start = 16.dp),
                            text = "ADD NEW ACCOUNT",
                            onClick = {
                                onDismissRequest.invoke()
                            }
                        )
                    } else {
                        PinkOutlinedButton(
                            modifier = Modifier.weight(1F),
                            text = "CANCEL",
                            onClick = {
                                onDismissRequest.invoke()
                            }
                        )
                        RedFilledButton(
                            modifier = Modifier
                                .weight(1F)
                                .padding(start = 16.dp),
                            text = "REMOVE",
                            onClick = {
                                removedSuccess = true
                            }
                        )
                    }
                }

            }
        }
    }
}

@Composable
fun PinkOutlinedButton(
    modifier: Modifier = Modifier,
    text: String,
    cornerRadius: Dp = 25.dp,
    onClick: ()-> Unit
){
    OutlinedButton(
        modifier = modifier,
        onClick = { onClick.invoke() },
        border = BorderStroke(color = ColorAccent2, width = 1.dp, ),
        shape = RoundedCornerShape(cornerRadius),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.Transparent,
            contentColor = ColorAccent2
        )
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontFamily = Fonts.roboto,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun RedFilledButton(
    modifier: Modifier = Modifier,
    text: String,
    cornerRadius: Dp = 25.dp,
    onClick: ()-> Unit
){
    Button(
        modifier = modifier,
        onClick = { onClick.invoke() },
        shape = RoundedCornerShape(cornerRadius),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = RedButtonColor,
            contentColor = Color.White
        )
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontFamily = Fonts.roboto,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun PinkFilledButton(
    modifier: Modifier = Modifier,
    text: String,
    cornerRadius: Dp = 25.dp,
    onClick: ()-> Unit
    ){
    Button(
        modifier = modifier,
        onClick = { onClick.invoke() },
        shape = RoundedCornerShape(cornerRadius),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = ColorAccent2,
            contentColor = Color.White
        )
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontFamily = Fonts.roboto,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}