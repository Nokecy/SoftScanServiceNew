package com.hhw.ssn.cm60;

import android.content.Context;
import android.content.Intent;
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

    private final String TAG = BootInitFragment.class.getSimpleName();

    private ThreadFactory threadFactory = Executors.defaultThreadFactory();
    private ExecutorService mExecutorService = new ThreadPoolExecutor(3, 200, 0L,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024),
            threadFactory, new ThreadPoolExecutor.AbortPolicy());
    private boolean mIsBoot;

    private static WeakReference<Context> mWeakReference;

    public static BootInitFragment newInstance(Context context, boolean isBoot) {
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
            Intent toService = new Intent(this.getActivity(), Cm60Service.class);
            this.getActivity().startService(toService);
            mExecutorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                        LogUtils.i(TAG, "start send boot init broadcast");
                        Intent configIntent = new Intent();
                        configIntent.setAction(ServiceActionKey.ACTION_SCAN_BOOT_INIT);
                        mWeakReference.get().sendBroadcast(configIntent);
                        LogUtils.i(TAG, "bootCompleted");
//                        callBack.onFinish();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

    }

//    public interface BootInitCallBack{
//        /**
//         * Fragment初始化完成之后回调
//         */
//        void onFinish();
//    }

}
