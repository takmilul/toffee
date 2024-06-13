package com.banglalink.toffee.ui.landing

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

class LatestVideosLayoutManager constructor(context: Context, ): LinearLayoutManager(context) {
    override fun supportsPredictiveItemAnimations(): Boolean {
        return false
    }
}