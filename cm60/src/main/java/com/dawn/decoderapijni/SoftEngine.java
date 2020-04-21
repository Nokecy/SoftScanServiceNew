package com.dawn.decoderapijni;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;

public class SoftEngine {

    private static ScanningCallback mScanningCallback;
    private final static String TAG = "SoftEngine_Barcode";
    private final static String SCAN_COMMON_PATH = "/storage/sdcard0/ScanCommon/";
    private final static String XML_PATH = SCAN_COMMON_PATH + "xml/";
    private final static String CACHE_PATH = SCAN_COMMON_PATH + "cache/";
    private final static String DATA_PATH = SCAN_COMMON_PATH + "data/";
    private final static String LICENSE_PATH = SCAN_COMMON_PATH + "license/";
    private final static String CONFIG_XML = "xml/CONFIG.xml";
    private final static String SOFTENGINE_XML = "xml/SoftEngineEN";
    private final static String DRIVER_XML = "xml/DRIVEREN";
    private final static String DEFAULT_XML = "xml/DEFAULTEN";
    private final static String ENGINE_SETTING_CODE_XML = "xml/EngineSettingCodeEN";

    private final static int JNI_SOFTENGINE_IOCTRL_SET_CONTEXT                  = 0x10F2;
    private final static int JNI_SOFTENGINE_IOCTRL_SET_DISPLAY_ORIENTATION      = 0x10F3;
    private final static int JNI_SOFTENGINE_IOCTRL_SET_PREVIEW_TEXTURE          = 0x10F4;
    private final static int JNI_SOFTENGINE_IOCTRL_SET_PREVIEW_DISPLAY          = 0x10F5;
    private final static int JNI_SOFTENGINE_IOCTRL_SET_FLASH_MODE               = 0x10F6;
    private final static int JNI_SOFTENGINE_IOCTRL_SET_CAMERA_ID                = 0x10F7;
    private final static int JNI_SOFTENGINE_IOCTRL_SET_FOCUS_MODE               = 0x10F8;
    private final static int JNI_SOFTENGINE_IOCTRL_SET_CONTRAST                 = 0x10F9;
    private final static int JNI_SOFTENGINE_IOCTRL_SET_EXPOSURE_COMPENSATION    = 0x10FA;
    private final static int JNI_SOFTENGINE_IOCTRL_SET_ZOOM                     = 0x10FB;
    private final static int JNI_SOFTENGINE_IOCTRL_GET_PARAMETER                = 0x10FC;
    private final static int JNI_SOFTENGINE_IOCTRL_SET_SCANCAMEAR               = 0x10E1;
    boolean bStarted;


//    private static SoftEngine mInstance;
    private Context mContext;

