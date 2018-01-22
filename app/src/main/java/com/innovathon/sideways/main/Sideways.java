package com.innovathon.sideways.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.innovathon.sideways.R;
import com.innovathon.sideways.util.PhoneNumberVerificationActivity;
import com.innovathon.sideways.util.UserManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class Sideways extends Activity
{
    Context   mThisContext;
    Activity  mThisAct;
    UserManager mUserManager;

    private final static int requestCode2 = 1000;
    private boolean mbAlreadyRegistered;
    private  Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setContentView(R.layout.activity_sideways_launcher);
        mThisAct = this;
        mThisContext = this;
        Types.con = mThisContext;
        super.onCreate(savedInstanceState);
//        clearAllPreferences();
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mUserManager = UserManager.getAUserManager(this);
        if (savedInstanceState != null)
        {
            Boolean bAlreadyCreated = savedInstanceState.getBoolean("CREATED");
            if (bAlreadyCreated != null && bAlreadyCreated)
                timer.schedule(ShowLogoForAWhileThenCheckForRegistration, 5000);
        }
       else
            timer.schedule(ShowLogoForAWhileThenCheckForRegistration, 5000);

    }

    private void showLoginServiceChoicesAndHandleUserResponse()
    {
       startActivity(new Intent(this, PhoneNumberVerificationActivity.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        launchSideways(true);
    }

    class MyTimerTask extends TimerTask
    {

        @Override
        public void run()
        {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd:MMMM:yyyy HH:mm:ss a");
            final String strDate = simpleDateFormat.format(calendar.getTime());

            runOnUiThread(new Runnable()
            {

                @Override
                public void run()
                {
                    doThis();
                }

            });
        }

        public void doThis()
        {
            //Override this to get your thing done.
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        outState.putBoolean("CREATED", true);
        super.onSaveInstanceState(outState);
    }


    private void launchSideways(Boolean loggedin)
    {
        Intent intent = new Intent(mThisAct, MainActivity.class);
        if (loggedin == null)
            loggedin = false;
        intent.putExtra(getString(R.string.isloggedintag),loggedin);
        intent.putExtra("ALREADY_REGISTERED",mbAlreadyRegistered);

        startActivity(intent);
        finish();
    }

    MyTimerTask ShowLogoForAWhileThenCheckForRegistration = new MyTimerTask()
    {
        @Override
        public void doThis()
        {
            int userStatus = mUserManager.status();
            if (userStatus == UserManager.NONE)
            {
                showLoginServiceChoicesAndHandleUserResponse();
            }
            else
            {
                mUserManager.logUserIn();
                launchSideways(true);
            }
        }
    };
}

