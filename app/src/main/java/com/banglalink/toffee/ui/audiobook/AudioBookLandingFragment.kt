package com.banglalink.toffee.ui.audiobook

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
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
import com.banglalink.toffee.ui.widget.ToffeeProgressDialog
import com.banglalink.toffee.util.CoilUtils
import com.banglalink.toffee.util.unsafeLazy

class AudioBookLandingFragment<T : Any> : BaseFragment(), ProviderIconCallback<T> {
    
    private val viewModel by activityViewModels<AudioBookViewModel>()
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
            observeAudioBookEpisode()
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
        val topBannerList = viewModel.topBannerApiResponseCompose.value
        
        if (categoryList.isNotEmpty()) {
            progressDialog.dismiss()
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = rememberLazyListState(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
                contentPadding = PaddingValues(bottom = 8.dp),
            ) {
                item {
                    if (topBannerList.isNotEmpty()) {
                        ImageCarousel(topBannerList) { item->
                            viewModel.getAudioBookEpisode(item.id.toString())
                        }
                    }
                }
                items(categoryList.size) {
                    val category = categoryList[it]
                    AudioBookCategory(kabbikCategory = category)
                }
                item { 
                    Spacer(modifier = Modifier.size(4.dp))
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
                Text(text = kabbikCategory.name ?: "Unknown", fontSize = 16.sp, color = MaterialTheme.colors.onPrimary)
                TextButton(onClick = {
                    val bundle = bundleOf(
                        "myTitle" to kabbikCategory.name
                    )
                    findNavController().navigate(
                        R.id.audioBookCategoryDetails,
                        args = bundle
                    )
                }) {
                    Text(text = "See All", fontSize = 12.sp, color = MaterialTheme.colors.onPrimary)
                }
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                state = rememberLazyListState(),
            ) {
                items(kabbikCategory.itemsData.size) {
                    val item = kabbikCategory.itemsData[it]
                    AudioBookCard(kabbikItemBean = item, onclick = {
                        viewModel.getAudioBookEpisode(item.id.toString())
                    })
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