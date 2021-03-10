package com.banglalink.toffee.ui.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.banglalink.toffee.R
import kotlinx.android.synthetic.main.velbox_dialog_layout.view.*

data class VelBoxAlertDialogBuilder(
        var context: Context,
        private var title: String? = null,
        private var text: String? = null,
        private var icon: Int = -1,
        private var positiveButtonTitle: String = "Ok",
        private var negativeButtonTitle: String = "Cancel",
        private var positiveButtonListener: ((d: AlertDialog?) -> Unit)? = null,
        private var negativeButtonListener: ((d: AlertDialog?) -> Unit)? = null
    ) {
        private var dialog: AlertDialog? = null

        fun setTitle(title: String) = apply { this.title = title }
        fun setText(text: String) = apply { this.text = text }
        fun setTitle(title: Int) = apply { setTitle(context.getString(title)) }
        fun setText(text: Int) = apply { setText(context.getString(text)) }

        fun setIconResource(icon: Int) = apply { this.icon = icon }
        fun setPositiveButtonListener(title: String, listener: (d: AlertDialog?)->Unit) = apply {
            this.positiveButtonTitle = title
            this.positiveButtonListener = listener
        }
        fun setNegativeButtonListener(title: String, listener: (d: AlertDialog?)->Unit) = apply {
            this.negativeButtonTitle = title
            this.negativeButtonListener = listener
        }

        fun setPositiveButtonListener(title: Int, listener: (d: AlertDialog?)->Unit) =
            setPositiveButtonListener(context.getString(title), listener)

        fun setNegativeButtonListener(title: Int, listener: (d: AlertDialog?)->Unit) =
            setNegativeButtonListener(context.getString(title), listener)

        fun create(): AlertDialog {
            val dialogBuilder = AlertDialog.Builder(context).apply {
                val view = LayoutInflater.from(context).inflate(R.layout.velbox_dialog_layout, null)
                setView(view)
                if(icon > 0) view.dialog_icon.setImageResource(icon) else { view.dialog_icon.visibility = View.GONE }
                title?.let { view.dialog_title.text = it } ?: run {view.dialog_title.visibility = View.GONE}
                text?.let { view.dialog_text.text = it } ?: run {view.dialog_text.visibility = View.GONE}
                positiveButtonTitle.let { view.dialog_positive_button.text = it }
                negativeButtonTitle.let { view.dialog_negative_button.text = it }

                if(positiveButtonListener == null && negativeButtonListener == null) {
                    view.dialog_buttons.visibility = View.GONE
                }

                positiveButtonListener?.let {
                    view.dialog_positive_button.setOnClickListener { _->
                        it.invoke(dialog)
                    }
                } ?: run {
                    view.dialog_positive_button.visibility = View.GONE
                }

                negativeButtonListener?.let {
                    view.dialog_negative_button.setOnClickListener { _->
                        it.invoke(dialog)
                    }
                } ?: run {
                    view.dialog_negative_button.visibility = View.GONE
                }

                view.close_button.setOnClickListener { dialog?.dismiss() }
            }
            return dialogBuilder.create().apply {
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog = this
            }
        }
    }