    static {
        try{
            // 载入本地库
            System.loadLibrary("nls-scanner.software");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public static SoftEngine getInstance(){
//        if(mInstance != null)
//            return mInstance;
//        else
//            return null;
//    }

//    public static SoftEngine getInstance(Context context){
//        if(mInstance != null)
//            return mInstance;
//        else
//            return new SoftEngine(context);
//    }


    public SoftEngine(Context context)
    {
        mContext = context;
//        mInstance = this;
    }

    public boolean initSoftEngine(){
        if (JNI_Init())
        {
//            JNI_Scn_IOCtrlEx(JNI_SOFTENGINE_IOCTRL_SET_CONTEXT, 0, mContext);
            JNI_Scn_IOCtrlEx(JNI_SOFTENGINE_IOCTRL_SET_CONTEXT, 0, ScanCamera.getInstance());
            JNI_CmOpen();
            bStarted = false;
            return true;
        }

        return false;
    }

    public int setSoftEngineIOCtrlEx(int cmd, int param1, Object obj){
        return JNI_Scn_IOCtrlEx(cmd, param1,obj);
    }

    public void setScanningCallback(ScanningCallback scanningCallback) {
        mScanningCallback = scanningCallback;
    }

    public boolean StartDecode() {
        //boolean bRet = JNI_NewImage(imageData, nWidth, nHeight);
        if (!bStarted)
        {
            JNI_CmStartDecode("sendScanningResultFromNative", 0);
            bStarted = true;
        }
        return true;
    }

    public boolean StopDecode() {
        bStarted = false;
        return JNI_CmStopDecode(0);
    }

    public boolean Open() {
        return JNI_CmOpen();
    }

    public boolean Close() {
        return JNI_CmClose();
    }

    /**
     * Native 代码中回调该接口，返回扫描结果
     *
     * @param event_code
     */
    public static int sendScanningResultFromNative(int event_code, int msgType, byte[] bMsg1,
                                                   byte[] bMsg2, int length)
    {
        if (event_code == 1)
        {
            mScanningCallback.onScanningCallback(event_code, msgType, bMsg1, length);
        }
        return 1;
    }

    public Object getSystemService(String name)
    {
        Log.d(TAG, "getSystemService, name:" + name);
        return null;
    }

    public interface ScanningCallback {
        public void onScanningCallback(int eventCode, int param1, byte[] param2, int length);
    }

    public boolean Deinit()
    {
        JNI_CmClose();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return JNI_DeInit();
    }

    public String SDKVersion()
    {
        return JNI_Get_Version();
    }

    public int ScanSet(String Id, String Param1, String Param2)
    {
        return JNI_Scn_set(Id, Param1, Param2);
    }

    public String ScanGet(String Id, String Param1)
    {
        return JNI_Scn_get(Id, Param1);
    }

    public void setPreviewDisplay(SurfaceHolder holder)
    {
        if (holder != null) {
            JNI_Scn_IOCtrlEx(JNI_SOFTENGINE_IOCTRL_SET_PREVIEW_DISPLAY, 0, holder.getSurface());
        } else {
            JNI_Scn_IOCtrlEx(JNI_SOFTENGINE_IOCTRL_SET_PREVIEW_DISPLAY, 0, null);
        }
    }

    public void setDisplayOrientation(int degrees)
    {
        JNI_Scn_IOCtrlEx(JNI_SOFTENGINE_IOCTRL_SET_DISPLAY_ORIENTATION, degrees, null);
    }

    public void setFlashMode(String value) {
        JNI_Scn_IOCtrlEx(JNI_SOFTENGINE_IOCTRL_SET_FLASH_MODE, 0, value);
    }

    public void setCameraId(int cameraId)
    {
        JNI_Scn_IOCtrlEx(JNI_SOFTENGINE_IOCTRL_SET_CAMERA_ID, cameraId, null);
    }

    public void setFocusMode(String value)
    {
        JNI_Scn_IOCtrlEx(JNI_SOFTENGINE_IOCTRL_SET_FOCUS_MODE, 0, value);
    }

    public void setContrast(int contrast)
    {
        JNI_Scn_IOCtrlEx(JNI_SOFTENGINE_IOCTRL_SET_CONTRAST, contrast, null);
    }

    public void setExposureCompensation(int value)
    {
        JNI_Scn_IOCtrlEx(JNI_SOFTENGINE_IOCTRL_SET_EXPOSURE_COMPENSATION, value, null);
    }

    public void setZoom(int value)
    {
        JNI_Scn_IOCtrlEx(JNI_SOFTENGINE_IOCTRL_SET_ZOOM, value, null);
    }

    public byte[] getLastImage(){
        return JNI_GetLImage();
    }


    public SensorParam getSensorParam(int mode){
        SensorParam sensorParam = new SensorParam();
        JNI_Scn_GetParam(mode,sensorParam );
        return sensorParam;
    }

    public int[] getScanWheelTime(){
        return JNI_GetWheelTime();
    }

    public void setSensorParam(int mode, SensorParam param){
        JNI_Scn_SetParam(mode,param);
    }

    private native boolean JNI_Init();

    private native boolean JNI_CmOpen();

    private native boolean JNI_CmClose();

    private native boolean JNI_CmStartDecode(String callbackFunc, int WorkMode);

    private native boolean JNI_CmStopDecode(int WorkMode);

    private native int JNI_Scn_set(String Id, String Param1, String Param2);

    private native String JNI_Scn_get(String Id, String Param1);

    private native String JNI_Get_Engine();

    private native int JNI_Get_ScanType();

    private native int JNI_Scn_IOCtrlEx(int cmd, int param1, Object obj);

    private native String JNI_Get_Version();

    private native boolean JNI_DeInit();

    private native boolean JNI_Get_State();

    private native byte[] JNI_GetLImage();

    private native int JNI_Scn_SetParam(int mode, SensorParam param);

    private native int JNI_Scn_GetParam(int mode, SensorParam param);

    private native int[] JNI_GetWheelTime();
}
