package com.hhw.ssn.combean;

/**
 * @author HuangLei 1252065297@qq.com
 * <code>
 * Create At 2019/5/5 12:00
 * Update By 更新者
 * Update At 2019/5/5 12:00
 * </code>
 * 扫描服务中接收设置命令所用到的ACTION
 */
public class ServiceActionKey {
    /**
     * 连接扫描头(开机时连接扫描头)
     */
    public static final String ACTION_SCAN_BOOT_INIT = "com.rfid.SCAN_BOOT_INIT";
    /**
     * 启用扫描头(开机后重新打开扫描开关或调用连接扫描头)
     */
    public static final String ACTION_SCAN_INIT = "com.rfid.SCAN_INIT";
    /**
     * 设置扫描超时
     */
    public static final String ACTION_SCAN_TIME = "com.rfid.SCAN_TIME";
    /**
     * 扫描头照明开关设置
     */
    public static final String ACTION_ILLUMINATION = "com.rfid.ILLUMINATION";
    /**
     * 扫描头瞄准开关设置
     */
    public static final String ACTION_AIMING_PATTERN = "com.rfid.AIMING_PATTERN";
    /**
     * 扫描头PickListMode设置
     */
    public static final String ACTION_PICK_LIST_MODE = "com.rfid.PICK_LIST_MODE";
    /**
     * 条码或二维码数据的发送方式设置
     */
    public static final String ACTION_SET_SCAN_MODE = "com.rfid.SET_SCAN_MODE";
    /**
     * 照明光强度设置
     */
    public static final String ACTION_ILLUMINATION_LEVEL = "com.rfid.ILLUMINATION_LEVEL";
    /**
     * 扫描按键设置
     */
    public static final String ACTION_KEY_SET = "com.rfid.KEY_SET";
    /**
     * 设置扫描参数，码制参数设置
     */
    public static final String ACTION_SCAN_PARAM = "com.rfid.SCAN_PARAM";
    /**
     * 开始扫描
     */
    public static final String ACTION_SCAN = "com.rfid.SCAN_CMD";
    /**
     * 停止扫描
     */
    public static final String ACTION_STOP_SCAN = "com.rfid.STOP_SCAN";
    /**
     * 禁用扫描头
     */
    public static final String ACTION_CLOSE_SCAN = "com.rfid.CLOSE_SCAN";
    /**
     * 扫描声音开关
     */
    public static final String ACTION_SCAN_VOICE = "com.rfid.SCAN_VOICE";
    /**
     * 扫描振动开关
     */
    public static final String ACTION_SCAN_VIBERATE = "com.rfid.SCAN_VIBERATE";
    /**
     * 连续扫描开关
     */
    public static final String ACTION_SCAN_CONTINUOUS = "com.rfid.SCAN_CONTINUOUS";
    /**
     * 连续扫描时间间隔
     */
    public static final String ACTION_SCAN_INTERVAL = "com.rfid.SCAN_INTERVAL";
    /**
     * 条码前缀设置
     */
    public static final String ACTION_SCAN_PREFIX = "com.rfid.SCAN_PREFIX";
    /**
     * 条码后缀设置
     */
    public static final String ACTION_SCAN_SUFFIX = "com.rfid.SCAN_SUFFIX";
    /**
     * 条码结束符设置
     */
    public static final String ACTION_SCAN_END_CHAR = "com.rfid.SCAN_END_CHAR";
    /**
     * 过滤首尾空格开关
     */
    public static final String ACTION_SCAN_FILTER_BLANK = "com.rfid.SCAN_FILTER_BLANK";
    /**
     * 过滤不可见字符
     */
    public static final String ACTION_SCAN_FILTER_INVISIBLE_CHARS = "com.rfid.SCAN_FILTER_INVISIBLE_CHARS";

    /**
     * ACTION_SCAN_CONFIG
     */
    public static final String ACTION_SCAN_CONFIG = "com.rfid.SCAN_CONFIG";
    /**
     * ACTION_ENABLE_SYM
     */
    public static final String ACTION_ENABLE_SYM = "com.rfid.ENABLE_SYM";
    /**
     * ACTION_DISENABLE_SYM
     */
    public static final String ACTION_DISENABLE_SYM = "com.rfid.DISENABLE_SYM";
    /**
     * 发送扫描结果的Action，用户可通过注册该Action 的广播来接收扫描数据
     */
    public static final String ACTION_SCAN_RESULT = "com.rfid.SCAN";
    /**
     * MULL Action
     */
    public static final String ACTION_NULL = "com.rfid.NULL";
    /**
     * 悬浮按钮开关
     */
    public static final String ACTION_FLOAT_BUTTON = "com.rfid.float_button";
}
