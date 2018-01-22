package com.innovathon.sideways.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.innovathon.sideways.main.MainActivity;

import org.json.simple.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by Payman on 8/7/2016.
 */
public class ActivitySendingInfo extends FragmentActivity {
    private boolean m_bPostedSuccessfully = false;
    private boolean m_bErrorOcurredInPosting = false;

    public void showProgDialogBox() {

    }

    public void hideProgDialogBox() {

    }

    public boolean postInfo(final HashMap<String, String> locInfo, final String postUrl, final String congratMessage)
    {
        final String[] submission_string_holder = new String[1];
        m_bPostedSuccessfully = false;
        Activity act = null;
        if (MainActivity.mActStack != null && !MainActivity.mActStack.isEmpty())
            act = MainActivity.mActStack.firstElement();

        DefaultAsyncProcess Poster = new DefaultAsyncProcess(act)
        {
            String message = "";

            @Override
            protected void onPreExecute() {
                showProgDialogBox();
            }

            @Override
            protected void onPostExecute(Void result) {
                hideProgDialogBox();
                Intent intent = getIntent();
                while (MainActivity.mActStack.size() > 1)
                    MainActivity.mActStack.pop().finish();

                doThisForMessage(message);

            }

            @Override
            protected void doTheThing()
            {
                try
                {
                    String urlpost = postUrl;
                    String submission_json_string = JSONObject.toJSONString(locInfo);
                    String msg = postJSONString(submission_json_string, urlpost);

//                    final PFile mylog = new PFile("mylog.txt");
//                    mylog.putText(msg);
                    message = msg;
                    if (!msg.contains(" error ") && !msg.contains("Database query failed"))
                    {
                        m_bPostedSuccessfully = true;
                        MarkerManager.getMarkerManager().removeAll();
                        if (act != null)
                            act.runOnUiThread(
                                    new Runnable() {
                                @Override
                                public void run()
                                {
                                    if (congratMessage != null && !congratMessage.isEmpty())
                                    {
                                        Toast.makeText(act, congratMessage, Toast.LENGTH_LONG).show();
//                                      Toast.makeText(act, mylog.getAbsolutePath(), Toast.LENGTH_LONG).show();
                                    }

                                    if (act instanceof MainActivity)
                                    {
                                        ((MainActivity) act).refreshNow();
                                    }
                                }
                            });


                    }
                    else
                    {
                        m_bErrorOcurredInPosting = true;
                        prompt(act, "Unfortunately there was a problem: " + msg);
                    }
                }
                catch (Exception e)
                {
                    prompt(act, "Unfortunately there was a problem: " + e.getMessage());
                }


            }
        };


        Poster.launch();

        int counter = 0;


        return true;

    }

    @SuppressLint("LongLogTag")
    protected void prompt(Context act, String msg) {
        try {
            Looper.prepare();
        } catch (Exception e) {
            Log.w("Loop prepare threw and exception", e.getMessage());
        }
        if (act != null && msg != null && !msg.isEmpty())
            Toast.makeText(act, msg, Toast.LENGTH_LONG).show();
    }

    public String postJSONString(String data, String target) throws Exception {
        URL endpoint = new URL(target);
        HttpURLConnection urlc = (HttpURLConnection) endpoint.openConnection();
        try {
            urlc.setRequestMethod("POST");
        } catch (ProtocolException e) {
            throw new Exception("Shouldn't happen: HttpURLConnection doesn't support POST??", e);
        }

//		String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.


        urlc.setDoOutput(true);
        urlc.setDoInput(true);
        urlc.setUseCaches(false);
        urlc.setAllowUserInteraction(false);
        urlc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        DataOutputStream out = new DataOutputStream(urlc.getOutputStream());
//        String charset = "UTF-8";
//        Writer writer = null;
        try {

            String content = "json=" + data;
            out.writeBytes(content);
            out.flush();
            out.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            String line = "";
            String msg = "";
            while ((line = in.readLine()) != null) {
                msg += line;
            }
            in.close();


            return msg;


        } catch (IOException e) {
            throw new Exception("IOException while posting data", e);
        }

    }

    public void doThisForMessage(String msg) {

    }


}
