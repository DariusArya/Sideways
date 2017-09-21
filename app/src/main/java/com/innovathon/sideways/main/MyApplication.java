package com.innovathon.sideways.main;

import android.app.Application;

import com.innovathon.sideways.R;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(formKey = "", // will not be used
mailTo = "paymane@gmail.com",
mode = ReportingInteractionMode.TOAST,
resToastText = R.string.crash_dialog_text)

public class MyApplication extends Application 
{
    @Override
    public void onCreate() 
    {
        super.onCreate();
        
        // The following line triggers the initialization of ACRA
        ACRA.init(this);
//        File root = Environment.getExternalStorageDirectory();
//		String dest = root.getAbsolutePath() +"/crashurl.txt";
//        PFile crashurl = new PFile(dest);
//        String urldoingthis = crashurl.getText();
//        ACRA.getErrorReporter().putCustomData("OFFENDING_URL", urldoingthis);
    }
}