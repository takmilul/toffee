package com.banglalink.toffee.ui.player;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.banglalink.toffee.R;


/**
 * Created by shantanu on 9/2/16.
 */

public class QualityListAdapter extends ArrayAdapter<Quality> {
    private final int resource;
    private LayoutInflater inflater;
    public QualityListAdapter(Context context, int resource) {
        super(context, resource);
        this.resource = resource;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    class ViewHolder{
        ImageView icon;
        TextView text;
        CheckBox checkBox;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            convertView = inflater.inflate(resource,parent,false);
            holder = new ViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            holder.text = (TextView) convertView.findViewById(R.id.text);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }
        Quality quality = getItem(position);
        holder.text.setText(quality.format);
        if(quality.selected){
            holder.icon.setImageResource(R.drawable.ic_checkmark_holo_light);
        }
        else{
            holder.icon.setImageDrawable(null);
        }
        holder.checkBox.setChecked(quality.selected);
        return convertView;
    }
}
