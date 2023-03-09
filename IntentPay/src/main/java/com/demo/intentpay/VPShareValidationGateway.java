package com.demo.intentpay;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VPShareValidationGateway extends AppCompatActivity {

    String sharedImageUri, apiKey;
    ImageView previewShare;
    Date date_img_name = new Date();
    long timeMilli = date_img_name.getTime();
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vpshare_validation_gateway);
        progressBar = findViewById(R.id.my_progress_bar);
        Objects.requireNonNull(getSupportActionBar()).hide();
        HashMap hashMap = (HashMap) this.getIntent().getSerializableExtra("SHARE");
        sharedImageUri = (String) hashMap.get("URI");
        apiKey = (String) hashMap.get("API_KEY");

        previewShare = findViewById(R.id.shared_image);
        getImageUri();
    }

    private void getImageUri() {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(sharedImageUri));
            Uri af_path = getImageUri(getApplicationContext(), bitmap);
            previewShare.setImageURI(af_path);
            try {
                InputStream inputStream = this.getContentResolver().openInputStream(af_path);
                byte[] bytes = IOUtils.toByteArray(inputStream);
                String base64String = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    base64String = Base64.getEncoder().encodeToString(bytes);

                    try {
                        JSONObject jsonBody = new JSONObject();
                        jsonBody.put("invoice", base64String);
                        jsonBody.put("api_key", apiKey);
                        callApiRequest(jsonBody);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Base 64: " + base64String);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void callApiRequest(JSONObject jsonObject) {

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);
            gzipOutputStream.write(jsonObject.toString().getBytes("UTF-8"));
            gzipOutputStream.close();
            StringRequest stringRequest = new StringRequest(Request.Method.POST, VPConstant.VP_SHARE_IMAGE_URL, response -> {
                try {
                    JSONObject response1 = new JSONObject(response);
                    System.out.println(response1);
                    String getStatus = response1.getString("status");
                    if (getStatus.equals("success")) {
                        Intent data = new Intent();
                        data.putExtra("RESPONSE", "VERIFY SUCCESS");
                        VPShareValidationGateway.this.setResult(-1, data);
                        VPShareValidationGateway.this.finish();
                    } else {
                        Intent data = new Intent();
                        data.putExtra("RESPONSE", "VERIFY FAILED");
                        VPShareValidationGateway.this.setResult(-1, data);
                        VPShareValidationGateway.this.finish();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }, error -> {
                Intent data = new Intent();
                data.putExtra("RESPONSE", "SERVER ERROR");
                VPShareValidationGateway.this.setResult(-1, data);
                VPShareValidationGateway.this.finish();
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


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 0, bytes);
        String test_path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, String.valueOf(timeMilli), null);
        return Uri.parse(test_path);
    }
}