package com.ssn.se4710;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.hhw.ssn.comui.BaseApplication;
import com.hhw.ssn.comutils.LogUtils;
import com.hhw.ssn.comutils.RegularUtils;
import com.ssn.se4710.dao.Symbology;

/**
 * description : 码制设置界面，由commonlib通过ARouter调用
 * update : 2019/7/3 17:06,LeiHuang,初始化提交
 * 2019/10/15
 *
 * @author : LeiHuang
 * @version : 1.1
 * @date : 2019/7/3 17:06
 */
@Route(path = "/codeset/fragment")
public class CodeSetFragment extends Fragment {

    private static final String TAG = CodeSetFragment.class.getSimpleName();

    private View mViewContent;

    private ExpandableListView mExpandableListView;
    private BaseApplication mApplication;
    private TextView mTvTips;


    public String[] groupString;
    public String[][] childString;
    private MyExtendableListViewAdapter mMyExtendableListViewAdapter;

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

        initData();
        initView();
    }

    private void initData() {
        groupString = this.getActivity().getResources().getStringArray(R.array.symbologies_parent);
        TypedArray array = this.getActivity().getResources().obtainTypedArray(R.array.symbologies_child);
        childString = new String[array.length()][];
        for (int i = 0; i < array.length(); i++) {
            childString[i] = this.getActivity().getResources().getStringArray(array.getResourceId(i, -1));
        }
        array.recycle();
    }

    public void initView() {
        mTvTips = (TextView) mViewContent.findViewById(R.id.codeset_tips);
        mApplication = ((BaseApplication) this.getActivity().getApplication());
        mExpandableListView = (ExpandableListView) mViewContent.findViewById(R.id.codesetExpand);
        mMyExtendableListViewAdapter = new MyExtendableListViewAdapter(CodeSetFragment.this.getActivity());
        mExpandableListView.setAdapter(mMyExtendableListViewAdapter);
        //设置分组的监听
        mExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                return false;
            }
        });
        //设置子项布局监听
        mExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Symbology symbology = (Symbology) mExpandableListView.getExpandableListAdapter().getChild(groupPosition, childPosition);
                int[] intArray = CodeSetFragment.this.getActivity().getResources().getIntArray(R.array.length_set_position);
                for (int value : intArray) {
                    if (value == groupPosition) {
                        if (childPosition == 1 || childPosition == 2) {
                            showEditDialog(groupPosition, childPosition, symbology);
                            return true;
                        }
                    }
                }
                int dialogType = getDialogType(groupPosition, childPosition);
                showSignalChoiceDialog(groupPosition, childPosition, symbology, dialogType);
                return true;
            }
        });

    }

    /**
     * 获取需要显示的设置单选框弹窗的类型:
     * 0--启动关闭设置的单选框，1--UPC/EAN的Preamble的设置单选框, 2--Inverse Choice, 3--I 2 of 5 Security Level, 4--MSI Check Digits, 5--GS1 DataBar Limited Security Level, 6--Decode Mirror Images (Data Matrix Only), 7--Redundancy Level, 8--Intercharacter Gap Size
     *
     */
    private int getDialogType(int groupPosition, int childPosition){
        if (groupPosition == 0) {
            int[] flagArray = CodeSetFragment.this.getActivity().getResources().getIntArray(R.array.upc_ean_prefix_set_position);
            for (int value :
                    flagArray) {
                if (childPosition == value) {
                    return 1;
                }
            }
        }
        int[] inverseArray = CodeSetFragment.this.getActivity().getResources().getIntArray(R.array.inverse_position);
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

    /**
     * 显示码制设置的单选框
     * @param groupPosition 父布局位置
     * @param childPosition 子布局位置
     * @param symbology 该条目所对应的实体类数据
     * @param type 需要显示的单选框的类型:
     *             0--启动关闭设置的单选框，1--UPC/EAN的Preamble的设置单选框, 2--Inverse Choice, 3--I 2 of 5 Security Level, 4--MSI Check Digits, 5--GS1 DataBar Limited Security Level, 6--Decode Mirror Images (Data Matrix Only), 7--Redundancy Level, 8--Intercharacter Gap Size
     */
    private void showSignalChoiceDialog(final int groupPosition, final int childPosition, final Symbology symbology, final int type) {
        String paramName = symbology.getParamName();
        Integer paramValue = symbology.getParamValue();
        LogUtils.e(TAG, "onChildClick, symbology:" + paramName + "," + paramValue);
        // 通过builder 构建器来构造
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        builder.setTitle(paramName.substring(4));
        int tempItem;
        String[] tempItems;
        if (type == 1) {
            tempItems = this.getActivity().getResources().getStringArray(R.array.upc_ean_prefix);
            tempItem = paramValue;
        } else if (type == 2){
            tempItems = this.getActivity().getResources().getStringArray(R.array.inverse_choice);
            tempItem = paramValue;
        } else if(type == 3){
            tempItems = this.getActivity().getResources().getStringArray(R.array.common_level_zero_start);
            tempItem = paramValue;
        } else if (type == 4){
            tempItems = this.getActivity().getResources().getStringArray(R.array.msi_check_digits);
            tempItem = paramValue;
        } else if (type == 5){
            tempItems = this.getActivity().getResources().getStringArray(R.array.gs1_limited_security_level);
            tempItem = paramValue - 1;
        } else if (type == 6){
            tempItems = this.getActivity().getResources().getStringArray(R.array.data_matrix_decode_mirror_images);
            tempItem = paramValue;
        } else if (type == 7){
            tempItems = this.getActivity().getResources().getStringArray(R.array.redundancy_level);
            tempItem = paramValue - 1;
        } else if (type == 8){
            tempItems = this.getActivity().getResources().getStringArray(R.array.intercharacter_gap_size);
            tempItem = paramValue == 6 ? 0 : 1;
        } else {
            tempItems = this.getActivity().getResources().getStringArray(R.array.single_choice_availability);
            tempItem = paramValue == 1 ? 0 : 1;
        }
        final int checkedItem = tempItem;
        final String[] items = tempItems;
        final int[] selectedItem = new int[]{-1};
        // -1代表默认没有条目被选中
        builder.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                selectedItem[0] = which;
            }
        });
        builder.setPositiveButton(CodeSetFragment.this.getActivity().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (selectedItem[0] != -1) {
                    Toast.makeText(CodeSetFragment.this.getActivity().getApplicationContext(), items[selectedItem[0]], Toast.LENGTH_SHORT).show();
                    if (selectedItem[0] != checkedItem) {
                        LogUtils.e(TAG, "onClick, selected changed");
                        int newParamValue;
                        if (type == 0) {
                            newParamValue = selectedItem[0] == 0 ? 1 : 0;
                        } else if (type == 5 || type == 7) {
                            newParamValue = selectedItem[0] + 1;
                        } else if (type == 8){
                            newParamValue = selectedItem[0] == 0 ? 6 : 10;
                        } else {
                            newParamValue = selectedItem[0];
                        }
                        updateSymbology(symbology, newParamValue, groupPosition, childPosition);
                    }
                }
            }
        });
        // 最后一步 一定要记得 和Toast 一样 show出来
        builder.show();
    }

    /**
     * 码制设置输入框，设置长度
     * @param groupPosition 父布局位置
     * @param childPosition 子布局位置
     * @param symbology 该条目所对应的实体类数据
     */
    private void showEditDialog(final int groupPosition, final int childPosition, final Symbology symbology) {
        String paramName = symbology.getParamName();
        Integer paramValue = symbology.getParamValue();
        LogUtils.e(TAG, "onChildClick, symbology:" + paramName + "," + paramValue);
        final EditText et = new EditText(this.getActivity());
        et.setInputType(InputType.TYPE_CLASS_NUMBER);
        et.setText(String.valueOf(paramValue));
        final AlertDialog alertDialog = new AlertDialog.Builder(this.getActivity()).setTitle(paramName.substring(4))
                .setView(et)
                .setPositiveButton(CodeSetFragment.this.getActivity().getString(R.string.ok), null).create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button btnPositive = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                btnPositive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //按下确定键后的事件
                        String trim = et.getText().toString().trim();
                        int integer = Integer.parseInt(trim);
                        if (!RegularUtils.verifyCodeLength(integer)) {
                            Toast.makeText(CodeSetFragment.this.getActivity().getApplicationContext(), CodeSetFragment.this.getString(R.string.code_len_error), Toast.LENGTH_LONG).show();
                        } else {
                            if (integer != symbology.getParamValue()) {
                                updateSymbology(symbology, integer, groupPosition, childPosition);
                                alertDialog.dismiss();
                            }
                        }
                    }
                });
            }
        });
        alertDialog.show();
    }

    /**
     * 生成一个新的Symbology对象，除paramValue之外，其他成员变量值与原Symbology的值一致
     * 然后更新数据库中的配置表，之后更新扫描头中的码制设置，最后更新界面
     * @param oldSymbology 旧Symbology对象
     * @param newParamValue 新的Symbology的paramValue值
     * @param groupPosition 父布局位置
     * @param childPosition 子布局位置
     * @return 新的Symbology对象
     */
    private void updateSymbology(Symbology oldSymbology, Integer newParamValue, int groupPosition, int childPosition){
        Symbology newSymbology = new Symbology();
        newSymbology.setId(oldSymbology.getId());
        newSymbology.setParamNum(oldSymbology.getParamNum());
        newSymbology.setParamName(oldSymbology.getParamName());
        newSymbology.setParamValue(newParamValue);
        newSymbology.setParamValueDef(oldSymbology.getParamValueDef());
        if (!oldSymbology.getParamValueDef().equals(newParamValue)) {
            newSymbology.setParamNeedSet(true);
        } else {
            newSymbology.setParamNeedSet(false);
        }
        Se4710Application.getInstances().getDaoSession().getSymbologyDao().update(newSymbology);
        mMyExtendableListViewAdapter.updateChildItem(groupPosition, childPosition, newSymbology);
        mExpandableListView.collapseGroup(groupPosition);
        mExpandableListView.expandGroup(groupPosition, false);
        if (Se4710Service.bcr != null) {
            Se4710Service.bcr.setParameter(newSymbology.getParamNum(), newSymbology.getParamValue());
        }
    }

    /**
     * @param isVisibleToUser true if this fragment's UI is currently visible to the user (default),
     *                        false if it is not.
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        LogUtils.e(TAG, "setUserVisibleHint, isVisibleToUser:" + isVisibleToUser);
        if (mApplication != null && isVisibleToUser) {
//            mApplication.mSpUtils.putIsInSettingUi(true);
            if (Se4710Service.bcr == null) {
                mTvTips.setVisibility(View.VISIBLE);
                mExpandableListView.setVisibility(View.GONE);
            } else {
                mTvTips.setVisibility(View.GONE);
                mExpandableListView.setVisibility(View.VISIBLE);
            }
        }
    }
}
