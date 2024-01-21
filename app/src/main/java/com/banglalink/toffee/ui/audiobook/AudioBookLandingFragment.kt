package com.banglalink.toffee.ui.audiobook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.R.drawable
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.data.network.response.KabbikCategory
import com.banglalink.toffee.data.network.response.KabbikItemBean
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.audiobook.carousel.ImageCarousel
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeViewModel
import com.banglalink.toffee.ui.compose_theme.CardTitleColorDark
import com.banglalink.toffee.ui.compose_theme.CardTitleColor
import com.banglalink.toffee.ui.compose_theme.Fonts
import com.banglalink.toffee.ui.compose_theme.ScreenBackground
import com.banglalink.toffee.ui.compose_theme.ScreenBackgroundDark
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.util.CoilUtils
import com.banglalink.toffee.util.unsafeLazy

class AudioBookLandingFragment<T : Any> : BaseFragment(), ProviderIconCallback<T> {

    private val viewModel by viewModels<AudioBookViewModel>()
    private val homeViewModel by activityViewModels<HomeViewModel>()
    private val progressDialog by unsafeLazy { ToffeeProgressDialog(requireContext()) }
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AudioBookCategoryComposeScreen(
                    viewModel
                )
            }
        }
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = "Kabbik - Audio Book"
    }
    
    @Composable
    fun AudioBookCategoryComposeScreen(
        viewModel: AudioBookViewModel,
    ) {
        LaunchedEffect(key1 = true) {
            viewModel.grantToken(
                success = { token ->
                    viewModel.topBannerApiCompose(token)
                },
                failure = {}
            )
        }
        LaunchedEffect(key1 = true) {
            viewModel.grantToken(
                success = {
                    viewModel.homeApiCompose(it)
                },
                failure = {}
            )
        }
        
        val categoryList = viewModel.homeApiResponseCompose.value
        var topBannerList = viewModel.topBannerApiResponseCompose.value
        
        if (categoryList.isNotEmpty()) {
            progressDialog.dismiss()
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isSystemInDarkTheme()) {
                            ScreenBackgroundDark
                        } else {
                            ScreenBackground
                        }
                    ),
                state = rememberLazyListState(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                item {
                    topBannerList = topBannerList.filter { it.premium == 0 && it.price == 0 }
                    if (topBannerList.isNotEmpty()) {
                        ImageCarousel(topBannerList) { item->
                            observeAudioBookEpisode()
                            viewModel.getAudioBookEpisode(item.id.toString())
                        }
                    }
                }
                items(categoryList.size) {
                    val category = categoryList[it]
                    if (category.itemsData.containsFree()){
                        AudioBookCategory(kabbikCategory = category)
                    }
                }
                item { 
                    Spacer(modifier = Modifier.size(24.dp))
                }
            }
        }
        if (viewModel.isLoadingCategory.value) {
            progressDialog.show()
        }
    }
    
    @Composable
    fun AudioBookCategory(
        modifier: Modifier = Modifier,
        kabbikCategory: KabbikCategory,
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(top = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = kabbikCategory.name ?: "Unknown",
                    fontSize = 16.sp,
                    fontFamily = Fonts.roboto,
                    fontWeight = FontWeight.Medium,
                    color = if(isSystemInDarkTheme()){ CardTitleColorDark } else { CardTitleColor }
                )
                TextButton(onClick = {
                    val bundle = bundleOf(
                        "myTitle" to kabbikCategory.name
                    )
                    findNavController().navigate(
                        R.id.audioBookCategoryDetails,
                        args = bundle
                    )
                }) {
                    Text(
                        text = "See All",
                        fontSize = 12.sp,
                        fontFamily = Fonts.roboto,
                        fontWeight = FontWeight.Medium,
                        color = if(isSystemInDarkTheme()){ CardTitleColorDark } else { CardTitleColor })
                }
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                state = rememberLazyListState(),
            ) {
                items(kabbikCategory.itemsData.size) {
                    val item = kabbikCategory.itemsData[it]
                    if (item.isFree()){
                        AudioBookCard(kabbikItemBean = item, onclick = {
                            observeAudioBookEpisode()
                            viewModel.getAudioBookEpisode(item.id.toString())
                        })
                    }
                }
            }
        }
    }
    @Composable
    fun AudioBookCard(
        modifier: Modifier = Modifier,
        kabbikItemBean: KabbikItemBean,
        onclick: ()->Unit? = {}
    ) {
        Card(
            shape = RoundedCornerShape(10.dp),
            modifier = modifier
                .padding(end = 8.dp)
                .height(147.dp)
                .width(98.dp)
                .clickable {
                    onclick.invoke()
                }
        ) {
            Image(
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop,
                painter = CoilUtils.getAsyncImagePainter(
                    model = kabbikItemBean.thumbPath ?: drawable.placeholder,
                    placeholder = drawable.placeholder
                ),
                contentDescription = "image_"
            )
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        progressDialog.dismiss()
    }
    
    private fun List<KabbikItemBean>.containsFree(): Boolean {
        return any { it.premium == 0 && it.price == 0 }
    }
    private fun KabbikItemBean.isFree(): Boolean {
        return premium == 0 && price == 0
    }

    private fun observeAudioBookEpisode() {
        observe(viewModel.audioBookEpisodeResponse) { response ->
            when (response) {
                is Resource.Success -> {
                    progressDialog.dismiss()
                    response.data?.let { responseData ->
                        if (responseData.isNotEmpty()) {
                            responseData.firstOrNull()?.let {
                                homeViewModel.playContentLiveData.value = it
                            }
                        } else {
                            progressDialog.dismiss()
                        }
                    }
                }
                is Resource.Failure -> {
                    progressDialog.dismiss()
                }
            }
        }
    }
}