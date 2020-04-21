package com.hhw.ssn.comui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hhw.ssn.combean.Barcode;
import com.hhw.ssn.commonlib.R;

import java.util.List;

/**
 * @author HuangLei 1252065297@qq.com
 * @CreateDate 2019/5/9 14:24
 * @UpdateUser 更新者
 * @UpdateDate 2019/5/9 14:24
 */
public class EpcLvAdapter extends BaseAdapter {

    private List<Barcode> mBarcodeList;

    EpcLvAdapter(List<Barcode> list){
        mBarcodeList = list;
    }

    @Override
    public int getCount() {
        if (mBarcodeList != null) {
            return mBarcodeList.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mBarcodeList != null) {
            return mBarcodeList.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_epclv_item, parent, false);
            holder.tvSn = (TextView) convertView.findViewById(R.id.textView_list_item_id);
            holder.tvBarcode = (TextView) convertView.findViewById(R.id.textView_list_item_barcode);
            holder.tvCount = (TextView) convertView.findViewById(R.id.textView_list_item_count);
            holder.tvCodeID = (TextView) convertView.findViewById(R.id.textView_list_item_code_id);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        if (mBarcodeList != null && !mBarcodeList.isEmpty()) {
            Barcode b = mBarcodeList.get(position);
            holder.tvSn.setText(String.valueOf(b.sn));
            holder.tvBarcode.setText(String.valueOf(b.barcode));
            holder.tvCount.setText(String.valueOf(b.count));
            holder.tvCodeID.setText(String.format("(0x%x)", b.codeId));
        }
        return convertView;
    }

    class ViewHolder {
        TextView tvSn;
        TextView tvBarcode;
        TextView tvCount;
        TextView tvCodeID;
    }
}
