package com.banglalink.toffee.ui.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.banglalink.toffee.R;
import com.banglalink.toffee.model.NavCategory;
import com.banglalink.toffee.model.NavSubcategory;
import com.banglalink.toffee.model.NavigationMenu;

import java.util.List;

public class SecondLevelAdapter extends BaseExpandableListAdapter {
    private final Context mContext;
    private final List<NavCategory> mListDataHeader;
    private final NavigationMenu navigationMenu;
    private final ParentLevelAdapter.OnNavigationItemClickListener onNavigationItemClickListener;

    public SecondLevelAdapter(Context mContext, NavigationMenu navigationMenu, List<NavCategory> navCategory, ParentLevelAdapter.OnNavigationItemClickListener onNavigationItemClickListener) {
        this.mContext = mContext;
        this.mListDataHeader = navCategory;
        this.navigationMenu = navigationMenu;
        this.onNavigationItemClickListener = onNavigationItemClickListener;
    }

    @Override
    public NavSubcategory getChild(int groupPosition, int childPosition) {
        return mListDataHeader.get(groupPosition).getSubCategoryList().get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    class ChildViewHolder{
        TextView name;
        LinearLayout rootView;
    }

    class GroupViewHolder{
        TextView name;
        ImageView stateView;
    }


    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder holder;
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.nav_menu_second_item, parent, false);
            holder = new ChildViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.menu_name);
            holder.rootView = (LinearLayout) convertView.findViewById(R.id.nav_item_root);
            convertView.setTag(holder);
        }
        else{
            holder = (ChildViewHolder) convertView.getTag();
        }
        holder.name.setText(getChild(groupPosition,childPosition).getSubcategoryName());
        holder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onNavigationItemClickListener != null){
                    onNavigationItemClickListener.onSubCategoryClick(getChild(groupPosition,childPosition),getGroup(groupPosition),navigationMenu);
                }
            }
        });
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        try {
            return mListDataHeader.get(groupPosition).getSubCategoryList().size();
        }catch (Exception e){
            return 0;
        }
    }

    @Override
    public NavCategory getGroup(int groupPosition) {
        return mListDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        try {
            return mListDataHeader.size();
        }catch (Exception e){
            return 0;
        }
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        GroupViewHolder groupViewHolder;
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.nav_menu_second_group_item, parent, false);
            groupViewHolder = new GroupViewHolder();
            groupViewHolder.name = (TextView) convertView.findViewById(R.id.menu_name);
            groupViewHolder.stateView = (ImageView) convertView.findViewById(R.id.state_view);
            convertView.setTag(groupViewHolder);
        }
        else{
            groupViewHolder = (GroupViewHolder) convertView.getTag();
        }
        groupViewHolder.name.setText(getGroup(groupPosition).getCategoryName());
        if(getChildrenCount(groupPosition) == 0){
            groupViewHolder.stateView.setVisibility(View.INVISIBLE);
        }
        else{
            groupViewHolder.stateView.setVisibility(View.VISIBLE);
        }
        if(isExpanded){
            groupViewHolder.stateView.setImageResource(R.mipmap.ic_action_collapse);
        }
        else{
            groupViewHolder.stateView.setImageResource(R.mipmap.ic_action_expand);
        }
        return convertView;
    }
}
