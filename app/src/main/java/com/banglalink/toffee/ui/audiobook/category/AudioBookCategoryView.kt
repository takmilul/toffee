package com.banglalink.toffee.ui.audiobook.category

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.ProviderIconCallback
import com.banglalink.toffee.data.network.response.KabbikItemBean
import com.banglalink.toffee.extension.showToast

class AudioBookCategoryView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defAttrStyle: Int = 0
): ConstraintLayout(context, attrs, defAttrStyle), ProviderIconCallback<KabbikItemBean> {
    private var titleTextView: TextView? = null
    private var seeAllButton: TextView? = null
    private var recyclerView: RecyclerView? = null
    private var mAdapter: AudioBookCategoryViewAdapter? = null

    init {
        View.inflate(context, R.layout.fragment_audio_book_category_base, this)
        titleTextView = findViewById(R.id.titleTextView)
        recyclerView = findViewById(R.id.rvAudioBooks)
        seeAllButton = findViewById(R.id.seeAllButton)

        mAdapter = AudioBookCategoryViewAdapter(this)
        with(recyclerView) {
            this?.adapter = mAdapter
        }
    }

    fun setConfiguration(cardTitle: String, items: List<KabbikItemBean>, onSeeAllClick: ()->Unit = {}){
        titleTextView?.text = cardTitle
        mAdapter?.removeAll()
        val freeItems = items.filter {item->
            item.premium == 0 && item.price == 0
        }
        mAdapter?.addAll(freeItems)

        seeAllButton?.setOnClickListener {
            onSeeAllClick.invoke()
        }
    }

    override fun onItemClicked(item: KabbikItemBean) {
        super.onItemClicked(item)
        context.showToast("Clicked")
    }
}
