package com.hhw.ssn.comutils;

import android.util.Log;

/**
 * description : 日志打印工具类
 * update : 2019/10/28 11:10,LeiHuang,Init commit
 *
 * @author : LeiHuang
 * @version : 1.2
 */
public class LogUtils {
    private static boolean DEBUG = true;

    public static void setMode(boolean isDebug){
        DEBUG = isDebug;
    }

    public static void v(String className, String content) {
        if (DEBUG) {
            Log.v("Huang," + className, content);
        }
    }

    public static void d(String className, String content) {
        if (DEBUG) {
            Log.d("Huang," + className, content);
        }
    }

    public static void i(String className, String content) {
        if (DEBUG) {
            Log.i("Huang," + className, content);
        }
    }

    public static void w(String className, String content) {
        if (DEBUG) {
            Log.w("Huang," + className, content);
        }
    }

    public static void e(String className, String content) {
        if (DEBUG) {
            Log.e("Huang," + className, content);
        }
    }
}
