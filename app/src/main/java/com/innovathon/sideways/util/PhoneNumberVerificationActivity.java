package com.innovathon.sideways.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.innovathon.sideways.R;

import java.util.HashMap;

public class PhoneNumberVerificationActivity extends ActivitySendingInfo {
    private static final int MY_PERMISSIONS_REQUEST_READ_SMS = 12345;
    public static String phone_number = "";
    private static Boolean bPermissionGranted = null;
    Context mContext = null;
    int state = 0;
    EditText editBox = null;
    TextView mInstr = null;
    int numberofattempts = 3;
    private Activity mAct;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number_verification);
        mAct = this;
        mContext = this;
        final EditText tv = (EditText) findViewById(R.id.phonenumberedit);
        String retrievedphonenumber = this.getPhoneNumber();
        if (retrievedphonenumber.startsWith("+"))
            retrievedphonenumber = retrievedphonenumber.substring(1);
        Resources res = getResources();
        String[] country_codes = res.getStringArray(R.array.CountryCodes);
        for (String code : country_codes) {
            String ccode = code.split(",")[0];
            if (retrievedphonenumber.startsWith(ccode)) {
                if (retrievedphonenumber.charAt(ccode.length()) != '-') {
                    retrievedphonenumber = "+" + ccode + "-" + retrievedphonenumber.substring(ccode.length());
                    break;
                }
            }

        }
        tv.setText(retrievedphonenumber);
        final TextView instr = (TextView) findViewById(R.id.instr1);
        mInstr = instr;
        editBox = tv;
        Button fab = (Button) findViewById(R.id.submit);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tv.getText().toString().isEmpty())
                {
                    if (state == 0)
                        Toast.makeText(mContext, "You must enter a valid phone number", Toast.LENGTH_LONG).show();

                    if (state == 1)
                        Toast.makeText(mContext, "You must enter the verification code we sent you in SMS", Toast.LENGTH_LONG).show();
                }
                else
                {
                    if (state == 0)
                    {
                        instr.setText(R.string.instr_for_entering_verification_code);
                        Toast.makeText(mContext, "A verification code will be sent to that phone number via SMS.", Toast.LENGTH_LONG).show();
                        sendPhoneForVerificationCodeTransmit();

                        return;
                    }

                    if (state == 1)
                    {
                        verifyCode();
                    }
                }
            }
        });
    }

    public String getPhoneNumber()
    {
        TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String MyPhoneNumber = "0000000000";
        String ret = "";
        //**************************


        boolean permissionGranted = getPermission();

        if (permissionGranted)
        {
            try
            {
                MyPhoneNumber = mngr.getLine1Number();
            }
            catch (NullPointerException ex)
            {
            }

//            if (MyPhoneNumber.equals(""))
//            {
//                MyPhoneNumber = mngr.getSubscriberId();
//            }

            ret = MyPhoneNumber;
            if (ret == null)
                return "";
            if (User.country_code != null && ret.startsWith(User.country_code))
            {
                ret = ret.substring(User.country_code.length());
            }

            if (ret.toString().length() == 10)
                ret = String.format("%s-%s-%s", ret.substring(0, 3), ret.substring(3, 6), ret.substring(6, 10));
            if (ret.toString().length() == 11)
                ret = String.format("+%s %s-%s-%s", ret.substring(0, 1), ret.substring(1, 4), ret.substring(4, 7), ret.substring(7, 11));
        }
        else
            ret = "";

        return ret;
    }

    private boolean getPermission() {
        // Here, thisActivity is the current activity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(mAct, Manifest.permission.READ_SMS)) {

                    // Show an explanation to the user *asynchronously* -- don't block
                    // this thread waiting for the user's response! After the user
                    // sees the explanation, try again to request the permission.

                } else {
                    // No explanation needed, we can request the permission.
                    Thread askpermissionthread = new Thread() {
                        @Override
                        public void run() {
                            mAct.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ActivityCompat.requestPermissions(mAct, new String[]{Manifest.permission.READ_SMS}, MY_PERMISSIONS_REQUEST_READ_SMS);
                                }
                            });
                        }
                    };

                    askpermissionthread.run();

                    while (bPermissionGranted == null) {

                    }

                    return bPermissionGranted;

                }


            }

        }

        return true;
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_SMS: {
                // If request is cancelled, the result arrays are empty.
                // permission was granted, yay! do the thing,
// permission denied, boo! Disable the
// functionality that depends on this permission.
                bPermissionGranted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void verifyCode()
    {
        String code = editBox.getText().toString().trim();
        HashMap<String, String> info = new HashMap<String, String>();
        info.put("code", code);
        info.put("number", phone_number);

        String congratmessage = "Congratualtions ! we have verified your phone number to be correct.";
        String urlpost = getString(R.string.baseurl) + getString(R.string.verifycode);
        postInfo(info, urlpost, congratmessage);
    }

    private void sendPhoneForVerificationCodeTransmit()
    {
        String number = editBox.getText().toString().trim();
        phone_number = number;
        //if a number is equal to 999-315-1492, we don't ask for a sms verification
        //and bypass the sms verification part.
        if (number.equals(getString(R.string.passbynumber)))
        {
            Intent goforemailandpassword = new Intent(PhoneNumberVerificationActivity.this, RegisterPanel.class);
            goforemailandpassword.putExtra(getString(R.string.phonenumber), phone_number);
            User.phonenumber = phone_number;
            startActivity(goforemailandpassword);
            finish();
        }

        HashMap<String, String> info = new HashMap<String, String>();
        number = getPhoneNumber();
        if (number == null)
            number = phone_number;


        number = number.replaceAll("[^0-9]", "");
        number = number.trim();
        number = "+" + number;
        phone_number = number;

        info.put("Phone:", number);
        String congratmessage = "A verification code has been sent to you, Please enter it into the window above";
        String urlpost = getString(R.string.baseurl) + getString(R.string.sendverifcode);
        postInfo(info, urlpost, congratmessage);
        editBox.setText("");

    }

    @Override
    public void doThisForMessage(String msg)
    {
        if (state == 1)
        {
            if (msg.endsWith("true"))
            {
                Intent goforemailandpassword = new Intent(PhoneNumberVerificationActivity.this, RegisterPanel.class);
                goforemailandpassword.putExtra(getString(R.string.phonenumber), phone_number);
                User.phonenumber = phone_number;
                startActivity(goforemailandpassword);
            }
            else
            {
                if (--numberofattempts > 0)
                {
                    mInstr.setText("Your entry does not match the code we sent you. \n" +
                            "You can try " + numberofattempts + " times");
                    editBox.setText("");
                }
                else
                {
                    mInstr.setText("We have to start over.");
                    editBox.setText("");
                    try {
                        Thread.sleep(3000);
                    } catch (Exception e) {

                    }
                    state = 0;
                    editBox.setText("");
                    mInstr.setText(R.string.instr0);
                    numberofattempts = 3;
                }

            }
        } else {
            state = 1;
        }

    }

    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(" Do you want to cancel? ");
        builder.setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mAct.finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).show();
    }
}
