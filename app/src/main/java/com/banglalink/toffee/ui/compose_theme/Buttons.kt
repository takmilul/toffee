package com.banglalink.toffee.ui.compose_theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.banglalink.toffee.ui.compose_theme.ColorAccent2
import com.banglalink.toffee.ui.compose_theme.Fonts
import com.banglalink.toffee.ui.compose_theme.RedButtonColor

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
        border = BorderStroke(color = ColorAccent2, width = 1.dp),
        shape = RoundedCornerShape(cornerRadius),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.Transparent,
            contentColor = ColorAccent2
        ),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 9.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontFamily = Fonts.roboto,
            fontWeight = FontWeight.Bold,
            letterSpacing = TextUnit(0.25F, TextUnitType.Sp)
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
        ),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 9.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontFamily = Fonts.roboto,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            letterSpacing = TextUnit(0.25F, TextUnitType.Sp)
        )
    }
}

@Composable
fun PinkFilledButton(
    modifier: Modifier = Modifier,
    text: String,
    cornerRadius: Dp = 25.dp,
    onClick: ()-> Unit
) {
    Button(
        modifier = modifier,
        onClick = { onClick.invoke() },
        shape = RoundedCornerShape(cornerRadius),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = ColorAccent2,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 9.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontFamily = Fonts.roboto,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            letterSpacing = TextUnit(0.25F, TextUnitType.Sp)
        )
    }
}