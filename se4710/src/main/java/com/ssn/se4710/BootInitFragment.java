package com.ssn.se4710;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.hhw.ssn.combean.ServiceActionKey;
import com.hhw.ssn.comutils.LogUtils;

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
        startService();
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
