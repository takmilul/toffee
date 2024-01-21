package com.banglalink.toffee.ui.audiobook.carousel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.banglalink.toffee.R
import com.banglalink.toffee.data.network.response.KabbikTopBannerApiResponse.BannerItemBean
import com.banglalink.toffee.util.CoilUtils
import com.google.accompanist.pager.HorizontalPagerIndicator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
fun ImageCarousel(
    sliderList: List<BannerItemBean> = emptyList(),
    onItemClick: ((BannerItemBean) -> Unit)? = null
) {
    var itemIndex = 0
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 1) { Int.MAX_VALUE }
    Box(
        modifier = Modifier.wrapContentSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clickable {
                    onItemClick?.invoke(sliderList[pagerState.currentPage % sliderList.size])
                }
        ) { index ->
            itemIndex = index % sliderList.size
            sliderList[index % sliderList.size].featuredImage?.let {
                Image(
                    modifier = Modifier.aspectRatio(16f / 9f),
                    painter = CoilUtils.getAsyncImagePainter(model = it),
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Transparent,
                            Color.Transparent,
                            Color.Transparent,
                            Color.Transparent,
                            Color.Transparent,
                            Color(0x4D000000),
                            Color(0x80000000),
                            Color(0x99000000),
                            Color(0xB3000000),
                        )
                    )
                )
        )
        Box(
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            HorizontalPagerIndicator(
                pageCount = sliderList.size,
                pagerState = pagerState,
                modifier = Modifier
                    .align(Alignment.Center)
                    .clickable {
                        val currentPage = pagerState.currentPage
                        val totalPages = sliderList.size
                        val nextPage = if (currentPage < totalPages - 1) currentPage + 1 else 0
                        coroutineScope.launch { pagerState.animateScrollToPage(nextPage) }
                    },
                pageIndexMapping = {
                    itemIndex = it % sliderList.size
                    itemIndex
                },
                activeColor = colorResource(id = R.color.colorAccent2),
                inactiveColor = Color.White,
//                indicatorShape = RoundedCornerShape(100.dp)
            )
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