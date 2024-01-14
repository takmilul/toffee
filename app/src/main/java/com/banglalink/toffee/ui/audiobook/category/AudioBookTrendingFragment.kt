package com.banglalink.toffee.ui.audiobook.category

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.extension.show
import com.banglalink.toffee.ui.audiobook.carousel.PosterDemo
import com.banglalink.toffee.ui.category.movie.MoviesContinueWatchingFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class AudioBookTrendingFragment : AudioBookCategoryBaseFragment<PosterDemo>() {

    override val cardTitle: String = "Trending"

    companion object {
        @JvmStatic
        fun newInstance() = AudioBookTrendingFragment()
    }

    override fun loadContent() {
        val demoImages = listOf<PosterDemo>(
            PosterDemo(
                "https://s3-alpha-sig.figma.com/img/0e08/dfc1/4ffe27ea0c7e170a7e92494f6e0757a3?Expires=1705881600&Key-Pair-Id=APKAQ4GOSFWCVNEHN3O4&Signature=CB6CRyxQOx1ZHBnvRGDzrKLTN3DkDaqvGutZG93DJDV-u1yoyRCGV1MswVqCqRcpjy-ktqvCCZ0bKYBDr0U~SxDuArArsURNvAfBIyrPJddm-hfXWG9IPMwMzGEqBjssaosFfhyoi-BgG848GSm0Feplhgp3FKPH0LjKQd-JVr9mEkXdfmGz6~yleF~-Ualp8uWNbK~Hwr-zJyJt~6QKx7pOpRxj2AFPHnsQLPIePjsKrwkPb9vlYZBVpiwxGbhN8ARNG-CVMQ30Ai-gdBk6EMv~PQP-m5IGA721zNBdsfjkkWcMc7fWafWDywWBOH9NY4EQlek1Tf-WYkE~HV~Kdw__"
            ),
            PosterDemo(
                "https://www.boierduniya.com/drive/2020/02/Base-1-118.jpg"
            ),
            PosterDemo(
                "https://static-01.daraz.com.bd/p/3d8260806fa6d48e79e0dba156dfe58d.jpg_750x750.jpg_.webp"
            ),
            PosterDemo(
                "https://s3-alpha-sig.figma.com/img/0e08/dfc1/4ffe27ea0c7e170a7e92494f6e0757a3?Expires=1705881600&Key-Pair-Id=APKAQ4GOSFWCVNEHN3O4&Signature=CB6CRyxQOx1ZHBnvRGDzrKLTN3DkDaqvGutZG93DJDV-u1yoyRCGV1MswVqCqRcpjy-ktqvCCZ0bKYBDr0U~SxDuArArsURNvAfBIyrPJddm-hfXWG9IPMwMzGEqBjssaosFfhyoi-BgG848GSm0Feplhgp3FKPH0LjKQd-JVr9mEkXdfmGz6~yleF~-Ualp8uWNbK~Hwr-zJyJt~6QKx7pOpRxj2AFPHnsQLPIePjsKrwkPb9vlYZBVpiwxGbhN8ARNG-CVMQ30Ai-gdBk6EMv~PQP-m5IGA721zNBdsfjkkWcMc7fWafWDywWBOH9NY4EQlek1Tf-WYkE~HV~Kdw__"
            ),
            PosterDemo(
                "https://www.boierduniya.com/drive/2020/02/Base-1-118.jpg"
            ),
            PosterDemo(
                "https://static-01.daraz.com.bd/p/3d8260806fa6d48e79e0dba156dfe58d.jpg_750x750.jpg_.webp"
            ),
            PosterDemo(
                "https://s3-alpha-sig.figma.com/img/0e08/dfc1/4ffe27ea0c7e170a7e92494f6e0757a3?Expires=1705881600&Key-Pair-Id=APKAQ4GOSFWCVNEHN3O4&Signature=CB6CRyxQOx1ZHBnvRGDzrKLTN3DkDaqvGutZG93DJDV-u1yoyRCGV1MswVqCqRcpjy-ktqvCCZ0bKYBDr0U~SxDuArArsURNvAfBIyrPJddm-hfXWG9IPMwMzGEqBjssaosFfhyoi-BgG848GSm0Feplhgp3FKPH0LjKQd-JVr9mEkXdfmGz6~yleF~-Ualp8uWNbK~Hwr-zJyJt~6QKx7pOpRxj2AFPHnsQLPIePjsKrwkPb9vlYZBVpiwxGbhN8ARNG-CVMQ30Ai-gdBk6EMv~PQP-m5IGA721zNBdsfjkkWcMc7fWafWDywWBOH9NY4EQlek1Tf-WYkE~HV~Kdw__"
            ),
            PosterDemo(
                "https://www.boierduniya.com/drive/2020/02/Base-1-118.jpg"
            ),
            PosterDemo(
                "https://static-01.daraz.com.bd/p/3d8260806fa6d48e79e0dba156dfe58d.jpg_750x750.jpg_.webp"
            )
        )
        demoImages.let {
            adapter.removeAll()
            adapter.addAll(it)
            showCard(it.isNotEmpty())
        }

        onSeeAllButtonClicked{
            val args = Bundle().apply {
                putString("myTitle", "SeeMore")
            }
            findNavController().navigate(R.id.audioBookCategoryDetails, args)
        }
    }

    override fun onItemClicked(item: PosterDemo) {
        super.onItemClicked(item)
        Toast.makeText(requireContext(), "Item clicked", Toast.LENGTH_SHORT).show()

    }
}