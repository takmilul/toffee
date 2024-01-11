package com.banglalink.toffee.ui.audiobook.category

import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.ui.common.MyBaseAdapter

class AudioBookCategoryBaseAdapter <T: Any>(listener: ProviderIconCallback<T>) : MyBaseAdapter<T>(listener) {
    override fun getLayoutIdForPosition(position: Int): Int {
        return R.layout.list_item_audio_book_category
    }
}