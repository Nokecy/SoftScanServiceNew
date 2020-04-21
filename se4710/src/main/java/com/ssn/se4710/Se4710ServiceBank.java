package com.ssn.se4710;

import android.app.Instrumentation;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.hhw.ssn.combean.PreferenceKey;
import com.hhw.ssn.combean.ServiceActionKey;
import com.hhw.ssn.comui.KeyBroadcastReceiver;
import com.hhw.ssn.comui.TabHostActivity;
import com.hhw.ssn.comutils.LogUtils;
import com.hhw.ssn.comutils.SoundPoolMgr;
import com.zebra.adc.decoder.BarCodeReaderBank;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static android.support.v4.app.NotificationCompat.FLAG_NO_CLEAR;

/**
 * @author LeiHuang
 */
public class Se4710ServiceBank extends Service implements BarCodeReaderBank.DecodeCallback, BarCodeReaderBank.ErrorCallback, SurfaceTexture.OnFrameAvailableListener {
    private static final String TAG = "Se4710Service";
    /**
     * 扫描头初始化标志
     */
    private boolean mIsInit = false;
    /**
     * 外部调用初始化标志
     */
    private boolean mExternalInit = false;
    /**
     * 内部调用初始化标志
     */
    private boolean mInternalInit = false;
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
    /**
     * 扫描头控制器
     */
    private BarCodeReaderBank bcr = null;
    /**
     * 用于接收设置扫描参数，开启扫描，关闭服务等指令
     */
    private ScanCommandBroadcast mSettingsReceiver;

