package com.hhw.ssn.comui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.hhw.ssn.combean.PreferenceKey;
import com.hhw.ssn.combean.ServiceActionKey;
import com.hhw.ssn.commonlib.R;
import com.hhw.ssn.comutils.LogUtils;

/**
 * @author HuangLei 1252065297@qq.com
 * <code>
 * Create At 2019/4/27 10:53
 * Update By 更新者
 * Update At 2019/4/27 10:53
 * </code>
 */
public class TabHostActivity extends AppCompatActivity implements TopToolbar.MenuToolBarListener, TabLayout.OnTabSelectedListener {

    private final String TAG = TabHostActivity.class.getSimpleName();

    ViewPager mViewPager;
    TabLayout mTabLayout;
    TopToolbar mTabTopToolBar;
    IOSAlertDialog mIosAlertDialog;

    private AlertDialog mHelpDialog;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ((getIntent().getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
            // 用于判断这个Activity的启动标志，看它所在的应用是不是从后台跑到前台的。如果是，则直接把它finish（）掉，
            // 然后系统会去Activity启动历史栈查询上一个activity，然后再新建它，所以还原到了我们按home键出去的那个界面
            finish();
            return;
        }
        setContentView(R.layout.activity_tab_host);

        initId();
        initView();
    }

    @Override
    public void onBackPressed() {
        // TODO: 2019/5/10  提示确认后台运行或者直接退出
        mIosAlertDialog.setGone().setTitle(this.getString(R.string.alert_title_tips)).setMsg(this.getString(R.string.alert_title_exit)).setNegativeButton(getString(R.string.alert_btn_exit), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 关闭后台扫描
                Intent toKillService = new Intent();
                toKillService.putExtra("CallFromService", true);
                toKillService.setAction(ServiceActionKey.ACTION_CLOSE_SCAN);
                TabHostActivity.this.getApplicationContext().sendBroadcast(toKillService);
                finish();
            }
        }).setPositiveButton(getString(R.string.alert_btn_persist), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 关闭后台扫描
                finish();
            }
        }).show();
    }

    @Override
    protected void onDestroy() {
        if (mHelpDialog != null && mHelpDialog.isShowing()) {
            mHelpDialog.dismiss();
        }
        super.onDestroy();
    }

    private void initId() {
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mTabTopToolBar = (TopToolbar) findViewById(R.id.tab_topToolBar);
        mIosAlertDialog = new IOSAlertDialog(this).builder();
    }

    private void initView() {
        // 设置TopToolBar标题栏图片去除
        mTabTopToolBar.setLeftTitleVisibility(View.GONE);
        // 设置TopToolBar标题栏右边图片内容
        mTabTopToolBar.setRightTitleVisiable(View.VISIBLE);
        mTabTopToolBar.setRightTitleDrawable(R.drawable.ic_toolbar_help);
        // 获取应用版本号名称
        String versionName = "";
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            LogUtils.e(TAG, "initView, packageInfo " + packageInfo);
            versionName = " v" + packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        // 设置TopToolBar标题栏内容（显示版本号名称）
        mTabTopToolBar.setMainTitle(getString(R.string.app_name) + versionName);
        //设置TopToolBar标题栏点击监听
        mTabTopToolBar.setMenuToolBarListener(this);
        // 添加Tab标题
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.scan_tab_title)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.settings_tab_title)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getString(R.string.code_tab_title)));
        // 添加Fragment进ViewPager
        final PagerAdapter adapter = new TestPagerAdapter(getFragmentManager(), mTabLayout.getTabCount());
        mViewPager.setAdapter(adapter);
        //为ViewPager添加页面改变监听
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        //为TabLayout添加Tab选择监听
        mTabLayout.addOnTabSelectedListener(this);
        // 关于应用
        mHelpDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.about))
                .setMessage(this.getString(R.string.app_description) + " " + versionName).create();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        boolean scanSwitch = defaultSharedPreferences.getBoolean(PreferenceKey.KEY_SWITCH_SCAN, false);
        LogUtils.e(TAG, "onResume, scanSwitch:" + scanSwitch);
        if (!scanSwitch) {
            mViewPager.setCurrentItem(1);
        }
    }

    /**
     * ---------------------------------------------TopToolbar 标题栏点击监听-----------------------------------------------
     */
    @Override
    public void onToolBarClickLeft(View v) {
    }

    @Override
    public void onToolBarClickRight(View v) {
        mHelpDialog.show();
    }

    /**
     * ---------------------------------------------TabLayout 选择监听-----------------------------------------------
     */
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        // 显示对应Fragment
        int position = tab.getPosition();
        LogUtils.e(TAG, "onTabSelected, position:" + position);
        mViewPager.setCurrentItem(position);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }
}
