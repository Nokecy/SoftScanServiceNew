package com.hhw.ssn.comui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.hhw.ssn.commonlib.R;

import java.util.List;

public class CodeSetItemAdapter extends BaseAdapter {

    private static final String TAG = CodeSetItemAdapter.class.getCanonicalName();

    private List<CodeSetItem> mList;
    private Context mContext;
    private View.OnClickListener mOnClickListener;

    public CodeSetItemAdapter() {
    }

    public CodeSetItemAdapter(Context mContext, List<CodeSetItem> mList,
                              View.OnClickListener onClickListener) {
        this.mList = mList;
        this.mContext = mContext;
        this.mOnClickListener = onClickListener;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        if (this.mList == null) {
            return null;
        }
        return this.mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(position >= this.mList.size())
            return convertView;
        ViewHolder holder;
        if(convertView == null){
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.codeset_item, null,false);

            holder = new ViewHolder();
            holder.checkBox_enable = convertView.findViewById(R.id.cb_codeset_enable);
            holder.textView_name = convertView.findViewById(R.id.tv_codeset_name);
            holder.button_detail = convertView.findViewById(R.id.btn_details);

            if(this.mOnClickListener != null)
                holder.button_detail.setOnClickListener(this.mOnClickListener);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final CodeSetItem item = mList.get(position);
        if(item.getEnable() != null) {
            boolean cb_status = item.getEnable().equals("1");
            holder.checkBox_enable.setChecked(cb_status);
        }
        holder.textView_name.setText(item.getCodeName());
        holder.button_detail.setTag(item.getCodeName());

        return convertView;
    }

    private static class ViewHolder {
        private CheckBox checkBox_enable;
        private TextView textView_name;
        private Button button_detail;

    }
}
