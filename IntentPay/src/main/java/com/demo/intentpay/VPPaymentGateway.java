package com.demo.intentpay;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.ClientError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;

public class VPPaymentGateway extends AppCompatActivity {

    VPParams vpParams;
    private static final int UPI_ID = 0;
    Intent intent;
    String amount, apiKey;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vnpayment_gateway);
        Objects.requireNonNull(getSupportActionBar()).hide();
        progressBar = findViewById(R.id.progress_bar);

        HashMap hashMap = (HashMap) this.getIntent().getSerializableExtra("PAYMENT");
        amount = (String) hashMap.get("AMOUNT");
        apiKey = (String) hashMap.get("API_KEY");
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("amount", amount);
            jsonBody.put("api_key", apiKey);
            getOrderId(jsonBody);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getOrderId(JSONObject jsonObject) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);
            gzipOutputStream.write(jsonObject.toString().getBytes("UTF-8"));
            gzipOutputStream.close();
            StringRequest stringRequest = new StringRequest(Request.Method.POST, VPConstant.VP_ORDER_URL, response -> {
                try {
                    JSONObject response1 = new JSONObject(response);
                    System.out.println(response1);
                    if (response1.getString("status").equals("success")) {
                        String getIntentUrl = response1.getString("intent_url");
                        startPayment(getIntentUrl);
                    } else {
                        Intent data = new Intent();
                        data.putExtra("RESPONSE", response1.getString("status"));
                        VPPaymentGateway.this.setResult(-1, data);
                        VPPaymentGateway.this.finish();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }, error -> {
                Intent data = new Intent();
                data.putExtra("RESPONSE", "SERVER ERROR");
                VPPaymentGateway.this.setResult(-1, data);
                VPPaymentGateway.this.finish();
                System.out.println(error instanceof ClientError);
                String message = "";
                if (error instanceof NetworkError) {
                    message = "Cannot connect to internet......please check your internet connection";
                } else if (error instanceof ServerError) {
                    message = "";
                } else if (error instanceof AuthFailureError) {
                    message = "Cannot connect to internet......please check your internet connection";
                } else if (error instanceof ParseError) {
                    message = "Cannot connect to internet......please check your internet connection";
                } else if (error instanceof TimeoutError) {
                    message = "Connection Timedout......please check your internet connection";
                }
                if (!message.isEmpty()) {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }
            }) {
                @Override
                public byte[] getBody() throws AuthFailureError {
                    return outputStream.toByteArray();
                }

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<>();
                    params.put("Content-Encoding", "gzip");
                    params.put("Content-Type", "application/gzip");
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(0, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            stringRequest.setShouldCache(false);
            requestQueue.add(stringRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void startPayment(String getIntentUrl) {
        progressBar.setVisibility(View.GONE);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(getIntentUrl));
        Intent intent1 = Intent.createChooser(intent, "Pay with..");
        if (intent1.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent1, UPI_ID);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (Activity.RESULT_OK == resultCode || resultCode == 11) {

            if (data != null) {
                Bundle extras = data.getExtras();
                System.out.println(extras);
                String result = data.getStringExtra("response");
                List<String> list = new ArrayList<>();
                if (result != null) {
                    list.add(result);
                    upiPaymentDataOperation(list);
                } else {
                    list.add("nothing");
                    upiPaymentDataOperation(list);
                }
            } else {
                List<String> list = new ArrayList<>();
                list.add("nothing");
                upiPaymentDataOperation(list);
            }
        } else {
            List<String> list = new ArrayList<>();
            list.add("nothing");
            upiPaymentDataOperation(list);
        }

    }

    private void upiPaymentDataOperation(List<String> list) {
        if (VPCheckNetwork.isInternetAvailable(this)) {
            String response = list.get(0).toLowerCase(Locale.ROOT);
            String paymentCancel = "";
            String status = "";
            String approvalRefNo = "";
            String[] response1 = response.split("&");
            for (int i = 0; i < response1.length; i++) {
                String[] equalStr = response1[i].split("=");
                if (equalStr.length >= 2) {
                    if (equalStr[0].equalsIgnoreCase("Status")) {
                        status = equalStr[1].toLowerCase();
                    } else if (equalStr[0].equalsIgnoreCase("ApprovalRefNo") || equalStr[0].equalsIgnoreCase("txnRef")) {
                        approvalRefNo = equalStr[1];
                    }
                } else {
                    paymentCancel = "Payment cancelled by user.";
                }
            }
            if (status.equals("success")) {
                Intent data = new Intent();
                data.putExtra("RESPONSE", "PAYMENT SUCCESS");
                VPPaymentGateway.this.setResult(-1, data);
                VPPaymentGateway.this.finish();
            } else if ("Payment cancelled by user.".equals(paymentCancel)) {
                Intent data = new Intent();
                data.putExtra("RESPONSE", "PAYMENT CANCELLED");
                VPPaymentGateway.this.setResult(-1, data);
                VPPaymentGateway.this.finish();
            } else {
                Intent data = new Intent();
                data.putExtra("RESPONSE", "PAYMENT FAILED");
                VPPaymentGateway.this.setResult(-1, data);
                VPPaymentGateway.this.finish();
            }
        } else {
            Intent data = new Intent();
            data.putExtra("RESPONSE", "CONNECTION LOST");
            VPPaymentGateway.this.setResult(-1, data);
            VPPaymentGateway.this.finish();
        }

    }

}