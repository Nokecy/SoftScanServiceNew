package com.hhw.ssn.comui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.hhw.ssn.combean.PreferenceKey;
import com.hhw.ssn.combean.ServiceActionKey;
import com.hhw.ssn.commonlib.R;
import com.hhw.ssn.comutils.FloatService;
import com.hhw.ssn.comutils.LogUtils;
import com.hhw.ssn.comutils.RegularUtils;
import com.hhw.ssn.comutils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * @author HuangLei 1252065297@qq.com
 * <code>
 * Create At 2019/4/27 11:14
 * Update By 更新者
 * Update At 2019/4/27 11:14
 * </code>
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private final String TAG = SettingsFragment.class.getSimpleName();
    private SharedPreferences mDefaultSharedPreferences;
    private ListPreference mListPreferenceInput;
    private ListPreference mListPreferenceAppend;
    private ListPreference mListPreferenceCharset;
    private SwitchPreference mSwitchScanService;
    private SwitchPreference mSwitchContinuous;
    private EditTextPreference mIntervalPreference;
    private EditTextPreference mPrefixPreference;
    private EditTextPreference mSuffixPreference;
    private SwitchPreference mSwitchVoice;
    private SwitchPreference mSwitchVibrate;
    private SwitchPreference mSwitchInvisibleChar;
    private SwitchPreference mSwitchRmSpace;
    private SwitchPreference mSwitchFloatButton;
    private SwitchPreference mSwitchStopOnUp;
    private EditTextPreference mEditPreferenceTimeout;
    private BaseApplication mApplication;
    private SwitchPreference mSwitchIllumination;
    private SwitchPreference mSwitchAimingPattern;

    private Dialog dialogLoading;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.general_settings);
        initView();
        LogUtils.i(TAG, "onCreate");
        EventBus.getDefault().register(this);
    }

    private void initView() {
        mDefaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity().getApplicationContext());
        // 照明光开关设置
        mSwitchIllumination = (SwitchPreference) findPreference(PreferenceKey.KEY_SCANNING_ILLUMINATION);
        mSwitchIllumination.setOnPreferenceChangeListener(this);
        // 瞄准图标开关设置
        mSwitchAimingPattern = (SwitchPreference) findPreference(PreferenceKey.KEY_SCANNING_AIMING_PATTERN);
        mSwitchAimingPattern.setOnPreferenceChangeListener(this);
        // 前缀设置
        mPrefixPreference = (EditTextPreference) findPreference(PreferenceKey.KEY_PREFIX_CONFIG);
        mPrefixPreference.setOnPreferenceChangeListener(this);
        // 后缀设置
        mSuffixPreference = (EditTextPreference) findPreference(PreferenceKey.KEY_SUFFIX_CONFIG);
        mSuffixPreference.setOnPreferenceChangeListener(this);
        //输入模式设置
        mListPreferenceInput = (ListPreference) findPreference(PreferenceKey.KEY_INPUT_CONFIG);
        mListPreferenceInput.setOnPreferenceChangeListener(this);
        // 追加结束符设置
        mListPreferenceAppend = (ListPreference) findPreference(PreferenceKey.KEY_APPEND_ENDING_CHAR);
        mListPreferenceAppend.setOnPreferenceChangeListener(this);
        // 扫描结果字符编码设置
        mListPreferenceCharset = (ListPreference) findPreference(PreferenceKey.KEY_RESULT_CHAR_SET);
        mListPreferenceCharset.setOnPreferenceChangeListener(this);
        // 扫描开关监听
        mSwitchScanService = (SwitchPreference) findPreference(PreferenceKey.KEY_SWITCH_SCAN);
        mSwitchScanService.setOnPreferenceChangeListener(this);
        mSwitchScanService.setOnPreferenceClickListener(this);
        // 连续扫描开关
        mSwitchContinuous = (SwitchPreference) findPreference(PreferenceKey.KEY_CONTINUOUS_SCANNING);
        mSwitchContinuous.setOnPreferenceChangeListener(this);
        // 声音设置
        mSwitchVoice = (SwitchPreference) findPreference(PreferenceKey.KEY_SCANNING_VOICE);
        mSwitchVoice.setOnPreferenceChangeListener(this);
        // 震动设置
        mSwitchVibrate = (SwitchPreference) findPreference(PreferenceKey.KEY_SCANNING_VIBRATOR);
        mSwitchVibrate.setOnPreferenceChangeListener(this);
        // 去除扫描结果中的不可见字符设置
        mSwitchInvisibleChar = (SwitchPreference) findPreference(PreferenceKey.KEY_INVISIBLE_CHAR);
        mSwitchInvisibleChar.setOnPreferenceChangeListener(this);
        // 去除扫描结果中的首尾空格设置
        mSwitchRmSpace = (SwitchPreference) findPreference(PreferenceKey.KEY_RM_SPACE);
        mSwitchRmSpace.setOnPreferenceChangeListener(this);
        // 松开按键是否停止扫描的设置
        mSwitchStopOnUp = (SwitchPreference) findPreference(PreferenceKey.KEY_STOP_ON_UP);
        mSwitchStopOnUp.setOnPreferenceChangeListener(this);
        // 悬浮按钮设置
        mSwitchFloatButton = (SwitchPreference) findPreference(PreferenceKey.KEY_FLOAT_BUTTON);
        mSwitchFloatButton.setOnPreferenceChangeListener(this);
        // 扫描超时设置
        mEditPreferenceTimeout = (EditTextPreference) findPreference(PreferenceKey.KEY_DECODE_TIME);
        mEditPreferenceTimeout.setOnPreferenceChangeListener(this);
        // 连续扫描间隔设置
        mIntervalPreference = (EditTextPreference) findPreference(PreferenceKey.KEY_SCANNING_INTERVAL);
        mIntervalPreference.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        LogUtils.i(TAG, "onPreferenceChange, key = " + preference.getKey() + ", newValue = " + newValue);
        switch (preference.getKey()) {
            case PreferenceKey.KEY_SWITCH_SCAN:
                if ((boolean) newValue) {
                    sendCmd(ServiceActionKey.ACTION_SCAN_INIT);
                    mSwitchScanService.setSummary(R.string.opened);
                } else {
                    sendCmd(ServiceActionKey.ACTION_CLOSE_SCAN);
                    mSwitchScanService.setSummary(R.string.closed);
                }
                enableAllSettings((boolean) newValue);
                break;
            case PreferenceKey.KEY_SCANNING_VOICE:
                updateSwtichPreference(mSwitchVoice, (boolean) newValue);
                break;
            case PreferenceKey.KEY_SCANNING_VIBRATOR:
                updateSwtichPreference(mSwitchVibrate, (boolean) newValue);
                break;
            case PreferenceKey.KEY_INVISIBLE_CHAR:
                updateSwtichPreference(mSwitchInvisibleChar, (boolean) newValue);
                break;
            case PreferenceKey.KEY_RM_SPACE:
                updateSwtichPreference(mSwitchRmSpace, (boolean) newValue);
                break;
            case PreferenceKey.KEY_FLOAT_BUTTON:
                updateSwtichPreference(mSwitchFloatButton, (boolean) newValue);
                Intent intent = new Intent(ServiceActionKey.ACTION_FLOAT_BUTTON);
                intent.putExtra("isShow", (Boolean) newValue);
                LocalBroadcastManager.getInstance(this.getActivity()).sendBroadcast(intent);
                break;
            case PreferenceKey.KEY_STOP_ON_UP:
                updateSwtichPreference(mSwitchStopOnUp, (boolean) newValue);
                break;
            case PreferenceKey.KEY_PREFIX_CONFIG:
                mPrefixPreference.setSummary((String) newValue);
                break;
            case PreferenceKey.KEY_SUFFIX_CONFIG:
                mSuffixPreference.setSummary((String) newValue);
                break;
            case PreferenceKey.KEY_CONTINUOUS_SCANNING:
                boolean isContinuousSwitchOpen = (boolean) newValue;
                if (isContinuousSwitchOpen) {
                    mSwitchContinuous.setSummary(R.string.opened);
                    // 自动扫描开启时，禁用抬起停止扫描项
                    if (mSwitchStopOnUp.isChecked()) {
                        mSwitchStopOnUp.setChecked(false);
                        updateSwtichPreference(mSwitchStopOnUp, false);
                    }
                } else {
                    mSwitchContinuous.setSummary(R.string.closed);
                    sendCmd(ServiceActionKey.ACTION_STOP_SCAN);
                }
                mIntervalPreference.setEnabled(isContinuousSwitchOpen);
                mSwitchStopOnUp.setEnabled(!isContinuousSwitchOpen);
                break;
            case PreferenceKey.KEY_SCANNING_INTERVAL:
                String interval = (String) newValue;
                if (!RegularUtils.verifyScanInterval(interval)) {
                    ToastUtils.showShortToast(SettingsFragment.this.getActivity(), getString(R.string.scanning_interval_illegal));
                    return false;
                } else {
                    mIntervalPreference.setSummary(interval);
                }
                break;
            case PreferenceKey.KEY_DECODE_TIME:
                String decodeTimeout = (String) newValue;
                if (!RegularUtils.verifyScanTimeout(decodeTimeout)) {
                    ToastUtils.showShortToast(SettingsFragment.this.getActivity(), getString(R.string.decode_time_limit_illegal));
                    return false;
                } else {
                    mEditPreferenceTimeout.setSummary(decodeTimeout);
                    sendCmdWiths(ServiceActionKey.ACTION_SCAN_TIME, "time", (String) newValue);
                }
                break;
            case PreferenceKey.KEY_SCANNING_ILLUMINATION:
                updateSwtichPreference(mSwitchIllumination, (boolean) newValue);
                sendCmdWithi(ServiceActionKey.ACTION_ILLUMINATION, PreferenceKey.KEY_SCANNING_ILLUMINATION, (boolean) newValue ? 1 : 0);
                break;
            case PreferenceKey.KEY_SCANNING_AIMING_PATTERN:
                updateSwtichPreference(mSwitchAimingPattern, (boolean) newValue);
                sendCmdWithi(ServiceActionKey.ACTION_AIMING_PATTERN, PreferenceKey.KEY_SCANNING_AIMING_PATTERN, (boolean) newValue ? 1 : 0);
                break;
            case PreferenceKey.KEY_ILLUMINATION_LEVEL:
                updatePreference((ListPreference) preference, (String) newValue);
                sendCmdWiths(ServiceActionKey.ACTION_ILLUMINATION_LEVEL, PreferenceKey.KEY_ILLUMINATION_LEVEL, (String) newValue);
                break;
            case PreferenceKey.KEY_INPUT_CONFIG:
            case PreferenceKey.KEY_RESULT_CHAR_SET:
            case PreferenceKey.KEY_APPEND_ENDING_CHAR:
                updatePreference((ListPreference) preference, (String) newValue);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtils.e(TAG, "onResume()");
        mApplication = ((BaseApplication) this.getActivity().getApplication());
        // 更新扫描开关状态
        initSwitchPreferen(mSwitchScanService, PreferenceKey.KEY_SWITCH_SCAN, false);
        // 更新声音开关状态
        initSwitchPreferen(mSwitchVoice, PreferenceKey.KEY_SCANNING_VOICE, true);
        // 更新震动开关状态
        initSwitchPreferen(mSwitchVibrate, PreferenceKey.KEY_SCANNING_VIBRATOR, false);
        // 更新照明开关状态
        initSwitchPreferen(mSwitchIllumination, PreferenceKey.KEY_SCANNING_ILLUMINATION, true);
        // 更新瞄准开关状态
        initSwitchPreferen(mSwitchAimingPattern, PreferenceKey.KEY_SCANNING_AIMING_PATTERN, true);
        // 更新去除不可见字符设置的状态
        initSwitchPreferen(mSwitchInvisibleChar, PreferenceKey.KEY_INVISIBLE_CHAR, false);
        // 更新去除首尾空格设置的状态
        initSwitchPreferen(mSwitchRmSpace, PreferenceKey.KEY_RM_SPACE, false);
        // 更新抬起按键是否停止扫描的设置项的状态
        initSwitchPreferen(mSwitchStopOnUp, PreferenceKey.KEY_STOP_ON_UP, true);
        // 更新连续扫描开关状态
        initSwitchPreferen(mSwitchContinuous, PreferenceKey.KEY_CONTINUOUS_SCANNING, false);
        // 更新前后缀内容
        updateEditPreference(mPrefixPreference, PreferenceKey.KEY_PREFIX_CONFIG, "");
        updateEditPreference(mSuffixPreference, PreferenceKey.KEY_SUFFIX_CONFIG, "");
//        mPrefixPreference.setSummary(mDefaultSharedPreferences.getString(PreferenceKey.KEY_PREFIX_CONFIG, ""));
//        mSuffixPreference.setSummary(mDefaultSharedPreferences.getString(PreferenceKey.KEY_SUFFIX_CONFIG, ""));
        // 更新扫描超时项的状态
//        String scanningTimeout = mDefaultSharedPreferences.getString(PreferenceKey.KEY_DECODE_TIME, "10000");
//        mEditPreferenceTimeout.setSummary(scanningTimeout);
//        mEditPreferenceTimeout.setText(scanningTimeout);
        updateEditPreference(mEditPreferenceTimeout, PreferenceKey.KEY_DECODE_TIME, "10000");
        // 更新扫描间隔项的状态
//        String scanningInterval = mDefaultSharedPreferences.getString(PreferenceKey.KEY_SCANNING_INTERVAL, "1000");
//        mIntervalPreference.setSummary(scanningInterval);
        updateEditPreference(mIntervalPreference, PreferenceKey.KEY_SCANNING_INTERVAL, "1000");
        mIntervalPreference.setEnabled(mSwitchContinuous.isChecked());
        // 更新扫描结果的发送方式
        updatePreference(mListPreferenceInput, mDefaultSharedPreferences.getString(PreferenceKey.KEY_INPUT_CONFIG, "1"));
        // 更新追加结束符设置项
        updatePreference(mListPreferenceAppend, mDefaultSharedPreferences.getString(PreferenceKey.KEY_APPEND_ENDING_CHAR, "4"));
        // 更新扫描结果的字符编码设置项
        updatePreference(mListPreferenceCharset, mDefaultSharedPreferences.getString(PreferenceKey.KEY_RESULT_CHAR_SET, "1"));
        // 更新所有设置项的可访问状态
        enableAllSettings(mSwitchScanService.isChecked());
        // 如果连续扫描开关打开，抬起停止设置项不可设置
        mSwitchStopOnUp.setEnabled(!mSwitchContinuous.isChecked());
    }

    /**
     * @param isVisibleToUser true if this fragment's UI is currently visible to the user (default),
     *                        false if it is not.
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
//        LogUtils.e(TAG, "setUserVisibleHint, isVisibleToUser:" + isVisibleToUser);
//        if (mApplication != null && isVisibleToUser) {
//            mApplication.mSpUtils.putIsInSettingUi(true);
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dialogLoading != null && dialogLoading.isShowing()) {
            dialogLoading.cancel();
        }
        EventBus.getDefault().unregister(this);
    }

    /**
     * 更新ListPreference的Summary
     *
     * @param preference 待更新的preference
     * @param newValue   EntryValue值
     */
    private void updatePreference(ListPreference preference, String newValue) {
        CharSequence[] entries = preference.getEntries();
        int index = preference.findIndexOfValue(newValue);
        preference.setSummary(entries[index]);
    }

    /**
     * 更新EditTextPreference
     *
     * @param preference    待更新的Preference
     * @param preferenceKey 获取默认值的KEY
     * @param defaultValue  默认值
     */
    private void updateEditPreference(EditTextPreference preference, String preferenceKey, String defaultValue) {
        String string = mDefaultSharedPreferences.getString(preferenceKey, defaultValue);
        preference.setText(string);
        preference.setSummary(string);
    }

    /**
     * 更新SwitchPreference的Summary
     */
    private void updateSwtichPreference(SwitchPreference switchPreference, boolean isOpened) {
        if (isOpened) {
            switchPreference.setSummary(getString(R.string.opened));
        } else {
            switchPreference.setSummary(getString(R.string.closed));
        }
    }

    /**
     * 进入页面时更新SwitchPreference
     */
    private void initSwitchPreferen(SwitchPreference switchPreference, String preferenceKey, boolean defaultValue) {
        boolean isOpened = mDefaultSharedPreferences.getBoolean(preferenceKey, defaultValue);
        if (isOpened) {
            switchPreference.setSummary(R.string.opened);
        } else {
            switchPreference.setSummary(R.string.closed);
        }
        switchPreference.setChecked(isOpened);
    }

    /**
     * 设置项是否可访问
     *
     * @param b 是否允许
     */
    private void enableAllSettings(boolean b) {
        findPreference(PreferenceKey.KEY_CATEGORY_SCANNING).setEnabled(b);
        findPreference(PreferenceKey.KEY_CATEGORY_DECODING).setEnabled(b);
    }

    /**
     * 通知Service改变设置
     *
     * @param action 需要改变的设置
     */
    private void sendCmd(String action) {
        Intent configIntent = new Intent();
        configIntent.setAction(action);
        this.getActivity().getApplicationContext().sendBroadcast(configIntent);
    }

    private void sendCmdWithi(String action, String key, int extras) {
        Intent configIntent = new Intent();
        configIntent.setAction(action);
        configIntent.putExtra(key, extras);
        this.getActivity().getApplicationContext().sendBroadcast(configIntent);
    }

    private void sendCmdWiths(String action, String key, String extras) {
        Intent configIntent = new Intent();
        configIntent.setAction(action);
        configIntent.putExtra(key, extras);
        this.getActivity().getApplicationContext().sendBroadcast(configIntent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(String event) {
        if (mApplication == null) {
            mApplication = ((BaseApplication) this.getActivity().getApplication());
        }
        // 更新扫描开关状态
        initSwitchPreferen(mSwitchScanService, PreferenceKey.KEY_SWITCH_SCAN, false);
        // 更新声音开关状态
        initSwitchPreferen(mSwitchVoice, PreferenceKey.KEY_SCANNING_VOICE, true);
        // 更新震动开关状态
        initSwitchPreferen(mSwitchVibrate, PreferenceKey.KEY_SCANNING_VIBRATOR, false);
        // 更新照明开关状态
        initSwitchPreferen(mSwitchIllumination, PreferenceKey.KEY_SCANNING_ILLUMINATION, true);
        // 更新瞄准开关状态
        initSwitchPreferen(mSwitchAimingPattern, PreferenceKey.KEY_SCANNING_AIMING_PATTERN, true);
        // 更新去除不可见字符设置的状态
        initSwitchPreferen(mSwitchInvisibleChar, PreferenceKey.KEY_INVISIBLE_CHAR, false);
        // 更新去除首尾空格设置的状态
        initSwitchPreferen(mSwitchRmSpace, PreferenceKey.KEY_RM_SPACE, false);
        // 更新抬起按键是否停止扫描的设置项的状态
        initSwitchPreferen(mSwitchStopOnUp, PreferenceKey.KEY_STOP_ON_UP, true);
        // 更新连续扫描开关状态
        initSwitchPreferen(mSwitchContinuous, PreferenceKey.KEY_CONTINUOUS_SCANNING, false);
        // 更新前后缀内容
        updateEditPreference(mPrefixPreference, PreferenceKey.KEY_PREFIX_CONFIG, "");
        updateEditPreference(mSuffixPreference, PreferenceKey.KEY_SUFFIX_CONFIG, "");
//        mPrefixPreference.setSummary(mDefaultSharedPreferences.getString(PreferenceKey.KEY_PREFIX_CONFIG, ""));
//        mSuffixPreference.setSummary(mDefaultSharedPreferences.getString(PreferenceKey.KEY_SUFFIX_CONFIG, ""));
        // 更新扫描超时项的状态
//        String scanningTimeout = mDefaultSharedPreferences.getString(PreferenceKey.KEY_DECODE_TIME, "10000");
//        mEditPreferenceTimeout.setSummary(scanningTimeout);
//        mEditPreferenceTimeout.setText(scanningTimeout);
        updateEditPreference(mEditPreferenceTimeout, PreferenceKey.KEY_DECODE_TIME, "10000");
        // 更新扫描间隔项的状态
//        String scanningInterval = mDefaultSharedPreferences.getString(PreferenceKey.KEY_SCANNING_INTERVAL, "1000");
//        mIntervalPreference.setSummary(scanningInterval);
        updateEditPreference(mIntervalPreference, PreferenceKey.KEY_SCANNING_INTERVAL, "1000");
        mIntervalPreference.setEnabled(mSwitchContinuous.isChecked());
        // 更新扫描结果的发送方式
        updatePreference(mListPreferenceInput, mDefaultSharedPreferences.getString(PreferenceKey.KEY_INPUT_CONFIG, "1"));
        // 更新追加结束符设置项
        updatePreference(mListPreferenceAppend, mDefaultSharedPreferences.getString(PreferenceKey.KEY_APPEND_ENDING_CHAR, "4"));
        // 更新扫描结果的字符编码设置项
        updatePreference(mListPreferenceCharset, mDefaultSharedPreferences.getString(PreferenceKey.KEY_RESULT_CHAR_SET, "1"));
        // 更新所有设置项的可访问状态
        enableAllSettings(mSwitchScanService.isChecked());
        // 如果连续扫描开关打开，抬起停止设置项不可设置
        mSwitchStopOnUp.setEnabled(!mSwitchContinuous.isChecked());
        // 取消提示框
        dialogLoading.cancel();
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        LogUtils.i(TAG, "onPreferenceClick key = " + key);
        if (key.equals(PreferenceKey.KEY_SWITCH_SCAN)) {
            createLoadingDialog(!mSwitchScanService.isChecked());
        }
        return false;
    }

    /**
     * 提示框，提示用户正在初始化，用以避免用户快速点击开关造成异常
     */
    private void createLoadingDialog(boolean checked) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        View view = LayoutInflater.from(this.getActivity()).inflate(R.layout.dialog_loading, null, false);
        TextView tv = view.findViewById(R.id.textView1);
        if (checked) {
            tv.setText(R.string.opening);
        } else {
            tv.setText(R.string.closeing);
        }
        builder.setView(view);
        dialogLoading = builder.create();
        dialogLoading.setCancelable(false);
        dialogLoading.show();
    }
}
