package com.ssn.se4710;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.zebra.adc.decoder.BarCodeReader;

import static com.ssn.se4710.Se4710Service.bcr;

/**
 * description : 设置扫描参数
 * update : 2019/11/25 14:54,LeiHuang,Init commit
 *
 * @author : LeiHuang
 * @version : VERSION
 */
public class CodeParamSetActivity extends AppCompatActivity {

    private EditText edPnum = null;
    private TextView tvStat = null;
    private EditText edPval = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_param_set);

        ((Button) findViewById(R.id.buttonGet)).setOnClickListener(mGetParamListener);
        edPnum = (EditText) findViewById(R.id.editPnum);
        edPval = (EditText) findViewById(R.id.editPval);
        tvStat = (TextView) findViewById(R.id.textStatus);
    }

    View.OnClickListener mGetParamListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            getParam();
        }
    };

    private void getParam()
    {
        // get param #
        String s = edPnum.getText().toString();
        try
        {
            int num = Integer.parseInt(s);
            doGetParam(num);
        }
        catch (NumberFormatException nx)
        {
            dspStat("value ERROR");
        }
    }

    View.OnClickListener mSetParamListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            setParam();
        }
    };

    private void setParam()
    {
        // get param #
        String sn = edPnum.getText().toString();
        String sv = edPval.getText().toString();
        try
        {
            int num = Integer.parseInt(sn);
            int val = Integer.parseInt(sv);
            doSetParam(num, val);
        }
        catch (NumberFormatException nx)
        {
            dspStat("value ERROR");
        }
    }

    private int doGetParam(int num)
    {
        int val = bcr.getNumParameter(num);
        if (val != BarCodeReader.BCR_ERROR)
        {
            dspStat("Get # " + num + " = " + val);
            edPval.setText(Integer.toString(val));
        }
        else
        {
            dspStat("Get # " + num + " FAILED (" + val + ")");
            edPval.setText(Integer.toString(val));
        }
        return val;
    }

    private int doSetParam(int num, int val)
    {
        String s= "";
        int ret = bcr.setParameter(num, val);
        if (ret != BarCodeReader.BCR_ERROR)
        {
            if (num == BarCodeReader.ParamNum.PRIM_TRIG_MODE)
            {
                if (val == BarCodeReader.ParamVal.HANDSFREE)
                {
                    s = "HandsFree";
                }
                else if (val == BarCodeReader.ParamVal.AUTO_AIM)
                {
                    s = "AutoAim";
                    ret = bcr.startHandsFreeDecode(BarCodeReader.ParamVal.AUTO_AIM);
                    if (ret != BarCodeReader.BCR_SUCCESS)
                    {
                        dspErr("AUtoAIm start FAILED");
                    }
                }
                else if (val == BarCodeReader.ParamVal.LEVEL)
                {
                    s = "Level";
                }
            }
        }
        else {
            s = " FAILED (" + ret +")";
        }

        dspStat("Set #" + num + " to " + val + " " + s);
        return ret;
    }

    private void dspStat(String s)
    {
        tvStat.setText(s);
    }

    private void dspErr(String s)
    {
        tvStat.setText("ERROR" + s);
    }
}
