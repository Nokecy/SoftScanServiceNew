package com.ssn.se4710;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.hhw.ssn.combean.PreferenceKey;
import com.hhw.ssn.comutils.LogUtils;

/**
 * description : 提供扫描服务的状态查询
 * update : 2020/3/12 8:44,LeiHuang,Init commit
 *
 * @author : LeiHuang
 * @version : 1.0
 */
public class StateProvider extends ContentProvider {
    private final String TAG = StateProvider.class.getSimpleName();
    private SharedPreferences mDefaultSharedPreferences;

    @Override
    public boolean onCreate() {
        LogUtils.e(TAG, "onCreate");
        mDefaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        boolean aBoolean = mDefaultSharedPreferences.getBoolean(PreferenceKey.KEY_SWITCH_SCAN, false);
        LogUtils.e(TAG, "query aBoolean = " + aBoolean);
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        LogUtils.e(TAG, "getType");
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        LogUtils.e(TAG, "insert");
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        LogUtils.e(TAG, "delete");
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        LogUtils.e(TAG, "update");
        return 0;
    }
}
