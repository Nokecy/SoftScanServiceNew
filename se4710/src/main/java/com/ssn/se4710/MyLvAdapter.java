package com.ssn.se4710;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hhw.ssn.comutils.LogUtils;
import com.ssn.se4710.dao.Symbology;

import java.util.ArrayList;
import java.util.List;

/**
 * description : DESCRIPTION
 * update : 2019/11/21 16:57,LeiHuang,Init commit
 *
 * @author : LeiHuang
 * @version : VERSION
 */
public class MyLvAdapter extends BaseAdapter {

    private Context mContext;

    private List<Symbology> mSymbologyList = new ArrayList<>();

    public MyLvAdapter(Context context) {
        mContext = context;
    }

    public List<Symbology> getSymbologyList(){
        return mSymbologyList;
    }

    public void addSymbologyList(List<Symbology> symbologyList) {
        mSymbologyList.clear();
        mSymbologyList.addAll(symbologyList);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mSymbologyList.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.layout_codeset_item, null);
            holder = new ViewHolder();
            holder.mTextView = convertView.findViewById(R.id.codeset_tv);
            holder.mCheckBox = convertView.findViewById(R.id.codeset_cb);
            holder.mLl = convertView.findViewById(R.id.codeset_ll);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (position == mSymbologyList.size()){
            holder.mCheckBox.setVisibility(View.GONE);
            holder.mTextView.setText(R.string.code_advance);
            holder.mTextView.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
            holder.mLl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startActivity(new Intent(mContext, CodeParamSetActivity.class));
                }
            });
        } else {
            final View finalConvertView = convertView;
//            holder.mTextView.setText(mSymbologyList.get(position).getName());
            holder.mCheckBox.setVisibility(View.VISIBLE);
            holder.mTextView.setTextColor(mContext.getResources().getColor(R.color.colorTextDefault));
//            holder.mCheckBox.setChecked(mSymbologyList.get(position).getEnabled() == 1);
            holder.mCheckBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.onClick(finalConvertView, position);
                }
            });
        }
        return convertView;
    }

    public OnMyLvItemClickListener mListener;

    public void setListener(OnMyLvItemClickListener listener){
        mListener = listener;
    }

    interface OnMyLvItemClickListener {
        /**
         * ListView的item点击事件回调
         * @param view 被点击的item
         * @param position item的位置
         */
        void onClick(View view, int position);
    }

    static class ViewHolder {
        LinearLayout mLl;
        TextView mTextView;
        CheckBox mCheckBox;
    }
}
