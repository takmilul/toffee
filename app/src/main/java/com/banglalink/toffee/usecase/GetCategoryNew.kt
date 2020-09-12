package com.banglalink.toffee.usecase

import com.banglalink.toffee.R
import com.banglalink.toffee.data.network.request.NavCategoryRequest
import com.banglalink.toffee.data.network.retrofit.ToffeeApi
import com.banglalink.toffee.data.network.util.tryIO
import com.banglalink.toffee.model.NavCategory
import com.banglalink.toffee.model.NavCategoryGroup
import com.banglalink.toffee.model.NavSubcategory
import com.banglalink.toffee.ui.common.SingleListRepository

class GetCategoryNew(private val toffeeApi: ToffeeApi): SingleListRepository<NavCategory> {

    override suspend fun execute(): List<NavCategory>{
//        val response = tryIO { toffeeApi.getCategory(NavCategoryRequest()) }
        return mutableListOf<NavCategory>().apply {
            add(NavCategory(1, "Movie", listOf(
                NavSubcategory(33, "Movie", "All"),
                NavSubcategory(34, "Movie", "Action"),
                NavSubcategory(35, "Movie", "Romance"),
                NavSubcategory(36, "Movie", "Sci-Fi"),
                NavSubcategory(37, "Movie", "Adventure"),
                NavSubcategory(38, "Movie", "Fantasy"),
                NavSubcategory(39, "Movie", "Comedy")
            ), "#FF6C3E", R.drawable.ic_cat_movie))
            add(NavCategory(2, "Music", null, "#FF53A8", R.drawable.ic_cat_music))
            add(NavCategory(4, "Games", null, "#7974FF", R.drawable.ic_cat_game))
            add(NavCategory(3, "News", null, "#4CDB4C", R.drawable.ic_cat_news))
            add(NavCategory(5, "Podcasts", null, "#FBCC32", R.drawable.ic_cat_podcasts))
            add(NavCategory(6, "Science", null, "#4AE0FF", R.drawable.ic_cat_science))
            add(NavCategory(7, "Fashion", null, "#B93EFF", R.drawable.ic_cat_fashion))
            add(NavCategory(8, "Food", null, "#FF523E", R.drawable.ic_cat_movie))
            add(NavCategory(9, "Drama", null, "#58E4A7", R.drawable.ic_cat_music))
            add(NavCategory(10, "Movie", null, "#FF6C3E", R.drawable.ic_cat_movie))
            add(NavCategory(12, "Music", null, "#FF53A8", R.drawable.ic_cat_music))
            add(NavCategory(14, "Games", null, "#7974FF", R.drawable.ic_cat_game))
            add(NavCategory(13, "News", null, "#4CDB4C", R.drawable.ic_cat_news))
            add(NavCategory(15, "Podcasts", null, "#FBCC32", R.drawable.ic_cat_podcasts))
            add(NavCategory(16, "Science", null, "#4AE0FF", R.drawable.ic_cat_science))
            add(NavCategory(17, "Fashion", null, "#B93EFF", R.drawable.ic_cat_fashion))
            add(NavCategory(18, "Food", null, "#FF523E", R.drawable.ic_cat_movie))
            add(NavCategory(19, "Drama", null, "#58E4A7", R.drawable.ic_cat_music))
        }
    }
}