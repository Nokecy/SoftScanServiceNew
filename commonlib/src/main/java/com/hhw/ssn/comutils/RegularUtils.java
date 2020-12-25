package com.hhw.ssn.comutils;

import android.text.TextUtils;

/**
 * description : 正则检查，用于检查扫描服务各项参数设置的合规性
 * update : 2019/7/17 16:59,LeiHuang,初次提交，添加扫描超时检查,
 *
 * @author : LeiHuang
 * @version : 1.0
 * @date : 2019/7/17 16:59
 */
public class RegularUtils {

    /**
     * 扫描解码超时时间的值应该在500(ms) - 10000(ms)之间
     */
    public static boolean verifyScanTimeout(String timeout){
        if (TextUtils.isEmpty(timeout) || timeout.length() > 5){
            return false;
        }
        int i = Integer.parseInt(timeout);
        int maxTime = 10000;
        int minTime = 500;
        return i >= minTime && i <= maxTime;
    }

    /**
     * 连续扫描之间的间隔时间应该在0(ms) - 60000(ms)
     */
    public static boolean verifyScanInterval(String interval){
        if (TextUtils.isEmpty(interval) || interval.length() > 5){
            return false;
        }
        int i = Integer.parseInt(interval);
        int maxTime = 60000;
        int minTime = 0;
        return i >= minTime && i <= maxTime;
    }

    /**
     * 扫描结果输入模式取值范围：0(广播), 1(焦点), 2(模拟按键输入), 3(剪切板)
     */
    public static boolean verifyResultMode(int i){
        return i == 0 || i == 1 || i == 2 || i == 3;
    }

    /**
     * 扫描照明和瞄准开关取值范围：0(关), 1(开)
     */
    public static boolean verifyIllumAim(int i){
        return i == 0 || i == 1;
    }

    /**
     * PICKLISTMODE开关取值范围：0(关), 2(开)
     */
    public static boolean verifyPickListMode(int i){
        return i == 0 || i == 2;
    }

    /**
     * 长度设置输入框的内容校验，只能输入0~55之间的长度
     * @param i 待校验的长度数据
     * @return 校验结果
     */
    public static boolean verifyCodeLength(int i){
        return i >= 0 && i <= 55;
    }
}
