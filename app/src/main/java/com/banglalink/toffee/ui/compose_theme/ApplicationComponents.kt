package com.banglalink.toffee.ui.compose_theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.banglalink.toffee.R

@Composable
fun ProgressLoader(
    modifier: Modifier = Modifier,
) {
    Box(modifier = Modifier.border(BorderStroke(1.5.dp, FixedSecondTextColor), shape = CircleShape).padding(1.dp)){
        AsyncImage(
            model = R.drawable.content_loader,
            contentDescription = "loading...",
            modifier = modifier.size(40.dp),
        )
    }
}

@Preview
@Composable
fun ContentLoader(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.padding(bottom = 56.dp),
        contentAlignment = Alignment.Center
    ) {
        ProgressLoader(Modifier.size(40.dp))
    }
}