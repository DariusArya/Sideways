package com.innovathon.sideways.util;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.innovathon.sideways.R;

import java.util.HashMap;

public class PhoneNumberVerificationActivity extends ActivitySendingInfo
{
    Context mContext = null;
    int state = 0;
    EditText editBox = null;
    TextView mInstr = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number_verification);

        mContext = (Context) this;
        final EditText tv = (EditText) findViewById(R.id.phonenumberedit);
        String retrievedphonenumber = this.getPhoneNumber();
        tv.setText(retrievedphonenumber);
        final TextView instr = (TextView) findViewById(R.id.instr1);
        mInstr = instr;
        editBox = tv;
        Button fab = (Button) findViewById(R.id.submit);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
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
        TelephonyManager mngr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String MyPhoneNumber = "0000000000";

        try
        {
            MyPhoneNumber = mngr.getLine1Number();
        }
        catch(NullPointerException ex)
        {
        }

        if(MyPhoneNumber.equals(""))
        {
            MyPhoneNumber = mngr.getSubscriberId();
        }
        String ret = MyPhoneNumber;
        if (User.country_code != null && ret.startsWith(User.country_code))
        {
            ret = ret.substring(User.country_code.length());
        }

        if (ret.toString().length() == 10)
             ret = String.format("%s-%s-%s", ret.substring(0, 3), ret.substring(3, 6), ret.substring(6, 10));

        return ret;
    }


    private void verifyCode()
    {
        String code = editBox.getText().toString();
        HashMap<String,String> info = new HashMap<String,String>();
        info.put("code",code);
        info.put("number",phone_number);

        String congratmessage = "Congratualtions ! we have verified your phone number to be correct.";
        String urlpost = getString(R.string.baseurl) + getString(R.string.verifycode);
        postInfo(info,urlpost, congratmessage);
    }
    public static String phone_number = "";
    private void sendPhoneForVerificationCodeTransmit()
    {
        String number = editBox.getText().toString();
        phone_number = number;
        HashMap<String,String> info = new HashMap<String,String>();
        info.put("Phone:",number);
        String congratmessage = "A verification code has been sent to you, Please enter it into the window above";
        String urlpost = getString(R.string.baseurl) + getString(R.string.sendverifcode);
        postInfo(info,urlpost, congratmessage);
        editBox.setText(" ".subSequence(0,1));

    }

    int numberofattempts = 3;
    @Override
    public void doThisForMessage(String msg)
    {
        if(state == 1)
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
                    mInstr.setText("Your entry does not match the code we sent you. \n"+
                                   "You can try " + numberofattempts + " times");
                    editBox.setText("");
                }
                else
                {
                    mInstr.setText("We have to start over.");
                    editBox.setText("");
                    try
                    {
                        Thread.sleep(3000);
                    }
                    catch(Exception e)
                    {

                    }
                    state = 0;
                    editBox.setText("");
                    mInstr.setText(R.string.instr0);
                    numberofattempts = 3;
                }

            }
        }
        else
        {
            state = 1;
        }

    }
}