    private class ScanCommandBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // System Camera call
            LogUtils.e(TAG, "ScanCommandBroadcast, receive at " + System.currentTimeMillis());
            boolean iscamera = intent.getBooleanExtra("iscamera", false);
            LogUtils.i(TAG, "ScanCommandBroadcast, iscamera: " + iscamera);
            String action = intent.getAction() == null ? ServiceActionKey.ACTION_NULL : intent.getAction();
            boolean callFromService = intent.getBooleanExtra("CallFromService", false);
            switch (action) {
                case ServiceActionKey.ACTION_NULL:
                    Log.i(TAG, "ScanCommandBroadcast, receive action = null");
                    break;
                case ServiceActionKey.ACTION_SCAN_INIT:
                    LogUtils.i(TAG, "ScanCommandBroadcast, receive action scan_init");
                    initReader(callFromService, iscamera);
                    break;
                case ServiceActionKey.ACTION_SCAN_TIME:
                    LogUtils.i(TAG, "ScanCommandBroadcast, receive action set_timeout");
//                    setDecodeTimeout(callFromService, intent);
                    break;
//                case ServiceActionKey.ACTION_LIGHT_CONFIG:
//                    LogUtils.i(TAG, "ScanCommandBroadcast, receive action set_lightMod");
//                    setDecoderLightMod(callFromService, intent);
//                    break;
                case ServiceActionKey.ACTION_SET_SCAN_MODE:
                    LogUtils.i(TAG, "ScanCommandBroadcast, receive action set_scan_mode");
//                    setScanMode(callFromService, intent);
                    break;
                case ServiceActionKey.ACTION_ILLUMINATION_LEVEL:
                    LogUtils.i(TAG, "ScanCommandBroadcast, receive action set_illumination_level");
//                    setIlluminationLevel(callFromService, intent);
                    break;
                case ServiceActionKey.ACTION_KEY_SET:
                    LogUtils.i(TAG, "ScanCommandBroadcast, receive action set_scan_key");
//                    setScanKey(callFromService, intent);
                    break;
                case ServiceActionKey.ACTION_SCAN_PARAM:
                    LogUtils.i(TAG, "ScanCommandBroadcast, receive action set_scan_param");
//                    setScanParam(intent);
                    break;
                case ServiceActionKey.ACTION_SCAN:
                    LogUtils.i(TAG, "ScanCommandBroadcast, receive action start_scan");
                    startScan(callFromService);
                    break;
                case ServiceActionKey.ACTION_SCAN_CONTINUOUS:
                    boolean continuousMode = intent.getBooleanExtra("ContinuousMode", false);
                    String continuousInternal = intent.getStringExtra("ContinuousInternal");
                    LogUtils.i(TAG, "ScanCommandBroadcast, receive action set continuous mode:" + continuousMode + ", continuousInternal:" + continuousInternal);
//                    setContinuousMode(continuousMode, continuousInternal);
                    break;
                case ServiceActionKey.ACTION_STOP_SCAN:
                    LogUtils.i(TAG, "ScanCommandBroadcast, receive action stop_scan");
                    stopScan(callFromService);
                    break;
                case ServiceActionKey.ACTION_CLOSE_SCAN:
                    LogUtils.i(TAG, "ScanCommandBroadcast, receive action close_scan");
                    closeReader(callFromService, iscamera);
                    break;
                default:
                    break;
            }
        }
    }

    public Se4710ServiceBank() {
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
        intentFilter.addAction(ServiceActionKey.ACTION_STOP_SCAN);
        intentFilter.addAction(ServiceActionKey.ACTION_CLOSE_SCAN);
        intentFilter.addAction(ServiceActionKey.ACTION_SCAN_PARAM);
        registerReceiver(mSettingsReceiver, intentFilter);
        // 注册按键广播接收者
        IntentFilter keyReceiverFilter = new IntentFilter();
        keyReceiverFilter.addAction("android.intent.action.FUN_KEY");
        keyReceiverFilter.addAction("android.rfid.FUN_KEY");
        registerReceiver(mKeyReceiver, keyReceiverFilter);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.i(TAG, "onStartCommand");
        mDefaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        mSoundPoolMgr = SoundPoolMgr.getInstance(this);
        initReader(true, false);
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
        // 释放音频资源
        mSoundPoolMgr.release();
    }

    /**
     * ------------------------------------------------------SE4710监听回调-------------------------------------------------------------------
     */
    @Override
    public void onDecodeComplete(int symbology, int length, byte[] data, BarCodeReaderBank reader) {
        LogUtils.i(TAG, "onDecodeComplete : symbology = " + symbology);
        LogUtils.i(TAG, "onDecodeComplete : length = " + length);
        /*
         * When symbology is 0 and length is 0, it's the result of decode timeout
         * When symbology is 0 and length is -1, canceled by the user
         */
        if (data != null && length > 0) {
            byte[] codeData = new byte[length];
            System.arraycopy(data, 0, codeData, 0, length);
            // Reset stat to waiting
            bcr.stopDecode();
            mIsScanning = false;
            sendData(codeData, symbology);
            boolean voiceEnabled = mDefaultSharedPreferences.getBoolean(PreferenceKey.KEY_SCANNING_VOICE, true);
            if (voiceEnabled) {
                mSoundPoolMgr.play(1);
            }
            LogUtils.e(TAG, "onDecodeComplete, playVoice");
        } else if (length == -1) {
            // Reset stat to waiting
            mIsScanning = false;
        } else if (length == 0) {
            bcr.stopDecode();
            mIsScanning = false;
        }
    }

    @Override
    public void onEvent(int event, int info, byte[] data, BarCodeReaderBank reader) {

    }

    @Override
    public void onError(int error, BarCodeReaderBank reader) {

    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }

    /**
     * ------------------------------------------------------SE4710扫描控制接口-------------------------------------------------------------------
     * 开启扫描头，连接扫描头
     */
    private void initReader(boolean callFromService, boolean iscamera) {
//        boolean switchReinit = mDefaultSharedPreferences.getBoolean(PreferenceKey.KEY_REINIT_SCAN, false);
//        if (iscamera && switchReinit){
//            // 相机调用完毕之后，如果后台扫描开关在之前是打开状态，则打开
//            mDefaultSharedPreferences.edit().putBoolean(PreferenceKey.KEY_SWITCH_SCAN, true).apply();
//            mDefaultSharedPreferences.edit().putBoolean(PreferenceKey.KEY_REINIT_SCAN, false).apply();
//            mInternalInit = true;
//            setNotification();
//        }
        if (!iscamera && callFromService) {
            mInternalInit = true;
            setNotification();
        } else if (!iscamera){
            mExternalInit = true;
        }
        LogUtils.i(TAG, "initReader mIsInit = " + mIsInit);
        if (!mIsInit) {
            bcr = BarCodeReaderBank.open(2, getApplicationContext());
            // Set parameter - Uncomment for QC/MTK platforms
            // For QC/MTK platforms
            bcr.setParameter(765, 0);
            bcr.setParameter(764, 5);
//            bcr.setParameter(8610, 1);
            // Set Orientation
            // 4 - omnidirectional
            bcr.setParameter(687, 4);
            if (Build.VERSION.SDK_INT >= 28) {
                SurfaceTexture surfaceTexture = new SurfaceTexture(5);
                surfaceTexture.setOnFrameAvailableListener(this);
                bcr.setPreviewTexture(surfaceTexture);
            }
            // add callback
            bcr.setDecodeCallback(this);
            bcr.setErrorCallback(this);
            mIsInit = true;
            // load previous settings
//            String timeout = mDefaultSharedPreferences.getString(ConfigFragment.KEY_DECODE_TIME, "99");
//            setDecodeTimeout(true, new Intent().putExtra(ConfigFragment.KEY_DECODE_TIME, timeout));
//            String lightMod = mDefaultSharedPreferences.getString(ConfigFragment.KEY_LIGHTS_CONFIG, "2");
//            setDecoderLightMod(true, new Intent().putExtra(ConfigFragment.KEY_LIGHTS_CONFIG, lightMod));
//            String illuminationLevel = mDefaultSharedPreferences.getString(ConfigFragment.KEY_ILLUMINATION_LEVEL, "4");
//            setIlluminationLevel(true, new Intent().putExtra(ConfigFragment.KEY_ILLUMINATION_LEVEL, illuminationLevel));
        }
    }

    /**
     * 开始扫描
     */
    private void startScan(boolean callFromService) {
        if (callFromService) {
            if (mIsInit && !mIsScanning && mInternalInit) {
                // Start decode
                bcr.startDecode();
                LogUtils.i(TAG, "startScan, completed");
                mIsScanning = true;
            } else {
                Log.i("Huang, " + TAG, "startScan fail! Internal Reader is not init or is busy now");
            }
        } else {
            if (mIsInit && !mIsScanning && mExternalInit) {
                // Start decode
                bcr.startDecode();
                LogUtils.i(TAG, "startScan, completed");
                mIsScanning = true;
            } else {
                Log.i("Huang, " + TAG, "startScan fail! External Reader is not init or is busy now");
            }
        }
    }

    /**
     * 停止扫描
     */
    private void stopScan(boolean callFromService) {
        if (callFromService) {
            if (mIsInit && mIsScanning && mInternalInit) {
                bcr.stopDecode();
                // Key is released
                mIsScanning = false;
                LogUtils.i(TAG, "stopScan, completed");
            } else {
                Log.i("Huang, " + TAG, "stopScan fail! Internal Reader is not init or event don't work");
            }
        } else {
            if (mIsInit && mIsScanning && mExternalInit) {
                bcr.stopDecode();
                // Key is released
                mIsScanning = false;
                LogUtils.i(TAG, "stopScan, completed");
            } else {
                Log.i("Huang, " + TAG, "stopScan fail! External Reader is not init or event don't work");
            }
        }
    }

    /**
     * 断开扫描头连接
     */
    private void closeReader(boolean callFromService, boolean iscamera) {
        if (iscamera) {
            stopScan(true);
            mInternalInit = false;
            mExternalInit = false;
            setNotification();
            bcr.release();
            bcr = null;
            mIsInit = false;
            LogUtils.i(TAG, "closeReader, close engine");
            // 存储调用相机关闭扫描头之前的后台扫描的打开状态
//            boolean switchScan = mDefaultSharedPreferences.getBoolean(PreferenceKey.KEY_SWITCH_SCAN, false);
//            if (switchScan) {
//                mDefaultSharedPreferences.edit().putBoolean(PreferenceKey.KEY_REINIT_SCAN, true).apply();
//            }
            mDefaultSharedPreferences.edit().putBoolean(PreferenceKey.KEY_SWITCH_SCAN, false).apply();
        } else {
            if (callFromService) {
                // 关闭后台扫描开关
                mDefaultSharedPreferences.edit().putBoolean(PreferenceKey.KEY_SWITCH_SCAN, false).apply();
                if (mIsInit && mInternalInit) {
                    stopScan(true);
                    mInternalInit = false;
                    setNotification();
                    LogUtils.i(TAG, "closeReader, end");
                } else {
                    Log.i("Huang, " + TAG, "closeReader fail! Reader is not init");
                }
            } else {
                if (mIsInit && mExternalInit) {
                    stopScan(false);
                    mExternalInit = false;
                    LogUtils.i(TAG, "closeReader, end");
                } else {
                    Log.i("Huang, " + TAG, "closeReader fail! Reader is not init");
                }
            }
        }
    }

    /**
     * -------------------------------------------------------------------------------------------------------------------------------
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel(){
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel("SoftScanService_Channel_1", "SoftScanService_Channel_1", NotificationManager.IMPORTANCE_LOW);
        notificationManager.createNotificationChannel(notificationChannel);
    }

    /**
     * 发送扫码数据
     *
     * @param data 扫码数据
     */
    private void sendData(byte[] data, int sym) {
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
                    showToast(Se4710ServiceBank.this.getString(R.string.charset_err));
                }
                mIsScanning = false;
                break;
            case "2":
                // 模拟按键输入
                try {
                    String result = bytesToString(data);
                    softInput(result);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    showToast(Se4710ServiceBank.this.getString(R.string.charset_err));
                }
                mIsScanning = false;
                break;
            case "3":
                // 剪切板

                break;
            default:
                break;
        }
    }

    /**
     * 将扫描结果以广播的形式发送，其他APP可以通过注册Action为"com.rfid.SCAN"的广播，接收扫描到的数据
     */
    private void broadScanResult(byte[] data, int codeId) {
        Intent intent = new Intent();
        intent.putExtra("data", data);
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
    public static String filter(String content){
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
        String result = getfixChar(data);
        String append = getAppendChar();
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
                String appendChar = getAppendChar();
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

    private String getAppendChar() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        return prefs.getString("append_ending_char", "4");
    }

    /**
     * 对扫描结果添加前缀后缀
     */
    private String getfixChar(String data) {
        String result;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String prefix = prefs.getString("prefix_config", "");
        String suffix = prefs.getString("suffix_config", "");
        result = prefix + data + suffix;
        return result;
    }

    /**
     * 展示土司
     *
     * @param content 展示的内容
     */
    private void showToast(String content) {
        if (mToast == null) {
            mToast = Toast.makeText(Se4710ServiceBank.this, content, Toast.LENGTH_SHORT);
            mToast.show();
        } else {
            mToast.setText(content);
            mToast.show();
        }
    }
}
