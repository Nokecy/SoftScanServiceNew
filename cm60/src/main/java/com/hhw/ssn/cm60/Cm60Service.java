package com.hhw.ssn.cm60;

import android.app.Instrumentation;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.dawn.decoderapijni.EngineCode;
import com.dawn.decoderapijni.EngineCodeMenu;
import com.dawn.decoderapijni.SoftEngine;
import com.hhw.ssn.combean.PreferenceKey;
import com.hhw.ssn.combean.ServiceActionKey;
import com.hhw.ssn.comui.KeyBroadcastReceiver;
import com.hhw.ssn.comui.TabHostActivity;
import com.hhw.ssn.comutils.LogUtils;
import com.hhw.ssn.comutils.RegularUtils;
import com.hhw.ssn.comutils.SoundPoolMgr;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static android.support.v4.app.NotificationCompat.FLAG_NO_CLEAR;

/**
 * @author HuangLei 1252065297@qq.com
 * <code>
 * Create At 2019/4/30 18:20
 * Update By 更新者
 * Update At 2019/4/30 18:20
 * </code>
 * CM60扫描服务
 */
public class Cm60Service extends Service {

    private static final String TAG = "Cm60Service";
    /**
     * 管理PreferenceKey中的各Preference中的存储的值
     */
    private SharedPreferences mDefaultSharedPreferences;
    /**
     * 扫描头控制器
     */
    private static SoftEngine mSoftEngine;
    /**
     * 音频播放
     */
    private SoundPoolMgr mSoundPoolMgr;
    /**
     * 按键广播接收者，接收按键广播，开始扫描
     */
    private BroadcastReceiver mKeyReceiver = new KeyBroadcastReceiver();
    public static List<EngineCode> engineCodeList;
    /**
     * 本地广播管理器
     */
    private LocalBroadcastManager mLbm;

