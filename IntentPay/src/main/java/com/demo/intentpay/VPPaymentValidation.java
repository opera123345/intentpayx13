package com.demo.intentpay;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class VPPaymentValidation {

    private final Activity context;
    private final VPParams vpParams;
    private final HashMap<String, String> params = new LinkedHashMap<>();


    public VPPaymentValidation(VPParams pnParams, Activity context) {
        this.vpParams = pnParams;
        this.context = context;
        if (TextUtils.isEmpty(pnParams.getAmount()) && TextUtils.isEmpty(pnParams.getApiKey())) {
            throw new RuntimeException("Oops something went wrong");
        } else {
            this.params.put("AMOUNT", vpParams.getAmount());
            this.params.put("API_KEY", vpParams.getApiKey());
        }
    }


    public void startPayment() {
        Intent startActivity = new Intent(context, VPPaymentGateway.class);
        startActivity.putExtra("PAYMENT", this.params);
        startActivity.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivityForResult(startActivity, 11111);
    }

}
