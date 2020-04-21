package com.ssn.se4710;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.hhw.ssn.comui.BaseApplication;
import com.hhw.ssn.comui.CodeSetItem;
import com.hhw.ssn.comui.CodeSetItemAdapter;
import com.hhw.ssn.comutils.LogUtils;
import com.ssn.se4710.dao.Symbology;
import com.ssn.se4710.dao.SymbologyDao;

import java.util.ArrayList;
import java.util.List;

/**
 * description : 码制设置界面，由commonlib通过ARouter调用
 * update : 2019/7/3 17:06,LeiHuang,初始化提交
 *          2019/10/15 
 *
 * @author : LeiHuang
 * @version : 1.1
 * @date : 2019/7/3 17:06
 */
public class CodeSetFragmentBank extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener{

    private static final String TAG = CodeSetFragmentBank.class.getSimpleName();

    private View mViewContent;

    private ListView lv_codesetItem;
    private List<CodeSetItem> mCodeSetItemList;
    private CodeSetItemAdapter mCodeSetItemAdapter;
    private BaseApplication mApplication;
    private TextView mTvTips;

    public CodeSetFragmentBank() {
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

        mTvTips = (TextView) mViewContent.findViewById(R.id.codeset_tips);
        lv_codesetItem = mViewContent.findViewById(R.id.gv_codeSetMenu);

        initView();
        initData();
    }

    public void initView(){
        lv_codesetItem.setOnItemClickListener(this);
        final MyLvAdapter myLvAdapter = new MyLvAdapter(this.getActivity());
        final SymbologyDao symbologyDao = Se4710Application.getInstances().getDaoSession().getSymbologyDao();
        myLvAdapter.setListener(new MyLvAdapter.OnMyLvItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                if (Se4710Service.bcr != null){
                    LogUtils.e(TAG, "onItemClick, set paramNum:");
                    MyLvAdapter.ViewHolder holder = (MyLvAdapter.ViewHolder) view.getTag();
                    boolean checked = holder.mCheckBox.isChecked();
                    Symbology symbology = myLvAdapter.getSymbologyList().get(position);
                    int paramNum = Math.toIntExact(symbology.getParamNum());
                    LogUtils.e(TAG, "onItemClick, set paramNum:" + paramNum);
                    int enabled = checked ? 1 : 0;
                    LogUtils.e(TAG, "onItemClick, set paramNum enabled" + enabled);
                    Se4710Service.bcr.setParameter(paramNum, enabled);
//                    symbology.setEnabled(enabled);
                    symbologyDao.update(symbology);
                } else {
                    LogUtils.e(TAG, "onItemClick, Se4710Service.bcr == null ");
                }
            }
        });
        myLvAdapter.addSymbologyList(symbologyDao.loadAll());
        lv_codesetItem.setAdapter(myLvAdapter);
        mApplication = ((BaseApplication) this.getActivity().getApplication());
    }

    public void initData(){
        mCodeSetItemList = new ArrayList<>();
//        if(Cm60Service.engineCodeList == null) {
//            return;
//        }
//
//        for(EngineCode engineCode : Cm60Service.engineCodeList){
//            CodeSetItem item = new CodeSetItem(engineCode.getName(),engineCode.getEnable());
//            mCodeSetItemList.add(item);
//        }
//
//        mCodeSetItemAdapter = new CodeSetItemAdapter(this.getActivity().getApplicationContext(),mCodeSetItemList,this);
//        lv_codesetItem.setAdapter(mCodeSetItemAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        LogUtils.d(TAG,"onItemClick:" + mCodeSetItemList.get(position).getCodeName());
//        String status;
//        if(mCodeSetItemList.get(position).getEnable()!= null){
//            if(mCodeSetItemList.get(position).getEnable().equals("0")){
//                status = "1";
//            } else {
//                status = "0";
//            }
//            Cm60Service.engineCodeList.get(position).setEnable(status);
//            mCodeSetItemList.get(position).setEnable(status);
//            mCodeSetItemAdapter.notifyDataSetChanged();
//
//            Intent configIntent = new Intent(ServiceActionKey.ACTION_SCAN_PARAM);
//            configIntent.putExtra("id",mCodeSetItemList.get(position).getCodeName());
//            configIntent.putExtra("param", EngineCodeMenu.CodeParam.ENABLE.getParamName());
//            configIntent.putExtra("value", status);
//            this.getActivity().sendBroadcast(configIntent);
//            Cm60Service.setScanParam(mCodeSetItemList.get(position).getCodeName(),
//                    EngineCodeMenu.CodeParam.ENABLE.getParamName(),status);
//        }
    }

    @Override
    public void onClick(View v) {
//        LogUtils.d(TAG, "onClick:" + (String) v.getTag());
//        Intent paramSetIntent = new Intent(this.getActivity(),ParamSetActivity.class);
//        paramSetIntent.putExtra("Id",v.getTag().toString());
//        this.getActivity().startActivity(paramSetIntent);
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
//            mApplication.mSpUtils.putIsInSettingUi(true);
            if (Se4710Service.bcr == null){
                mTvTips.setVisibility(View.VISIBLE);
                lv_codesetItem.setVisibility(View.GONE);
            } else {
                mTvTips.setVisibility(View.GONE);
                lv_codesetItem.setVisibility(View.VISIBLE);
            }
        }
    }
}
