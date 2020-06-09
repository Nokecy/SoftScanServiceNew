package com.ssn.se4710;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.hhw.ssn.combean.ServiceActionKey;
import com.hhw.ssn.comutils.LogUtils;
import com.ssn.se4710.dao.Symbology;
import com.ssn.se4710.dao.SymbologyDao;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author HuangLei 1252065297@qq.com
 * <code>
 * Create At 2019/4/30 18:37
 * Update By 更新者
 * Update At 2019/4/30 18:37
 * </code>
 */
public class BootInitFragment extends PreferenceFragment {

    private static final String TAG = BootInitFragment.class.getSimpleName();

    private boolean mIsBoot;

    private static WeakReference<Context> mWeakReference;

    private ThreadFactory threadFactory = Executors.defaultThreadFactory();
    private ExecutorService mExecutorService = new ThreadPoolExecutor(1, 200, 0L,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024), threadFactory, new ThreadPoolExecutor.AbortPolicy());

    public static BootInitFragment newInstance(Context context, boolean isBoot) {
        LogUtils.e(TAG, "newInstance,  ");
        Bundle args = new Bundle();
        args.putBoolean("isBoot", isBoot);
        BootInitFragment fragment = new BootInitFragment();
        fragment.setArguments(args);
        mWeakReference = new WeakReference<>(context);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.general_settings);
        mIsBoot = getArguments().getBoolean("isBoot");
        LogUtils.e(TAG, "onCreate, ");
        // 初始化Se4710码制配置表
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                SymbologyDao symbologyDao = Se4710Application.getInstances().getDaoSession().getSymbologyDao();
                long count = symbologyDao.count();
                LogUtils.e(TAG, "onCreate, count:" + count);
                if (count <= 0) {
                    TypedArray array = BootInitFragment.this.getActivity().getResources().obtainTypedArray(R.array.symbologies_child);
                    int[] paramNum = BootInitFragment.this.getActivity().getResources().getIntArray(R.array.symbology_param_num);
                    int[] paramValueDef = BootInitFragment.this.getActivity().getResources().getIntArray(R.array.symbology_param_value_def);
                    int index = 0;
                    for (int i = 0; i < array.length(); i++) {
                        String[] tempString = BootInitFragment.this.getActivity().getResources().getStringArray(array.getResourceId(i, -1));
                        for (String s : tempString) {
                            Symbology symbology = new Symbology();
                            symbology.setParamNum(paramNum[index]);
                            symbology.setParamName(s);
                            symbology.setParamValueDef(paramValueDef[index]);
                            symbology.setParamValue(paramValueDef[index]);
                            symbologyDao.insert(symbology);
                            index++;
                        }
                    }
                    array.recycle();
//                    int[] intArray = getResources().getIntArray(R.array.symbologies_parameter);
//                    int[] defArray = getResources().getIntArray(R.array.default_enabled);
//                    String[] stringArray = getResources().getStringArray(R.array.symbologies);
//                    for (int i = 0; i < intArray.length; i++) {
//                        Symbology symbology = new Symbology();
//                        symbology.setParamNum((long) intArray[i]);
//                        symbology.setName(stringArray[i]);
//                        LogUtils.e(TAG, "onCreate, symbology:" + stringArray[i]);
//                        symbology.setEnabled(defArray[i]);
//                        symbologyDao.insert(symbology);
//                    }
                }
                // 启动扫描服务
                startService();
                LogUtils.e(TAG, "onCreate init database successfully: ");
//                mHandler.sendEmptyMessage(0);
            }
        });
    }

    public void startService(){
        LogUtils.e(TAG, "startService, ");
        if (mIsBoot) {
            Intent toService = new Intent(this.getActivity().getApplicationContext(), Se4710Service.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.getActivity().getApplicationContext().startForegroundService(toService);
                LogUtils.i(TAG, "onCreate-0");
            } else {
                this.getActivity().getApplicationContext().startService(toService);
                LogUtils.i(TAG, "onCreate-1");
            }
        }

    }
}
