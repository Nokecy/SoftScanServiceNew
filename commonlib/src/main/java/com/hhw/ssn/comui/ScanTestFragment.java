package com.hhw.ssn.comui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.hhw.ssn.combean.Barcode;
import com.hhw.ssn.combean.ServiceActionKey;
import com.hhw.ssn.commonlib.R;
import com.hhw.ssn.comutils.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author HuangLei 1252065297@qq.com
 * <code>
 * Create At 2019/4/27 11:05
 * Update By 更新者
 * Update At 2019/4/27 11:05
 * </code>
 * 扫描测试界面
 */
public class ScanTestFragment extends Fragment implements View.OnClickListener {

    private final String TAG = ScanTestFragment.class.getSimpleName();

    private View mViewContent;
    private Button mButtonClear;
    private ListView mListView;

    private EpcLvAdapter mAdapter;

    IOSAlertDialog mIosAlertDialog;

    private List<Barcode> mBarcodeList = new ArrayList<>();
    private HashMap<String, Integer> mBarcodeMap = new HashMap<>();

    private LocalBroadcastManager mLbm;

    /**
     * BroadcastReceiver to receiver the scan result
     */
    private BroadcastReceiver mBarcodeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] data = intent.getByteArrayExtra("data");
            byte codeId = intent.getByteExtra("code_id", (byte) 0);
            if (data != null) {
                String barcode = new String(data);
                LogUtils.e(TAG, "Receive data:" + barcode);
                //If the displayed scan results are different from the actual code value, use the following method to convert the scan results
                //String barcode = Tools&Bytes2HexString(data, data.length);
                //first add
                if (mBarcodeMap.isEmpty()) {
//                    mBarcodeSet.add(barcode);
                    Barcode b = new Barcode();
                    b.sn = 1;
                    b.barcode = barcode;
                    b.count = 1;
                    b.codeId = codeId;
                    mBarcodeList.add(b);
                    //list index
                    mBarcodeMap.put(barcode, 0);
                    mAdapter = new EpcLvAdapter(mBarcodeList);
                    mListView.setAdapter(mAdapter);
                } else {
                    if (mBarcodeMap.containsKey(barcode)) {
                        Barcode b = mBarcodeList.get(mBarcodeMap.get(barcode));
                        b.count += 1;
                        mBarcodeList.set(mBarcodeMap.get(barcode), b);
                    } else {
                        Barcode b = new Barcode();
                        b.sn = mBarcodeList.size();
                        b.barcode = barcode;
                        b.codeId = codeId;
                        b.count = 1;
                        mBarcodeList.add(b);
//                        mBarcodeMap.add(barcode);
                        //list index
                        mBarcodeMap.put(barcode, mBarcodeList.size() - 1);
                    }
                }
                mAdapter.notifyDataSetChanged();
            }

        }
    };
    private BaseApplication mApplication;

    public ScanTestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LogUtils.e(TAG, "onCreateView: ");
        // Inflate the layout for this fragment
        if (mViewContent == null) {
            mViewContent = inflater.inflate(R.layout.fragment_scan_test, container, false);
        }
        return mViewContent;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initId();
        initView();
    }

    private void initId(){
        mButtonClear = (Button) mViewContent.findViewById(R.id.button_clear);
        mListView = (ListView) mViewContent.findViewById(R.id.listview_barcode);

        mIosAlertDialog = new IOSAlertDialog(this.getActivity()).builder();
        mApplication = ((BaseApplication) this.getActivity().getApplication());

        mLbm = LocalBroadcastManager.getInstance(this.getActivity());
    }

    private void initView(){
        mButtonClear.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button_clear){
            mIosAlertDialog.setGone().setTitle(this.getActivity().getString(R.string.alert_title_tips)).setMsg(this.getActivity().getString(R.string.alert_title_clear))
                    .setNegativeButton(getString(R.string.alert_btn_confirm), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 清空标签列表
                    if (mBarcodeMap != null) {
                        mBarcodeMap.clear();
                    }
                    if (mBarcodeList != null) {
                        mBarcodeList.clear();
                    }
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }).setPositiveButton(getString(R.string.alert_title_cancel), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                }
            }).show();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mLbm.unregisterReceiver(mBarcodeReceiver);
        LogUtils.e(TAG, "onStop");
//        if (mApplication != null) {
//            mApplication.mSpUtils.putIsInSettingUi(false);
//        }
    }

    @Override
    public void onStart() {
        super.onStart();
        /*
         * Register receiver to receive scan results
         */
        IntentFilter filter = new IntentFilter();
        filter.addAction(ServiceActionKey.ACTION_SCAN_RESULT);
        mLbm.registerReceiver(mBarcodeReceiver, filter);
    }

    /**
     *
     * @param isVisibleToUser true if this fragment's UI is currently visible to the user (default),
     *                        false if it is not.
     */
//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        LogUtils.e(TAG, "setUserVisibleHint, isVisibleToUser:" + isVisibleToUser);
//        if (mApplication != null && isVisibleToUser) {
//            mApplication.mSpUtils.putIsInSettingUi(false);
//        }
//    }
}
