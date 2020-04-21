package com.hhw.ssn.comui;

public class CodeSetItem {

    private String codeName;
    private String enable;

    public CodeSetItem() {
    }

    public CodeSetItem(String codeName, String enable) {
        this.codeName = codeName;
        this.enable = enable;
    }

    public String getCodeName() {
        return codeName;
    }

    public void setCodeName(String codeName) {
        this.codeName = codeName;
    }

    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }
}
