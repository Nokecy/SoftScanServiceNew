package com.hhw.ssn.cm60;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.dawn.decoderapijni.EngineCode;
import com.dawn.decoderapijni.EngineCodeMenu;
import com.dawn.decoderapijni.SoftEngine;
import com.hhw.ssn.combean.ServiceActionKey;
import com.hhw.ssn.comui.TopToolbar;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ParamSetActivity extends Activity implements View.OnClickListener {

    private static final String TAG = ParamSetActivity.class.getCanonicalName();

    private TopToolbar topToolbar;
    private TextView tv_minlen;
    private TextView tv_maxlen;
    private EditText et_minlen;
    private EditText et_maxlen;

    private CheckBox cb_Check;
    private CheckBox cb_TrsmtChkChar;
    private CheckBox cb_Digit2;
    private CheckBox cb_Digit5;
    private CheckBox cb_AddonRequired;
    private CheckBox cb_TrsmtStasrtStop;
    private CheckBox cb_FullAscii;
    private CheckBox cb_BitCode32Prech;
    private CheckBox cb_Code32SpecEdit;
    private CheckBox cb_Code32TrsmtChkChar;
    private CheckBox cb_Code32TrsmtStasrtStop;
    private CheckBox cb_TrsmtSysDigit;
    private CheckBox cb_MsgToupca;
    private CheckBox cb_Tdoupca;
    private CheckBox cb_Uscode;

    private CheckBox cb_Gs1Coupon;
    private CheckBox cb_ReqCoupon;
    private CheckBox cb_Coupon;
    private CheckBox cb_CloseSysChar;

    private Button btn_save_param;

    private EngineCode paramEngineCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_param_set);
        initView(getIntent().getStringExtra("Id"));
        initData();
    }

    public void initView(String title) {
        paramEngineCode = new EngineCode();
        paramEngineCode.setName(title);

        tv_minlen = findViewById(R.id.tv_min_len);
        tv_maxlen = findViewById(R.id.tv_max_len);
        et_minlen = findViewById(R.id.et_min_len);
        et_maxlen = findViewById(R.id.et_max_len);

        cb_Check = findViewById(R.id.cb_Check);
        cb_Check.setOnClickListener(this);
        cb_TrsmtChkChar = findViewById(R.id.cb_TrsmtChkChar);
        cb_TrsmtChkChar.setOnClickListener(this);
        cb_Digit2 = findViewById(R.id.cb_Digit2);
        cb_Digit2.setOnClickListener(this);
        cb_Digit5 = findViewById(R.id.cb_Digit5);
        cb_Digit5.setOnClickListener(this);
        cb_AddonRequired = findViewById(R.id.cb_AddonRequired);
        cb_AddonRequired.setOnClickListener(this);
        cb_TrsmtStasrtStop = findViewById(R.id.cb_TrsmtStasrtStop);
        cb_TrsmtStasrtStop.setOnClickListener(this);
        cb_FullAscii = findViewById(R.id.cb_FullAscii);
        cb_FullAscii.setOnClickListener(this);
        cb_BitCode32Prech = findViewById(R.id.cb_BitCode32Prech);
        cb_BitCode32Prech.setOnClickListener(this);
        cb_Code32SpecEdit = findViewById(R.id.cb_Code32SpecEdit);
        cb_Code32SpecEdit.setOnClickListener(this);
        cb_Code32TrsmtChkChar = findViewById(R.id.cb_Code32TrsmtChkChar);
        cb_Code32TrsmtChkChar.setOnClickListener(this);
        cb_Code32TrsmtStasrtStop = findViewById(R.id.cb_Code32TrsmtStasrtStop);
        cb_Code32TrsmtStasrtStop.setOnClickListener(this);
        cb_TrsmtSysDigit = findViewById(R.id.cb_TrsmtSysDigit);
        cb_TrsmtSysDigit.setOnClickListener(this);
        cb_MsgToupca = findViewById(R.id.cb_MsgToupca);
        cb_MsgToupca.setOnClickListener(this);
        cb_Tdoupca = findViewById(R.id.cb_Tdoupca);
        cb_Tdoupca.setOnClickListener(this);
        cb_Uscode = findViewById(R.id.cb_Uscode);
        cb_Uscode.setOnClickListener(this);
        cb_Gs1Coupon = findViewById(R.id.cb_Gs1Coupon);
        cb_Gs1Coupon.setOnClickListener(this);
        cb_ReqCoupon = findViewById(R.id.cb_ReqCoupon);
        cb_ReqCoupon.setOnClickListener(this);
        cb_Coupon = findViewById(R.id.cb_Coupon);
        cb_Coupon.setOnClickListener(this);
        cb_CloseSysChar = findViewById(R.id.cb_CloseSysChar);
        cb_CloseSysChar.setOnClickListener(this);

        btn_save_param = findViewById(R.id.btn_save_param);
        btn_save_param.setOnClickListener(this);

        topToolbar = findViewById(R.id.tb_topToolBar);
        topToolbar.setRightTitleVisiable(View.GONE);
        topToolbar.setMainTitle(title);
        topToolbar.setMenuToolBarListener(new TopToolbar.MenuToolBarListener() {
            @Override
            public void onToolBarClickLeft(View v) {
                finish();
            }

            @Override
            public void onToolBarClickRight(View v) {

            }
        });

        for (EngineCode code : Cm60Service.engineCodeList) {
            if (code.getName().equals(title)) {
                if (code.getMinlen() != null) {
                    et_minlen.setText(code.getMinlen());
                } else {
                    et_minlen.setVisibility(View.GONE);
                    tv_minlen.setVisibility(View.GONE);
                }

                if (code.getMaxlen() != null) {
                    et_maxlen.setText(code.getMaxlen());
                } else {
                    et_maxlen.setVisibility(View.GONE);
                    tv_maxlen.setVisibility(View.GONE);
                }

                if (code.getCheck() != null) {
                    cb_Check.setChecked(code.getCheck().equals("1"));
                } else {
                    cb_Check.setVisibility(View.GONE);
                }

                if (code.getTrsmtChkChar() != null) {
                    cb_TrsmtChkChar.setChecked(code.getTrsmtChkChar().equals("1"));
                } else {
                    cb_TrsmtChkChar.setVisibility(View.GONE);
                }

                if (code.getDigit2() != null) {
                    cb_Digit2.setChecked(code.getDigit2().equals("1"));
                } else {
                    cb_Digit2.setVisibility(View.GONE);
                }

                if (code.getDigit5() != null) {
                    cb_Digit5.setChecked(code.getDigit5().equals("1"));
                } else {
                    cb_Digit5.setVisibility(View.GONE);
                }

                if (code.getAddonRequired() != null) {
                    cb_AddonRequired.setChecked(code.getAddonRequired().equals("1"));
                } else {
                    cb_AddonRequired.setVisibility(View.GONE);
                }

                if (code.getTrsmtStasrtStop() != null) {
                    cb_TrsmtStasrtStop.setChecked(code.getTrsmtStasrtStop().equals("1"));
                } else {
                    cb_TrsmtStasrtStop.setVisibility(View.GONE);
                }

                if (code.getFullAscii() != null) {
                    cb_FullAscii.setChecked(code.getFullAscii().equals("1"));
                } else {
                    cb_FullAscii.setVisibility(View.GONE);
                }

                if (code.getBitCode32Prech() != null) {
                    cb_BitCode32Prech.setChecked(code.getBitCode32Prech().equals("1"));
                } else {
                    cb_BitCode32Prech.setVisibility(View.GONE);
                }

                if (code.getCode32SpecEdit() != null) {
                    cb_Code32SpecEdit.setChecked(code.getCode32SpecEdit().equals("1"));
                } else {
                    cb_Code32SpecEdit.setVisibility(View.GONE);
                }

                if (code.getCode32TrsmtChkChar() != null) {
                    cb_Code32TrsmtChkChar.setChecked(code.getCode32TrsmtChkChar().equals("1"));
                } else {
                    cb_Code32TrsmtChkChar.setVisibility(View.GONE);
                }

                if (code.getCode32TrsmtStasrtStop() != null) {
                    cb_Code32TrsmtStasrtStop.setChecked(code.getCode32TrsmtStasrtStop().equals("1"));
                } else {
                    cb_Code32TrsmtStasrtStop.setVisibility(View.GONE);
                }

                if (code.getTrsmtSysDigit() != null) {
                    cb_TrsmtSysDigit.setChecked(code.getTrsmtSysDigit().equals("1"));
                } else {
                    cb_TrsmtSysDigit.setVisibility(View.GONE);
                }

                if (code.getTdoupca() != null) {
                    cb_Tdoupca.setChecked(code.getTdoupca().equals("1"));
                } else {
                    cb_Tdoupca.setVisibility(View.GONE);
                }

                if (code.getUscode() != null) {
                    cb_Uscode.setChecked(code.getUscode().equals("1"));
                } else {
                    cb_Uscode.setVisibility(View.GONE);
                }

                if (code.getGs1Coupon() != null) {
                    cb_Gs1Coupon.setChecked(code.getGs1Coupon().equals("1"));
                } else {
                    cb_Gs1Coupon.setVisibility(View.GONE);
                }

                if (code.getReqCoupon() != null) {
                    cb_ReqCoupon.setChecked(code.getReqCoupon().equals("1"));
                } else {
                    cb_ReqCoupon.setVisibility(View.GONE);
                }

                if (code.getCoupon() != null) {
                    cb_Coupon.setChecked(code.getCoupon().equals("1"));
                } else {
                    cb_Coupon.setVisibility(View.GONE);
                }

                if (code.getCloseSysChar() != null) {
                    cb_CloseSysChar.setChecked(code.getCloseSysChar().equals("1"));
                } else {
                    cb_CloseSysChar.setVisibility(View.GONE);
                }

                if (code.getMsgToupca() != null) {
                    cb_MsgToupca.setChecked(code.getMsgToupca().equals("1"));
                } else {
                    cb_MsgToupca.setVisibility(View.GONE);
                }

                break;
            }
        }
    }

    public void initData() {

    }


    public void updateParam() {

        try {
            for (EngineCode engineCode : Cm60Service.engineCodeList) {
                if (engineCode.getName().equals(paramEngineCode.getName())) {

                    Class<?> engineCodeClass = Class.forName(EngineCode.class.getName());
                    for (EngineCodeMenu.CodeParam param : EngineCodeMenu.CodeParam.values()) {
                        Method getMethod = engineCodeClass.getDeclaredMethod("get" + param.getParamName());
                        getMethod.setAccessible(true);
                        String value = (String) getMethod.invoke(paramEngineCode);
                        if (value != null) {
//                            SoftEngine.getInstance().ScanSet(paramEngineCode.getName(), param.getParamName(), value);
                            Intent configIntent = new Intent(ServiceActionKey.ACTION_SCAN_PARAM);
                            configIntent.putExtra("id",paramEngineCode.getName());
                            configIntent.putExtra("param", param.getParamName());
                            configIntent.putExtra("value", value);
                            this.sendBroadcast(configIntent);
                            Method setMethod = engineCodeClass.getDeclaredMethod("set" + param.getParamName(), String.class);
                            setMethod.setAccessible(true);
                            setMethod.invoke(engineCode, value);
                        }
                    }
                    break;
                }

            }
            finish();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } finally {
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cb_Check:
                paramEngineCode.setCheck(cb_Check.isChecked() ? "1" : "0");

                break;
            case R.id.cb_TrsmtChkChar:
                paramEngineCode.setTrsmtChkChar(cb_TrsmtChkChar.isChecked() ? "1" : "0");
                break;
            case R.id.cb_Digit2:
                paramEngineCode.setDigit2(cb_Digit2.isChecked() ? "1" : "0");
                break;
            case R.id.cb_Digit5:
                paramEngineCode.setDigit5(cb_Digit5.isChecked() ? "1" : "0");
                break;
            case R.id.cb_AddonRequired:
                paramEngineCode.setAddonRequired(cb_AddonRequired.isChecked() ? "1" : "0");
                break;
            case R.id.cb_TrsmtStasrtStop:
                paramEngineCode.setTrsmtStasrtStop(cb_TrsmtStasrtStop.isChecked() ? "1" : "0");
                break;
            case R.id.cb_FullAscii:
                paramEngineCode.setFullAscii(cb_FullAscii.isChecked() ? "1" : "0");
                break;
            case R.id.cb_BitCode32Prech:
                paramEngineCode.setBitCode32Prech(cb_BitCode32Prech.isChecked() ? "1" : "0");
                break;
            case R.id.cb_Code32SpecEdit:
                paramEngineCode.setCode32SpecEdit(cb_Code32SpecEdit.isChecked() ? "1" : "0");
                break;
            case R.id.cb_Code32TrsmtChkChar:
                paramEngineCode.setCode32TrsmtChkChar(cb_Code32TrsmtChkChar.isChecked() ? "1" : "0");
                break;
            case R.id.cb_Code32TrsmtStasrtStop:
                paramEngineCode.setCode32TrsmtStasrtStop(cb_Code32TrsmtStasrtStop.isChecked() ? "1" : "0");
                break;
            case R.id.cb_TrsmtSysDigit:
                paramEngineCode.setTrsmtSysDigit(cb_TrsmtSysDigit.isChecked() ? "1" : "0");
                break;
            case R.id.cb_MsgToupca:
                paramEngineCode.setMsgToupca(cb_MsgToupca.isChecked() ? "1" : "0");
                break;
            case R.id.cb_Tdoupca:
                paramEngineCode.setTdoupca(cb_Tdoupca.isChecked() ? "1" : "0");
                break;
            case R.id.cb_Uscode:
                paramEngineCode.setUscode(cb_Uscode.isChecked() ? "1" : "0");
                break;
            case R.id.cb_CloseSysChar:
                paramEngineCode.setCloseSysChar(cb_CloseSysChar.isChecked() ? "1" : "0");
                break;
            case R.id.cb_Gs1Coupon:
                paramEngineCode.setGs1Coupon(cb_Gs1Coupon.isChecked() ? "1" : "0");
                break;
            case R.id.cb_ReqCoupon:
                paramEngineCode.setReqCoupon(cb_ReqCoupon.isChecked() ? "1" : "0");
                break;
            case R.id.cb_Coupon:
                paramEngineCode.setCoupon(cb_Coupon.isChecked() ? "1" : "0");
                break;

            case R.id.btn_save_param:
                updateParam();
            default:
                break;


        }
    }
}
