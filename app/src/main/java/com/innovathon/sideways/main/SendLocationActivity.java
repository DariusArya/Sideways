package com.innovathon.sideways.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.innovathon.sideways.R;
import com.innovathon.sideways.util.ActivitySendingInfo;
import com.innovathon.sideways.util.CameraActivity;
import com.innovathon.sideways.util.User;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * Created by Ahmad on 7/16/2016.
 */
public class SendLocationActivity extends ActivitySendingInfo
{

    int location_type_global;
    private String mLontag,mLattag;
    private double mCurLon, mCurLat;
    static final int CAM_REQUEST = 1;
    static final int UCM_REQUEST = 2;
    LinearLayout mSubTypePanel = null;
    boolean m_bPostedSuccessfully, m_bErrorOcurredInPosting;
    static int subtype_choice = -1;
    Context mContext;
    Activity mThisAct;
    String mPostUrl = null;
    Boolean mPictureTakenAndReady = null;


    HashMap<String, String> mLocInfo = new HashMap<String, String>();

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private ImageView imageViewSendLocationImage;
    private TextView textViewSendLocationName;
    private String mId;

    public void onCreate(Bundle savedInstanceState)
    {
        MainActivity.mActStack.push(this);
        mContext = this;
        mThisAct = this;
        super.onCreate(savedInstanceState);
            setContentView(R.layout.send_location);
        mSubTypePanel = (LinearLayout) findViewById(R.id.sublocationtypepanel);
        imageViewSendLocationImage = (ImageView) findViewById(R.id.send_location_image);
        textViewSendLocationName   = (TextView)  findViewById(R.id.send_location_name);


        String location_type = getIntent().getStringExtra("location_type");


        mLontag = getString(R.string.curloc_lon_tag);
        mLattag = getString(R.string.curloc_lat_tag);

        Intent intent = getIntent();
        mCurLon = intent.getDoubleExtra(mLontag, -10000.0);
        mCurLat = intent.getDoubleExtra(mLattag, -10000.0);
        mPostUrl = getString(R.string.baseurl);
        mPostUrl+= getString(R.string.writescriptname);

        mLocInfo.put("`"+getString(R.string.LO)+"`",mCurLon+"");
        mLocInfo.put("`"+getString(R.string.LA)+"`",mCurLat+"");
        mLocInfo.put("`"+getString(R.string.TY)+"`","'"+location_type+"'");
        mLocInfo.put("`"+getString(R.string.VD)+"`","0");
        mLocInfo.put("`"+getString(R.string.VU)+"`","0");
        mLocInfo.put("`"+getString(R.string.SL)+"`","1");
        if (User.id_type == null || User.id_type.isEmpty() || User.id_type.equals("-"))
            User.id_type = getString(R.string.app_name).toUpperCase();
        mLocInfo.put("`"+getString(R.string.AL)+"`","'"+User.id+'@'+User.id_type + "'");

//        if (mCurLon != -10000.0 && mCurLat != -10000.0)
//            Toast.makeText(this, mCurLon + "," + mCurLat, Toast.LENGTH_SHORT).show();


        setImageIconAndButtonLabel(location_type);

        // Location Type: Select Location sub-type  (this is an imageview)
//        ImageView imageViewLocationSubType = (ImageView)findViewById(R.id.send_location_type_image);
//
//
//        imageViewLocationSubType.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //Toast.makeText(getApplicationContext(), "Location Sub-Type Clicked", Toast.LENGTH_LONG).show();
//            }
//        });


        // Camera  (this is an imageview)
        ImageView imageViewPicture = (ImageView) findViewById(R.id.send_location_picture);

        imageViewPicture.setOnClickListener(new View.OnClickListener()
        {
            //Toast.makeText(getApplicationContext(), "Camera Clicked", Toast.LENGTH_LONG).show();
            public void onClick(View view)
            {
                mPictureTakenAndReady = false;
//                Intent camera_intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                Intent camera_intent = new Intent(mThisAct, CameraActivity.class);
                File file = getFile();
                camera_intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                startActivityForResult(camera_intent, CAM_REQUEST);
            }
        });


        // Comment  (this is an imageview)
        ImageView imageViewComment = (ImageView) findViewById(R.id.send_location_comment);

        imageViewComment.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent ucomment_intent = new Intent(mThisAct, UserCommentActivity.class);
                String usercomments = mLocInfo.get("`" + getString(R.string.UC) + "`");
                if (usercomments != null && !usercomments.isEmpty())
                    ucomment_intent.putExtra(getResources().getString(R.string.users_previous_comments),usercomments);
                startActivityForResult(ucomment_intent,UCM_REQUEST);
            }
        });


        //Send  (this is a Button)
//        Button buttonSend = (Button) findViewById(R.id.send_location_sendtodatabase);
//
//        buttonSend.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View view)
//            {
////                Toast.makeText(getApplicationContext(), "Send Button Clicked", Toast.LENGTH_LONG).show();
//                if (!postInfo())
//                {
//                    Toast.makeText(getApplicationContext(), "Couldn't send the info !!! Sorry.", Toast.LENGTH_LONG).show();
//                }
//            }
//        });

        TextView buttonSendText = (TextView) findViewById(R.id.posting_button_text);
