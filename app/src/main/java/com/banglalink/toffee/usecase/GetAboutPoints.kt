package com.banglalink.toffee.usecase

import com.banglalink.toffee.R
import com.banglalink.toffee.model.AboutPoints
import com.banglalink.toffee.model.AboutPointsBean

class GetAboutPoints {
    suspend fun execute(): AboutPointsBean{
        val aboutPoints = listOf(
            AboutPoints(R.drawable.ic_level_1, "Level 1", "1 - 5,000 Point's"),
            AboutPoints(R.drawable.ic_level_2, "Level 2", "5,001 - 10,000 Point's"),
            AboutPoints(R.drawable.ic_level_3, "Level 3", "10,001 - 20,000 Point's"),
            AboutPoints(R.drawable.ic_level_4, "Level 4", "20,001 - 30,000 Point's"),
            AboutPoints(R.drawable.ic_level_5, "Level 5", "30,001 or more Point's")
        )
        return AboutPointsBean(aboutPoints)
    }
}