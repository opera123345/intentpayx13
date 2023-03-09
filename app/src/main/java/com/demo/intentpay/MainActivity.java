package com.demo.intentpay;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.IOException;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();

        Intent receivedIntent = getIntent();
        String receivedAction = receivedIntent.getAction();
        String receivedType = receivedIntent.getType();

        if (receivedAction.equals(Intent.ACTION_SEND)) {
            Uri receiveUri = (Uri) receivedIntent.getParcelableExtra(Intent.EXTRA_STREAM);
            System.out.println(receiveUri);


            if (receivedIntent.getType() != null) {
                if (receivedType.startsWith("image/") || receivedType.startsWith("image/*") || receivedType.startsWith("*/*")) {
                    VPParams builder = new VPParams();
                    builder.setApiKey("YOUR API KEY");
                    builder.setShareImage(receiveUri.toString());
                    VPImageValidation vpImageValidation = new VPImageValidation(builder, MainActivity.this);
                    vpImageValidation.startPayment();
                }
            }


        } else if (receivedAction.equals(Intent.ACTION_MAIN)) {
            Log.e("UpiPAY", "onSharedIntent: nothing shared");
        }

        Button button = findViewById(R.id.pay_upi);
        EditText editText = findViewById(R.id.enter_amount);
        button.setOnClickListener(v -> {
            String amount = editText.getText().toString().trim();
            if (amount.isEmpty()) {
                editText.setError("Please enter amount");
                editText.setFocusable(true);
            } else {
                VPParams builder = new VPParams();
                builder.setAmount(amount);
                builder.setApiKey("YOUR API KEY");
                VPPaymentValidation vpPaymentValidation = new VPPaymentValidation(builder, MainActivity.this);
                vpPaymentValidation.startPayment();
            }

        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            if (data != null) {
                if (!data.getStringExtra("RESPONSE").isEmpty()) {

                    new MaterialAlertDialogBuilder(this, com.google.android.material.R.style.Widget_AppCompat_ActionBar_Solid).setTitle("Alert!").setMessage(data.getStringExtra("RESPONSE")).setPositiveButton("Okay", (dialogInterface, i) -> {
                    }).show();

                }
            }

        }

    }
}