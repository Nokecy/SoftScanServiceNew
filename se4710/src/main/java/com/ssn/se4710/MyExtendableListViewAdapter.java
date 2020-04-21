package com.ssn.se4710;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hhw.ssn.comutils.LogUtils;
import com.ssn.se4710.dao.Symbology;
import com.ssn.se4710.dao.SymbologyDao;

import java.util.List;

/**
 * description : DESCRIPTION
 * update : 2019/11/25 16:20,LeiHuang,Init commit
 *
 * @author : LeiHuang
 * @version : VERSION
 */
public class MyExtendableListViewAdapter extends BaseExpandableListAdapter {

    private final String TAG = MyExtendableListViewAdapter.class.getSimpleName();

    public String[] groupString;
    public Symbology[][] childString;

    public MyExtendableListViewAdapter(Context context){
        groupString = context.getResources().getStringArray(R.array.symbologies_parent);
        TypedArray array = context.getResources().obtainTypedArray(R.array.symbologies_child);
        childString = new Symbology[array.length()][];
        SymbologyDao symbologyDao = Se4710Application.getInstances().getDaoSession().getSymbologyDao();
        List<Symbology> symbologies = symbologyDao.loadAll();
        int index = 0;
        for (int i = 0; i < array.length(); i++) {
            String[] tempStr = context.getResources().getStringArray(array.getResourceId(i, -1));
            childString[i] = new Symbology[tempStr.length];
            for (int j = 0; j < tempStr.length; j++) {
                childString[i][j] = symbologies.get(index);
                index++;
            }
        }
        array.recycle();
    }

    public void updateChildItem(int groupPosition, int childPostion, Symbology symbology){
        LogUtils.e(TAG, "updateChildItem, symbology:" + symbology.getParamName() + "," + symbology.getParamValue());
        childString[groupPosition][childPostion] = symbology;
    }
    @Override
    // 获取分组的个数
    public int getGroupCount() {
        return groupString.length;
    }

    //获取指定分组中的子选项的个数
    @Override
    public int getChildrenCount(int groupPosition) {
        return childString[groupPosition].length;
    }

    //        获取指定的分组数据
    @Override
    public Object getGroup(int groupPosition) {
        return groupString[groupPosition];
    }

    //获取指定分组中的指定子选项数据
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return childString[groupPosition][childPosition];
    }

    //获取指定分组的ID, 这个ID必须是唯一的
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    //获取子选项的ID, 这个ID必须是唯一的
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    //分组和子选项是否持有稳定的ID, 就是说底层数据的改变会不会影响到它们
    @Override
    public boolean hasStableIds() {
        return true;
    }
    /**
     *
     * 获取显示指定组的视图对象
     *
     * @param groupPosition 组位置
     * @param isExpanded 该组是展开状态还是伸缩状态
     * @param convertView 重用已有的视图对象
     * @param parent 返回的视图对象始终依附于的视图组
     */
