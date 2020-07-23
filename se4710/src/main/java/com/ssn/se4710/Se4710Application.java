package com.ssn.se4710;

import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.hhw.ssn.comui.BaseApplication;
import com.hhw.ssn.comutils.LogUtils;
import com.ssn.se4710.dao.DaoMaster;
import com.ssn.se4710.dao.DaoSession;
import com.ssn.se4710.dao.MySQLiteOpenHelper;
import com.ssn.se4710.dao.Symbology;
import com.ssn.se4710.dao.SymbologyDao;

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

    private final String TAG = "Se4710Application";

    private MySQLiteOpenHelper mHelper;
    private SQLiteDatabase mSQLiteDatabase;
    private DaoMaster mDaoMaster;
    private DaoSession mDaoSession;
    public static Se4710Application sInstance;

    @Override
    public void onCreate() {
        try {
            // 根据/sys/bus/platform/drivers/image_sensor/main2_camera_name路径是否存在，判断是否有4710扫描头
            PackageManager pm = getPackageManager();
            File file = new File("/sys/bus/platform/drivers/image_sensor/main2_camera_name");
            boolean exists = file.exists();
            Log.d("HwSoftScanControl", "onCreate /sys/bus/platform/drivers/image_sensor/main2_camera_name exists : " + exists);
            if (!exists) {
                pm.setApplicationEnabledSetting("com.ssn.se4710", PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
                return;
            } else {
                pm.setApplicationEnabledSetting("com.ssn.se4710", PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onCreate();
        sInstance = this;
        setDatabase();
        initEngineCode();
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

    /**
     * 初始化扫描头码制相关设置
     */
    private void initEngineCode() {
        SymbologyDao symbologyDao = Se4710Application.getInstances().getDaoSession().getSymbologyDao();
        long count = symbologyDao.count();
        LogUtils.e(TAG, "onCreate, count:" + count);
        // 码制设置列表是否加载完毕
        int requireCount = 0;
        TypedArray array = this.getResources().obtainTypedArray(R.array.symbologies_child);
        for (int i = 0; i < array.length(); i++) {
            String[] tempStr = this.getResources().getStringArray(array.getResourceId(i, -1));
            requireCount = tempStr.length + requireCount;
        }
        LogUtils.e(TAG, "onCreate, requireCount:" + requireCount);
        if (count < requireCount) {
            if (count > 0) {
                symbologyDao.deleteAll();
            }
            int[] paramNum = this.getResources().getIntArray(R.array.symbology_param_num);
            int[] paramValueDef = this.getResources().getIntArray(R.array.symbology_param_value_def);
            int index = 0;
            for (int i = 0; i < array.length(); i++) {
                String[] tempString = this.getResources().getStringArray(array.getResourceId(i, -1));
                for (String s : tempString) {
                    Symbology symbology = new Symbology();
                    symbology.setParamNum(paramNum[index]);
                    symbology.setParamName(s);
                    symbology.setParamValueDef(paramValueDef[index]);
                    symbology.setParamValue(paramValueDef[index]);
                    symbologyDao.insert(symbology);
                    index++;
                }
            }
            array.recycle();
        }
        LogUtils.e(TAG, "onCreate init database successfully: ");
    }
}
