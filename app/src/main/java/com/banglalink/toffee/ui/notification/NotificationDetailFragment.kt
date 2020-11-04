package com.banglalink.toffee.ui.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.data.database.entities.NotificationInfo
import com.banglalink.toffee.databinding.FragmentNotificationDetailBinding

class NotificationDetailFragment : Fragment(), OnClickListener {
    private var notificationInfo: NotificationInfo? = null
    private lateinit var binding: FragmentNotificationDetailBinding
    companion object {
        @JvmStatic
        fun newInstance() = NotificationDetailFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = NotificationDetailFragmentArgs.fromBundle(requireArguments())
        notificationInfo = args.notificationInfo
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notification_detail, container, false)
        binding.lifecycleOwner = this
        binding.data = notificationInfo
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        findNavController().navigateUp()
    }

}