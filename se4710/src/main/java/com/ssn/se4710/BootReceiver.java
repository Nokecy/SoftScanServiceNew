package com.ssn.se4710;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hhw.ssn.comutils.LogUtils;


/**
 * @author HuangLei 1252065297@qq.com
 * @CreateDate 2019/1/17 18:15
 * @UpdateUser 更新者
 * @UpdateDate 2019/1/17 18:15
 * 开机启动Service
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG= BootReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.i(TAG, "boot receive = " + intent.getAction());
        Intent toService = new Intent(context, BootInitActivity.class);
        toService.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(toService);
    }
}
