package com.banglalink.toffee.ui.useractivities

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.fragment.app.viewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.common.paging.BaseListFragment
import com.banglalink.toffee.common.paging.BaseListItemCallback
import com.banglalink.toffee.model.ChannelInfo
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserActivitiesListFragment: BaseListFragment<ChannelInfo>(),
    BaseListItemCallback<ChannelInfo> {

    override val mViewModel by viewModels<UserActivitiesListViewModel>()
    override val mAdapter by lazy { UserActivitiesListAdapter(this) }

    companion object {
        fun newInstance(): UserActivitiesListFragment {
            return UserActivitiesListFragment()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initTopPanel(view)
    }

    private fun initTopPanel(view: View) {
        val topPanel = view.findViewById<MotionLayout>(R.id.top_panel)
        if(topPanel != null && topPanel is MotionLayout) {
            topPanel.findViewById<ImageButton>(R.id.back_button)?.let {backButton->
                backButton.setOnClickListener {
                    topPanel.transitionToStart()
                }
            }
        }
    }

    override fun getEmptyViewInfo(): Pair<Int, String?> {
        return Pair(R.drawable.ic_activities_empty, "You don't have any activities yet")
    }

    override fun onItemClicked(item: ChannelInfo) {

    }
}