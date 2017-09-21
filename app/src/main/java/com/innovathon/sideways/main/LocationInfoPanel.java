package com.innovathon.sideways.main;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.innovathon.sideways.R;
import com.innovathon.sideways.util.ActivitySendingInfo;
import com.innovathon.sideways.util.DefaultAsyncProcess;
import com.innovathon.sideways.util.FileDnUp;
import com.innovathon.sideways.util.User;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.HashMap;

public class LocationInfoPanel extends ActivitySendingInfo
{

    Context mContext ;
    ImageView mSubtypeview = null;
    TextView mSubtypetext = null;
    String mLocationID = null;
    String mUpdateUrl = "updateLocs.php";
    private int iconid;
    private String subtypefullname;
    private String mUserComment;
    private String mBaseUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        MainActivity.mActStack.push(this);
        mContext = this;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receive_location);
        String locExtraInfo = getIntent().getStringExtra("EXTRA_INFO");
        JSONParser parser = new JSONParser();
        mBaseUrl = getString(R.string.baseurl);
        mUpdateUrl+= mBaseUrl + getString(R.string.updatescriptname);

        try
        {
            JSONObject object = (JSONObject) parser.parse(locExtraInfo);
            mLocationID = (String) object.get(getString(R.string.ID));
            String pictureurl = (String) object.get(getString(R.string.PP));

            String comment = (String) object.get(getString(R.string.UC));
            if (comment != null && !comment.isEmpty())
                ((TextView) findViewById(R.id.send_location_type_name)).setText(comment);
            String typestr = (String) object.get(getString(R.string.TY));
            //Integer typeindex = Types.locationTypes.valueOf(typestr).ordinal();
            String subtypestr = (String) object.get(getString(R.string.ST));
            //Integer subtypeindex = Integer.parseInt(subtypestr);
            //Types.locationTypes type = Types.locationTypes.values()[typeindex];
            Types.locationTypes type = Types.locationTypes.get(typestr);

            String voteup = (String) object.get(getString(R.string.VU));
            if (voteup != null)
            {
                TextView tv = (TextView) findViewById(R.id.receive_location_thumpup_text);
                tv.setText(voteup);
            }

            String votedn = (String) object.get(getString(R.string.VD));
            if (votedn != null)
            {
                TextView tv = (TextView) findViewById(R.id.receive_location_notthere_text);
                tv.setText(votedn);
            }

            //getIconIdAndSubtypeFullName(subtypestr, type);
            getIconIdAndSubtypeFullName(subtypestr);
            mSubtypeview = (ImageView) findViewById(R.id.location_subtype_image);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                mSubtypeview.setImageDrawable(getResources().getDrawable(iconid,getTheme()));
            }
            else
                mSubtypeview.setImageDrawable(getResources().getDrawable(iconid));

            mSubtypetext = (TextView) findViewById(R.id.location_subtype_name);
            mSubtypetext.setText(subtypefullname);

            if (pictureurl != null && !pictureurl.isEmpty())
            {
                pictureurl = mBaseUrl + "/" + pictureurl;
                final String finalPictureurl = pictureurl;
                DefaultAsyncProcess downloadpic = new DefaultAsyncProcess(this)
                {
                    @Override
                    protected void doTheThing()
                    {
                        final String dest = act.getExternalFilesDir(null) + "/temp.jmp";

                        try
                        {
//                            location_image_placeholder_msg

                            act.runOnUiThread(new Runnable()
                            {

                                @Override
                                public void run()
                                {
                                    TextView msg = (TextView) findViewById(R.id.location_image_placeholder_msg);
                                    msg.setText(getString(R.string.downloading_msg));
                                    msg.invalidate();
                                    msg.requestLayout();
                                }
                            });


                            FileDnUp.downloadFile(finalPictureurl, dest);

                            act.runOnUiThread(new Runnable()
                            {

                                @Override
                                public void run()
                                {
                                    TextView msg = (TextView) findViewById(R.id.location_image_placeholder_msg);
                                    msg.setVisibility(View.INVISIBLE);
                                    msg.invalidate();
                                    msg.requestLayout();
                                    Bitmap bmp = BitmapFactory.decodeFile(dest);
                                    ImageView img = (ImageView) findViewById(R.id.send_location_type_image);
                                    img.setImageBitmap(bmp);
                                    img.invalidate();
                                    img.requestLayout();
                                }
                            });

                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                };

                downloadpic.launch();
            }

        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }


        ((ImageView)findViewById(R.id.receive_location_notthere_image)).setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                       handleNotThereButtonClick();
                    }
                }
        );

        ((ImageView)findViewById(R.id.receive_location_thumpup_image)).setOnClickListener(
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        handleVoteupButtonClick();
                    }
                }
        );

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }

    //private void getIconIdAndSubtypeFullName(String subtypeindex, Types.locationTypes type)
    private void getIconIdAndSubtypeFullName(String subtypeindex)
    {
        iconid = Types.subTypes.get(subtypeindex).image();
        subtypefullname = Types.subTypes.get(subtypeindex).fullName();
    }

    HashMap<String,String> command = new HashMap<String,String>();
    private void handleVoteupButtonClick()
    {
        command.put("`"+getString(R.string.ID)+"`",mLocationID);
        command.put("`"+getString(R.string.VU)+"`","1");
        command.put("`"+getString(R.string.VD)+"`","0");
        command.put("`"+getString(R.string.AL)+"`", "'"+User.id+'@'+User.id_type + "'");
        postInfo(command,mUpdateUrl,getString(R.string.thankyouforinput));

    }

    private void handleNotThereButtonClick()
    {
        command.put("`"+getString(R.string.ID)+"`",mLocationID);
        command.put("`"+getString(R.string.VU)+"`","0");
        command.put("`"+getString(R.string.VD)+"`","1");
        postInfo(command,mUpdateUrl,getString(R.string.thankyouforinput));
    }

}