    /**
     * 初始化线程池管理类
     */
    ThreadFactory threadFactory = Executors.defaultThreadFactory();
    ExecutorService mExecutorService = new ThreadPoolExecutor(3, 200, 0L,
            TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1024), threadFactory, new ThreadPoolExecutor.AbortPolicy());
    ScheduledExecutorService mScheduledExecutorService = new ScheduledThreadPoolExecutor(3, threadFactory);

    /**
     * 定时器
     */
    private ScheduledFuture<?> mScheduledFuture;

    /**
     * 开启定时器结束扫描，timeout ms之后结束扫描
     */
    private void stopScanTimer(int timeout) {
        cancelStopScanTimer();
        mScheduledFuture = mScheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                boolean isContinuousSwitchOpen = mDefaultSharedPreferences.getBoolean(PreferenceKey.KEY_CONTINUOUS_SCANNING, false);
                LogUtils.e(TAG, "stopTimer, isContinuousSwitchOpen:" + isContinuousSwitchOpen);
                // 如果连续扫描开启，不触发超时关闭
                if (!isContinuousSwitchOpen) {
                    stopScan();
                }
            }
        }, timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * 取消定时器
     */
    private void cancelStopScanTimer() {
        if (mScheduledFuture != null) {
            mScheduledFuture.cancel(true);
        }
    }

    /**
     * 扫描头初始化标志
     */
    private boolean mIsInit = false;

    /**
     * 是否正在扫描，避免多次触发扫描
     */
    private boolean mIsScanning = false;

    /**
     * 是否处在循环（连续）扫描中
     */
    private boolean mIsLooping = false;

    private final static int SCAN_SET_DECODE_IMAGE = 0x10E1;

    /**
     * 用于接收设置扫描参数，开启扫描，关闭服务等指令
     */
    private ScanCommandBroadcast mSettingsReceiver;

    class ScanCommandBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // System Camera call
            LogUtils.e(TAG, "ScanCommandBroadcast, receive at " + System.currentTimeMillis());
            boolean iscamera = intent.getBooleanExtra("iscamera", false);
            LogUtils.i(TAG, "ScanCommandBroadcast, iscamera: " + iscamera);
            String action = intent.getAction() == null ? ServiceActionKey.ACTION_NULL : intent.getAction();
            LogUtils.i(TAG, "ScanCommandBroadcast, receive action:" + action);
            switch (action) {
                case ServiceActionKey.ACTION_NULL:
                    Log.i(TAG, "ScanCommandBroadcast, receive action = null");
                    break;
                case ServiceActionKey.ACTION_SCAN_BOOT_INIT:
                    bootInitReader();
                    break;
                case ServiceActionKey.ACTION_SCAN_INIT:
                    initReader(iscamera);
                    break;
                case ServiceActionKey.ACTION_SCAN_TIME:
                    setDecodeTimeout(intent);
                    break;
//                case ServiceActionKey.ACTION_LIGHT_CONFIG:
//                    setDecoderLightMod(intent);
//                    break;
                case ServiceActionKey.ACTION_SET_SCAN_MODE:
                    setScanMode(intent);
                    break;
                case ServiceActionKey.ACTION_ILLUMINATION_LEVEL:
                    setIlluminationLevel(intent);
                    break;
                case ServiceActionKey.ACTION_KEY_SET:
                    setScanKey(intent);
                    break;
                case ServiceActionKey.ACTION_SCAN_PARAM:
                    setScanParam(intent);
                    break;
                case ServiceActionKey.ACTION_SCAN:
                    startScan();
                    break;
                case ServiceActionKey.ACTION_SCAN_CONTINUOUS:
                    boolean continuousMode = intent.getBooleanExtra("ContinuousMode", false);
                    String continuousInternal = intent.getStringExtra("ContinuousInternal");
                    LogUtils.i(TAG, "ScanCommandBroadcast, receive action set continuous mode:" + continuousMode + ", continuousInternal:" + continuousInternal);
                    setContinuousMode(continuousMode, continuousInternal);
                    break;
                case ServiceActionKey.ACTION_STOP_SCAN:
                    stopScan();
                    break;
                case ServiceActionKey.ACTION_CLOSE_SCAN:
                    closeScan(iscamera);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
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
        intentFilter.addAction(ServiceActionKey.ACTION_STOP_SCAN);
        intentFilter.addAction(ServiceActionKey.ACTION_CLOSE_SCAN);
        intentFilter.addAction(ServiceActionKey.ACTION_SCAN_PARAM);
        registerReceiver(mSettingsReceiver, intentFilter);
        // 注册按键广播接收者
        IntentFilter keyReceiverFilter = new IntentFilter();
        keyReceiverFilter.addAction("android.intent.action.FUN_KEY");
        keyReceiverFilter.addAction("android.rfid.FUN_KEY");
        registerReceiver(mKeyReceiver, keyReceiverFilter);
        // TODO: 2019/5/5 监听屏幕状态
        // TODO: 2019/5/5 播放提示音
        LogUtils.i(TAG, "onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.i(TAG, "onStartCommand");
        mDefaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());
        mMessageHandler = new MessageHandler(this);
        mSoundPoolMgr = SoundPoolMgr.getInstance(this);
        mLbm = LocalBroadcastManager.getInstance(this);
        /*
         * PreferenceKey call or BootCompleteReceiver call, do not start scan
         */
        //initReader(true);
        setNotification();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        LogUtils.i(TAG, "onDestroy");
        closeScan(false);
        // Unregister BroadcastReceiver
        unregisterReceiver(mSettingsReceiver);
        unregisterReceiver(mKeyReceiver);
        //unregisterReceiver(powerModeReceiver);
        mSoundPoolMgr.release();
        super.onDestroy();
    }

    /**
     * 震动提示
     */
    private void vibrate() {
        Vibrator vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(100);
    }

    /**
     * ------------------------------------------------------CM60扫描接口回调实现-------------------------------------------------------------------
     */
    private static final int MSG_SEND_SCAN_RESULT = 1;
    private MessageHandler mMessageHandler;

    private static class MessageHandler extends Handler {
        private WeakReference<Cm60Service> mWeakReference;

        MessageHandler(Cm60Service cm60service) {
            mWeakReference = new WeakReference<>(cm60service);
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            final Cm60Service cm60service = mWeakReference.get();
            //处理扫描结果
            if (what == MSG_SEND_SCAN_RESULT) {
                String strResult;
                String strAim;
                byte[] data = (byte[]) msg.obj;
                int i;
                i = 0;
                while (data[i] != 0) {
                    i++;
                }
                strAim = new String((byte[]) msg.obj, 0, i);
                LogUtils.e(TAG, "Aim:" + strAim);
                strResult = new String((byte[]) msg.obj, 128, msg.arg2 - 128);
                byte[] codeBytes = new byte[msg.arg2 - 128];
                byte[] msgBytes = (byte[]) msg.obj;
                System.arraycopy(msgBytes, 128, codeBytes, 0, codeBytes.length);
                LogUtils.e(TAG, "Result:" + msg.arg2 + " body:" + strResult);
                cm60service.sendData(codeBytes, (byte) 0);
                // 播放提示
                boolean neededPlay = cm60service.mDefaultSharedPreferences.getBoolean(PreferenceKey.KEY_SCANNING_VOICE, true);
                LogUtils.i(TAG, "Result, neededPlay = " + neededPlay);
                if (neededPlay) {
                    cm60service.mSoundPoolMgr.play(1);
                }
                // 震动提示
                boolean neededVibrate = cm60service.mDefaultSharedPreferences.getBoolean(PreferenceKey.KEY_SCANNING_VIBRATOR, false);
                LogUtils.i(TAG, "Result, neededPlay = " + neededPlay);
                if (neededVibrate) {
                    cm60service.vibrate();
                }
                // 判断是否需要连续扫描
                boolean isContinuousSwitchOpen = cm60service.mDefaultSharedPreferences.getBoolean(PreferenceKey.KEY_CONTINUOUS_SCANNING, false);
                LogUtils.e(TAG, "handleMessage, isContinuousSwitchOpen:" + isContinuousSwitchOpen);
                // 重置扫描标志
                mSoftEngine.StopDecode();
                // Key is released
                cm60service.mIsScanning = false;
                if (isContinuousSwitchOpen) {
                    cm60service.mExecutorService.execute(new Runnable() {
                        @Override
                        public void run() {
                            // 如果连续扫描，开启下一次扫描
                            try {
                                String interval = cm60service.mDefaultSharedPreferences.getString(PreferenceKey.KEY_SCANNING_INTERVAL, "1000");
                                LogUtils.e(TAG, "handleMessage, scanning interval:" + interval);
                                Thread.sleep(Integer.valueOf(interval != null ? interval : "1000"));
                                LogUtils.e(TAG, "handleMessage, isContinuousSwitchOpen:" + cm60service.mDefaultSharedPreferences.getBoolean(PreferenceKey.KEY_CONTINUOUS_SCANNING, false));
                                LogUtils.e(TAG, "handleMessage, cm60service.mIsLooping:" + cm60service.mIsLooping);
                                if (cm60service.mDefaultSharedPreferences.getBoolean(PreferenceKey.KEY_CONTINUOUS_SCANNING, false) && cm60service.mIsLooping) {
                                    synchronized (Cm60Service.class) {
                                        // Start decode
                                        long startTime = System.currentTimeMillis();
                                        mSoftEngine.Open();
                                        mSoftEngine.StartDecode();
                                        LogUtils.i(TAG, "触发扫码耗时 = " + (System.currentTimeMillis() - startTime));
                                    }
                                    cm60service.mIsScanning = true;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else {
                    // 取消用于停止扫描的定时器
                    cm60service.cancelStopScanTimer();
                }
            }
            super.handleMessage(msg);
        }
    }

    public static void scanSetDecodeImage(byte[] data, int length) {
        mSoftEngine.setSoftEngineIOCtrlEx(SCAN_SET_DECODE_IMAGE, length, data);
    }

    /**
     * ------------------------------------------------------扫描头控制接口-------------------------------------------------------------------
     * <p>
     * 开机开启扫描头，连接扫描头
     */
    private void bootInitReader() {
        mSoftEngine = new SoftEngine(this);
        mExecutorService.execute(scanInitThread);
    }

    /**
     * 初始化扫描头
     *
     * @param iscamera 是否是相机退出时调用，如果是相机退出时调用且扫描开关未开启，则不进行初始化
     */
    private void initReader(boolean iscamera) {
        LogUtils.i(TAG, "initReader iscamera:" + iscamera);
        // System Camera call
        boolean isOpen = mDefaultSharedPreferences.getBoolean(PreferenceKey.KEY_SWITCH_SCAN, false);
        if (iscamera && !isOpen) {
            // Camera Call init, if the switch don't open before, and do not open when exit camera
            return;
        }
        LogUtils.i(TAG, "initReader mIsInit = " + mIsInit);
        if (!mIsInit) {
            // 初始化标志置为true
            mIsInit = true;
            // 将扫描开关的值置为true
            mDefaultSharedPreferences.edit().putBoolean(PreferenceKey.KEY_SWITCH_SCAN, true).apply();
            // 更新通知栏通知内容
            setNotification();
        }
    }

    private Runnable scanInitThread = new Runnable() {
        @Override
        public void run() {
            long startTime = System.currentTimeMillis();
            if (mSoftEngine == null) {
                Log.e(TAG, "SoftEngine.getInstance return null");
            } else {
                if (mSoftEngine.initSoftEngine()) {
                    mSoftEngine.setScanningCallback(new SoftEngine.ScanningCallback() {
                        @Override
                        public void onScanningCallback(int eventCode, int param1, byte[] param2, int length) {
                            Message msg = Message.obtain(mMessageHandler, MSG_SEND_SCAN_RESULT, 0, length, param2);
                            msg.sendToTarget();
                        }
                    });
                    mSoftEngine.Open();
                    mExecutorService.execute(scanParamThread);
                    LogUtils.e(TAG, "init time = " + (System.currentTimeMillis() - startTime));
                } else {
                    LogUtils.e(TAG, "initSoftEngine fail ");
                }
            }
        }
    };

    private Runnable scanParamThread = new Runnable() {
        @Override
        public void run() {
            try {
                Class<?> engineCodeClass = Class.forName(EngineCode.class.getName());
                engineCodeList = new ArrayList<>();
                for (EngineCodeMenu.Code1DName scanName : EngineCodeMenu.Code1DName.values()) {
                    EngineCode engineCode = new EngineCode();
                    engineCode.setName(scanName.getDname());
                    for (EngineCodeMenu.CodeParam param : EngineCodeMenu.CodeParam.values()) {
                        String data = null;
                        if (mSoftEngine != null) {
                            data = mSoftEngine.ScanGet(scanName.getDname(), param.getParamName());
                        }
                        if (data != null) {
                            LogUtils.i(TAG, "scanParam " + param.getParamName() + ", data: " + data);
                            Method method = engineCodeClass.getDeclaredMethod("set" + param.getParamName(), String.class);
                            method.setAccessible(true);
                            method.invoke(engineCode, data);
                        }
                    }
                    engineCodeList.add(engineCode);
                    LogUtils.d(TAG, "engineCodeList add :" + engineCode.getName());
                }
                // 开机启动Service时判断扫描开关是否开启，如果开启的话，需要继续初始化扫描头的操作
                boolean isOpen = mDefaultSharedPreferences.getBoolean(PreferenceKey.KEY_SWITCH_SCAN, false);
                if (isOpen) {
                    initReader(false);
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 扫描超时时间设置
     */
    private void setDecodeTimeout(Intent intent) {
        if (!mIsInit) {
            Log.i(TAG, "setDecodeTimeout fail! Reader is not init, init first");
            return;
        }
        int decodeTime = intent.getIntExtra("time", 5000);
        try {
            // TODO: 2019/5/5 设置扫描超时
            if (RegularUtils.verifyScanTimeout(String.valueOf(decodeTime))) {
                mDefaultSharedPreferences.edit().putString(PreferenceKey.KEY_DECODE_TIME, String.valueOf(decodeTime)).apply();
            } else {
                Log.i("Huang, Cm60Service", "setDecodeTimeout fail! Parameter error");
            }
        } catch (Exception e) {
            Log.i("Huang, Cm60Service", "setDecodeTimeout fail! Parameter error");
        }
    }

    /**
     * 扫描灯光模式设置
     */
    private void setDecoderLightMod(Intent intent) {
        if (!mIsInit) {
            Log.i(TAG, "setDecoderLightMod fail! Reader is not init, init first");
            return;
        }
//        String lightMod = intent.getStringExtra(PreferenceKey.KEY_LIGHTS_CONFIG);
//        switch (lightMod) {
//            case "0":
//                // 仅开启瞄准（关闭照明）
//// TODO: 2019/5/5 扫描灯光模式
//                mDefaultSharedPreferences.edit().putString(PreferenceKey.KEY_LIGHTS_CONFIG, lightMod).apply();
//                break;
//            case "1":
//                // 仅开启照明（关闭瞄准）
//
//                mDefaultSharedPreferences.edit().putString(PreferenceKey.KEY_LIGHTS_CONFIG, lightMod).apply();
//                break;
//            case "2":
//                // 同时使用
//
//                mDefaultSharedPreferences.edit().putString(PreferenceKey.KEY_LIGHTS_CONFIG, lightMod).apply();
//                break;
//            case "3":
//                // 同时关闭
//
//                mDefaultSharedPreferences.edit().putString(PreferenceKey.KEY_LIGHTS_CONFIG, lightMod).apply();
//                break;
//            default:
//                Log.i("Huang, Cm60Service", "setDecoderLightMod fail! Parameter error");
//                break;
//        }
    }

    /**
     * 条码或二维码数据的发送方式设置
     */
    private void setScanMode(Intent intent) {
        try {
            int mode = intent.getIntExtra("mode", 1);
            if (RegularUtils.verifyResultMode(mode)) {
                mDefaultSharedPreferences.edit().putString(PreferenceKey.KEY_INPUT_CONFIG, String.valueOf(mode)).apply();
            } else {
                Log.i("Huang, Cm60Service", "setScanMode fail! Parameter error");
            }
        } catch (Exception e) {
            Log.i("Huang, Cm60Service", "setScanMode fail! error:" + Log.getStackTraceString(e));
        }
    }

    /**
     * 照明光强度设置
     */
    private void setIlluminationLevel(Intent intent) {
        if (!mIsInit) {
            Log.i(TAG, "setIlluminationLevel fail! Reader is not init, init first");
            return;
        }
        String illuminationLevel = intent.getStringExtra(PreferenceKey.KEY_ILLUMINATION_LEVEL);
        try {
            // TODO: 2019/5/5 照明光强度设置
            mDefaultSharedPreferences.edit().putString(PreferenceKey.KEY_ILLUMINATION_LEVEL, illuminationLevel).apply();
        } catch (Exception e) {
            Log.i(TAG, "setIlluminationLevel fail! Parameter error");
        }
    }

    /**
     * 设置扫描按键
     */
    private void setScanKey(@NonNull Intent intent) {
        if (!mIsInit) {
            Log.i(TAG, "setScanKey fail! Reader is not init, init first");
            return;
        }
//        boolean[] scanKeyArray = intent.getBooleanArrayExtra(PreferenceKey.KEY_SCAN_KEY);
//        int requiredLen = 7;
//        if (scanKeyArray == null || scanKeyArray.length != requiredLen) {
//            Log.i(TAG, "setScanKey fail! Parameter error");
//        } else {
//            for (int i = 1; i <= requiredLen; i++) {
//                boolean flag = scanKeyArray[i - 1];
//                mDefaultSharedPreferences.edit().putBoolean(PreferenceKey.KEY_SCAN_KEY_FX + i, flag).apply();
//            }
//        }

        String keyName = intent.getStringExtra("keyName");
        SharedPreferences.Editor edit = mDefaultSharedPreferences.edit();
        LogUtils.i(TAG, "setScanKey, keyName:" + keyName);
        if (keyName.contains("key_f1")) {
            boolean f1Enable = intent.getBooleanExtra("key_f1", true);
            edit.putBoolean("key_f1", f1Enable);
        }
        if (keyName.contains("key_f2")) {
            boolean f2Enable = intent.getBooleanExtra("key_f2", true);
            edit.putBoolean("key_f2", f2Enable);
        }
        if (keyName.contains("key_f3")) {
            boolean f3Enable = intent.getBooleanExtra("key_f3", true);
            edit.putBoolean("key_f3", f3Enable);
        }
        if (keyName.contains("key_f4")) {
            boolean f4Enable = intent.getBooleanExtra("key_f4", true);
            edit.putBoolean("key_f4", f4Enable);
        }
        if (keyName.contains("key_f5")) {
            boolean f5Enable = intent.getBooleanExtra("key_f5", true);
            edit.putBoolean("key_f5", f5Enable);
        }
        if (keyName.contains("key_f6")) {
            boolean f6Enable = intent.getBooleanExtra("key_f6", true);
            edit.putBoolean("key_f6", f6Enable);
        }
        if (keyName.contains("key_f7")) {
            boolean f7Enable = intent.getBooleanExtra("key_f7", true);
            edit.putBoolean("key_f7", f7Enable);
        }
        edit.apply();
    }

    /**
     * 设置扫描码制参数
     */
    private void setScanParam(Intent configIntent) {
        String id = configIntent.getStringExtra("id");
        String param = configIntent.getStringExtra("param");
        String value = configIntent.getStringExtra("value");
        mSoftEngine.ScanSet(id, param, value);
    }

    /**
     * 开启扫描
     */
    private void startScan() {
        boolean isContinuousSwitchOpen;
        isContinuousSwitchOpen = mDefaultSharedPreferences.getBoolean(PreferenceKey.KEY_CONTINUOUS_SCANNING, false);
        LogUtils.e(TAG, "startScan, isContinuousSwitchOpen:" + isContinuousSwitchOpen);
        // 更新mIsLooping标志，原起KeyReceiver可能会更改此标志
        mIsLooping = mDefaultSharedPreferences.getBoolean("mIsLooping", false);
        if (mIsInit && !mIsScanning && !mIsLooping) {
            synchronized (Cm60Service.class) {
                // Start decode
                long startTime = System.currentTimeMillis();
                mSoftEngine.Open();
                mSoftEngine.StartDecode();
                if (isContinuousSwitchOpen) {
                    mIsLooping = true;
                    // 按键广播接收者里面的isLooping的标志
                    mDefaultSharedPreferences.edit().putBoolean("mIsLooping", true).apply();
                } else {
                    // 扫描超时停止扫描计时器
                    String decodeTimeStr = mDefaultSharedPreferences.getString(PreferenceKey.KEY_DECODE_TIME, "5000");
                    int decodeTime = Integer.valueOf(decodeTimeStr != null ? decodeTimeStr : "5000");
                    LogUtils.i(TAG, "startScan, decodeTime = " + decodeTime);
                    stopScanTimer(decodeTime);
                }
                LogUtils.i(TAG, "触发扫码耗时 = " + (System.currentTimeMillis() - startTime));
            }
            mIsScanning = true;
        } else {
            Log.i(TAG, "startScan fail! Reader is not init or is busy now");
        }
    }

    /**
     * 设置连续扫描模式
     *
     * @param isContinuous       是否开启连续扫描模式
     * @param continuousInternal 连续扫描模式下，每次扫描之间的间隔
     */
    private void setContinuousMode(boolean isContinuous, String continuousInternal) {
        try {
            if (mIsInit) {
                mDefaultSharedPreferences.edit().putBoolean(PreferenceKey.KEY_CONTINUOUS_SCANNING, isContinuous).apply();
                if (RegularUtils.verifyScanInterval(continuousInternal)) {
                    mDefaultSharedPreferences.edit().putString(PreferenceKey.KEY_SCANNING_INTERVAL, continuousInternal).apply();
                } else {
                    Log.i(TAG, "setContinuousMode fail! Parameter error:");
                }
            } else {
                Log.i(TAG, "startContinuousScan fail! Reader is not init");
            }
        } catch (Exception e) {
            Log.i(TAG, "setContinuousMode fail! error:" + Log.getStackTraceString(e));
        }
    }

    /**
     * 停止扫描
     */
    private void stopScan() {
        if (mIsInit && mIsScanning && !mIsLooping) {
            mSoftEngine.StopDecode();
            // Key is released
            mIsScanning = false;
            // 取消用于停止扫描的定时器
            cancelStopScanTimer();
        } else if (mIsLooping) {
            // 停止循环（连续）扫描
            mSoftEngine.StopDecode();
            // Key is released
            mIsScanning = false;
            // 取消用于停止扫描的定时器
            cancelStopScanTimer();
            mIsLooping = false;
            // 重置按键广播接收者里面的isLooping的标志
            mDefaultSharedPreferences.edit().putBoolean("mIsLooping", false).apply();
        } else {
            Log.i(TAG, "stopScan fail! Reader is not init or event don't work");
        }
    }

    /**
     * 关闭扫描开关
     */
    private void closeScan(boolean iscamera) {
        if (mIsInit) {
            stopScan();
//            mSoftEngine.Close();
//            mSoftEngine.Deinit();
//            if (!iscamera) {
            // Camera call, don't reset scan switch stat
            // 关闭扫描开关
            mDefaultSharedPreferences.edit().putBoolean(PreferenceKey.KEY_SWITCH_SCAN, false).apply();
//            }
            mIsInit = false;
            // 更新通知栏通知
            setNotification();
        } else {
            Log.i(TAG, "closeScan fail! Reader is not init");
        }
//        if (mIsInit && mIsInitByApp && isAppCall) {
//            mSoftEngine.StopDecode();
//            mSoftEngine.Close();
//            mSoftEngine.Deinit();
//            if (!iscamera) {
        // Camera call, don't reset scan switch stat
//            mDefaultSharedPreferences.edit().putBoolean(PreferenceKey.KEY_SWITCH_SCAN, false).apply();
//            }
//            mIsInit = false;
//            setNotification();
//        } else if (mIsInit & !isAppCall){
//            LogUtils.i(TAG, "closeScan by service switch");
//            mIsInit = false;
//        } else {
//            Log.i(TAG, "closeScan fail! Reader is not init");
//        }
    }

    /**
     * ------------------------------------------------------扫描头扫码结果处理-------------------------------------------------------------------
     * <p>
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
                    showToast(Cm60Service.this.getString(R.string.charset_err));
                }
                break;
            case "2":
                // 模拟按键输入
                try {
                    String result = bytesToString(data);
                    softInput(result);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    showToast(Cm60Service.this.getString(R.string.charset_err));
                }
                break;
            case "3":
                // 剪切板
                try {
                    String result = bytesToString(data);
                    copyToClipboard(result);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    showToast(Cm60Service.this.getString(R.string.charset_err));
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
            showToast(Cm60Service.this.getString(R.string.charset_err));
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

    private Toast mToast;

    private void showToast(String content) {
        if (mToast == null) {
            mToast = Toast.makeText(Cm60Service.this, content, Toast.LENGTH_SHORT);
            mToast.show();
        } else {
            mToast.setText(content);
            mToast.show();
        }
    }

    private void setNotification() {
        boolean isOpen = mDefaultSharedPreferences.getBoolean(PreferenceKey.KEY_SWITCH_SCAN, false);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle(getString(R.string.app_name));
        if (!isOpen) {
            // 后台扫描开关未打开
            builder.setContentText(getString(R.string.service_stopped));
        } else {
            // 后台扫描开关已打开
            builder.setContentText(getString(R.string.service_started));
        }
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        // 添加事件
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        Intent intent = new Intent(this, TabHostActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, flags);
        builder.setContentIntent(pi);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.scan));
        Notification notification = builder.build();
        notification.flags |= FLAG_NO_CLEAR;
        assert mNotificationManager != null;
        startForeground(R.string.app_name, notification);
    }
}
