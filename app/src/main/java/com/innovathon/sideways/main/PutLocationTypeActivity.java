package com.innovathon.sideways.main;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.innovathon.sideways.R;
import com.innovathon.sideways.util.DateTimePicker;

import java.util.HashMap;
import java.util.Vector;


/**
 * Created by Ahmad on 7/16/2016.
 */
public class PutLocationTypeActivity extends Activity
{

    private double mCurLon, mCurLat;
    private int mUserChoice;
    private String mLontag,mLattag;
    private HashMap<String, ImageView> mButtonIcons;
    private HashMap<String, TextView>  mButtonLabels;
    private Vector<String> specialTypes = new Vector<String>();
    private String mId;

    public void onCreate(Bundle savedInstanceState)
    {
        MainActivity.mActStack.push(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.put_location_type_dynamic);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        int index = 0;
        int numColumns = 2;
        final Types.locationTypes[] columns = new Types.locationTypes[2];
        ViewGroup root = (ViewGroup) findViewById(R.id.scroll_panel_put_location);
        final LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        mButtonIcons =  new HashMap<String, ImageView>();
        mButtonLabels = new HashMap<String, TextView>();
        for(Types.locationTypes type: Types.locationTypes.values())
        {
            if (type.name().startsWith("special"))
            {
                specialTypes.add(type.identifier());
            }
            if (type != Types.locationTypes.BEST_LAST_KNOWN   &&
                type != Types.locationTypes.CURR_GPS_LOCATION &&
                type != Types.locationTypes.BEST_LAST_KNOWN   &&
                type != Types.locationTypes.FROM_OUR_DATABASE &&
                type != Types.locationTypes.USER_ADDED_TMP    )
            {
                columns[index % numColumns] = type;
                if ((index + 1) % numColumns == 0)
                {
                    LinearLayout rowlayout = (LinearLayout) inflater.inflate(R.layout.row_layout, null);
                    for(int j = 0; j < numColumns ; j++)
                    {
                        ImageView itemImage = (ImageView) (j == 0 ? rowlayout.findViewById(R.id.item1_image) : rowlayout.findViewById(R.id.item2_image));
                        TextView itemName   = (TextView)  (j == 0 ? rowlayout.findViewById(R.id.item1_text)  : rowlayout.findViewById(R.id.item2_text));
                        itemImage.setImageResource(columns[j].image());
                        itemName.setText(columns[j].fullName());
                        mButtonIcons.put(columns[j].identifier(),itemImage);
                        mButtonLabels.put(columns[j].identifier(), itemName);
                    }

                    root.addView(rowlayout);
                }
                index++;
            }

        }

        for(final String identifier: mButtonIcons.keySet())
        {
            mButtonIcons.get(identifier).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if (specialTypes.contains(identifier))
                        launchSpecialTypeActivity(identifier, view);
                    else
                        launchSendLocationActivity(identifier, view);
                }
             });
        }

        for(final String identifier: mButtonLabels.keySet())
        {
            mButtonLabels.get(identifier).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    if (specialTypes.contains(identifier))
                        launchSpecialTypeActivity(identifier, view);
                    else
                        launchSendLocationActivity(identifier, view);
                }
            });
        }

        mLontag = getString(R.string.curloc_lon_tag);
        mLattag = getString(R.string.curloc_lat_tag);

        Intent intent = getIntent();
        mCurLon = intent.getDoubleExtra(mLontag,-10000.0);
        mCurLat = intent.getDoubleExtra(mLattag,-10000.0);
        mId = intent.getStringExtra(getResources().getString(R.string.user_id_label));



    }

    private void launchSpecialTypeActivity(String identifier, View view)
    {
        Intent intent = new Intent(view.getContext(), DateTimePicker.class);
        intent.putExtra("location_type", mUserChoice);
        intent.putExtra(mLontag, mCurLon);
        intent.putExtra(mLattag, mCurLat);
        intent.putExtra(getResources().getString(R.string.user_id_label),mId);
        startActivity(intent);
    }

    private void launchSendLocationActivity(String mUserChoice, View view)
    {
        Intent intent = new Intent(view.getContext(), SendLocationActivity.class);
        intent.putExtra("location_type", mUserChoice);
        intent.putExtra(mLontag, mCurLon);
        intent.putExtra(mLattag, mCurLat);
        intent.putExtra(getResources().getString(R.string.user_id_label),mId);
        startActivity(intent);
    }

    public void onBackPressed()
    {
        MainActivity.UserAbortedLocationInput();
        super.onBackPressed();
    }
}
