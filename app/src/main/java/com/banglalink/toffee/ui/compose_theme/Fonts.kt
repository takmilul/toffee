package com.banglalink.toffee.ui.compose_theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.banglalink.toffee.R

object Fonts {
    val roboto = FontFamily(
        Font(R.font.roboto_light, weight = FontWeight.Light),
        Font(R.font.roboto_regular, weight = FontWeight.Normal),
        Font(R.font.roboto_thin, weight = FontWeight.Thin),
        Font(R.font.roboto_medium, weight = FontWeight.Medium),
        Font(R.font.roboto_bold, weight = FontWeight.Bold),
    )
}