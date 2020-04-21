package com.hhw.ssn.comui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.KeyEvent;

import com.hhw.ssn.combean.PreferenceKey;
import com.hhw.ssn.combean.ServiceActionKey;
import com.hhw.ssn.comutils.LogUtils;


/**
 * @author huang
 * 接收按键广播，然后按照条件触发扫描；接收熄屏和亮屏的广播，关闭和打开扫描头
 */
public class KeyBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = KeyBroadcastReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtils.i(TAG, "onReceive, action:" + action);
        if (action == null) {
            return;
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int keyCode = intent.getIntExtra("keyCode", 0);
        //兼容H941
        if (keyCode == 0) {
            keyCode = intent.getIntExtra("keycode", 0);
        }
        LogUtils.i(TAG, "keyCode:" + keyCode);
//        boolean isInSettingUi = ((BaseApplication) context.getApplicationContext()).mSpUtils.getIsInSettingUi();
//        if (isInSettingUi) {
//            LogUtils.e(TAG, "onReceive, in the setting ui");
//            return;
//        }
        // 如果后台扫描开关未打开，不执行
        boolean isOpen = prefs.getBoolean(PreferenceKey.KEY_SWITCH_SCAN, false);
        if (!isOpen) {
            LogUtils.i("Huang, KeyReceiver", "Switch is not opened");
            return;
        }
        boolean keyDown = intent.getBooleanExtra("keydown", false);
        boolean f1Enable = prefs.getBoolean(PreferenceKey.KEY_SCAN_KEY_F1, true);
        boolean f2Enable = prefs.getBoolean(PreferenceKey.KEY_SCAN_KEY_F2, true);
        boolean f3Enable = prefs.getBoolean(PreferenceKey.KEY_SCAN_KEY_F3, true);
        boolean f4Enable = prefs.getBoolean(PreferenceKey.KEY_SCAN_KEY_F4, true);
        boolean f5Enable = prefs.getBoolean(PreferenceKey.KEY_SCAN_KEY_F5, true);
        boolean f6Enable = prefs.getBoolean(PreferenceKey.KEY_SCAN_KEY_F6, true);
        boolean f7Enable = prefs.getBoolean(PreferenceKey.KEY_SCAN_KEY_F7, true);
        // If the scan switch in ConfigFragment is opened, start scan
        if (f1Enable && keyCode == KeyEvent.KEYCODE_F1) {
            sendScanBroad(context, keyDown);
        } else if (f2Enable && keyCode == KeyEvent.KEYCODE_F2) {
            sendScanBroad(context, keyDown);
        } else if (f3Enable && keyCode == KeyEvent.KEYCODE_F3) {
            sendScanBroad(context, keyDown);
        } else if (f4Enable && keyCode == KeyEvent.KEYCODE_F4) {
            sendScanBroad(context, keyDown);
        } else if (f5Enable && keyCode == KeyEvent.KEYCODE_F5) {
            sendScanBroad(context, keyDown);
        } else if (f6Enable && keyCode == KeyEvent.KEYCODE_F6) {
            sendScanBroad(context, keyDown);
        } else if (f7Enable && keyCode == KeyEvent.KEYCODE_F7) {
            sendScanBroad(context, keyDown);
        }

    }

    private void sendScanBroad(Context context, boolean keydown) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isKeyPressed = prefs.getBoolean("isKeyPressed", false);
        LogUtils.i(TAG, "keydown = " + keydown + ", isKeyPressed = " + isKeyPressed);
        Intent broadIntent = new Intent();
        // 判断连续扫描开关是否开启
        boolean isContinuousSwitchOpen = prefs.getBoolean(PreferenceKey.KEY_CONTINUOUS_SCANNING, false);
        if (isContinuousSwitchOpen) {
            // 如果连续扫描开启，抬起按键需要判断是否处于循环扫描状态(isLooping用于本类中的循环判断，mIsLooping用于Service中的循环判断)
            if (!keydown) {
                boolean mIsLooping = prefs.getBoolean("mIsLooping", false);
                if (mIsLooping) {
                    prefs.edit().putBoolean("mIsLooping", false).apply();
                    broadIntent.setAction(ServiceActionKey.ACTION_STOP_SCAN);
                    LogUtils.i(TAG, "sendScanBroad: reset mIsLooping");
                } else {
                    broadIntent.setAction(ServiceActionKey.ACTION_SCAN);
                }
            }
        } else {
            if (keydown && !isKeyPressed) {
                broadIntent.setAction(ServiceActionKey.ACTION_SCAN);
                isKeyPressed = true;
            } else if (!keydown && isKeyPressed) {
                boolean stopOnUp = prefs.getBoolean(PreferenceKey.KEY_STOP_ON_UP, true);
                if (stopOnUp) {
                    broadIntent.setAction(ServiceActionKey.ACTION_STOP_SCAN);
                }
                isKeyPressed = false;
            }
        }
        if (broadIntent.getAction() != null) {
            context.sendBroadcast(broadIntent);
        }
        prefs.edit().putBoolean("isKeyPressed", isKeyPressed).apply();
    }
}
