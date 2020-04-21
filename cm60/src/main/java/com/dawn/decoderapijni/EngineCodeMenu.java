package com.dawn.decoderapijni;

public class EngineCodeMenu {

    public enum Code1DName {

        CODE128("CODE128"),
        UCCEAN128("UCCEAN128"),
        AIM128("AIM128"),
        ISBT128("ISBT128"),
        EAN8("EAN8"),
        EAN13("EAN13"),
        ISSN("ISSN"),
        ISBN("ISBN"),
        UPCE("UPCE"),
        UPCA("UPCA"),
        ITF("ITF"),
        ITF6("ITF6"),
        ITF14("ITF14"),
        DP14("DP14"),
        DP12("DP12"),
        COOP25("COOP25"),
        MATRIX25("MATRIX25"),
        IND25("IND25"),
        STD25("STD25"),
        CODE39("CODE39"),
        CODEBAR("CODEBAR"),
        CODE93("CODE93"),
        CODE11("CODE11"),
        PLSY("PLSY"),
        MSIPLSY("MSIPLSY"),
        RSS("RSS"),
        COMPOSITE("COMPOSITE"),
        PDF417("PDF417"),
        QR("QR"),
        AZTEC("AZTEC"),
        DM("DM"),
        DM_ECS129("DM_ECS129"),
        MAXIC("MAXIC"),
        CSC("CSC"),
        MICROPDF("MICROPDF"),
        MICROQR("MICROQR"),
        USERC("USERC"),
        GM("GM"),
        CENTERDECODE("CENTERDECODE"),
        OTHER("OTHER");

        private final String Dname;


        Code1DName(String Name) {
            Dname = Name;
        }

        public String getDname() {
            return Dname;
        }
    }

    public enum Code2DName {

        PDF417("PDF417"),
        QR("QR"),
        AZTEC("AZTEC"),
        DM("DM"),
        DM_ECS129("DM_ECS129"),
        MICROPDF("MICROPDF"),
        MICROQR("MICROQR"),
        MAXIC("MAXIC"),
        CSC("CSC"),
        ;

        private final String DDname;


        Code2DName(String dDname) {
            DDname = dDname;
        }

        public String getDDname() {
            return DDname;
        }
    }

    public enum CodeParam {
        ENABLE("Enable"),
        MINLEN("Minlen"),
        MAXLEN("Maxlen"),
        TRSMTCHKCHAR("TrsmtChkChar"),
        FNC1("Fnc1"),
        DIGIT2("Digit2"),
        DIGIT5("Digit5"),
        MSGTOEAN13("MsgtoEan13"),
        TYPETOEAN13("TypetoEan13"),
        ADDONREQUIRED("AddonRequired"),
        TDOUPCA("Tdoupca"),
        USCODE("Uscode"),
        CLOSESYSCHAR("CloseSysChar"),
        COUPON("Coupon"),
        REQCOUPON("ReqCoupon"),
        GS1COUPON("Gs1Coupon"),
        CHECK("Check"),
        TRSMTSTASRTSTOP("TrsmtStasrtStop"),
        FULLASCII("FullAscii"),
        BITCODE32PRECH("BitCode32Prech"),
        CODE32SPECEDIT("Code32SpecEdit"),
        CODE32TRSMTCHKCHAR("Code32TrsmtChkChar"),
        CODE32TRSMTSTASRTSTOP("Code32TrsmtStasrtStop"),
        STARTSTOPMODE("StartStopMode"),
        CHEKMODE("ChkMode"),
        UPCEAN("UpcEan"),
        CODENUM("CodeNum"),
        NUMFIXED("NumFixed"),
        VIDEOMODE("VideoMode"),
        MIRROR("Mirror"),
        CLOSEECI("CloseECI"),
        MODELL("Model1"),
        MICOR("Micor"),
        ONQRSPEC("Onqrspec"),
        RECTANGLE("RectAngle"),
        ADDPADCODE("AddPadcode"),
        ESC129("Esc129"),
        MOBILE("Mobile"),
        LOTTERY("Lottery"),
        CTRDECLEVEL("CtrdecLevel"),
        CTRDECVAULE("CtrdecVaule"),
        TRSMTSYSDIGIT("TrsmtSysDigit"),
        MSGTOUPCA("MsgToupca"),


        DENOISE("Denoise"),
        EXPOSURELEVEL("ExposureLevel"),
        AIMIDOUTPUT("AIMIDoutput"),
        SLEEPTIMESWITCH("SleepTimeSwitch"),
        ;

        private final String param;

        CodeParam(String param) {
            this.param = param;
        }

        public String getParamName() {
            return param;
        }
    }

}
