package com.ssn.se4710;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.hhw.ssn.comutils.LogUtils;
import com.ssn.se4710.dao.Symbology;
import com.ssn.se4710.dao.SymbologyDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author HuangLei 1252065297@qq.com
 * <code>
 * Create At 2019/4/30 18:27
 * Update By 更新者
 * Update At 2019/4/30 18:27
 * </code>
 * 开机初始化界面，不可见，只做启动Service逻辑
 */
public class BootInitActivity extends Activity {

    private ThreadFactory threadFactory = Executors.defaultThreadFactory();
    private ExecutorService mExecutorService = new ThreadPoolExecutor(1, 200, 0L,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024), threadFactory, new ThreadPoolExecutor.AbortPolicy());

    private final String TAG = BootInitActivity.class.getSimpleName();

    /**
     * 上下文环境，用来获取配置信息
     */
    private Context mContext;

//    private static class MessageHandler extends Handler {
//        private WeakReference<BootInitActivity> mWeakReference;
//
//        private MessageHandler(BootInitActivity bootInitActivity) {
//            mWeakReference = new WeakReference<>(bootInitActivity);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            LogUtils.e("BootInitActivity", "startService, ");
////            Intent toService = new Intent(mWeakReference.get().getApplicationContext(), Se4710Service.class);
////            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
////                mWeakReference.get().startForegroundService(toService);
////                LogUtils.i("BootInitActivity", "onCreate-0");
////            } else {
////                mWeakReference.get().startService(toService);
////                LogUtils.i("BootInitActivity", "onCreate-1");
////            }
//        }
//    }

//    private Handler mHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boot);
        // 获取应用上下文
        mContext = this.getApplicationContext();
    }

    public void startService() {
        LogUtils.e(TAG, "startService, ");
//        if (mIsBoot) {
        Intent toService = new Intent(mContext, Se4710Service.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mContext.startForegroundService(toService);
            LogUtils.i(TAG, "onCreate-0");
        } else {
            mContext.startService(toService);
            LogUtils.i(TAG, "onCreate-1");
        }
//        }

    }

    @Override
    protected void onResume() {
        // 初始化Se4710码制配置表
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                SymbologyDao symbologyDao = Se4710Application.getInstances().getDaoSession().getSymbologyDao();
                long count = symbologyDao.count();
                LogUtils.e(TAG, "onCreate, count:" + count);
                if (count <= 0) {
                    TypedArray array = mContext.getResources().obtainTypedArray(R.array.symbologies_child);
                    int[] paramNum = mContext.getResources().getIntArray(R.array.symbology_param_num);
                    int[] paramValueDef = mContext.getResources().getIntArray(R.array.symbology_param_value_def);
                    int index = 0;
                    for (int i = 0; i < array.length(); i++) {
                        String[] tempString = mContext.getResources().getStringArray(array.getResourceId(i, -1));
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
//        mHandler = new MessageHandler(this);
        boolean isBoot = this.getIntent().getBooleanExtra("isBoot", true);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        BootInitFragment bootInitFragment = BootInitFragment.newInstance(getApplicationContext(), isBoot);
        transaction.add(R.id.content, bootInitFragment);
        transaction.commit();
//        try {
//            Thread.sleep(30000);
//            this.finish();
//            LogUtils.e(TAG, "onResume, finish");
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        try {
//            Thread.sleep(800);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        super.onResume();
        finish();
    }
}
