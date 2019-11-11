package com.banglalink.toffee.ui.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListAdapter;

import com.banglalink.toffee.R;

/**
 * Created by shantanu on 9/2/16.
 */

public class ActionMenuList extends AlertDialog.Builder{
    protected ActionMenuList(Context context, final ListAdapter adapter, final DialogInterface.OnClickListener listener) {
        super(context, R.style.DialogTheme);
        setAdapter(adapter,listener);

    }

    @Override
    public AlertDialog show() {
        AlertDialog dialog =  super.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);
        return dialog;
    }
}
