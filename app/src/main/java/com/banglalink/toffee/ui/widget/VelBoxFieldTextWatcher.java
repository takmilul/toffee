package com.banglalink.toffee.ui.widget;

import android.view.View;
import android.widget.EditText;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.banglalink.toffee.R;

/**
 * Created by MD.TAUFIQUR RAHMAN on 3/5/2017.
 */

public class VelBoxFieldTextWatcher implements View.OnFocusChangeListener {

    @DrawableRes
    private int leftDrawable;
    @DrawableRes
    private int leftDrawableSelected;

    @DrawableRes
    private int rightDrawable;

    private int validLength;

    private EditText editText;
    private FieldType fieldType;


    public VelBoxFieldTextWatcher(@NonNull EditText editText, @DrawableRes int leftDrawable, @DrawableRes int leftDrawableSelected, @DrawableRes int rightDrawable, int validLength) {
        this.validLength = validLength;
        this.leftDrawableSelected = leftDrawable;
        this.rightDrawable = rightDrawable;
    }

    public VelBoxFieldTextWatcher(@NonNull EditText editText, FieldType type) {
        if (type == FieldType.NAME_FIELD) {
            this.validLength = 4;
            this.leftDrawable = R.mipmap.ic_user;
            this.leftDrawableSelected = R.mipmap.ic_user;
            this.rightDrawable = R.mipmap.ic_field_tick;
            this.editText = editText;
            this.fieldType = type;
        } else if (type == FieldType.EMAIL_FIELD) {
            this.leftDrawable = R.mipmap.ic_email;
            this.leftDrawableSelected = R.mipmap.ic_email;
            this.rightDrawable = R.mipmap.ic_field_tick;
            this.editText = editText;
            this.fieldType = type;
        } else if (type == FieldType.ADDRESS_FIELD) {
            this.leftDrawable = R.mipmap.ic_location;
            this.leftDrawableSelected = R.mipmap.ic_location;
            this.rightDrawable = R.mipmap.ic_field_tick;
            this.editText = editText;
            this.fieldType = type;
        }

    }

//    @Override
//    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//        if (s.length() > validLength) {
//            editText.setCompoundDrawablesWithIntrinsicBounds(leftDrawableSelected, 0, rightDrawable, 0);
//        } else {
//            editText.setCompoundDrawablesWithIntrinsicBounds(leftDrawableSelected, 0, 0, 0);
//        }
//    }

//    @Override
//    public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//    }
//
//    @Override
//    public void afterTextChanged(Editable s) {
//
//    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            editText.setCompoundDrawablesWithIntrinsicBounds(leftDrawableSelected, 0, 0, 0);
        } else {
            editText.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, 0, 0, 0);
        }
    }

    public enum FieldType {
        NAME_FIELD(1),
        EMAIL_FIELD(2),
        ADDRESS_FIELD(3);

        private final int levelCode;

        FieldType(int levelCode) {
            this.levelCode = levelCode;
        }

        public int getLevelCode() {
            return this.levelCode;
        }

    }

    private boolean isValid() {
        if (fieldType == FieldType.NAME_FIELD) {
            return editText.getText().length() > validLength ? true : false;
        } else return true;
    }
}
