package com.hhw.ssn.comutils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * description : DESCRIPTION
 * update : 2019/7/20 11:26,LeiHuang,DESCRIPTION
 *
 * @author : LeiHuang
 * @version : VERSION
 * @date : 2019/7/20 11:26
 */
public class SpUtils {

    private static WeakReference<Context> sWeakReference;

    private SharedPreferences mSharedPreferences;

    private SpUtils(Context context){
        if (context == null) {
            Log.e("Huang,SpUtils", "initialize SpUtils fail with context == null");
        }
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static SpUtils getInstance(Context context){
        if (sWeakReference == null) {
            sWeakReference = new WeakReference<>(context.getApplicationContext());
        }
        return SpUtilsHolder.sSpUtils;
    }

    private static class SpUtilsHolder{
        private static SpUtils sSpUtils = new SpUtils(sWeakReference.get());
    }

    public boolean putIsInSettingUi(boolean isInSettingUi){
        return mSharedPreferences.edit().putBoolean("isInSettingUi", isInSettingUi).commit();
    }

    public boolean getIsInSettingUi(){
        return mSharedPreferences.getBoolean("isInSettingUi", false);
    }
}
