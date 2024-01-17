package com.banglalink.toffee.ui.audiobook.category

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.data.network.response.KabbikCategory
import com.banglalink.toffee.data.network.response.KabbikItemBean
import com.banglalink.toffee.ui.audiobook.AudioBookViewModel
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.util.CoilUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AudioBookCategoryFragment: BaseFragment() {
    private val viewModel by viewModels<AudioBookViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

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
    }

    @Composable
    fun AudioBookCategoryComposeScreen(
        viewModel: AudioBookViewModel
    ) {
        Column(
            modifier =  Modifier.padding(bottom = 24.dp)
        ) {

            AudioBookCategory(kabbikCategory = KabbikCategory(
                name = "ট্রেন্ডিং",
                itemsData = listOf(
                    KabbikItemBean(
                        thumbPath = "https://ds.rokomari.store/rokomari110/ProductNew20190903/260X372/Itikaf_Atmogothone_Dibos_Doshok-Sadiq_Farhan-1392d-233805.jpg"
                    ),
                    KabbikItemBean(
                        thumbPath = "https://ds.rokomari.store/rokomari110/ProductNew20190903/260X372/Itikaf_Atmogothone_Dibos_Doshok-Sadiq_Farhan-1392d-233805.jpg"
                    ),
                    KabbikItemBean(
                        thumbPath = "https://ds.rokomari.store/rokomari110/ProductNew20190903/260X372/Itikaf_Atmogothone_Dibos_Doshok-Sadiq_Farhan-1392d-233805.jpg"
                    ),
                    KabbikItemBean(
                        thumbPath = "https://ds.rokomari.store/rokomari110/ProductNew20190903/260X372/Itikaf_Atmogothone_Dibos_Doshok-Sadiq_Farhan-1392d-233805.jpg"
                    ),
                    KabbikItemBean(
                        thumbPath = "https://ds.rokomari.store/rokomari110/ProductNew20190903/260X372/Itikaf_Atmogothone_Dibos_Doshok-Sadiq_Farhan-1392d-233805.jpg"
                    ),
                    KabbikItemBean(
                        thumbPath = "https://ds.rokomari.store/rokomari110/ProductNew20190903/260X372/Itikaf_Atmogothone_Dibos_Doshok-Sadiq_Farhan-1392d-233805.jpg"
                    ),
                    KabbikItemBean(
                        thumbPath = "https://ds.rokomari.store/rokomari110/ProductNew20190903/260X372/Itikaf_Atmogothone_Dibos_Doshok-Sadiq_Farhan-1392d-233805.jpg"
                    ),
                    KabbikItemBean(
                        thumbPath = "https://ds.rokomari.store/rokomari110/ProductNew20190903/260X372/Itikaf_Atmogothone_Dibos_Doshok-Sadiq_Farhan-1392d-233805.jpg"
                    ),
                )
            ))

            AudioBookCategory(kabbikCategory = KabbikCategory(
                name = "নতুন",
                itemsData = listOf(
                    KabbikItemBean(
                        thumbPath = "https://ds.rokomari.store/rokomari110/ProductNew20190903/260X372/Itikaf_Atmogothone_Dibos_Doshok-Sadiq_Farhan-1392d-233805.jpg"
                    ),
                    KabbikItemBean(
                        thumbPath = "https://ds.rokomari.store/rokomari110/ProductNew20190903/260X372/Itikaf_Atmogothone_Dibos_Doshok-Sadiq_Farhan-1392d-233805.jpg"
                    ),
                    KabbikItemBean(
                        thumbPath = "https://ds.rokomari.store/rokomari110/ProductNew20190903/260X372/Itikaf_Atmogothone_Dibos_Doshok-Sadiq_Farhan-1392d-233805.jpg"
                    ),
                    KabbikItemBean(
                        thumbPath = "https://ds.rokomari.store/rokomari110/ProductNew20190903/260X372/Itikaf_Atmogothone_Dibos_Doshok-Sadiq_Farhan-1392d-233805.jpg"
                    ),
                    KabbikItemBean(
                        thumbPath = "https://ds.rokomari.store/rokomari110/ProductNew20190903/260X372/Itikaf_Atmogothone_Dibos_Doshok-Sadiq_Farhan-1392d-233805.jpg"
                    ),
                    KabbikItemBean(
                        thumbPath = "https://ds.rokomari.store/rokomari110/ProductNew20190903/260X372/Itikaf_Atmogothone_Dibos_Doshok-Sadiq_Farhan-1392d-233805.jpg"
                    ),
                    KabbikItemBean(
                        thumbPath = "https://ds.rokomari.store/rokomari110/ProductNew20190903/260X372/Itikaf_Atmogothone_Dibos_Doshok-Sadiq_Farhan-1392d-233805.jpg"
                    ),
                    KabbikItemBean(
                        thumbPath = "https://ds.rokomari.store/rokomari110/ProductNew20190903/260X372/Itikaf_Atmogothone_Dibos_Doshok-Sadiq_Farhan-1392d-233805.jpg"
                    ),
                )
            ))

            AudioBookCategory(kabbikCategory = KabbikCategory(
                name = "ফ্রি অডিওবুক",
                itemsData = listOf(
                    KabbikItemBean(
                        thumbPath = "https://ds.rokomari.store/rokomari110/ProductNew20190903/260X372/Itikaf_Atmogothone_Dibos_Doshok-Sadiq_Farhan-1392d-233805.jpg"
                    ),
                    KabbikItemBean(
                        thumbPath = "https://ds.rokomari.store/rokomari110/ProductNew20190903/260X372/Itikaf_Atmogothone_Dibos_Doshok-Sadiq_Farhan-1392d-233805.jpg"
                    ),
                    KabbikItemBean(
                        thumbPath = "https://ds.rokomari.store/rokomari110/ProductNew20190903/260X372/Itikaf_Atmogothone_Dibos_Doshok-Sadiq_Farhan-1392d-233805.jpg"
                    ),
                    KabbikItemBean(
                        thumbPath = "https://ds.rokomari.store/rokomari110/ProductNew20190903/260X372/Itikaf_Atmogothone_Dibos_Doshok-Sadiq_Farhan-1392d-233805.jpg"
                    ),
                    KabbikItemBean(
                        thumbPath = "https://ds.rokomari.store/rokomari110/ProductNew20190903/260X372/Itikaf_Atmogothone_Dibos_Doshok-Sadiq_Farhan-1392d-233805.jpg"
                    ),
                    KabbikItemBean(
                        thumbPath = "https://ds.rokomari.store/rokomari110/ProductNew20190903/260X372/Itikaf_Atmogothone_Dibos_Doshok-Sadiq_Farhan-1392d-233805.jpg"
                    ),
                    KabbikItemBean(
                        thumbPath = "https://ds.rokomari.store/rokomari110/ProductNew20190903/260X372/Itikaf_Atmogothone_Dibos_Doshok-Sadiq_Farhan-1392d-233805.jpg"
                    ),
                    KabbikItemBean(
                        thumbPath = "https://ds.rokomari.store/rokomari110/ProductNew20190903/260X372/Itikaf_Atmogothone_Dibos_Doshok-Sadiq_Farhan-1392d-233805.jpg"
                    ),
                )
            ))
        }
    }

    @Composable
    fun AudioBookCategory(
        modifier: Modifier = Modifier,
        kabbikCategory: KabbikCategory
    ){
        Column (
            modifier = modifier
                .fillMaxWidth().padding(top = 12.dp)
        ) {
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ){
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
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp)) {
                items(kabbikCategory.itemsData.size) {
                    AudioBookCard (kabbikItemBean = kabbikCategory.itemsData[it])
                }
            }
        }
    }

    @Composable
    fun AudioBookCard(
        modifier: Modifier = Modifier,
        kabbikItemBean: KabbikItemBean
    ) {
        Card(
            shape = RoundedCornerShape(10.dp),
            modifier = modifier
                .padding(end = 8.dp)
                .height(147.dp)
                .width(98.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    painter = CoilUtils.getAsyncImagePainter(
                        model = kabbikItemBean.thumbPath ?: R.drawable.placeholder,
                        placeholder = R.drawable.placeholder
                    ),
                    contentDescription = "image_"
                )
            }
        }
    }
}