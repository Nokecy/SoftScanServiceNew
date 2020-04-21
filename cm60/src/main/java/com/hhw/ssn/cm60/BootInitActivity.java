package com.hhw.ssn.cm60;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;

import com.hhw.ssn.comutils.LogUtils;

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

        LogUtils.e(TAG, "onCreate, ");
        boolean isBoot = this.getIntent().getBooleanExtra("isBoot", true);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        BootInitFragment bootInitFragment = BootInitFragment.newInstance(getApplicationContext(), isBoot);
        transaction.add(R.id.content, bootInitFragment);
        transaction.commit();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
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
