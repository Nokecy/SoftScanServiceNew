package com.hhw.ssn.comutils;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.hhw.ssn.combean.PreferenceKey;
import com.hhw.ssn.combean.ServiceActionKey;
import com.hhw.ssn.commonlib.R;

/**
 * @author huang
 * 用于显示悬浮窗
 */
public class FloatService extends Service {

    private final String TAG = FloatService.class.getSimpleName();

    private WindowManager wm;

    private float mTouchX;
    private float mTouchY;
    private float x;
    private float y;
    private float mStartX;
    private float mStartY;

    LinearLayout mFloatLayout;
    WindowManager.LayoutParams wmParams;

    public FloatService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createFloatWindow();
        LogUtils.e(TAG, "onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void createFloatWindow() {
        wmParams = new WindowManager.LayoutParams();
        wm = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        assert wm != null;
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);


        wmParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        wmParams.format = PixelFormat.RGBA_8888;
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        wmParams.x = dm.widthPixels;
        wmParams.y = dm.heightPixels / 2;
        wmParams.width = 100;
        wmParams.height = 100;
        LayoutInflater inflater = LayoutInflater.from(getApplication());
        mFloatLayout = (LinearLayout) inflater.inflate(R.layout.float_window, null);
        mFloatLayout.setOnTouchListener(new View.OnTouchListener() {


            @Override
            public boolean onTouch(View v, MotionEvent event) {
                x = event.getRawX();
                y = event.getRawY();
                switch (event.getAction()) {
                    //down
                    case 0:
                        Log.e("", "ACTION_DOWN");
                        mTouchX = event.getX();
                        mTouchY = event.getY();
                        mStartX = x;
                        mStartY = y;

                        mFloatLayout.setBackground(getResources().getDrawable(R.drawable.bg_yellow));
                        break;
                    //move
                    case 2:

                        if ((x - mStartX) < 5 && (y - mStartY) < 5) {

                        } else {
                            updateView();
                        }
                        break;
                    //up
                    case 1:
                        mFloatLayout.setBackground(getResources().getDrawable(R.drawable.bg_red));
                        if ((x - mStartX) < 5 && (y - mStartY) < 5) {
                            sendScanBroad(FloatService.this);
                        } else {
                            Log.e("onclice", "finish++++");
                            updateView();
                            mFloatLayout.setBackground(getResources().getDrawable(R.drawable.bg_red));
                            mTouchX = mTouchY = 0;
                        }
                        break;
                    default:

                        break;
                }
                return true;
            }
        });
        wm.addView(mFloatLayout, wmParams);
    }

    private void updateView() {
        wmParams.x = (int) (x - mTouchX);
        wmParams.y = (int) (y - mTouchY);
        wm.updateViewLayout(mFloatLayout, wmParams);
    }

    @Override
    public void onDestroy() {
        wm.removeViewImmediate(mFloatLayout);
        super.onDestroy();
    }

    private void sendScanBroad(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Intent broadIntent = new Intent();
        // 判断连续扫描开关是否开启
        boolean isContinuousSwitchOpen = prefs.getBoolean(PreferenceKey.KEY_CONTINUOUS_SCANNING, false);
        if (isContinuousSwitchOpen) {
            // 如果连续扫描开启，抬起按键需要判断是否处于循环扫描状态(isLooping用于本类中的循环判断，mIsLooping用于Service中的循环判断)
            boolean mIsLooping = prefs.getBoolean("mIsLooping", false);
            if (mIsLooping) {
                prefs.edit().putBoolean("mIsLooping", false).apply();
                broadIntent.setAction(ServiceActionKey.ACTION_STOP_SCAN);
                LogUtils.i(TAG, "sendScanBroad: reset mIsLooping");
            } else {
                broadIntent.setAction(ServiceActionKey.ACTION_SCAN);
            }
        } else {
            broadIntent.setAction(ServiceActionKey.ACTION_SCAN);
        }
        if (broadIntent.getAction() != null) {
            context.sendBroadcast(broadIntent);
        }
    }
}
