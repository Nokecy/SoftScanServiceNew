package com.ssn.se4710.dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

/**
 * description : Se4710码制属性
 * update : 2019/11/15 19:22,LeiHuang,Init commit
 *
 * @author : LeiHuang
 * @version : 1.0
 */
@Entity
public class Symbology {
    @Id(autoincrement = true)
    private Long id;
    /**
     * 设置列表中的设置项，在Se4710设置中的Number值
     */
    @NotNull
    @Unique
    private Integer paramNum;
    /**
     * 设置列表中的设置项的名称
     */
    @NotNull
    private String paramName;
    /**
     * 设置项的默认值
     */
    @NotNull
    private Integer paramValueDef;
    /**
     * 设置项的现在的值
     */
    private Integer paramValue;
    /**
     * 是否需要打开扫描头时设置该项，由默认值和现在的值比对决定
     */
    private boolean paramNeedSet;
    @Generated(hash = 859801509)
    public Symbology(Long id, @NotNull Integer paramNum, @NotNull String paramName,
            @NotNull Integer paramValueDef, Integer paramValue,
            boolean paramNeedSet) {
        this.id = id;
        this.paramNum = paramNum;
        this.paramName = paramName;
        this.paramValueDef = paramValueDef;
        this.paramValue = paramValue;
        this.paramNeedSet = paramNeedSet;
    }
    @Generated(hash = 805131072)
    public Symbology() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Integer getParamNum() {
        return this.paramNum;
    }
    public void setParamNum(Integer paramNum) {
        this.paramNum = paramNum;
    }
    public String getParamName() {
        return this.paramName;
    }
    public void setParamName(String paramName) {
        this.paramName = paramName;
    }
    public Integer getParamValueDef() {
        return this.paramValueDef;
    }
    public void setParamValueDef(Integer paramValueDef) {
        this.paramValueDef = paramValueDef;
    }
    public Integer getParamValue() {
        return this.paramValue;
    }
    public void setParamValue(Integer paramValue) {
        this.paramValue = paramValue;
    }
    public boolean getParamNeedSet() {
        return this.paramNeedSet;
    }
    public void setParamNeedSet(boolean paramNeedSet) {
        this.paramNeedSet = paramNeedSet;
    }
}
