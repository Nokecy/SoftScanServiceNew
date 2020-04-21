package com.ssn.se4710;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hhw.ssn.combean.ServiceActionKey;
import com.hhw.ssn.comutils.LogUtils;

/**
 * description : DESCRIPTION
 * update : 2019/11/28 18:51,LeiHuang,Init commit
 *
 * @author : LeiHuang
 * @version : VERSION
 */
public class ScreenBroadcastReceiver extends BroadcastReceiver {

    private final String TAG = ScreenBroadcastReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtils.i(TAG, "onReceive, action:" + action);
        if (action == null){
            return;
        }
        if (action.equals(Intent.ACTION_SCREEN_OFF)){
            doSomethingScreenOff(context);
        } else if (action.equals(Intent.ACTION_SCREEN_ON)){
            doSomethingScreenOn(context);
        }
    }

    private void doSomethingScreenOff(Context context) {
        Intent intent = new Intent(ServiceActionKey.ACTION_CLOSE_SCAN);
        intent.putExtra("iscamera", true);
        context.sendBroadcast(intent);
    }

    private void doSomethingScreenOn(Context context) {
        Intent intent = new Intent(ServiceActionKey.ACTION_SCAN_INIT);
        intent.putExtra("iscamera", true);
        context.sendBroadcast(intent);
    }
}
