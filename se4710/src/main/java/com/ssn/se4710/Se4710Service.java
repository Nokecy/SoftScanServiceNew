package com.ssn.se4710;

import android.app.Instrumentation;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.ClipboardManager;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.hhw.ssn.combean.PreferenceKey;
import com.hhw.ssn.combean.ServiceActionKey;
import com.hhw.ssn.comui.KeyBroadcastReceiver;
import com.hhw.ssn.comui.TabHostActivity;
import com.hhw.ssn.comutils.FloatService;
import com.hhw.ssn.comutils.LogUtils;
import com.hhw.ssn.comutils.RegularUtils;
import com.hhw.ssn.comutils.SoundPoolMgr;
import com.ssn.se4710.dao.Symbology;
import com.ssn.se4710.dao.SymbologyDao;
import com.zebra.adc.decoder.BarCodeReader;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static android.support.v4.app.NotificationCompat.FLAG_NO_CLEAR;

/**
 * @author LeiHuang
 */
public class Se4710Service extends Service implements BarCodeReader.DecodeCallback, BarCodeReader.PictureCallback, BarCodeReader.PreviewCallback,
        SurfaceHolder.Callback, BarCodeReader.VideoCallback, BarCodeReader.ErrorCallback, SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = "Se4710Service";
    /**
     * 扫描头初始化标志
     */
    private boolean mIsInit = false;
    /**
     * 是否正在扫描，避免多次触发扫描
     */
    private boolean mIsScanning = false;
    /**
     * 管理ConfigFragment中的各Preference中的存储的值
     */
    private SharedPreferences mDefaultSharedPreferences;
    /**
     * 按键广播接收者，接收按键广播，开始扫描
     */
    private BroadcastReceiver mKeyReceiver = new KeyBroadcastReceiver();
    /**
     * 屏幕状态监听广播接收者，熄屏时关闭扫描头，亮屏时打开扫描头
     */
    private BroadcastReceiver mScreenStatReciver = new ScreenBroadcastReceiver();
    /**
     * 土司
     */
    private Toast mToast;
    /**
     * 音频播放
     */
    private SoundPoolMgr mSoundPoolMgr;
    /**
     * 线程池管理器
     */
    private ThreadFactory threadFactory = Executors.defaultThreadFactory();
    private ExecutorService mExecutorService = new ThreadPoolExecutor(3, 200, 0L,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024), threadFactory, new ThreadPoolExecutor.AbortPolicy());
    private ScheduledExecutorService mScheduledExecutorService = new ScheduledThreadPoolExecutor(3, threadFactory);
    /**
     * 本地广播管理器
     */
    private LocalBroadcastManager mLbm;
    /**
     * 扫描头控制器
     */
    public static BarCodeReader bcr = null;
    /**
     * 用于接收设置扫描参数，开启扫描，关闭服务等指令
     */
    private ScanCommandBroadcast mSettingsReceiver;
    /**
     * 预览控件，请勿改变该变量的任何属性
     */
    public SurfaceTexture mSurfaceTexture;
    /**
     * 用来判断是否允许进行开始扫描和停止扫描操作，避免两个操作之间间隔过小，最小间隔不小于50ms
     */
    private boolean mIsAllowableNextOperation = true;
    private boolean mIsWaiting = false;

    private class ScanCommandBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // System Camera call
            String action = intent.getAction() == null ? ServiceActionKey.ACTION_NULL : intent.getAction();
            LogUtils.i(TAG, "onReceive, action:" + action);
            switch (action) {
                case ServiceActionKey.ACTION_NULL:
                    Log.i("Huang," + TAG, "ScanCommandBroadcast, receive action = null");
                    break;
                case ServiceActionKey.ACTION_SCAN_INIT:
                    initReader(intent);
                    break;
                case ServiceActionKey.ACTION_SCAN_TIME:
                    assignDecodeTimeout(intent);
                    break;
                case ServiceActionKey.ACTION_ILLUMINATION:
                    assignIllumination(intent);
                    break;
                case ServiceActionKey.ACTION_AIMING_PATTERN:
                    assignAimingPattern(intent);
                    break;
                case ServiceActionKey.ACTION_SET_SCAN_MODE:
                    assignResultMode(intent);
                    break;
                case ServiceActionKey.ACTION_ILLUMINATION_LEVEL:
//                    setIlluminationLevel(callFromService, intent);
                    break;
                case ServiceActionKey.ACTION_KEY_SET:
//                    setScanKey(callFromService, intent);
                    assignScanKey(intent);
                    break;
                case ServiceActionKey.ACTION_SCAN_PARAM:
//                    setScanParam(intent);
                    assignScanParam(intent);
                    break;
                case ServiceActionKey.ACTION_SCAN:
                    startScan();
                    break;
                case ServiceActionKey.ACTION_STOP_SCAN:
                    stopScan();
                    break;
                case ServiceActionKey.ACTION_CLOSE_SCAN:
                    uninitReader(intent);
                    break;
                case ServiceActionKey.ACTION_SCAN_VOICE:
                    assignScanVoice(intent);
                    break;
                case ServiceActionKey.ACTION_SCAN_VIBERATE:
                    assignScanViberate(intent);
                    break;
                case ServiceActionKey.ACTION_SCAN_CONTINUOUS:
                    assignScanContinuous(intent);
                    break;
                case ServiceActionKey.ACTION_SCAN_INTERVAL:
                    assignScanConitnuousInterval(intent);
                    break;
                case ServiceActionKey.ACTION_SCAN_FILTER_BLANK:
                    assignScanFilterBlank(intent);
                    break;
                case ServiceActionKey.ACTION_SCAN_FILTER_INVISIBLE_CHARS:
                    assignScanFilterInvisibleChars(intent);
                    break;
                case ServiceActionKey.ACTION_SCAN_PREFIX:
                    assignScanPrefix(intent);
                    break;
                case ServiceActionKey.ACTION_SCAN_SUFFIX:
                    assignScanSuffix(intent);
                    break;
                case ServiceActionKey.ACTION_SCAN_END_CHAR:
                    assignScanEndChar(intent);
                    break;
                default:
                    break;
            }
        }
    }

    private BroadcastReceiver mLocalReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.e(TAG, "onReceive: action=" + intent.getAction());
            boolean isShow = intent.getBooleanExtra("isShow", false);
            updateFloatButton(isShow);
        }
    };

    public Se4710Service() {
    }

    @Override
    public void onCreate() {
        LogUtils.i(TAG, "onCreate");
        // Register scan command BroadCastReceiver
        mSettingsReceiver = new ScanCommandBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ServiceActionKey.ACTION_SCAN_BOOT_INIT);
        intentFilter.addAction(ServiceActionKey.ACTION_SCAN_INIT);
        intentFilter.addAction(ServiceActionKey.ACTION_SCAN_TIME);
        intentFilter.addAction(ServiceActionKey.ACTION_SET_SCAN_MODE);
        intentFilter.addAction(ServiceActionKey.ACTION_KEY_SET);
        intentFilter.addAction(ServiceActionKey.ACTION_SCAN);
        intentFilter.addAction(ServiceActionKey.ACTION_SCAN_CONTINUOUS);
        intentFilter.addAction(ServiceActionKey.ACTION_SCAN_INTERVAL);
        intentFilter.addAction(ServiceActionKey.ACTION_STOP_SCAN);
        intentFilter.addAction(ServiceActionKey.ACTION_CLOSE_SCAN);
        intentFilter.addAction(ServiceActionKey.ACTION_SCAN_PARAM);
        intentFilter.addAction(ServiceActionKey.ACTION_SCAN_VOICE);
        intentFilter.addAction(ServiceActionKey.ACTION_SCAN_VIBERATE);
        intentFilter.addAction(ServiceActionKey.ACTION_ILLUMINATION);
        intentFilter.addAction(ServiceActionKey.ACTION_AIMING_PATTERN);
        intentFilter.addAction(ServiceActionKey.ACTION_SCAN_FILTER_BLANK);
        intentFilter.addAction(ServiceActionKey.ACTION_SCAN_FILTER_INVISIBLE_CHARS);
        intentFilter.addAction(ServiceActionKey.ACTION_SCAN_PREFIX);
        intentFilter.addAction(ServiceActionKey.ACTION_SCAN_SUFFIX);
        intentFilter.addAction(ServiceActionKey.ACTION_SCAN_END_CHAR);
        registerReceiver(mSettingsReceiver, intentFilter);
        // 注册按键广播接收者
        IntentFilter keyReceiverFilter = new IntentFilter();
        keyReceiverFilter.addAction("android.intent.action.FUN_KEY");
        keyReceiverFilter.addAction("android.rfid.FUN_KEY");
        registerReceiver(mKeyReceiver, keyReceiverFilter);
        IntentFilter screenFilter = new IntentFilter();
        screenFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenStatReciver, screenFilter);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.i(TAG, "onStartCommand");
        mDefaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        mSoundPoolMgr = SoundPoolMgr.getInstance(this);
        mLbm = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter(ServiceActionKey.ACTION_FLOAT_BUTTON);
        mLbm.registerReceiver(mLocalReceiver, intentFilter);
        boolean aBoolean = mDefaultSharedPreferences.getBoolean(PreferenceKey.KEY_SWITCH_SCAN, false);
        if (aBoolean) {
            Intent initIntent = new Intent();
            initIntent.putExtra("iscamera", false);
            initReader(initIntent);
        }
        setNotification();
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogUtils.i(TAG, "onBind");
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 注销广播
        unregisterReceiver(mSettingsReceiver);
        unregisterReceiver(mKeyReceiver);
        unregisterReceiver(mLocalReceiver);
        // 释放音频资源
        mSoundPoolMgr.release();
    }

    /**
     * ------------------------------------------------------SE4710监听回调-------------------------------------------------------------------
     */
    @Override
    public void onDecodeComplete(int symbology, int length, byte[] data, BarCodeReader reader) {
        LogUtils.i(TAG, "onDecodeComplete : symbology = " + symbology);
        LogUtils.i(TAG, "onDecodeComplete : length = " + length);
        /*
         * When symbology is 0 and length is 0, it's the result of decode timeout
         * When symbology is 0 and length is -1, canceled by the user
         */
        if (data != null && length > 0) {
            byte[] codeData = new byte[length];
            System.arraycopy(data, 0, codeData, 0, length);
            sendData(codeData, (byte) symbology);
            boolean voiceEnabled = mDefaultSharedPreferences.getBoolean(PreferenceKey.KEY_SCANNING_VOICE, true);
            if (voiceEnabled) {
                mSoundPoolMgr.play(1);
                LogUtils.e(TAG, "onDecodeComplete, playVoice");
            }
            boolean vibrate = mDefaultSharedPreferences.getBoolean(PreferenceKey.KEY_SCANNING_VIBRATOR, false);
            if (vibrate) {
                vibrate();
                LogUtils.i(TAG, "onDecodeComplete, vibrate ");
            }
            // Reset stat to waiting
            continuousScan();
        } else if (length == -1) {
            // Reset stat to waiting
            mIsScanning = false;
        } else if (length == 0) {
            continuousScan();
        }
    }

    private void continuousScan() {
        boolean continuousModeFlag = mDefaultSharedPreferences.getBoolean(PreferenceKey.KEY_CONTINUOUS_SCANNING, false);
        boolean isLooping = mDefaultSharedPreferences.getBoolean("mIsLooping", false);
        LogUtils.d(TAG, "continuousScan continuousModeFlag=" + continuousModeFlag + ", isLooping=" + isLooping);
        if (continuousModeFlag && isLooping) {
            // 连续扫描
            String intervalStr = mDefaultSharedPreferences.getString(PreferenceKey.KEY_SCANNING_INTERVAL, "1000");
            int interval = Integer.valueOf(intervalStr != null ? intervalStr : "1000");
            mScheduledExecutorService.schedule(new Runnable() {
                @Override
                public void run() {
                    boolean continuousModeFlag = mDefaultSharedPreferences.getBoolean(PreferenceKey.KEY_CONTINUOUS_SCANNING, false);
                    boolean isLooping = mDefaultSharedPreferences.getBoolean("mIsLooping", false);
                    LogUtils.d(TAG, "continuousScan continuousModeFlag=" + continuousModeFlag + ", isLooping=" + isLooping);
                    if (continuousModeFlag && isLooping) {
                        bcr.startDecode();
                    }
                }
            }, interval, TimeUnit.MILLISECONDS);
        } else {
            bcr.stopDecode();
            mIsScanning = false;
        }
    }

    @Override
    public void onEvent(int event, int info, byte[] data, BarCodeReader reader) {

    }

    @Override
    public void onPictureTaken(int format, int width, int height, byte[] data, BarCodeReader reader) {

    }

    @Override
    public void onVideoFrame(int format, int width, int height, byte[] data, BarCodeReader reader) {

    }

    @Override
    public void onPreviewFrame(byte[] data, BarCodeReader reader) {

    }

    @Override
    public void onError(int error, BarCodeReader reader) {
        LogUtils.i(TAG, "error = " + error);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        // TODO Auto-generated method stub
        Log.d("Huang," + TAG, "onFrameAvailable");
    }

    /**
     * ------------------------------------------------------SE4710扫描控制接口-------------------------------------------------------------------
     * 开启扫描头，连接扫描头
     */
    private void initReader(Intent intent) {
        LogUtils.i(TAG, "initReader initPrevious = " + mIsInit);
        boolean iscamera = intent.getBooleanExtra("iscamera", false);
        boolean needReInit = mDefaultSharedPreferences.getBoolean("needReInit", false);
        if (iscamera && !needReInit) {
            LogUtils.e(TAG, "initReader, open action from camera or screen on, don't need re-init");
            return;
        }
        try {
            if (!mIsInit) {
                boolean switchScan = mDefaultSharedPreferences.getBoolean(PreferenceKey.KEY_SWITCH_SCAN, false);
                if (!switchScan) {
                    mDefaultSharedPreferences.edit().putBoolean(PreferenceKey.KEY_SWITCH_SCAN, true).apply();
                }
                setNotification();
                // 开启悬浮窗
                boolean aBoolean = mDefaultSharedPreferences.getBoolean(PreferenceKey.KEY_FLOAT_BUTTON, false);
                LogUtils.e(TAG, "updateFloatButton: aBoolean=" + aBoolean);
                updateFloatButton(aBoolean);
                bcr = BarCodeReader.open(2, getApplicationContext());
                // add callback
                bcr.setDecodeCallback(this);
                bcr.setErrorCallback(this);
                // Set parameter - Uncomment for QC/MTK platforms
                // For QC/MTK platforms
                bcr.setParameter(765, 0);
                bcr.setParameter(764, 5);
//            bcr.setParameter(8610, 1);
                // Set Orientation
                // 4 - omnidirectional
                bcr.setParameter(687, 4);
                if (android.os.Build.VERSION.SDK_INT >= 28) {
                    mSurfaceTexture = new SurfaceTexture(5);
                    mSurfaceTexture.setOnFrameAvailableListener(this);
                    bcr.setPreviewTexture(mSurfaceTexture);
                }
                mIsInit = true;
                // load previous settings
                String timeout = mDefaultSharedPreferences.getString(PreferenceKey.KEY_DECODE_TIME, "5000");
                assignDecodeTimeout(new Intent().putExtra("time", timeout));
                int illu = mDefaultSharedPreferences.getBoolean(PreferenceKey.KEY_SCANNING_ILLUMINATION, true) ? 1 : 0;
                assignIllumination(new Intent().putExtra(PreferenceKey.KEY_SCANNING_ILLUMINATION, illu));
                int anInt = mDefaultSharedPreferences.getBoolean(PreferenceKey.KEY_SCANNING_AIMING_PATTERN, true) ? 1 : 0;
                assignAimingPattern(new Intent().putExtra(PreferenceKey.KEY_SCANNING_AIMING_PATTERN, anInt));
                initSymbologies();
                mDefaultSharedPreferences.edit().putBoolean("needReInit", false).apply();
//            setDecoderLightMod(true, new Intent().putExtra(PreferenceKey.KEY_LIGHTS_CONFIG, lightMod));
//            String illuminationLevel = mDefaultSharedPreferences.getString(PreferenceKey.KEY_ILLUMINATION_LEVEL, "4");
//            setIlluminationLevel(true, new Intent().putExtra(PreferenceKey.KEY_ILLUMINATION_LEVEL, illuminationLevel));
            }
        } catch (Exception e) {
//            if (e.getMessage().contains("Failed to connect to reader service")) {
//                initReader();
//            } else {
            e.printStackTrace();
//            }
        }
    }

    private void initSymbologies() {
        SymbologyDao symbologyDao = Se4710Application.getInstances().getDaoSession().getSymbologyDao();
        List<Symbology> list = symbologyDao.queryBuilder().where(SymbologyDao.Properties.ParamNeedSet.eq(1)).list();
        for (Symbology symbology :
                list) {
            LogUtils.e(TAG, "initSymbologies, setPram " + symbology.getParamName() + symbology.getParamValue());
            bcr.setParameter(symbology.getParamNum(), symbology.getParamValue());
        }
    }

    /**
     * 扫描超时时间设置，默认为99*100ms
     */
    private void assignDecodeTimeout(Intent intent) {
        if (!mIsInit) {
            Log.i("Huang," + TAG, "assignDecodeTimeout fail! Reader is not init, init first");
        } else {
            String decodeTimeStr = intent.getStringExtra("time");
            try {
                if (RegularUtils.verifyScanTimeout(decodeTimeStr)) {
                    mDefaultSharedPreferences.edit().putString(PreferenceKey.KEY_DECODE_TIME, decodeTimeStr).apply();
                    int decodeTimeout = Integer.valueOf(decodeTimeStr) / 100;
                    if (decodeTimeout == 100) {
                        decodeTimeout = 99;
                    }
                    bcr.setParameter(BarCodeReader.ParamNum.LASER_ON_PRIM, decodeTimeout);
                } else {
                    Log.i("Huang," + TAG, "assignDecodeTimeout fail! Parameter error");
                }
            } catch (Exception e) {
                Log.i("Huang," + TAG, "assignDecodeTimeout fail! Parameter error");
            }
        }
    }

    /**
     * 扫描头照明光开关设置，默认为关
     */
    private void assignIllumination(Intent intent) {
        if (mIsInit) {
            int intExtra = intent.getIntExtra(PreferenceKey.KEY_SCANNING_ILLUMINATION, 1);
            if (RegularUtils.verifyIllumAim(intExtra)) {
                int i = bcr.getNumParameter(BarCodeReader.ParamNum.IMG_ILLUM);
                if (intExtra != i) {
                    bcr.setParameter(BarCodeReader.ParamNum.IMG_ILLUM, intExtra);
                    mDefaultSharedPreferences.edit().putBoolean(PreferenceKey.KEY_SCANNING_ILLUMINATION, intExtra == 1).apply();
                }
            } else {
                Log.i("Huang," + TAG, "assignIllumination fail! Parameter error");
            }
        } else {
            Log.i("Huang," + TAG, "assignIllumination fail! Reader is not init, init first");
        }
    }

    /**
     * 扫描头瞄准开关设置，默认为开
     */
    private void assignAimingPattern(Intent intent) {
        if (mIsInit) {
            int intExtra = intent.getIntExtra(PreferenceKey.KEY_SCANNING_AIMING_PATTERN, 1);
            if (RegularUtils.verifyIllumAim(intExtra)) {
                int i = bcr.getNumParameter(BarCodeReader.ParamNum.IMG_AIM_MODE);
                if (intExtra != i) {
                    bcr.setParameter(BarCodeReader.ParamNum.IMG_AIM_MODE, intExtra);
                    mDefaultSharedPreferences.edit().putBoolean(PreferenceKey.KEY_SCANNING_AIMING_PATTERN, intExtra == 1).apply();
                }
            } else {
                Log.i("Huang," + TAG, "assignAimingPattern fail! Parameter error");
            }
        } else {
            Log.i("Huang," + TAG, "assignAimingPattern fail! Reader is not init, init first");
        }
    }

    /**
     * 条码或二维码数据的发送方式设置，0(Broadcast), 1(Focus), 2(EmuKey), 3(Clipboard)
     */
    private void assignResultMode(Intent intent) {
        if (!mIsInit) {
            Log.i("Huang," + TAG, "assignResultMode fail! Reader is not init, init first");
        } else {
            try {
                int mode = intent.getIntExtra("mode", 1);
                if (RegularUtils.verifyResultMode(mode)) {
                    mDefaultSharedPreferences.edit().putString(PreferenceKey.KEY_INPUT_CONFIG, String.valueOf(mode)).apply();
                } else {
                    Log.i("Huang," + TAG, "assignResultMode fail! Parameter error");
                }
            } catch (Exception e) {
                Log.i("Huang," + TAG, "assignResultMode fail! error:" + Log.getStackTraceString(e));
            }
        }
    }

    /**
     * 设置可以触发扫描的按键
     */
    private void assignScanKey(Intent intent){
        if (!mIsInit) {
            Log.i("Huang," + TAG, "assignScanKey fail! Reader is not init, init first");
        } else {
            try {
                String[] keyValueArray = intent.getStringArrayExtra("keyValueArray");
                for (String keyValue : keyValueArray) {
                    if (keyValue.contains("13")) {
                        boolean keyFlag = intent.getBooleanExtra(keyValue, true);
                        mDefaultSharedPreferences.edit().putBoolean(keyValue, keyFlag).apply();
                    } else {
                        Log.i("Huang," + TAG, "assignScanKey fail! param error");
                    }
                }
            } catch (Exception e) {
                Log.i("Huang," + TAG, "assignScanKey fail! error:" + Log.getStackTraceString(e));
            }
        }
    }

    /**
     * 扫描头参数设置
     */
    private void assignScanParam(Intent intent){
        if (!mIsInit) {
            Log.i("Huang," + TAG, "assignScanParam fail! Reader is not init, init first");
        } else {
            try {
                int paramNum = intent.getIntExtra("paramNum", -1);
                if (paramNum <= -1){
                    paramNum = intent.getIntExtra("number", -1);
                    if (paramNum <= -1) {
                        Log.i("Huang," + TAG, "assignScanParam fail! Parameter paramNum error");
                    } else {
                        intent.getIntExtra("value", -1);
                        // TODO: 2019/12/20 如何兼容东大的扫描接口的条码参数设置广播？
                    }
                } else {
                    int paramVal = intent.getIntExtra("paramVal", -1);
                    if (paramVal <= -1){
                        Log.i("Huang," + TAG, "assignScanParam fail! Parameter paramVal error");
                    } else {
                        bcr.setParameter(paramNum, paramVal);
                    }
                }
            } catch (Exception e) {
                Log.i("Huang," + TAG, "assignScanParam fail! error:" + Log.getStackTraceString(e));
            }
        }
    }

    /**
     * 开始扫描
     */
    private void startScan() {
        if (mIsInit && !mIsScanning) {
            if (!mIsWaiting) {
                if (mIsAllowableNextOperation) {
                    boolean continuousFlag = mDefaultSharedPreferences.getBoolean(PreferenceKey.KEY_CONTINUOUS_SCANNING, false);
                    LogUtils.i(TAG, "startScan: continuousFlag=" + continuousFlag);
                    if (continuousFlag) {
                        // 按键广播接收者里面的isLooping的标志
                        mDefaultSharedPreferences.edit().putBoolean("mIsLooping", true).apply();
                    }
                    // Start decode
                    bcr.startDecode();
                    mIsScanning = true;
                    mIsAllowableNextOperation = false;
                    changeOperationFlag();
                    LogUtils.i(TAG, "startScan, completed");
                } else {
                    LogUtils.i(TAG, "startScan, mIsAllowableNextOperation:" + false);
                    mIsWaiting = true;
                    mScheduledExecutorService.schedule(new Runnable() {
                        @Override
                        public void run() {
                            boolean continuousFlag = mDefaultSharedPreferences.getBoolean(PreferenceKey.KEY_CONTINUOUS_SCANNING, false);
                            if (continuousFlag) {
                                // 按键广播接收者里面的isLooping的标志
                                mDefaultSharedPreferences.edit().putBoolean("mIsLooping", true).apply();
                            }
                            bcr.startDecode();
                            mIsAllowableNextOperation = false;
                            mIsWaiting = false;
                            mIsScanning = true;
                            changeOperationFlag();
                            LogUtils.i(TAG, "startScan, completed");
                        }
                    }, 50, TimeUnit.MILLISECONDS);
                }
            } else {
                LogUtils.i(TAG, "startScan, operation reject, scanner is waiting for next operation");
            }
        } else {
            Log.i("Huang," + TAG, "startScan fail! Reader is not init or is busy now");
        }
    }

    /**
     * 停止扫描
     */
    private void stopScan() {
        if (mIsInit && mIsScanning) {
            if (!mIsWaiting) {
                if (mIsAllowableNextOperation) {
                    boolean isLooping = mDefaultSharedPreferences.getBoolean("mIsLooping", false);
                    if (isLooping) {
                        // 重置按键广播接收者里面的isLooping的标志
                        mDefaultSharedPreferences.edit().putBoolean("mIsLooping", false).apply();
                    }
                    if (mIsScanning) {
                        bcr.stopDecode();
                        // Key is released
                        mIsScanning = false;
                        mIsAllowableNextOperation = false;
                        changeOperationFlag();
                        LogUtils.i(TAG, "stopScan, completed");
                    }
                } else {
                    LogUtils.i(TAG, "stopScan, mIsAllowableNextOperation:" + false);
                    mIsWaiting = true;
                    mScheduledExecutorService.schedule(new Runnable() {
                        @Override
                        public void run() {
                            boolean isLooping = mDefaultSharedPreferences.getBoolean("mIsLooping", false);
                            if (isLooping) {
                                // 重置按键广播接收者里面的isLooping的标志
                                mDefaultSharedPreferences.edit().putBoolean("mIsLooping", false).apply();
                            }
                            if (mIsScanning) {
                                bcr.stopDecode();
                                // Key is released
                                mIsAllowableNextOperation = false;
                                mIsScanning = false;
                                changeOperationFlag();
                                LogUtils.i(TAG, "stopScan, completed");
                            }
                            mIsWaiting = false;
                        }
                    }, 50, TimeUnit.MILLISECONDS);
                }
            } else {
                LogUtils.i(TAG, "stopScan, operation reject, scanner is waiting for next operation");
            }
        } else if (!mIsInit) {
            Log.i("Huang," + TAG, "stopScan fail! Reader is not init");
        } else {
            Log.i("Huang," + TAG, "stopScan fail! Reader event don't work");
        }
    }

    /**
     * 改变操作控制标记，用以控制开始扫描和停止扫描之间的间隔
     */
    private void changeOperationFlag() {
        mScheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                mIsAllowableNextOperation = true;
            }
        }, 15, TimeUnit.MILLISECONDS);
    }

    /**
     * 断开扫描头连接
     */
    private void uninitReader(Intent intent) {
        if (mIsInit) {
            stopScan();
            bcr.release();
            bcr = null;
            mIsInit = false;
            mDefaultSharedPreferences.edit().putBoolean(PreferenceKey.KEY_SWITCH_SCAN, false).apply();
            setNotification();
            updateFloatButton(false);
            LogUtils.i(TAG, "uninitReader, close engine");
            boolean iscamera = intent.getBooleanExtra("iscamera", false);
            if (iscamera) {
                LogUtils.e(TAG, "uninitReader, close action from camera or screen off, storage state");
                mDefaultSharedPreferences.edit().putBoolean("needReInit", true).apply();
            }
        } else {
            Log.i("Huang," + TAG, "uninitReader fail! Reader is not init");
        }
    }

    /**
     * 扫描声音开关设置，默认为开
     */
    private void assignScanVoice(Intent intent) {
        if (mIsInit) {
            boolean soundPlay = intent.getBooleanExtra("sound_play", true);
            mDefaultSharedPreferences.edit().putBoolean(PreferenceKey.KEY_SCANNING_VOICE, soundPlay).apply();
        } else {
            Log.i("Huang," + TAG, "assignScanVoice fail! Reader is not init");
        }
    }

    /**
     * 扫描振动开关设置，默认为关
     */
    private void assignScanViberate(Intent intent) {
        if (mIsInit) {
            boolean viberate = intent.getBooleanExtra("viberate", false);
            mDefaultSharedPreferences.edit().putBoolean(PreferenceKey.KEY_SCANNING_VIBRATOR, viberate).apply();
        } else {
            Log.i("Huang," + TAG, "assignScanViberate fail! Reader is not init");
        }
    }

    /**
     * 连续扫描开关设置，默认为关
     */
    private void assignScanContinuous(Intent intent) {
        if (mIsInit) {
            boolean continuousMode = intent.getBooleanExtra("ContinuousMode", false);
            LogUtils.i(TAG, "assignScanContinuous, receive action set continuous mode:" + continuousMode);
            mDefaultSharedPreferences.edit().putBoolean(PreferenceKey.KEY_CONTINUOUS_SCANNING, continuousMode).apply();
            // 启用连续扫描后，禁用抬起停止扫描设置项；反之亦然
            mDefaultSharedPreferences.edit().putBoolean(PreferenceKey.KEY_STOP_ON_UP, !continuousMode).apply();
        } else {
            Log.i("Huang," + TAG, "assignScanContinuous fail! Reader is not init");
        }
    }

    /**
     * 连续扫描间隔时间设置，默认为1000ms
     */
    private void assignScanConitnuousInterval(Intent intent) {
        if (mIsInit) {
            String continuousInternal = intent.getStringExtra("ContinuousInternal");
            LogUtils.i(TAG, "assignScanConitnuousInterval, receive action set continuousInternal:" + continuousInternal);
            if (RegularUtils.verifyScanInterval(continuousInternal)) {
                mDefaultSharedPreferences.edit().putString(PreferenceKey.KEY_SCANNING_INTERVAL, continuousInternal).apply();
            } else {
                Log.i("Huang," + TAG, "assignScanConitnuousInterval fail! Param error");
            }
        } else {
            Log.i("Huang," + TAG, "assignScanConitnuousInterval fail! Reader is not init");
        }
    }

    /**
     * 过滤首尾空格开关设置，默认为关
     */
    private void assignScanFilterBlank(Intent intent) {
        if (mIsInit) {
            boolean filterPrefixSuffixBlank = intent.getBooleanExtra("filter_prefix_suffix_blank", false);
            mDefaultSharedPreferences.edit().putBoolean(PreferenceKey.KEY_RM_SPACE, filterPrefixSuffixBlank).apply();
        } else {
            Log.i("Huang," + TAG, "assignScanFilterBlank fail! Reader is not init");
        }
    }

    /**
     * 过滤不可见字符开关设置，默认为关
     */
    private void assignScanFilterInvisibleChars(Intent intent) {
        if (mIsInit) {
            boolean filterInvisibleChars = intent.getBooleanExtra("filter_invisible_chars", false);
            mDefaultSharedPreferences.edit().putBoolean(PreferenceKey.KEY_INVISIBLE_CHAR, filterInvisibleChars).apply();
        } else {
            Log.i("Huang," + TAG, "assignScanFilterInvisibleChars fail! Reader is not init");
        }
    }

    /**
     * 条码前缀设置, 默认为""
     */
    private void assignScanPrefix(Intent intent) {
        if (mIsInit) {
            String prefix = intent.getStringExtra("prefix");
            if (prefix == null) {
                Log.i("Huang," + TAG, "assignScanPrefix fail! param error");
            } else {
                mDefaultSharedPreferences.edit().putString(PreferenceKey.KEY_PREFIX_CONFIG, prefix).apply();
            }
        } else {
            Log.i("Huang," + TAG, "assignScanPrefix fail! Reader is not init");
        }
    }

    /**
     * 条码后缀设置，默认为""
     */
    private void assignScanSuffix(Intent intent) {
        if (mIsInit) {
            String suffix = intent.getStringExtra("suffix");
            if (suffix == null) {
                Log.i("Huang," + TAG, "assignScanSuffix fail! param error");
            } else {
                mDefaultSharedPreferences.edit().putString(PreferenceKey.KEY_SUFFIX_CONFIG, suffix).apply();
            }
        } else {
            Log.i("Huang," + TAG, "assignScanSuffix fail! Reader is not init");
        }
    }

    /**
     * 条码和二维码追缴结束符设置
     * "1" -- ENTER, "2" -- TAB, "3" -- SPACE, "4" -- NONE
     */
    private void assignScanEndChar(Intent intent) {
        if (mIsInit) {
            String endchar = intent.getStringExtra("endchar");
            if (endchar == null) {
                Log.i("Huang," + TAG, "assignScanEndChar fail! param error");
            } else {
                String endCharValue = "4";
                switch (endchar) {
                    case "ENTER":
                        endCharValue = "1";
                        break;
                    case "TAB":
                        endCharValue = "2";
                        break;
                    case "SPACE":
                        endCharValue = "3";
                        break;
                    case "NONE":
                        endCharValue = "4";
                        break;
                    default:
                        Log.i("Huang," + TAG, "assignScanEndChar fail! param error");
                        return;
                }
                mDefaultSharedPreferences.edit().putString(PreferenceKey.KEY_APPEND_ENDING_CHAR, endCharValue).apply();
            }
        } else {
            Log.i("Huang," + TAG, "assignScanEndChar fail! Reader is not init");
        }
    }

    /**
     * -------------------------------------------------------------------------------------------------------------------------------------------
     * 显示前台进程通知，以减少Service被kill的几率
     */
    private void setNotification() {
        boolean isOpen = mDefaultSharedPreferences.getBoolean(PreferenceKey.KEY_SWITCH_SCAN, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
        String channelId = "SoftScanService_Channel_1";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(getString(R.string.app_name));
        LogUtils.e(TAG, "setNotification, isOpen: " + isOpen);
        if (isOpen) {
            builder.setContentText(getString(R.string.service_started));
        } else {
            builder.setContentText(getString(R.string.service_stopped));
        }
        //添加事件
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        Intent intent = new Intent(this, TabHostActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, flags);
        builder.setContentIntent(pi);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.scan));
        Notification notification = builder.build();
        notification.flags |= FLAG_NO_CLEAR;
        startForeground(R.string.app_name, notification);
    }

    /**
     * 显示或隐藏悬浮按钮
     */
    private void updateFloatButton(boolean isShow) {
        if (isShow) {
            Intent floatButtonIntent = new Intent(Se4710Service.this.getApplicationContext(), FloatService.class);
            startService(floatButtonIntent);
        } else {
            Intent floatButtonIntent = new Intent(Se4710Service.this.getApplicationContext(), FloatService.class);
            stopService(floatButtonIntent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel("SoftScanService_Channel_1", "SoftScanService_Channel_1", NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    /**
     * 发送扫码数据
     *
     * @param data 扫码数据
     */
    private void sendData(byte[] data, byte sym) {
        String inputMod = mDefaultSharedPreferences.getString(PreferenceKey.KEY_INPUT_CONFIG, "1");
        inputMod = inputMod == null ? "1" : inputMod;
        switch (inputMod) {
            case "0":
                // 广播发送扫描数据
                broadScanResult(data, sym);
                break;
            case "1":
                // 焦点录入
                try {
                    String result = bytesToString(data);
                    sendToInput(result);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    showToast(Se4710Service.this.getString(R.string.charset_err));
                }
                break;
            case "2":
                // 模拟按键输入
                try {
                    String result = bytesToString(data);
                    softInput(result);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    showToast(Se4710Service.this.getString(R.string.charset_err));
                }
                break;
            case "3":
                // 剪切板
                try {
                    String result = bytesToString(data);
                    copyToClipboard(result);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    showToast(Se4710Service.this.getString(R.string.charset_err));
                }
                break;
            default:
                break;
        }
        // 发送至本软件中的ScanTestFragment中显示
        Intent intent = new Intent();
        intent.putExtra("data", data);
        intent.putExtra("code_id", sym);
        intent.setAction(ServiceActionKey.ACTION_SCAN_RESULT);
        mLbm.sendBroadcast(intent);
    }

    /**
     * 将扫描结果以广播的形式发送，其他APP可以通过注册Action为"com.rfid.SCAN"的广播，接收扫描到的数据
     */
    private void broadScanResult(byte[] data, int codeId) {
        Intent intent = new Intent();
        intent.putExtra("data", data);
        try {
            String result = bytesToString(data);
            intent.putExtra("scannerdata", result);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            showToast(Se4710Service.this.getString(R.string.charset_err));
        }
        intent.putExtra("code_id", codeId);
        intent.setAction(ServiceActionKey.ACTION_SCAN_RESULT);
        sendBroadcast(intent);
    }

    /**
     * 将扫描结果转换为字符
     */
    private String bytesToString(byte[] data) throws UnsupportedEncodingException {
        String utf8Num = "1";
        String gbkNum = "2";
        String charsetNum = mDefaultSharedPreferences.getString(PreferenceKey.KEY_RESULT_CHAR_SET, "1");
        charsetNum = charsetNum == null ? "1" : charsetNum;
        String result = "";
        LogUtils.i(TAG, "charsetNum = " + charsetNum);
        if (charsetNum.equals(utf8Num)) {
            result = new String(data, 0, data.length, StandardCharsets.UTF_8);
            LogUtils.i(TAG, "onDecodeComplete : data = " + Arrays.toString(data));
            LogUtils.i(TAG, "onDecodeComplete : data = " + result);
        } else if (charsetNum.equals(gbkNum)) {
            result = new String(data, 0, data.length, "GBK");
            LogUtils.i(TAG, "onDecodeComplete : data = " + Arrays.toString(data));
            LogUtils.i(TAG, "onDecodeComplete : data = " + result);
        }
        // 去除不可见字符
        boolean filterInvisibleChar = mDefaultSharedPreferences.getBoolean(PreferenceKey.KEY_INVISIBLE_CHAR, false);
        LogUtils.i(TAG, "bytesToString, filterInvisibleChar:" + filterInvisibleChar);
        if (filterInvisibleChar) {
            result = filter(result);
        }
        // 去除首尾空格
        boolean filterSpace = mDefaultSharedPreferences.getBoolean(PreferenceKey.KEY_RM_SPACE, false);
        if (filterSpace) {
            result = result.trim();
        }
        return result;
    }

    /**
     * 去除不可见字符
     */
    public static String filter(String content) {
        if (content != null && content.length() > 0) {
            char[] contentCharArr = content.toCharArray();
            char[] contentCharArrTem = new char[contentCharArr.length];
            int j = 0;
            for (char c : contentCharArr) {
                if (c >= 0x20 && c != 0x7F) {
                    contentCharArrTem[j] = c;
                    j++;
                }
            }
            return new String(contentCharArrTem, 0, j);
        }
        return "";
    }

    /**
     * 调用本司加入系统中的键盘输入方法，将结果直接输入光标处
     */
    private void sendToInput(String data) {
        boolean enterFlag = false;
        String result = fixChar(data);
        String append = appendChar();
        switch (append) {
            case "1":
                enterFlag = true;
                break;
            case "2":
                result += "\n";
                break;
            case "3":
                result += "\t";
                break;
            case "4":

                break;
            default:
                break;
        }
        Intent toBack = new Intent();
        toBack.setAction("android.rfid.INPUT");
        //发送添加前缀后缀的数据
        toBack.putExtra("data", result);
        toBack.putExtra("enter", enterFlag);
        sendBroadcast(toBack);
    }

    /**
     * 模拟键盘输入
     */
    private void softInput(final String dataStr) {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                String dataStr1 = dataStr;
                // 去除结束符
                String endChar0 = "\r";
                String endChar1 = "\n";
                if (dataStr1.contains(endChar0)) {
                    dataStr1 = dataStr1.replace("\r", "");
                }
                if (dataStr1.contains(endChar1)) {
                    dataStr1 = dataStr1.replace("\n", "");
                }
                // 拼接前后缀
                String prefixStr = mDefaultSharedPreferences.getString(PreferenceKey.KEY_PREFIX_CONFIG, "");
                String surfixStr = mDefaultSharedPreferences.getString(PreferenceKey.KEY_SUFFIX_CONFIG, "");
                dataStr1 = prefixStr + dataStr1 + surfixStr;
                Instrumentation instrumentation = new Instrumentation();
                instrumentation.sendStringSync(dataStr1);
                // 追加结束符
                String appendChar = appendChar();
                switch (appendChar) {
                    case "1":
                        // ENTER
                        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
                        break;
                    case "2":
                        // TAB
                        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_TAB);
                        break;
                    case "3":
                        // SPACE
                        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_SPACE);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * 将扫描结果复制到剪切板
     */
    private void copyToClipboard(String data){
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mClipData = ClipData.newPlainText("Label", data);
        cm.setPrimaryClip(mClipData);
    }

    /**
     * 获取存储的追加结束符
     * "1" -- ENTER, "2" -- TAB, "3" -- SPACE, "4" -- NONE
     */
    private String appendChar() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        return prefs.getString(PreferenceKey.KEY_APPEND_ENDING_CHAR, "4");
    }

    /**
     * 对扫描结果添加前缀后缀
     */
    private String fixChar(String data) {
        String result;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String prefix = prefs.getString("prefix_config", "");
        String suffix = prefs.getString("suffix_config", "");
        result = prefix + data + suffix;
        return result;
    }

    /**
     * 震动提示
     */
    private void vibrate() {
        Vibrator vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(100);
    }

    /**
     * 展示土司
     *
     * @param content 展示的内容
     */
    private void showToast(String content) {
        if (mToast == null) {
            mToast = Toast.makeText(Se4710Service.this, content, Toast.LENGTH_SHORT);
            mToast.show();
        } else {
            mToast.setText(content);
            mToast.show();
        }
    }
}
