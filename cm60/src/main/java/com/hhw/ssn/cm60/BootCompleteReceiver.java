package com.hhw.ssn.cm60;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.hhw.ssn.comutils.LogUtils;

/**
 * @author HuangLei 1252065297@qq.com
 * <code>
 * Create At 2019/4/30 18:21
 * Update By 更新者
 * Update At 2019/4/30 18:21
 * </code>
 * 开机广播接收，初始化扫描头
 */
public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        LogUtils.i("Huang, Receiver", "BootCompleteReceiver action = " + action);
        Intent bootInit = new Intent(context, BootInitActivity.class);
        bootInit.putExtra("isBoot", true);
        bootInit.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(bootInit);
    }
}
