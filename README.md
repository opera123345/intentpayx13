# IntentPayx13
## Introduction

Greetings, Integrate the SDK to initiate UPI Payment and Valide the payment made in Android application. 
It is mandatory to have atleast one UPI app installed, otherwise the sdk will be unable to process the payment. 


## Implementation

**Gradle**

In your build.gradle file of app module, add below dependency to import this library

    dependencies {
      implementation 'com.github.opera123345:intentpayx13:1.0'
    }

Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
	

**Note** : If you are having newer plugin version. Add above maven pack in your settings.gradle file:
	
	dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        ...
        maven { url 'https://jitpack.io' }
      }
	}
	
## Setting Up Activity

### For Payment :

In Android app, create a new activity to implement payment integration. Here, I have created MainActivity.java

**Initializing VPParams :**

You can see below code, these are minimum and mandatory calls to enable payment processing. If any of the below code is missed then Runtime Exception will generated.

	     VPParams builder = new VPParams();
         builder.setAmount(AMOUNT);     
         builder.setApiKey(API_KEY);      
    

**Calls and Descriptions :**

|    Method     |   Mandatory   |  Description  |
| ------------- | ------------- |  -----------  | 
| setAmount()  | YES  | It takes the amount in String decimal format (xx.xx) to be paid. For e.g. 90.88 will pay Rs. 90.88. |
| setApiKey()  | YES  | Api Key given by merchant  | 


**Proceed to payment :**

To start the payment, just call startPayment() method of VPPaymentValidation and after that transaction is initiated.

        VPPaymentValidation vpPaymentValidation = new VPPaymentValidation(builder, MainActivity.this);
        vpPaymentValidation.startPayment();


### To Validate the Payment :

In order to validate the payment, the SDK needs the invoice of the transaction. To receive the invoice image, your application should be listed as an option in share list while sharing.
To list your application as an option follow the below steps :


**Working with the AndroidManifest.xml file :**

For adding data to SDK we should give permissions for accessing the internet. For adding these permissions navigate to the app > AndroidManifest.xml and Inside that file add the below permissions to it along with the following changes. 
The complete AndroidManifest.xml file should be as follows. (Make sure all the code mentioned below is being reflected in the manifest file)


<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    package="com.demo.vppayment">

    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.VPPayment"
        tools:targetApi="31">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>

            <intent-filter>
                <action android:name="android.intent.action.SEND" />

                <category android:name="android.intent.category.DEFAULT" />

                <data android:mimeType="image/*" />
                <data android:mimeType="*/*" />
            </intent-filter>

            <meta-data
                android:name="android.app.lib_name"
                android:value="" />
        </activity>
    </application>

</manifest>


**Note** : Before Initializing the params. we need to receive the image and send to SDK. In order to implement this, follow the below steps in the activity you created. (I have done these in the MainActivity.java file i created) :

    //call this in onCreate method
    Intent receivedIntent = getIntent();
        String receivedAction = receivedIntent.getAction();
        String receivedType = receivedIntent.getType();

        if (receivedAction.equals(Intent.ACTION_SEND)) {
            Uri receiveUri = (Uri) receivedIntent.getParcelableExtra(Intent.EXTRA_STREAM);
            System.out.println(receiveUri);

            if (receivedIntent.getType() != null) {
                if (receivedType.startsWith("image/") || receivedType.startsWith("image/*") || receivedType.startsWith("*/*")) {
                    //Need to initialize the vpParams
                }
            }

        } else if (receivedAction.equals(Intent.ACTION_MAIN)) {
            Log.e("VPPayment", "onSharedIntent: nothing shared");
        }
        
        
**Initializing VPParams :**

You can see below code, these are minimum and mandatory calls to enable payment processing. If any of it is missed then Runtime Exception will generated.

	      VPParams builder = new VPParams();
                    builder.setApiKey(API_KEY);
                    builder.setShareImage(IMAGE_URI);  
    

**Calls and Descriptions :**

|    Method     |   Mandatory   |  Description  |
| ------------- | ------------- |  -----------  | 
| setAmount()  | YES  | It takes the amount in String decimal format (xx.xx) to be paid. For e.g. 90.88 will pay Rs. 90.88. |
| setShareImage()  | YES  | URI received from intent. It will accept only String format. so convert URI format to String  | 


**Proceed to payment :**

To start the payment, just call startPayment() method of VPPaymentValidation and after that image is received.

        VPPaymentValidation vpPaymentValidation = new VPPaymentValidation(builder, MainActivity.this);
        vpPaymentValidation.startPayment();
        
        
  ## Payment response handling :
  
  After redirection from app to SDK, you will get the response in onActivityResult().
  
    @Override
      protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            if (data != null) {
                if (!data.getStringExtra(VPConstant.VP_RESPONSE).isEmpty()) {
                    new MaterialAlertDialogBuilder(this, com.google.android.material.R.style.Widget_AppCompat_ActionBar_Solid).setTitle("Alert!").setMessage(data.getStringExtra("RESPONSE")).setPositiveButton("Okay", (dialogInterface, i) -> {
                    }).show();

                }
            }

        }