//        buttonSendText.bringToFront();
        buttonSendText.setVisibility(View.VISIBLE);
        buttonSendText.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (mPictureTakenAndReady != null && mPictureTakenAndReady)
                {
                    File file = getFile();
                    try
                    {
                        prepareImageForSubmission(file);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }

               // if (mLocInfo.get(getString(R.string.UC)) == null)
//                    mLocInfo.put("`" + getString(R.string.UC) + "`", "''");

                //if (mLocInfo.get(getString(R.string.IMG_NAME)) == null)
//                    mLocInfo.put("`"+getString(R.string.IMG_NAME)+"`", "");

//                Toast.makeText(getApplicationContext(), "Send Button Clicked", Toast.LENGTH_LONG).show();
                if (!postInfo(mLocInfo, mPostUrl, getString(R.string.congrat_for_posting)))
                {
                    Toast.makeText(getApplicationContext(), "Couldn't send the info !!! Sorry.", Toast.LENGTH_LONG).show();
                }

            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        mId = intent.getStringExtra(getResources().getString(R.string.user_id_label));
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        File file = getFile();
        if (file.exists())
            file.delete();
    }

    private void setImageIconAndButtonLabel(String location_type)
    {
        //Types.locationTypes type = Types.locationTypes.values()[location_type];
        Types.locationTypes type = Types.locationTypes.get(location_type);
        imageViewSendLocationImage.setImageResource(type.image());
        textViewSendLocationName.setText(type.fullName());
        createTheSubtypePanel(type);
    }


    public void hideProgDialogBox()
    {
        ProgressBar pbar = (ProgressBar) findViewById(R.id.posting_progressbar);
        TextView buttonLabel = (TextView) findViewById(R.id.posting_button_text);
//        buttonLabel.setVisibility(View.VISIBLE);
        pbar.setVisibility(View.INVISIBLE);
        buttonLabel.setText(getString(R.string.send));
    }

    public void showProgDialogBox()
    {
        ProgressBar pbar = (ProgressBar) findViewById(R.id.posting_progressbar);
        pbar.setIndeterminate(true);
        TextView buttonLabel = (TextView) findViewById(R.id.posting_button_text);
        buttonLabel.setText("");
//        buttonLabel.setVisibility(View.INVISIBLE);
        pbar.bringToFront();
        pbar.setVisibility(View.VISIBLE);
    }



    private void createTheSubtypePanel(Types.locationTypes location_type)
    {
        //TreeSet<String> names = new TreeSet<String>();
        //TreeSet<Integer> images = new TreeSet<Integer>();

        LinkedHashSet names = new LinkedHashSet();
        LinkedHashSet images = new LinkedHashSet();
        LinkedHashSet locSubTypeIdentifier = new LinkedHashSet();

        createNamesImagesIndices(location_type, names, images, locSubTypeIdentifier);

        createTheSubtypePanel(names, images, locSubTypeIdentifier);
    }

    private void createNamesImagesIndices(Types.locationTypes location_type, LinkedHashSet names, LinkedHashSet images, LinkedHashSet locSubTypeIdentifier)
    {
        int index = 0;
        for (Types.subTypes subtype : Types.subTypes.values())
        {
            if (index >= location_type.iSubtypeBeg && index <= location_type.iSubtypeEnd)
            {
                String subtypename = subtype.fullName();
                Integer subtypeimage = subtype.image();
                images.add(subtypeimage);
                names.add(subtypename);
                locSubTypeIdentifier.add(subtype.identifier());
            }
            index++;
        }

    }

    ArrayList<View> panel_items = new ArrayList<View>();
    private void createTheSubtypePanel(Collection<String> names, Collection<Integer> images, Collection<String> locSubTypeIdentifier)
    {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        panel_items.clear();

        Iterator<Integer> imageIterator = images.iterator();
        Iterator<String> nameIterator = names.iterator();
        Iterator<String> identifierIterator = locSubTypeIdentifier.iterator();
        final int[] index = new int[]{0};
        while (nameIterator.hasNext())
        {
            final String subtypename;
            final Integer subtypeimage;
            final String subtypeIdentifier;
            subtypename = nameIterator.next();
            subtypeimage = imageIterator.next();
            subtypeIdentifier = identifierIterator.next();

            View v = inflater.inflate(R.layout.sublocationtype_item_layout, null);

            LinearLayout panel = (LinearLayout) v.findViewById(R.id.item_layout);
            TextView label = (TextView) v.findViewById(R.id.sublocation_type_name);
            label.setText(subtypename);
            label.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    selectSubtype(subtypename,subtypeIdentifier);
                }
            });

            ImageView icon = (ImageView) v.findViewById(R.id.sublocation_image);
            //icon.setImageResource(R.drawable.curloc_user);
            //icon.setImageResource(R.drawable.ic_2_10_none);
            icon.setImageResource(subtypeimage);
            icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    selectSubtype(subtypename, subtypeIdentifier);
                }
            });
            ViewGroup par = (ViewGroup) panel.getParent();
            panel.setContentDescription(subtypeIdentifier);
            panel.setTag("subtype_item");
            if (par != null)
                par.removeView(panel);
            mSubTypePanel.addView(panel);
            panel_items.add(panel);
            index[0]++;
        }
    }

    private void selectSubtype(String subtypename, String subtypeIdentifier)
    {
        ArrayList<View> outViews = new ArrayList<View>();
        mSubTypePanel.findViewsWithText(outViews,subtypeIdentifier, View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION);

        for(View v: panel_items)
        {
            v.setBackgroundResource(R.drawable.backbut);
            v.invalidate();
        }

        if (!outViews.isEmpty())
        {
            View v = outViews.get(0);
            v.setBackgroundResource(R.drawable.backbuthighlighted);
            v.invalidate();
        }

        mLocInfo.put("`"+getString(R.string.ST)+"`","'"+subtypeIdentifier+"'");
    }


    private File getFile()
    {
        File folder = new File("sdcard/camera_app");

        if (!folder.exists())
        {
            folder.mkdir();
        }

        File image_file = new File(folder, getString(R.string.filenameforimages));

        return image_file;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch(requestCode)
        {
            case (CAM_REQUEST):
                try
                {
                    handleImageSubmissionRequest();
                }
                catch (IOException e)
                {
                    Toast.makeText(this, "Couldn't read the image file.", Toast.LENGTH_LONG).show();

                }
                break;
            case (UCM_REQUEST):
                //TODO to get the comments from data.
                handleUserCommentSubmissionRequest(data);
                break;
        }

    }

    private void handleUserCommentSubmissionRequest(Intent intent)
    {
        TextView textViewCameraText = (TextView) findViewById(R.id.send_location_comment_text);

        if (intent != null)
        {
            String usercomment = intent.getStringExtra(getString(R.string.user_comment_tag));
            if (usercomment != null && !usercomment.isEmpty())
            {
                usercomment = processSpecialChar(usercomment);
                textViewCameraText.setTextColor(Color.parseColor("#4876FF"));
                textViewCameraText.setText("Comment Ready!");
            }
            else
                usercomment = "";

            mLocInfo.put("`" + getString(R.string.UC) + "`", "'" + usercomment + "'");
        }
        else
            mLocInfo.put("`" + getString(R.string.UC) + "`", "''");


    }

    private String processSpecialChar(String usercomment)
    {
        String ret = "";
        for(int i = 0; i < usercomment.length(); i++)
        {
            char c = usercomment.charAt(i);
            switch(c)
            {
                case '\'': ret += "'" + "'" ;
                           break;
                case ',' : ret += "\\" + ",";
                           break;
                default : ret += c;
            }
        }

        return ret;
    }

    private void handleImageSubmissionRequest() throws IOException
    {
        TextView textViewCameraText = (TextView) findViewById(R.id.send_location_picture_text);
        if (textViewCameraText != null)
        {
            textViewCameraText.setTextColor(Color.parseColor("#4876FF"));
            File file = getFile();

            if (file.exists())
            {
                mPictureTakenAndReady = true;
                prepareImageForSubmission(file);
                textViewCameraText.setText("Image Ready!");
            }
        }
    }

    private void prepareImageForSubmission(File file) throws IOException
    {
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        bitmap = Bitmap.createScaledBitmap(bitmap, w/2, h/2, false);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 15, stream);

        bitmap.recycle();

        byte[] b = stream.toByteArray();
        String imageinbase64string = Base64.encodeToString(b,0);
        mLocInfo.put("`"+getString(R.string.IMG_NAME)+"`", imageinbase64string);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        client.connect();
    }

//    @Override
//    public void onStart()
//    {
//        super.onStart();
//
//        // ATTENTION: This was auto-generated to implement the App Indexing API.
//        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        client.connect();
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "SendLocation Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app URL is correct.
//                Uri.parse("android-app://com.innovathon.sideways.main/http/host/path")
//        );
//        AppIndex.AppIndexApi.start(client, viewAction);
//    }

    @Override
    public void onStop()
    {
        super.onStop();
//
//        // ATTENTION: This was auto-generated to implement the App Indexing API.
//        // See https://g.co/AppIndexing/AndroidStudio for more information.
//        Action viewAction = Action.newAction(
//                Action.TYPE_VIEW, // TODO: choose an action type.
//                "SendLocation Page", // TODO: Define a title for the content shown.
//                // TODO: If you have web page content that matches this app activity's content,
//                // make sure this auto-generated web page URL is correct.
//                // Otherwise, set the URL to null.
//                Uri.parse("http://host/path"),
//                // TODO: Make sure this auto-generated app URL is correct.
//                Uri.parse("android-app://innovate.ae.arq.pathz/http/host/path")
//        );
//        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}