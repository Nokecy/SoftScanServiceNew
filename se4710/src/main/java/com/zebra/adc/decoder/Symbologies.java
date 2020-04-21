package com.zebra.adc.decoder;

/**
 * description : Se4710码制列表
 * update : 2019/11/15 11:00,LeiHuang,Init commit
 *
 * @author : LeiHuang
 * @version : 1.0
 */
public interface Symbologies {
    Integer UPC_A = 1;
    Integer UPC_E = 2;
    Integer EAN_8_JAN_8 = 4;
    Integer EAN_13_JAN_13 = 3;
    Integer ISSN_EAN = 617;
    Integer CODE_128 = 8;
    Integer GS1_128_UCC_EAN_128 = 14;
    Integer ISBT_128 = 84;
    Integer CODE_39 = 0;
    /**
     * Trioptic Code 39 is a variant of Code 39 used in the marking of computer tape cartridges. Trioptic Code 39
     * symbols always contain six characters.
     * NOTE Trioptic Code 39 and Code 39 Full ASCII cannot be enabled simultaneously
     */
    Integer TRIOPTIC_CODE_39 = 13;
    Integer CODE_93 = 9;
    Integer CODE_11 = 10;
    Integer INTERLEAVED_2_OF_5 = 6;
    Integer DISCRETE_2_OF_5 = 5;
    Integer CODABAR = 7;
    Integer MSI = 11;
    Integer CHINESE_2_OF_5 = 408;
    /**
     * NOTE The length for Korean 3 of 5 is fixed at 6
     */
    Integer KOREAN_3_OF_5 = 581;
    Integer MATRIX_2_OF_5 = 618;
    Integer US_POSTNET = 89;
    Integer US_PLANET = 90;
    Integer UK_POSTAL = 91;
    Integer JAPAN_POSTAL = 290;
    Integer AUSTRALIA_POST = 291;
    Integer NETHERLANDS_KIX_CODE = 326;
    Integer USPS_4CB_ONE_CODE_INTELLIGENT_MAIL = 592;
    Integer UPU_FICS_POSTAL = 611;
    Integer GS1_DATABAR_14 = 338;
    Integer GS1_DATABAR_LIMITED = 339;
    Integer GS1_DATABAR_EXPANDED = 340;
    Integer PDF417 = 15;
    Integer MICROPDF417 = 227;
    Integer DATA_MATRIX = 292;
    Integer MAXICODE = 294;
    Integer QR_CODE = 293;
    Integer MICROQR = 293;
    Integer AZTEC = 574;
    Integer HAN_XIN = 1167;
}
