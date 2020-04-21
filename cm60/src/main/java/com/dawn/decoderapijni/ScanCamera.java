package com.dawn.decoderapijni;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import com.hhw.ssn.cm60.Cm60Service;

import java.io.IOException;

public class ScanCamera {

    private static final String TAG = ScanCamera.class.getCanonicalName();

    private static ScanCamera mInstance;
    private static Camera mCamera;

    private SurfaceHolder mSurfaceHolder;
    private SurfaceTexture mSurfaceTexture;

    int width = 1280;
    int height = 800;

    public ScanCamera() { }

    public static ScanCamera getInstance(){

        Log.d(TAG,"Camera getInstance .... ");
        if(mInstance == null){
            mInstance = new ScanCamera();
        }
        return mInstance;
    }


    protected  int cameraCheckFacing(final int facing ){

        final int cameraCount = Camera.getNumberOfCameras();
        Camera.CameraInfo info = new Camera.CameraInfo();
        Log.d(TAG,"Camera num: "+ cameraCount);
        for(int i = 0; i< cameraCount;i++){
            Camera.getCameraInfo(i,info);
            if(facing == info.facing){
                return 0;
            }
        }

        return -2;
    }

    public void cameraInit(){
        Log.i(TAG," cameraInit ++++++++++++++");
    }

    public void cameraOpen(int port){
//        if(mCamera != null)return;

//        if(cameraCheckFacing(port) != 0){
//            Log.i(TAG,"检测摄像头失败 ++++++++++++++");
//            return;
//        }
        Log.i(TAG,"检测摄像头成功 初始化摄像头 ++++++++++++++");

        try {
            mCamera = Camera.open(1);// 打开后置摄像头
            Camera.Parameters params = mCamera.getParameters();
            params.setPreviewSize(width, height);// 设置外形尺寸
            mCamera.setParameters(params);
            mSurfaceTexture = new SurfaceTexture(10);
            mCamera.setPreviewTexture(mSurfaceTexture);
            mCamera.setPreviewCallback(new ScanPreviewCallback());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int cameraClose(){
        if(mCamera != null){
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        return 0;
    }

    public void cameraStart(){
        if(mCamera == null)
            return ;
        Log.i(TAG,"cameraStart ++++++++++++++");
        mCamera.startPreview();
    }

    public void cameraStop(){
        if(mCamera == null)
            return;

        mCamera.stopPreview();
    }

    public void cameraSetSurfaceHolder(SurfaceHolder surfaceHolder){
        mSurfaceHolder = surfaceHolder;
    }

    public class ScanPreviewCallback implements Camera.PreviewCallback {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {

            Camera.Size size = camera.getParameters().getPreviewSize();
            //SavaYUV2BMP(data,data.length);
            Log.i(TAG,"++++++++++++++++onPreviewFrame++++++++++  "+size.height+  "  "+size.width+ "bufsize : "+ data.length);
            Cm60Service.scanSetDecodeImage(data,data.length);
        }
    }
}
