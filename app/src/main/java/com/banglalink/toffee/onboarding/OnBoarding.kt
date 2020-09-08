package com.banglalink.toffee.onboarding

import android.app.Activity
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.banglalink.toffee.R
import com.banglalink.toffee.showcase_view.MaterialShowcaseSequence
import com.banglalink.toffee.showcase_view.MaterialShowcaseView
import com.banglalink.toffee.showcase_view.ShowcaseTooltip

class OnBoarding(private val context: Activity, private val numberOfTooltip: Int, isSingleUse: Boolean = true) {

    private var sequence: MaterialShowcaseSequence
    private lateinit var targetViewList: Array<View>
    private lateinit var titleList: Array<String>
    private lateinit var contentList: Array<String>

    companion object {
        private const val SHOWCASE_ID = "tooltip example"
    }

    init {
        sequence = if (isSingleUse)
            MaterialShowcaseSequence(context, SHOWCASE_ID, numberOfTooltip)
        else
            MaterialShowcaseSequence(context, numberOfTooltip)
    }

    fun build(targetViews: Array<View>, titles: Array<String>, contents: Array<String>) {
        if (targetViews.size != numberOfTooltip || titles.size != numberOfTooltip || contents.size != numberOfTooltip) {
            Toast.makeText(context, "Number of items ar not same as numberOfTooltip", Toast.LENGTH_SHORT).show()
            return
        }

        targetViewList = targetViews
        titleList = titles
        contentList = contents

        repeat(numberOfTooltip) {
            buildShowcase(targetViewList[it], titleList[it], contentList[it])
        }
        sequence.start()
    }
    
    private fun buildShowcase(target: View, title: String, content: String) {
        val toolTip = ShowcaseTooltip.build(context)
                .corner(30)
                .arrowWidth(36)
                .arrowHeight(46)
        sequence.addSequenceItem(
                MaterialShowcaseView.Builder(context)
                        .setTarget(target)
                        .setToolTip(toolTip)
                        .setTitleText(title)
                        .setContentText(content)
                        .setTooltipMargin(30)
                        .setShapePadding(50)
                        .setDelay(600)
                        .setDismissOnTouch(false)
                        .setMaskColour(ContextCompat.getColor(context, R.color.tooltip_mask))
                        .build()
        )
    }
}