// 获取显示指定分组的视图
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder groupViewHolder;
        if (convertView == null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_codeset_parent_item,parent,false);
            groupViewHolder = new GroupViewHolder();
            groupViewHolder.tvTitle = (TextView)convertView.findViewById(R.id.label_group_normal);
            groupViewHolder.img = (ImageView) convertView.findViewById(R.id.label_group_img);
            convertView.setTag(groupViewHolder);
        }else {
            groupViewHolder = (GroupViewHolder)convertView.getTag();
        }
        groupViewHolder.tvTitle.setText(groupString[groupPosition]);
        if (isExpanded){
            groupViewHolder.img.setImageResource(R.drawable.down_arrow_32px);
        } else {
            groupViewHolder.img.setImageResource(R.drawable.play_button_32px);
        }
        return convertView;
    }
    /**
     *
     * 获取一个视图对象，显示指定组中的指定子元素数据。
     *
     * @param groupPosition 组位置
     * @param childPosition 子元素位置
     * @param isLastChild 子元素是否处于组中的最后一个
     * @param convertView 重用已有的视图(View)对象
     * @param parent 返回的视图(View)对象始终依附于的视图组
     * @return
     * @see android.widget.ExpandableListAdapter#getChildView(int, int, boolean, android.view.View,
     *      android.view.ViewGroup)
     */

    //取得显示给定分组给定子位置的数据用的视图
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder childViewHolder;
        if (convertView==null){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_codeset_child_item,parent,false);
            childViewHolder = new ChildViewHolder();
            childViewHolder.tvTitle = (TextView)convertView.findViewById(R.id.expand_child_tv);
            convertView.setTag(childViewHolder);

        }else {
            childViewHolder = (ChildViewHolder) convertView.getTag();
        }
        Context context = parent.getContext();
        childViewHolder.tvTitle.setText(childString[groupPosition][childPosition].getParamName());

        int[] intArray = context.getResources().getIntArray(R.array.length_set_position);
        for (int value : intArray) {
            if (value == groupPosition) {
                if (childPosition == 1 || childPosition == 2) {
                    childViewHolder.tvTitle.append(String.valueOf(childString[groupPosition][childPosition].getParamValue()));
                    return convertView;
                }
            }
        }

        int childItemType = getChildItemType(context, groupPosition, childPosition);
        if (childItemType == 1){
            String[] stringArray = context.getResources().getStringArray(R.array.upc_ean_prefix);
            childViewHolder.tvTitle.append(stringArray[childString[groupPosition][childPosition].getParamValue()]);
        } else if (childItemType == 2){
            String[] stringArray = context.getResources().getStringArray(R.array.inverse_choice);
            childViewHolder.tvTitle.append(stringArray[childString[groupPosition][childPosition].getParamValue()]);
        } else if (childItemType == 3){
            String[] stringArray = context.getResources().getStringArray(R.array.common_level_zero_start);
            childViewHolder.tvTitle.append(stringArray[childString[groupPosition][childPosition].getParamValue()]);
        } else if (childItemType == 4){
            String[] stringArray = context.getResources().getStringArray(R.array.msi_check_digits);
            childViewHolder.tvTitle.append(stringArray[childString[groupPosition][childPosition].getParamValue()]);
        } else if (childItemType == 5){
            String[] stringArray = context.getResources().getStringArray(R.array.gs1_limited_security_level);
            childViewHolder.tvTitle.append(stringArray[childString[groupPosition][childPosition].getParamValue() - 1]);
        } else if (childItemType == 6){
            String[] stringArray = context.getResources().getStringArray(R.array.data_matrix_decode_mirror_images);
            childViewHolder.tvTitle.append(stringArray[childString[groupPosition][childPosition].getParamValue()]);
        } else if (childItemType == 7){
            String[] stringArray = context.getResources().getStringArray(R.array.redundancy_level);
            childViewHolder.tvTitle.append(stringArray[childString[groupPosition][childPosition].getParamValue() - 1]);
        } else if (childItemType == 8){
            String[] stringArray = context.getResources().getStringArray(R.array.intercharacter_gap_size);
            childViewHolder.tvTitle.append(stringArray[childString[groupPosition][childPosition].getParamValue() == 6 ? 0 : 1]);
        } else {
            childViewHolder.tvTitle.append(childString[groupPosition][childPosition].getParamValue() == 1 ? context.getText(R.string.config_on) : context.getText(R.string.config_off));
        }
        return convertView;
    }

    /**
     * 获取需要显示的设置单选框弹窗的类型:
     * 0--启动关闭设置的单选框，1--UPC/EAN的Preamble的设置单选框, 2--Inverse Choice, 3--I 2 of 5 Security Level, 4--MSI Check Digits, 5--GS1 DataBar Limited Security Level, 6--Decode Mirror Images (Data Matrix Only), 7--Redundancy Level, 8--Intercharacter Gap Size
     * 以便显示child item里面的显示值
     */
    private int getChildItemType(Context context, int groupPosition, int childPosition){
        if (groupPosition == 0) {
            int[] flagArray = context.getResources().getIntArray(R.array.upc_ean_prefix_set_position);
            for (int value :
                    flagArray) {
                if (childPosition == value) {
                    return 1;
                }
            }
        }
        int[] inverseArray = context.getResources().getIntArray(R.array.inverse_position);
        for (int value :
                inverseArray) {
            if (groupPosition == value) {
                if (value == 12 && childPosition == 0) {
                    return 2;
                } else if (childPosition == 1){
                    return 2;
                }
            }
        }
        if (groupPosition == 5 && childPosition == 5){
            return 3;
        } else if (groupPosition == 8 && childPosition == 3){
            return 4;
        } else if (groupPosition == 14 && childPosition == 2){
            return 5;
        } else if (groupPosition == 19 && childPosition == 2){
            return 6;
        } else if (groupPosition == 25 && childPosition == 1){
            return 3;
        } else if (groupPosition == 25 && childPosition == 2){
            return 3;
        } else if (groupPosition == 25 && childPosition == 0){
            return 7;
        } else if (groupPosition == 25 && childPosition == 3){
            return 8;
        }
        return 0;
    }

    //指定位置上的子元素是否可选中
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    static class GroupViewHolder {
        TextView tvTitle;
        ImageView img;
    }

    static class ChildViewHolder {
        TextView tvTitle;
    }
}
