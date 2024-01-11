package com.banglalink.toffee.ui.audiobook.carousel

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.model.ChannelInfo
import com.banglalink.toffee.ui.common.MyBaseAdapter

class CarouselContentAdapter (val listener: BaseListItemCallback<PosterDemo>): MyBaseAdapter<PosterDemo>(listener) {
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.item_audio_book_carousel
    }
}