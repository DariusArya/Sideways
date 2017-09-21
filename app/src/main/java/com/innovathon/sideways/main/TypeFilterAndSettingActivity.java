package com.innovathon.sideways.main;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.innovathon.sideways.R;

import java.util.ArrayList;
import java.util.HashMap;
/**
 * Created by Payman on 11/2/2016.
 */
public class TypeFilterAndSettingActivity extends Activity
{
    private HashMap<String, ImageView> mButtonIcons;
    private HashMap<String, TextView> mButtonLabels;
    private HashMap<String, CheckBox> mCheckBoxes;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setContentView(R.layout.setting_and_typefilter_activity_layout);
        super.onCreate(savedInstanceState);
        int index = 0;
        int numColumns = 1;
        final Types.locationTypes[] columns = new Types.locationTypes[2];
        ViewGroup root = (ViewGroup) findViewById(R.id.scroll_panel_put_locationsetting);
        final LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        mButtonIcons = new HashMap<String, ImageView>();
        mButtonLabels = new HashMap<String, TextView>();
        mCheckBoxes = new HashMap<String, CheckBox>();
        for (Types.locationTypes type : Types.locationTypes.values())
        {
            if (type != Types.locationTypes.BEST_LAST_KNOWN &&
                type != Types.locationTypes.CURR_GPS_LOCATION &&
                type != Types.locationTypes.BEST_LAST_KNOWN &&
                type != Types.locationTypes.FROM_OUR_DATABASE &&
                type != Types.locationTypes.USER_ADDED_TMP &&
                type != Types.locationTypes.FROM_GOOGLE_SEARCH)
            {
                columns[index % numColumns] = type;
                if ((index + 1) % numColumns == 0)
                {
                    LinearLayout rowlayout = (LinearLayout) inflater.inflate(R.layout.row_layout3, null);
                    for (int j = 0; j < numColumns; j++)
                    {
//                        ImageView itemImage = (ImageView) (j == 0 ? rowlayout.findViewById(R.id.item1_image) : rowlayout.findViewById(R.id.item2_image));
                        TextView itemName = (TextView) (j == 0 ? rowlayout.findViewById(R.id.item1_text) : rowlayout.findViewById(R.id.item2_text));
                        CheckBox itemCheck = (CheckBox) (j == 0 ? rowlayout.findViewById(R.id.item1_checkbox) : rowlayout.findViewById(R.id.item2_checkbox));
//                        itemImage.setImageResource(columns[j].image());
                        itemName.setText(columns[j].fullName());
//                        mButtonIcons.put(columns[j].identifier(),itemImage);
                        mButtonLabels.put(columns[j].identifier(), itemName);
                        mCheckBoxes.put(columns[j].identifier(), itemCheck);
                    }

                    root.addView(rowlayout);
                }
                index++;
            }

        }

//        for (final String identifier : mButtonIcons.keySet())
//        {
//            mButtonIcons.get(identifier).setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    mCheckBoxes.get(identifier).setChecked(!mCheckBoxes.get(identifier).isChecked());
//                }
//            });
//        }

        for (final String identifier : mButtonLabels.keySet())
        {
            mButtonLabels.get(identifier).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCheckBoxes.get(identifier).setChecked(!mCheckBoxes.get(identifier).isChecked());
                }
            });
        }

        findViewById(R.id.okbut_for_setting).setOnClickListener(
                new View.OnClickListener()
        {
            ArrayList<String> chosenOnes = new ArrayList<String>();

            @Override
            public void onClick(View v)
            {
                ArrayList<String> typeidentifiers = new ArrayList<String>();

                for (String identifier : mCheckBoxes.keySet())
                {
                    if (mCheckBoxes.get(identifier).isChecked())
                    {
                        chosenOnes.add(identifier);
                    }
                }

                Intent resultIntent = new Intent();
                Bundle b = new Bundle();
                b.putStringArrayList("CHOSEN_TYPES", chosenOnes);
                resultIntent.putExtra("CHOSEN_TYPES", b);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }


        });

        findViewById(R.id.cancelbut_for_setting).setOnClickListener(
                new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }


}
