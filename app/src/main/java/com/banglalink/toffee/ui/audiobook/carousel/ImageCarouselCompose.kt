package com.banglalink.toffee.ui.audiobook.carousel

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.banglalink.toffee.data.network.response.KabbikTopBannerApiResponse.BannerItemBean
import com.banglalink.toffee.util.CoilUtils
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun ImageCarousel(
    sliderList: List<BannerItemBean> = emptyList(),
    onItemClick: ((BannerItemBean) -> Unit)? = null
) {
    val pagerState = rememberPagerState(initialPage = 1) { Int.MAX_VALUE }
    Column(modifier = Modifier.wrapContentSize()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 80.dp),
            pageSpacing = 35.dp,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clickable { 
                    onItemClick?.invoke(sliderList[pagerState.currentPage % sliderList.size])
                }
        ) { index ->
            Card(
                border = BorderStroke(2.dp, Color.White),
                modifier = Modifier.padding(2.dp)
            ) {
                Box {
                    sliderList[index % sliderList.size].thumbPath?.let {
                        Image(
                            modifier = Modifier.aspectRatio(31f / 46f),
                            painter = CoilUtils.getAsyncImagePainter(model = it),
                            contentDescription = null,
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
    
    LaunchedEffect(key1 = Unit) {
        repeat(
            times = Int.MAX_VALUE,
            action = {
                delay(4_000)
                try {
                    pagerState.animateScrollToPage(
                        page = pagerState.currentPage + 1
                    )
                } catch (exp: Exception) {
                    exp.printStackTrace()
                }
            }
        )
    }
}