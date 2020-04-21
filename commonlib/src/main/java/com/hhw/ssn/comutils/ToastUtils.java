package com.hhw.ssn.comutils;

import android.content.Context;
import android.widget.Toast;

/**
 * description : Toast工具类，统一管理Toast
 * update : 2019/7/17 17:04,LeiHuang,初次提交
 *
 * @author : LeiHuang
 * @version : 1.0
 * @date : 2019/7/17 17:04
 */
public class ToastUtils {

    private static Toast sToast;

    public static void showShortToast(Context context, String content){
        if (sToast == null){
            sToast = Toast.makeText(context, content, Toast.LENGTH_SHORT);
            sToast.show();
        } else {
            sToast.setText(content);
            sToast.show();
        }
    }

    public static void showLongToast(Context context, String content){
        if (sToast == null){
            sToast = Toast.makeText(context, content, Toast.LENGTH_SHORT);
            sToast.show();
        } else {
            sToast.setText(content);
            sToast.show();
        }
    }
}
