package com.innovathon.sideways.main;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.innovathon.sideways.R;
import com.innovathon.sideways.util.ActivitySendingInfo;
import com.innovathon.sideways.util.DefaultAsyncProcess;
import com.innovathon.sideways.util.User;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.HashMap;
import java.util.Stack;

/**
 * Created by Payman on 10/4/2016.
 */
public class EditUserInfoActivity extends ActivitySendingInfo
{
    public static Stack<Activity> mActStack = new Stack<Activity>();
    public static String phonenumberprefix ;
    Activity thisact;
    Context mContext;
    protected void onCreate(Bundle savedInstanceState)
    {
        mActStack.push(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_user_info);

        mContext = this;
        // Put Location button (this is an imageview)

        phonenumberprefix = getResources().getString(R.string.phonenumber);


        //map functions,
//
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        SupportMapFragment mapFragment = (R.layoutSupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        thisact = this;



        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.


        DefaultAsyncProcess acquire_data = new DefaultAsyncProcess(this)
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                getUserDataLocally();
                getUserDataFromOurDatabase();
                updateUI();
                return null;
            }


        };

        acquire_data.launch();
    }

    private void updateUI()
    {
        runOnUiThread(
                new Runnable()
       {
           @Override
           public void run()
           {

               String phonenumber = User.phonenumber;

               if (phonenumber.startsWith(phonenumberprefix))
               {
                   phonenumber = phonenumber.substring(phonenumberprefix.length());
               }
               setText(R.id.yob, User.yob);
               setText(R.id.gender, User.gender);
               setText(R.id.email, User.email);
               setText(R.id.phone, phonenumber);
               setText(R.id.points, User.points);
               setText(R.id.reports, User.reports);
           }
       });

    }

    private void setText(int id, String text)
    {
        View view = findViewById(id);
        if (view instanceof EditText)
        {
            ((EditText) view).setText(text);
        }
        else
        if (view instanceof TextView)
        {
            ((TextView) view).setText(text);
        }
    }

    @Override
    public boolean postInfo(HashMap<String, String> locInfo, String postUrl, String congratMessage)
    {
        return super.postInfo(locInfo, postUrl, congratMessage);
    }

    private void getUserDataFromOurDatabase()
    {

        String point = getResources().getString(R.string.dbserverpath) + "getUserStatistics.php";
        HashMap<String, String> param = new HashMap<String, String>();
        param.put("`"+getString(R.string.AL)+"`","'"+User.id+'@'+User.id_type + "'");

        String submission_json_string = JSONObject.toJSONString(param);
        try
        {
            String msg = postJSONString(submission_json_string, point);
            JSONParser parser = new JSONParser();
            Object object = parser.parse(msg);
            if (object == null)
                return ;
            JSONArray jArray = (JSONArray) object;
            int num = jArray.size();
            if (num == 1)
            {

                JSONObject jsonUserSpec = (JSONObject) jArray.get(0);
               User.points = (String)jsonUserSpec.get("points");
               User.reports = (String) jsonUserSpec.get("reports");

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }


    }


    private void getUserDataLocally()
    {

    }
}
