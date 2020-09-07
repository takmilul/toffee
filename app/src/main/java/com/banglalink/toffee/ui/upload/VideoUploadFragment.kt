package com.banglalink.toffee.ui.upload

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.banglalink.toffee.R
import com.banglalink.toffee.model.ChannelInfo
import kotlinx.android.synthetic.main.video_upload_fragment.*

class VideoUploadFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.video_upload_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        camera_button.setOnClickListener {

        }

        gallary_button.setOnClickListener {

        }
    }
}