package com.hhw.ssn.cm60;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.dawn.decoderapijni.EngineCode;
import com.dawn.decoderapijni.EngineCodeMenu;
import com.hhw.ssn.combean.ServiceActionKey;
import com.hhw.ssn.comui.BaseApplication;
import com.hhw.ssn.comui.CodeSetItem;
import com.hhw.ssn.comui.CodeSetItemAdapter;
import com.hhw.ssn.comutils.LogUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * description : 码制设置界面，由commonlib通过ARouter调用
 * update : 2019/7/3 17:06,LeiHuang,初始化提交
 *
 * @author : LeiHuang
 * @version : 1.0
 * @date : 2019/7/3 17:06
 */
@Route(path = "/codeset/fragment")
public class CodeSetFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener{

    private static final String TAG = CodeSetFragment.class.getCanonicalName();

    private View mViewContent;

    private ListView lv_codesetItem;
    private List<CodeSetItem> mCodeSetItemList;
    private CodeSetItemAdapter mCodeSetItemAdapter;
    private BaseApplication mApplication;

    public CodeSetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        LogUtils.e(TAG, "onCreateView: ");
        // Inflate the layout for this fragment
        if (mViewContent == null) {
            mViewContent = inflater.inflate(R.layout.fragment_code_set, container, false);
        }
        return mViewContent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        initData();
    }

    public void initView(){
        lv_codesetItem = mViewContent.findViewById(R.id.gv_codeSetMenu);
        lv_codesetItem.setOnItemClickListener(this);
        mApplication = ((BaseApplication) this.getActivity().getApplication());
    }

    public void initData(){
        mCodeSetItemList = new ArrayList<>();
        if(Cm60Service.engineCodeList == null) {
            return;
        }

        for(EngineCode engineCode : Cm60Service.engineCodeList){
            CodeSetItem item = new CodeSetItem(engineCode.getName(),engineCode.getEnable());
            mCodeSetItemList.add(item);
        }

        mCodeSetItemAdapter = new CodeSetItemAdapter(this.getActivity().getApplicationContext(),mCodeSetItemList,this);
        lv_codesetItem.setAdapter(mCodeSetItemAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LogUtils.d(TAG,"onItemClick:" + mCodeSetItemList.get(position).getCodeName());
        String status;
        if(mCodeSetItemList.get(position).getEnable()!= null){
            if(mCodeSetItemList.get(position).getEnable().equals("0")){
                status = "1";
            } else {
                status = "0";
            }
            Cm60Service.engineCodeList.get(position).setEnable(status);
            mCodeSetItemList.get(position).setEnable(status);
            mCodeSetItemAdapter.notifyDataSetChanged();

            Intent configIntent = new Intent(ServiceActionKey.ACTION_SCAN_PARAM);
            configIntent.putExtra("id",mCodeSetItemList.get(position).getCodeName());
            configIntent.putExtra("param", EngineCodeMenu.CodeParam.ENABLE.getParamName());
            configIntent.putExtra("value", status);
            this.getActivity().sendBroadcast(configIntent);
//            Cm60Service.setScanParam(mCodeSetItemList.get(position).getCodeName(),
//                    EngineCodeMenu.CodeParam.ENABLE.getParamName(),status);
        }
    }

    @Override
    public void onClick(View v) {
        LogUtils.d(TAG, "onClick:" + (String) v.getTag());
        Intent paramSetIntent = new Intent(this.getActivity(),ParamSetActivity.class);
        paramSetIntent.putExtra("Id",v.getTag().toString());
        this.getActivity().startActivity(paramSetIntent);
    }

    /**
     *
     * @param isVisibleToUser true if this fragment's UI is currently visible to the user (default),
     *                        false if it is not.
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        LogUtils.e(TAG, "setUserVisibleHint, isVisibleToUser:" + isVisibleToUser);
        if (mApplication != null && isVisibleToUser) {
            mApplication.mSpUtils.putIsInSettingUi(true);
        }
    }
}
