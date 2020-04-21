package com.hhw.ssn.comutils;

import android.content.Context;
import android.content.Intent;

/**
 * @author Administrator
 * @date 2018/4/19
 * @UpdateDate 2019/04/03  Completed scan button setting, added stop scanning interface
 */

public class ScanUtil {
    private Context mContext;

    /**
     * Initialize the scan engine and scan service
     */
    public ScanUtil(Context context) {
        mContext = context ;
        Intent intent = new Intent();
        String actionScanInit = "com.rfid.SCAN_INIT";
        intent.setAction(actionScanInit);
        context.sendBroadcast(intent);
    }

    /**
     * Start scanning.
     * Trigger on the scan engine
     */
    public void scan() {
        Intent intent = new Intent();
        String actionScan = "com.rfid.SCAN_CMD";
        intent.setAction(actionScan);
        mContext.sendBroadcast(intent);
    }

    /**
     * Set output mode
         * @param mode 0 --> BroadcastReceiver mode(Developers can receive the scan result in their apps),
     *             1 --> EditText input mode(The scan service will enter the scan result into the input box where the cursor is located)
     */
    public void setScanMode(int mode) {
        Intent intent = new Intent();
        String actionSetScanMode = "com.rfid.SET_SCAN_MODE";
        intent.setAction(actionSetScanMode);
        intent.putExtra("mode", mode);
        mContext.sendBroadcast(intent);
    }

    /**
     * Set which buttons can trigger scanning (By default, all buttons can trigger scanning).
     * @param f1Available Whether the F1 button can trigger scanning
     * @param f2Available Whether the F2 button can trigger scanning
     * @param f3Available Whether the F3 button can trigger scanning
     * @param f4Available Whether the F4 button can trigger scanning
     * @param f5Available Whether the F5 button can trigger scanning
     * @param f6Available Whether the F6 button can trigger scanning
     * @param f7Available Whether the F7 button can trigger scanning
     */
    public void setScanKey(boolean f1Available, boolean f2Available, boolean f3Available, boolean f4Available, boolean f5Available, boolean f6Available, boolean f7Available){
        Intent intent = new Intent();
        String actionKeySet = "com.rfid.KEY_SET";
        intent.setAction(actionKeySet);
        boolean[] scanKeyArray = {f1Available, f2Available, f3Available, f4Available, f5Available, f6Available, f7Available};
        String keyScanKey = "scan_Key";
        intent.putExtra(keyScanKey, scanKeyArray);
        mContext.sendBroadcast(intent);
    }

    /**
     * Stop scanning.
     * This method will only trigger off the scan engine, app can start scanning by calling scan()
     */
    public void stopScan(){
        Intent intent = new Intent();
        String actionStopScan = "com.rfid.STOP_SCAN";
        intent.setAction(actionStopScan);
        mContext.sendBroadcast(intent);
    }

    /**
     * Close the scan service and scan engine.
     * This method will disconnect the scan engine, app need to reinitialize to start scanning
     */
    public void close() {
        Intent toKillService = new Intent();
        String actionCloseScan = "com.rfid.CLOSE_SCAN";
        toKillService.setAction(actionCloseScan);
        mContext.sendBroadcast(toKillService);
    }
}
