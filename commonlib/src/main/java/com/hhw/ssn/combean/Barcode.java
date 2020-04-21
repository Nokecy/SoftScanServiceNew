package com.hhw.ssn.combean;

/**
 * description : 封装有扫描数据的实体类，包含序列号（按照扫描到的顺序，自动生成）、数据内容、扫描到的次数、条码或者二维码的码制
 * update : 2019/10/23 18:07,LeiHuang,Init commit
 *
 * @author : LeiHuang
 * @version : 1.0
 */
public class Barcode {
    public int sn;
    public String barcode;
    public int count;
    public byte codeId;
}
