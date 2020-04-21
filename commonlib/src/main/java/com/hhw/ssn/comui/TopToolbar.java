package com.hhw.ssn.comui;


import android.content.Context;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hhw.ssn.commonlib.R;

/**
 * @author HuangLei 1252065297@qq.com
 * <code>
 * Create At 2019/4/30 15:27
 * Update By 更新者
 * Update At 2019/4/30 15:27
 * </code>
 * 自定义标题栏
 */
public class TopToolbar extends Toolbar implements View.OnClickListener{

    private ImageButton btnLeft;

    private TextView tvTitle;

    private ImageButton btnRight;

    private MenuToolBarListener menuToolBarListener;

    public TopToolbar(Context context) {
        this(context,null);
    }

    public TopToolbar(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public TopToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_top_toolbar, this);
        btnLeft = (ImageButton) findViewById(R.id.btn_htb_left);
        tvTitle = (TextView) findViewById(R.id.tv_htb_title);
        btnRight = (ImageButton) findViewById(R.id.btn_htb_right);

        btnLeft.setOnClickListener(this);
        btnRight.setOnClickListener(this);
    }

    /**
     * 设置中间title的内容
     * @param text 文字内容ID
     */
    public void setMainTitle(int text) {
        this.setTitle(" ");
        tvTitle.setText(text);
    }

    /**
     * 设置中间title的内容
     * @param text 文字内容
     */
    public void setMainTitle(String text) {
        this.setTitle(" ");
        tvTitle.setText(text);
    }

    /**
     * 设置中间title的内容文字的颜色
     * @param color 颜色ID
     */
    public void setMainTitleColor(int color) {
        tvTitle.setTextColor(color);
    }

    /**
     * 设置title左边图标资源
     * @param id 图标资源ID
     */
    public void setLeftTitleDrawable(int id) {
        btnLeft.setImageResource(id);
    }

    /**
     * 设置title左边图标可见性
     * @param visibility 可见性
     */
    public void setLeftTitleVisibility(int visibility) {
        btnLeft.setVisibility(visibility);
    }

    /**
     * 设置title右边图标资源
     * @param id 图标资源ID
     */
    public void setRightTitleDrawable(int id) {
        btnRight.setImageResource(id);
    }

    /**
     * 设置title右边图标可见性
     * @param visibility 可见性
     */
    public void setRightTitleVisiable(int visibility) {
        btnRight.setVisibility(visibility);
    }

    @Override
    public void onClick(View v) {
        if(menuToolBarListener == null){
            return;
        }
        int i = v.getId();
        if (i == R.id.btn_htb_left) {
            menuToolBarListener.onToolBarClickLeft(v);
        } else if (i == R.id.btn_htb_right) {
            menuToolBarListener.onToolBarClickRight(v);
        }
    }

    public void setMenuToolBarListener(MenuToolBarListener listener){
        menuToolBarListener = listener;
    }

    public interface MenuToolBarListener {
        /**
         * The icon on the left is clicked callback
         * @param v View that is clicked
         */
        void onToolBarClickLeft(View v);
        /**
         * The icon on the right is clicked callback
         * @param v  View that is clicked
         */
        void onToolBarClickRight(View v);
    }

}
