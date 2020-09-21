package com.banglalink.toffee.ui.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;


import com.banglalink.toffee.R;
import com.banglalink.toffee.model.NavCategory;
import com.banglalink.toffee.model.NavSubcategory;
import com.banglalink.toffee.model.NavigationMenu;

import java.util.List;


public class ParentLevelAdapter extends BaseExpandableListAdapter implements ExpandableListView.OnGroupClickListener {
    private final Context mContext;
    private final List<NavigationMenu> mListDataHeader;
    private final OnNavigationItemClickListener onNavigationItemClickListener;

    public ParentLevelAdapter(Context mContext, List<NavigationMenu> mListDataHeader, OnNavigationItemClickListener onNavigationItemClickListener, ExpandableListView listView){
        this.mContext = mContext;
        this.mListDataHeader = mListDataHeader;
        this.onNavigationItemClickListener = onNavigationItemClickListener;
        listView.setOnGroupClickListener(this);
    }

    public void insert(NavigationMenu menu, int position){
        mListDataHeader.add(position,menu);
        notifyDataSetChanged();
    }

    @Override
    public List<NavCategory> getChild(int groupPosition, int childPosition) {
        return mListDataHeader.get(groupPosition).getCategories();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final CustomExpListView secondLevelExpListView = new CustomExpListView(this.mContext);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        secondLevelExpListView.setLayoutParams(params);
        final SecondLevelAdapter secondaryAdapter = new SecondLevelAdapter(this.mContext, getGroup(groupPosition), getChild(groupPosition, childPosition),onNavigationItemClickListener);
        secondLevelExpListView.setAdapter(secondaryAdapter);
        secondLevelExpListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int gPosition, long id) {
                if(onNavigationItemClickListener != null){
                    NavCategory navigationCategory = secondaryAdapter.getGroup(gPosition);
                    onNavigationItemClickListener.onCategoryClick(navigationCategory,getGroup(groupPosition));
                }
                return true;
            }
        });
        secondLevelExpListView.setGroupIndicator(null);
        secondLevelExpListView.setDivider(null);
        return secondLevelExpListView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public NavigationMenu getGroup(int groupPosition) {
        return this.mListDataHeader.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.mListDataHeader.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
//        if(onNavigationItemClickListener != null && getChild(groupPosition,0).size() == 0){
//            onNavigationItemClickListener.onMenuClick(getGroup(groupPosition));
//            return true;
//        }
        if(onNavigationItemClickListener != null){
            onNavigationItemClickListener.onMenuClick(getGroup(groupPosition));
        }
        return true;
//        return (getChild(groupPosition,0).size() == 0);
    }

    static class ViewHolder{
        ImageView icon;
        TextView name;
        View topBorder;
    }
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.nav_menu_item_view, parent, false);
            holder = new ViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.icon);
            holder.name = (TextView) convertView.findViewById(R.id.menu_name);
            holder.topBorder = convertView.findViewById(R.id.top_boarder);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }
        holder.name.setText(getGroup(groupPosition).getName());
        holder.icon.setImageResource(getGroup(groupPosition).getIconResoluteID());
        if(getGroup(groupPosition).getHasTopBorder()){
            holder.topBorder.setVisibility(View.VISIBLE);
        }
        else{
            holder.topBorder.setVisibility(View.GONE);
        }
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public interface OnNavigationItemClickListener{
        void onCategoryClick(NavCategory category, NavigationMenu parent);
        void onSubCategoryClick(NavSubcategory subcategory, NavCategory category, NavigationMenu parent);
        void onMenuClick(NavigationMenu menu);
    }
}
