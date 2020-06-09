package com.ssn.se4710;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.KeyEvent;

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
 * Create At 2019/4/30 18:27
 * Update By 更新者
 * Update At 2019/4/30 18:27
 * </code>
 * 开机初始化界面，不可见，只做启动Service逻辑
 */
public class BootInitActivity extends Activity {

    private final String TAG = BootInitActivity.class.getSimpleName();


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
//        mHandler = new MessageHandler(this);
        boolean isBoot = this.getIntent().getBooleanExtra("isBoot", true);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        BootInitFragment bootInitFragment = BootInitFragment.newInstance(getApplicationContext(), isBoot);
        transaction.add(R.id.content, bootInitFragment);
        transaction.commit();
    }

    public void startService() {
        LogUtils.e(TAG, "startService, ");
//        if (mIsBoot) {
        Intent toService = new Intent(this.getApplicationContext(), Se4710Service.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.getApplicationContext().startForegroundService(toService);
            LogUtils.i(TAG, "onCreate-0");
        } else {
            this.getApplicationContext().startService(toService);
            LogUtils.i(TAG, "onCreate-1");
        }
//        }

    }

    @Override
    protected void onResume() {
        super.onResume();
//        try {
//            Thread.sleep(30000);
//            this.finish();
//            LogUtils.e(TAG, "onResume, finish");
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        try {
//            Thread.sleep(800);
        BootInitActivity.this.finish();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }
}
