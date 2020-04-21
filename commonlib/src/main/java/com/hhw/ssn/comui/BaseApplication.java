package com.hhw.ssn.comui;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import com.alibaba.android.arouter.launcher.ARouter;
import com.hhw.ssn.comutils.LogUtils;
import com.hhw.ssn.comutils.SpUtils;

/**
 * description : DESCRIPTION
 * update : 2019/7/3 15:29,LeiHuang,DESCRIPTION
 *
 * @author : LeiHuang
 * @version : VERSION
 * @date : 2019/7/3 15:29
 */
public class BaseApplication extends Application {

    public SpUtils mSpUtils;

    /**
     * Debug mode?
     */
    public boolean isDebug() {
        try {
            ApplicationInfo info = this.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("Huang,Application", "onCreate: debug mode " + isDebug());
        if (isDebug()){
            ARouter.openDebug();
        }
        ARouter.init(this);

        mSpUtils = SpUtils.getInstance(this);

        LogUtils.setMode(isDebug());
    }
}
