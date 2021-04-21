package com.banglalink.toffee.ui.refer

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.banglalink.toffee.R
import com.banglalink.toffee.databinding.FragmentReferAFriendBinding
import com.banglalink.toffee.extension.action
import com.banglalink.toffee.extension.observe
import com.banglalink.toffee.extension.showToast
import com.banglalink.toffee.extension.snack
import com.banglalink.toffee.model.Resource
import com.banglalink.toffee.ui.common.BaseFragment
import com.banglalink.toffee.ui.widget.VelBoxProgressDialog
import com.banglalink.toffee.util.unsafeLazy


class ReferAFriendFragment : BaseFragment() {

    private var _binding: FragmentReferAFriendBinding?=null
    private val binding get() = _binding!!
    private val viewModel by viewModels<ReferAFriendViewModel>()
    private val progressDialog by unsafeLazy {
        VelBoxProgressDialog(requireContext())
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentReferAFriendBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getMyReferralCode()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun getMyReferralCode() {
        progressDialog.show()
        observe(viewModel.getMyReferralCode()) {
            progressDialog.dismiss()
            when (it) {
                is Resource.Success -> {
                    binding.data = it.data
                    if (it.data.promotionMessage.isNotEmpty()) {
                        binding.policyText.setTextColor(Color.parseColor(it.data.fontColor))
                        binding.policyText.textSize = it.data.fontSize.toFloat()
                        binding.policyText.visibility = View.VISIBLE
                        setSpannableString(it.data.promotionMessage)
                    } else {
                        binding.policyText.visibility = View.GONE
                    }
                    binding.shareBtn.isEnabled = true
                    binding.copyBtn.isEnabled = true

                    setCopyBtnClick(it.data.referralCode)
                    setShareBtnClick(it.data.shareableString)
                }
                is Resource.Failure -> {
                    binding.root.snack(it.error.msg) {
                        action("Retry") {
                            getMyReferralCode()
                        }
                    }
                }
            }
        }
    }

    private fun setSpannableString(msg: String){

        val ss = SpannableString("$msg more")
        val clickableSpan = object : ClickableSpan() {

            private var lastClickTime: Long = 0

            override fun onClick(textView: View) {
                if (SystemClock.elapsedRealtime() - lastClickTime < 1000L) {
                    return
                }
                else binding.data?.let {
                    showReadMoreDialog(it.readMoreMessage)
                }
                lastClickTime = SystemClock.elapsedRealtime()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
                ds.color = ContextCompat.getColor(requireContext(),R.color.com_facebook_blue)
            }
        }
        ss.setSpan(clickableSpan, msg.length + 1, ss.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.fixed_second_text_color)), 0, msg.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding.policyText.text = ss
        binding.policyText.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setShareBtnClick(shareableText: String) {
        binding.shareBtn.setOnClickListener {
            val shareIntent: Intent = ShareCompat.IntentBuilder.from(requireActivity())
                .setType("text/plan")
                .setText(shareableText)
                .intent
            if (shareIntent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(shareIntent)
            }
        }
    }

    private fun setCopyBtnClick(referralCode: String) {
        binding.copyBtn.setOnClickListener {
            try {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(referralCode, referralCode)
                clipboard.setPrimaryClip(clip)
                requireContext().showToast(getString(R.string.copy_to_clipboard))
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    private fun showReadMoreDialog(msg:String ){
        val factory = LayoutInflater.from(requireContext())
        val alertView: View =
            factory.inflate(R.layout.read_more_dialog, null)
        val alertDialog = AlertDialog.Builder(requireContext()).create()
        alertDialog.setView(alertView)
        alertDialog.setCancelable(true)

        alertView.findViewById<View>(R.id.close_iv)
            .setOnClickListener {
                alertDialog.dismiss()
            }

        val message = alertView.findViewById<WebView>(R.id.webview)

        message.loadData(msg, "text/html", "UTF-8")
        message.computeScroll()
        message.isVerticalScrollBarEnabled = true
        message.isHorizontalScrollBarEnabled = true
        alertDialog.show()
    }

//    //For Android 5.0.0 webkit UI bug fix
//    override fun getAssets(): AssetManager {
//        return resources.assets
//    }
}