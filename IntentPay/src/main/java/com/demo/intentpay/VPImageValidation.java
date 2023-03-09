package com.demo.intentpay;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class VPImageValidation {

    private final Activity context;
    private final VPParams vpParams;
    private final HashMap<String, String> params = new LinkedHashMap<>();


    public VPImageValidation(VPParams vpParams, Activity context) {
        this.vpParams = vpParams;
        this.context = context;
        if (vpParams.getShareImage() == null && TextUtils.isEmpty(vpParams.getApiKey())) {
            throw new RuntimeException("Oops something went wrong");
        } else {
            this.params.put("URI", vpParams.getShareImage());
            this.params.put("API_KEY", vpParams.getApiKey());
        }
    }


    public void startPayment() {
        Intent startActivity = new Intent(context, VPShareValidationGateway.class);
        startActivity.putExtra("SHARE", this.params);
        startActivity.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivityForResult(startActivity, 11111);
    }

}
