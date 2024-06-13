package com.banglalink.toffee.ui.about

import androidx.annotation.ColorRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.banglalink.toffee.R
import com.banglalink.toffee.R.color
import com.banglalink.toffee.R.drawable
import com.banglalink.toffee.R.string

@Composable
fun AboutLogo() {
    Image(
        painter = painterResource(id = drawable.ic_splash_logo),
        contentDescription = "Toffee Logo",
        modifier = Modifier
            .width(120.dp)
            .height(100.dp)
            .padding(top = 24.dp),
    )
}
@Composable
@OptIn(ExperimentalUnitApi::class)
fun TitleText(
    text: String,
    modifier: Modifier = Modifier,
    @ColorRes textColor: Int? = null,
    textSize: Float = 15F,
    textAlign: TextAlign = TextAlign.Start,
) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        fontSize = TextUnit(textSize, TextUnitType.Sp),
        fontFamily = FontFamily(Font(resId = R.font.roboto_medium)),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        color = colorResource(id = textColor ?: color.main_text_color),
        textAlign = textAlign,
    )
}
@Composable
@OptIn(ExperimentalUnitApi::class)
fun BodyText(
    text: String,
    modifier: Modifier = Modifier,
    @ColorRes textColor: Int? = null,
    textSize: Float = 14F,
    lineHeight: Float = 16F,
    textAlign: TextAlign = TextAlign.Start,
) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        fontSize = TextUnit(textSize, TextUnitType.Sp),
        fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
        overflow = TextOverflow.Ellipsis,
        color = colorResource(id = textColor ?: color.main_text_color),
        textAlign = textAlign,
        lineHeight = TextUnit(lineHeight, TextUnitType.Sp),
    )
}

@Composable
fun AboutFeatureTextItem(text: String) {
    Row(horizontalArrangement = Arrangement.Start) {
        Text(
            text = stringResource(id = string.tick),
            modifier = Modifier.wrapContentSize(),
            fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
            color = colorResource(id = color.main_text_color),
        )
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp),
            fontFamily = FontFamily(Font(resId = R.font.roboto_regular)),
            color = colorResource(id = color.main_text_color),
        )
    }
}

@Composable
@OptIn(ExperimentalUnitApi::class)
fun UpdateButton(onClickCheckUpdateButton: () -> Unit) {
    Button(
        modifier = Modifier
            .fillMaxWidth(0.5f)
            .wrapContentHeight()
            .padding(top = 24.dp, bottom = 16.dp),
        shape = MaterialTheme.shapes.medium.copy(all = CornerSize(100.dp)),
        colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.colorAccent2)),
        onClick = { onClickCheckUpdateButton() },
    ) {
        Text(
            text = stringResource(id = string.check_update).uppercase(),
            color = Color.White,
            letterSpacing = TextUnit(0f, TextUnitType.Sp)
        )
    }
}