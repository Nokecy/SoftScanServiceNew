package com.ssn.se4710;

import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hhw.ssn.comui.BaseApplication;
import com.ssn.se4710.dao.DaoMaster;
import com.ssn.se4710.dao.DaoSession;
import com.ssn.se4710.dao.MySQLiteOpenHelper;

import java.io.File;

/**
 * description : DESCRIPTION
 * update : 2019/9/20 11:41,LeiHuang,DESCRIPTION
 *
 * @author : LeiHuang
 * @version : VERSION
 * @date : 2019/9/20 11:41
 */
public class Se4710Application extends BaseApplication {
    private MySQLiteOpenHelper mHelper;
    private SQLiteDatabase mSQLiteDatabase;
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;
    public static Se4710Application sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            // 根据/sys/bus/platform/drivers/image_sensor/main2_camera_name路径是否存在，判断是否有4710扫描头
            PackageManager pm = getPackageManager();
            File file = new File("/sys/bus/platform/drivers/image_sensor/main2_camera_name");
            boolean exists = file.exists();
            Log.d("HwSoftScanControl", "onCreate /sys/bus/platform/drivers/image_sensor/main2_camera_name exists : " + exists);
            if (!exists) {
                pm.setApplicationEnabledSetting("com.ssn.se4710", PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
            } else {
                pm.setApplicationEnabledSetting("com.ssn.se4710", PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        sInstance = this;
        setDatabase();
    }

    public static Se4710Application getInstances() {
        return sInstance;
    }

    private void setDatabase() {
        // 通过 DaoMaster 的内部类 DevOpenHelper，你可以得到一个便利的 SQLiteOpenHelper 对象。
        // 可能你已经注意到了，你并不需要去编写「CREATE TABLE」这样的 SQL 语句，因为 greenDAO 已经帮你做了。
        // 注意：默认的 DaoMaster.DevOpenHelper 会在数据库升级时，删除所有的表，意味着这将导致数据的丢失。
        // 所以，在正式的项目中，你还应该做一层封装，来实现数据库的安全升级。
        mHelper = new MySQLiteOpenHelper(this, "notes-db", null);
        mSQLiteDatabase = mHelper.getWritableDatabase();
        // 注意：该数据库连接属于 DaoMaster，所以多个 Session 指的是相同的数据库连接。
        mDaoMaster = new DaoMaster(mSQLiteDatabase);
        mDaoSession = mDaoMaster.newSession();
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }

    public SQLiteDatabase getDb() {
        return mSQLiteDatabase;
    }
}
