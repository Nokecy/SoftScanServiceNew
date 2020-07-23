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

    private final String TAG = BootInitActivity.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boot);
    }

    @Override
    protected void onResume() {
        boolean isBoot = this.getIntent().getBooleanExtra("isBoot", true);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        BootInitFragment bootInitFragment = BootInitFragment.newInstance(getApplicationContext(), isBoot);
        transaction.add(R.id.content, bootInitFragment);
        transaction.commit();
        super.onResume();
        finish();
    }
}
