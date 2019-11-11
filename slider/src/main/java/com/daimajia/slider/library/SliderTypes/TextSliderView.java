package com.daimajia.slider.library.SliderTypes;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.slider.library.R;

/**
 * This is a slider with a description TextView.
 */
public class TextSliderView extends BaseSliderView{
    private Object data;
    public TextSliderView(Activity context) {
        super(context);
    }
    public TextSliderView(Activity context,Object data) {
        super(context);
        this.data = data;
    }

    public Object getData(){
        return  data;
    }

    @Override
    public View getView() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.render_type_text,null);
        ImageView target = (ImageView)v.findViewById(R.id.daimajia_slider_image);
        TextView description = (TextView)v.findViewById(R.id.description);
        description.setText(getDescription());
        bindEventAndShow(v, target);
        return v;
    }
}
