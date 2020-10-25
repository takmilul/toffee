package com.banglalink.toffee.ui.common

import com.banglalink.toffee.R
import com.banglalink.toffee.enums.Reaction.*


public fun setReactionIcon(reaction: Int): Int {
    return when (reaction) {
        Like.value -> R.drawable.ic_reaction_like
        Love.value -> R.drawable.ic_reaction_love
        HaHa.value -> R.drawable.ic_reaction_haha
        Wow.value -> R.drawable.ic_reaction_wow
        Sad.value -> R.drawable.ic_reaction_sad
        Angry.value -> R.drawable.ic_reaction_angry
        else -> R.drawable.ic_like_emo
    }
}