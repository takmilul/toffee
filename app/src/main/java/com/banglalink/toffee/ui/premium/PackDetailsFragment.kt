package com.banglalink.toffee.ui.premium

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentLandingTvChannelsBinding
import com.banglalink.toffee.databinding.FragmentPacDetailsBinding
import com.banglalink.toffee.extension.checkVerification
import com.banglalink.toffee.extension.setVisibility
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.home.HomeActivity
import kotlinx.coroutines.launch

class PackDetailsFragment: BaseFragment()  {
    private  var _binding: FragmentPacDetailsBinding?=null
    private val binding get() = _binding!!
    private lateinit var navController: NavController
    private lateinit var navOptions: NavOptions
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        _binding = FragmentPacDetailsBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        changeToolBar()


//        findNavController()?.navigate(R.id.startWatchingDialog)
            with(binding){

                addUploadInfoButton.setOnClickListener{

                    activity?.checkVerification {

                        findNavController().navigate(R.id.bottomSheetPaymentMethods)

                    }

                }

                pacLinearChannels.setOnClickListener{
                    constraintLayout.setVisibility(View.GONE)
                    constraintLayoutPayed.setVisibility(View.GONE)
                    constraintLayoutTrail.setVisibility(View.VISIBLE)
                }
                pacLinearContent.setOnClickListener{
                    constraintLayout.setVisibility(View.GONE)
                    constraintLayoutTrail.setVisibility(View.GONE)
                    constraintLayoutPayed.setVisibility(View.VISIBLE)
                }
            }




    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun changeToolBar(){
        val toolbar = activity?.findViewById<Toolbar>(R.id.toolbar)
        toolbar?.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar?.setNavigationOnClickListener {
            try {

                findNavController().popBackStack()

            } catch (e:Exception){

            }
        }
    }
}