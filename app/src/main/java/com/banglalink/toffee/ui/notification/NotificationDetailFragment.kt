package com.banglalink.toffee.ui.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentNotificationDetailBinding
import com.banglalink.toffee.model.Notification
import com.banglalink.toffee.util.unsafeLazy

class NotificationDetailFragment : Fragment(), OnClickListener {
    private var notification: Notification? = null
    private lateinit var binding: FragmentNotificationDetailBinding
    private val viewModel by unsafeLazy { ViewModelProviders.of(this).get(NotificationDetailViewModel::class.java) }
    
    companion object {
        @JvmStatic
        fun newInstance() = NotificationDetailFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val args = NotificationDetailFragmentArgs.fromBundle(requireArguments())
        notification = args.notification
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notification_detail, container, false)
        binding.lifecycleOwner = this
        binding.viewmodel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backButton.setOnClickListener(this)
        binding.title.text = notification?.title
    }

    override fun onClick(v: View?) {
        findNavController().navigate(R.id.action_notificationDetailFragment_to_notificationDropdownFragment)
    }

}