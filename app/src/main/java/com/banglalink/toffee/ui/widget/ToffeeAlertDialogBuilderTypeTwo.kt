package com.banglalink.toffee.ui.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.banglalink.toffee.databinding.VelboxDialogLayoutTypeTwoBinding

data class ToffeeAlertDialogBuilderTypeTwo(
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
        private lateinit var binding: VelboxDialogLayoutTypeTwoBinding

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
                binding = VelboxDialogLayoutTypeTwoBinding.inflate(LayoutInflater.from(context), null, false)
                setView(binding.root)
                if(icon > 0) binding.dialogIcon.setImageResource(icon) else { binding.dialogIcon.visibility = View.GONE }
                title?.let { binding.dialogTitle.text = it } ?: run {binding.dialogTitle.visibility = View.GONE}
                text?.let { binding.dialogText.text = it } ?: run {binding.dialogText.visibility = View.GONE}
                positiveButtonTitle.let { binding.dialogPositiveButton.text = it }
                negativeButtonTitle.let { binding.dialogNegativeButton.text = it }

                if(positiveButtonListener == null && negativeButtonListener == null) {
                    binding.dialogButtons.visibility = View.GONE
                }

                positiveButtonListener?.let {
                    binding.dialogPositiveButton.setOnClickListener { _->
                        it.invoke(dialog)
                    }
                } ?: run {
                    binding.dialogPositiveButton.visibility = View.GONE
                }

                negativeButtonListener?.let {
                    binding.dialogNegativeButton.setOnClickListener { _->
                        it.invoke(dialog)
                    }
                } ?: run {
                    binding.dialogNegativeButton.visibility = View.GONE
                }

                binding.closeButton.setOnClickListener { dialog?.dismiss() }
            }
            return dialogBuilder.create().apply {
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog = this
            }
        }
    }