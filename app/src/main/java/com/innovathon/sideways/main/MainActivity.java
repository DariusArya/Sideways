package com.innovathon.sideways.main;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.innovathon.sideways.R;
import com.innovathon.sideways.util.ActivitySendingInfo;
import com.innovathon.sideways.util.DefaultAsyncProcess;
import com.innovathon.sideways.util.GoGetter;
import com.innovathon.sideways.util.MarkerManager;
import com.innovathon.sideways.util.TouchableWrapper;
import com.innovathon.sideways.util.User;
import com.innovathon.sideways.util.UserManager;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import static com.innovathon.sideways.util.MarkerManager.CRITERIATYPE.PLACETYPE;

/******************************************************
 * Main Activity for Sideways, v 1.0
 *
 * *
 ******************************************************/


public class MainActivity extends ActivitySendingInfo implements TouchableWrapper.UpdateMapAfterUserInterection,
        TouchableWrapper.OnMapClicked,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        LocationListener

{
    private long TIME_TO_HIDE ;
    private GoGetter gogetter;
    private static boolean m_bMovingMode = false;
    private static final int TYPE_FILTER_AND_SETTING_INITIATED = 5001;
    public static Stack<Activity> mActStack = new Stack<Activity>();
    private int note_time_in_ms;// = getResources().getInteger(R.integer.note_time_in_ms);
    public String dbserver;// = getResources().getString(R.string.dbserverpath);
    long timetofireprogressbar;// = getResources().getInteger(R.integer.time_to_fire_progressbar);
    long gpstimeout;// = getResources().getInteger(R.integer.gps_time_out);
    private double mDefaultRadiusInMeters;// = getResources().getInteger(R.integer.default_radius_meter);
    private double mRadiusMeters;// = mDefaultRadiusInMeters;
    private String urlreaddb;// = dbserver + "getLocs.php";
    private int defaultzoom_level;// = getResources().getInteger(R.integer.default_zoom_level);
    private Long mTimeInterval;// = (long) getResources().getInteger(R.integer.time_interval);
    private int mCurZoomLevel;// = defaultzoom_level;

    private Timer timer = null, mTimer, timeouttimer = null;

    private Timer mTimeOutTimer;
    private GoogleMap mMap;
    private MarkerManager mMarkerManager;
    private GoogleApiClient mGoogleApiClient;
    private LatLng bestLastKnown = null;
    private LatLng mCurLoc = null;

    Activity thisact, act;
    private LocationRequest mLocationRequest;
    private boolean bLatestLocationObtained;
    private boolean curLocationUpdated;
    private Context mContext;
//    private String mId;
    private boolean loggedIn = false;
    private String app_name;
    private String mPrefName;
    private SharedPreferences.Editor editor;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client2;
    private Animation slide_down, slide_up;
    private View mToolbar = null;

    private void init()
    {
        Resources res = getResources();
        TIME_TO_HIDE            = res.getInteger(R.integer.time_to_hide);
        note_time_in_ms         = res.getInteger(R.integer.note_time_in_ms);
        dbserver                = res.getString(R.string.dbserverpath);
        timetofireprogressbar   = res.getInteger(R.integer.time_to_fire_progressbar);
        gpstimeout              = res.getInteger(R.integer.gps_time_out);
        mDefaultRadiusInMeters  = res.getInteger(R.integer.default_radius_meter);
        mRadiusMeters           = mDefaultRadiusInMeters;
        urlreaddb               = dbserver + "getLocs.php";
        defaultzoom_level       = res.getInteger(R.integer.default_zoom_level);
        mTimeInterval           = (long) res.getInteger(R.integer.time_interval);
        mCurZoomLevel           = defaultzoom_level;
    }

    public Double[] getViewingArea()
    {
        final Double[] ret = new Double[4];
        final Boolean[] done = new Boolean[1];
        done[0] = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LatLng tl = mMap.getProjection().fromScreenLocation(new Point(0, 0));
                LatLng br = mMap.getProjection().fromScreenLocation(new Point(screenWidth, screenHeight));

                ret[0] = tl.latitude;
                ret[1] = tl.longitude;
                ret[2] = br.latitude;
                ret[3] = br.longitude;

                done[0] = true;
            }

        });

        long ts = System.currentTimeMillis();
        while (!done[0] && System.currentTimeMillis() - ts < 3000) ;

        return ret;

    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction()
    {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client2.connect();
        AppIndex.AppIndexApi.start(client2, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client2, getIndexApiAction());
        client2.disconnect();
    }

    public boolean isInMovingMode()
    {
        return m_bMovingMode;
    }

    public static void UserAbortedLocationInput()
    {
        mStatus = AppStatus.None;
    }


    private enum AppStatus {
        None,
        LocationEntryRequested
    }


    static protected AppStatus mStatus;

    PriorityQueue<String> noteque = new PriorityQueue<String>();

    public void addNotification(String s)
    {
        Log.i("INFO", "adding to notification queue");
        noteque.add(s);
    }
    public GoogleMap getMap()
    {
        return mMap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i("Sideways","MainPanel created");
        init();
        MapsInitializer.initialize(getApplicationContext());
        mMarkerManager = MarkerManager.getMarkerManager();
        mActStack.push(this);
        urlreaddb = getString(R.string.baseurl);
        urlreaddb += getString(R.string.readscriptname);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        // Put Location button (this is an imageview)
        app_name = getString(R.string.app_name);
        mPrefName = app_name;
        setCallBacks();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.

        Intent intent = getIntent();
        if (intent.hasExtra(getString(R.string.isloggedintag)) && intent.getBooleanExtra(getString(R.string.isloggedintag), false))
        {
            String profile = intent.getStringExtra(getResources().getString(R.string.profile_label));
            if (profile != null)
                getUserIdAndUserName(profile);
        }


        mMarkerManager.map = null;

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client2 = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        slide_down = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
        slide_down.setAnimationListener(new Animation.AnimationListener()
        {

            @Override
            public void onAnimationStart(Animation animation)
            {

            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                mToolbar.setVisibility(View.GONE);
                mbToolBarShown = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        slide_up   = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        slide_up.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation)
            {
                mbToolBarShown = true;
                mToolbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {
            }
        });

        mToolbar = findViewById(R.id.main_toolbar);
    }

    private void setCallBacks()
    {
        ImageView imageViewPutLocation = (ImageView) findViewById(R.id.put_marker);

        imageViewPutLocation.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mStatus = AppStatus.LocationEntryRequested;
                mTimer = new Timer();
                TimerTask task = new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        showGlobalProgressBar("Finding your current location...");
                    }

                };

                mTimer.schedule(task, timetofireprogressbar);

                mTimeOutTimer = new Timer();
                TimerTask timeouttask = new TimerTask()
                {

                    @Override
                    public void run()
                    {
                        hideGlobalProgressBar();
                        Log.d("com.pathz", "this is ridiculus");
                        Looper.prepare();
//						prompt("Your location can not be found using GPS. \n Since your present location is not known you can't enter a location.");
                        promptWithAlertDlgBox("Your location can not be found using GPS. \n Since your present location is not known you can't enter location.");
                    }

                };

                mTimeOutTimer.schedule(timeouttask, gpstimeout);

                startLocationUpdates();



            }
        });


        // Edit User Information button (this is an imageview)
        ImageView imageViewEditUserInformation = (ImageView) findViewById(R.id.user_info);

        imageViewEditUserInformation.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(view.getContext(), EditUserInfoActivity.class);
                startActivity(intent);
            }
        });


        ImageView imageViewSettingButton = (ImageView) findViewById(R.id.setting_button);

        imageViewSettingButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent locpanelIntent = new Intent(mContext, TypeFilterAndSettingActivity.class);
                startActivityForResult(locpanelIntent, TYPE_FILTER_AND_SETTING_INITIATED);
            }
        });

        ImageView movingModeButton = (ImageView) findViewById(R.id.moving_mode_button);
        movingModeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                flipMovingMode();
            }
        });

        //map functions,
//
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        SupportMapFragment mapFragment = (R.layoutSupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);  // this would fire onmapready
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        thisact = this;
        act = this;

    }

    protected void sendLogCatEmail()
    {
        // save logcat in file
        File outputFile = new File(Environment.getExternalStorageDirectory(),"logcat.txt");

        try
        {
            Runtime.getRuntime().exec("logcat -f " + outputFile.getAbsolutePath());
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //send file using email
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        String to[] = {"paymane@gmail.com"};
        emailIntent .putExtra(Intent.EXTRA_EMAIL, to);
        emailIntent.setType("text/plain");
        // the attachment
        Uri uri = Uri.fromFile(outputFile);
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
//	 emailIntent .putExtra(Intent.EXTRA_STREAM, outputFile.getAbsolutePath());
        // the mail subject
        emailIntent .putExtra(Intent.EXTRA_SUBJECT, "Subject");
        startActivity(Intent.createChooser(emailIntent , "Send email..."));
    }



    DefaultAsyncProcess mNoteMan = null;
    private void flipMovingMode()
    {
        m_bMovingMode = !m_bMovingMode;

        if (m_bMovingMode)
        {
            mRadiusMeters = 20;
            mCurZoomLevel = 20;
            Toast.makeText(getApplicationContext(), "about to zoom in to " + mCurZoomLevel + " level.", Toast.LENGTH_SHORT).show();

            mMap.animateCamera(CameraUpdateFactory.zoomTo(mCurZoomLevel), 2000, new GoogleMap.CancelableCallback()
            {

                @Override
                public void onFinish()
                {
                    startLocationUpdates(mTimeInterval);
                    if (mNoteMan == null)
                    {
//                        Log.i("INFO","Launching NoteMan for the first time.");
                        mNoteMan = createNoteMan();
                        mNoteMan.launch();
                    }
                    else
                        mNoteMan.resume();
                    refreshNow();
                }

                @Override
                public void onCancel()
                {

                }
            });

        }
        else
        {
            Toast.makeText(getApplicationContext(), "about to zoom out", Toast.LENGTH_SHORT).show();
            mRadiusMeters = mDefaultRadiusInMeters;
            mCurZoomLevel = defaultzoom_level;

            mMap.animateCamera(CameraUpdateFactory.zoomTo(mCurZoomLevel), 2000, null);
//            stopLocationUpdates();
//            startLocationUpdates(null);
//            mNoteMan.pause();
        }
    }
    final HashSet<String> notes = new HashSet<String>();
    private DefaultAsyncProcess createNoteMan()
    {
        return new DefaultAsyncProcess(act)
        {
            @Override
            protected void doTheThing()
            {
                final View[] noteview = {null};

                while (m_bMovingMode)
                {
                    while (noteque != null && !noteque.isEmpty() && m_bMovingMode)
                    {
                        act.runOnUiThread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                LinearLayout panel = null;
                                LayoutInflater inflater = null;

                                String note = noteque.poll();
                                while(note != null)
                                {
                                    if (!notes.contains(note))
                                    {
                                        notes.add(note);
                                        showPopupNote(note, thisact);
                                    }
                                    note = noteque.poll();
                                }


//
                            }
                        });

                        try {
                            Thread.sleep(note_time_in_ms);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }

            }
        };
    }

    private void showPopupNote(String note, final Activity context)
    {
        LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.popup);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.note_view, viewGroup);

        TextView  tv = (TextView) layout.findViewById(R.id.place_name);

        // set the text you want to show in  Toast
        tv.setText(note);

        Toast toast=new Toast(thisact); //context is object of Context write "this" if you are an Activity
        // Set The layout as Toast View
        toast.setView(layout);

        // Position your toast here toast position is 50 dp from top you can give any integral value
        toast.setGravity(Gravity.TOP, 0, 100);
        toast.show();
    }


    static final int requestCode1 = 10001;



    private SharedPreferences.Editor getEditor()
    {
        if (editor == null) {
            SharedPreferences settings;
            settings = mContext.getSharedPreferences(mPrefName, Context.MODE_PRIVATE); //1
            editor = settings.edit(); //2
        }

        return editor;
    }

    public void getUserIdAndUserName(String profile)
    {
        UserManager userManager = UserManager.getTheOnlyUserManager(this);
        if (profile != null)
        {
            JSONParser parser = new JSONParser();
            Object object = null;
            try
            {
                object = parser.parse(profile);
                JSONObject userprofile = (JSONObject) object;
                String age = (String) userprofile.get(getString(R.string.age_lable));
                String gender = (String) userprofile.get(getString(R.string.gender_label));
                String yob = (String) userprofile.get(getString(R.string.yob_label));
                String name = (String) userprofile.get(getString(R.string.name_lable));

                User.id = (String) userprofile.get("id");
                User.age = age;
                User.gender = gender;
                User.yob = yob;
                User.name = name;
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
            if (object == null)
                return ;

        }

        app_name = getString(R.string.app_name);
        mPrefName = app_name;
    }


    @Override
    protected void onResume()
    {
        super.onResume();
        if (mMap != null)
            refreshNow();
    }

    private void showGlobalProgressBar(final String msg) {
        runOnUiThread(
                new Runnable() {
                    @Override
                    protected void finalize() throws Throwable {
                        threadStarted = false;
                        super.finalize();
                    }

                    @Override
                    public void run() {
                        if (threadStarted)
                            return;
                        threadStarted = true;
                        try {
                            View v = findViewById(R.id.globalprogressbarcontainer);
                            if (msg != null && !msg.isEmpty()) {
                                TextView tv = (TextView) v.findViewById(R.id.gprogbartitle);
                                defaultmessage = tv.getText().toString();
                                tv.setText(msg);
                            }
                            if (v != null && !bLatestLocationObtained)
                                v.setVisibility(View.VISIBLE);
                            threadStarted = false;
                        } catch (Exception e) {
                            Log.e("com.forkit", e.getLocalizedMessage());
                            threadStarted = false;
                        }
                    }

                }

        );
    }

    boolean threadStarted = false;
    private static String defaultmessage = "";

    private void hideGlobalProgressBar() {
        runOnUiThread(
                new Runnable() {
                    @Override
                    protected void finalize() throws Throwable {
                        threadStarted = false;
                        super.finalize();
                    }

                    @Override
                    public void run() {
                        if (threadStarted)
                            return;
                        threadStarted = true;
                        try {
                            View v = findViewById(R.id.globalprogressbarcontainer);
                            TextView tv = (TextView) v.findViewById(R.id.gprogbartitle);
                            if (defaultmessage != null && !defaultmessage.isEmpty())
                                tv.setText(defaultmessage);
                            if (v != null)
                                v.setVisibility(View.INVISIBLE);
                            threadStarted = false;
                        } catch (Exception e) {
                            Log.e("pathz", e.getLocalizedMessage());
                            threadStarted = false;
                        }
                    }

                }

        );

    }


    final static int NUM_UPDATES = 1;
    private static final String TAG = "LocationActivity";
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    private static final long SINGLE_UPDATE_INTERVAL = 100;

    protected void createLocationRequest()
    {
        if (mLocationRequest == null)
            mLocationRequest = new LocationRequest();

        mLocationRequest.setNumUpdates(NUM_UPDATES);

        mLocationRequest.setInterval(SINGLE_UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(SINGLE_UPDATE_INTERVAL);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    private void performInitialLocation()
    {
        bestLastKnown = getBestLastKnown();

        try
        {
            if (bestLastKnown == null)
                return;
//				 bestLastKnown = getUsersInputForLocation();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        showLocation(bestLastKnown, Types.locationTypes.BEST_LAST_KNOWN, true, defaultzoom_level);
    }

    private void showLocation(LatLng loc, Types.locationTypes loctype, boolean moveTo, int zoomLevel)
    {
        if (loc == null)
        {
            mMap.animateCamera(CameraUpdateFactory.zoomTo(1), 2000, null);
            return;
        }

        mMarkerManager.addMarker(loc, loctype, false);
        if (moveTo)
        {
            mMap.animateCamera(CameraUpdateFactory.zoomTo(zoomLevel), 2000, null);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, zoomLevel));
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setZoomGesturesEnabled(true);
        }

        refreshNow();
    }

    protected void startLocationUpdates()
    {
        startLocationUpdates(null);
    }

    protected void startLocationUpdates(Long interval)
    {
        bLatestLocationObtained = false;
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
        {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            if (interval != null)
            {
                //remove location updates so that it resets
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

                //change the time of location updates
                mLocationRequest = LocationRequest.create()
                        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        .setInterval(interval)
                        .setSmallestDisplacement(0);
            }
            else
            {
                //remove location updates so that it resets
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
                createLocationRequest();
            }

            PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

        Log.d(TAG, "Location update started ..............: ");
    }

    public void refreshNow()
    {
//        if (mScaleBar != null)
//        {
//            mScaleBar.setMap(map);
//            mScaleBar.invalidate();
//        }
//        if (doneFromProgram)
//        {
//            doneFromProgram = false;
//            return;
//        }
//		clearTempMarkers();
        Double[] viewingarea = getViewingArea();
        double delta_lat = viewingarea[0] - viewingarea[2];
        String types = "restaurant|bar|grill";
        if (delta_lat == 0)
        {
            if (gogetter == null || !gogetter.isRunning())
            {
                LatLng searchcenter = (mCurLoc != null ? mCurLoc : bestLastKnown);
                if (searchcenter != null)
                {
                    gogetter = new GoGetter(thisact, urlreaddb, searchcenter.latitude, searchcenter.longitude, types, null, mRadiusMeters);
                    gogetter.launch();
                }
            }
        }
        else
        {
            double center_lat = 0.5 * (viewingarea[0] + viewingarea[2]);
            double center_lon = 0.5 * (viewingarea[1] + viewingarea[3]);
            if (gogetter != null && gogetter.isRunning())
                gogetter.stop();
            else
            {
                gogetter = new GoGetter(this, urlreaddb, center_lat, center_lon, types, null, mRadiusMeters);
                gogetter.launch();
            }

        }

    }


    final int REQUEST_LOCATION = 5;

    private LatLng getBestLastKnown() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
        else
        {
            // permission has been granted, continue as usual
            // fast fail, don't use if mGoogleApiClient is not connected,
            //otherwise throws illegal state exception.`d
            Location lastKnown = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (lastKnown == null)
            {
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                Location mylocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (mylocation == null)
                {
                    Criteria criteria = new Criteria();
                    criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                    String provider = lm.getBestProvider(criteria, true);
                    mylocation = lm.getLastKnownLocation(provider);
                    if (mylocation != null)
                        return new LatLng(mylocation.getLatitude(), mylocation.getLongitude());
                    return null;
                }
                else
                {
                    return new LatLng(mylocation.getLatitude(), mylocation.getLongitude());
                }

            }
            return new LatLng(lastKnown.getLatitude(), lastKnown.getLongitude());
        }

        return null;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        performInitialization();
        performInitialLocation();
        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        if (User.name != null && !User.name.isEmpty() && !User.name.equals("-"))
            Toast.makeText(thisact, getResources().getString(R.string.welcomemsg) + " " + User.name, Toast.LENGTH_LONG).show();
        else
            Toast.makeText(thisact,getResources().getString(R.string.anonwelcomemsg),Toast.LENGTH_LONG).show();

        hideToolbarAfterDelay();
    }


    private void showToolbar()
    {
        mToolbar.startAnimation(slide_up);
    }



    DefaultAsyncProcess delayedhide;

    private class HideProcess extends DefaultAsyncProcess
    {
       HideProcess(Activity act_)
       {
           super(act_);
       }

        protected void doTheThing()
        {
            try
            {
                Thread.sleep(TIME_TO_HIDE);
                hideToolbar();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }


    private void hideToolbar()
    {
        mToolbar.startAnimation(slide_down);
    }

    private void hideToolbarAfterDelay()
    {
        delayedhide = new HideProcess(this);
        delayedhide.launch();
    }


    private void performInitialization()
    {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();

        mMarkerManager.map = this.mMap;
        mMarkerManager.mainAct = this;
        mMarkerManager.setOnMarkerListener();
        getScreenDimensions();
        createLocationRequest();
    }

    @Override
    public void onConnected(Bundle bundle)
    {
        bestLastKnown = getBestLastKnown();
        if (bestLastKnown != null)
            showLocation(bestLastKnown, Types.locationTypes.BEST_LAST_KNOWN, true, mCurZoomLevel);
    }

    @Override
    public void onConnectionSuspended(int i)
    {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
    }

    public void prompt(String msg) {
        Toast.makeText(thisact, msg, Toast.LENGTH_LONG).show();
    }

    private int screenWidth = -1;
    private int screenHeight = -1;

    public Point getScreenDimensions()
    {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        if (size.x == screenWidth && size.y == screenHeight)
            return size;
        screenWidth = size.x;
        screenHeight = size.y;
        Point ret = new Point(screenWidth, screenHeight);
//		if (firstTimeScreenCalled)
//		{
//			int h = (int) (0.30 * screenHeight);
//			openLocIOPanel(h);
//			firstTimeScreenCalled = false;
//		}

//		adjustHeight(screenHeight*factor);
        // **


        refreshNow();
        return ret;
    }


    @Override
    public void onUpdateMapAfterUserInterection()
    {
        refreshNow();
    }

    private boolean mbToolBarShown = false;

    @Override
    public void onMapClicked()
    {
        if (!mbToolBarShown)
        {
            showToolbar();
            hideToolbarAfterDelay();
        }

    }

    public void openLocIOPanel(LatLng location, Marker marker, MarkerManager.InfoType infoType)
    {
        if (infoType == MarkerManager.InfoType.OUTPUT)
        {
            String locationExtraInfo = mMarkerManager.getExtraInfo().get(MarkerManager.createInfoMapKey(marker.getSnippet(), marker.getTitle(), location));
            Intent locpanelIntent = new Intent(mContext, LocationInfoPanel.class);
            locpanelIntent.putExtra("EXTRA_INFO", locationExtraInfo);
            startActivity(locpanelIntent);
        }
    }


    @Override
    public void onLocationChanged(Location location)
    {
        Log.i("INFO", "onlocationchanged triggered.");
        if (!bLatestLocationObtained)
            bLatestLocationObtained = true;

        if (timer != null)
            timer.cancel();

        if (timeouttimer != null)
            timeouttimer.cancel();

        hideGlobalProgressBar();

        if(!m_bMovingMode)
        {
            updateMyLocation(location);
            stopLocationUpdates();
        }
        else
        {
            mCurLoc = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLng(mCurLoc));
            gogetter = null;
        }

        refreshNow();

        if (mStatus == AppStatus.LocationEntryRequested)
        {
            Intent intent = new Intent(mContext, PutLocationTypeActivity.class);
            String lontag = getString(R.string.curloc_lon_tag);
            String lattag = getString(R.string.curloc_lat_tag);
            intent.putExtra(lontag, mCurLoc.longitude);
            intent.putExtra(lattag, mCurLoc.latitude);
            if (loggedIn)
                intent.putExtra(getResources().getString(R.string.user_id_label), User.id);
            startActivity(intent);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case (TYPE_FILTER_AND_SETTING_INITIATED):
                if (data == null ||
                        data.getBundleExtra("CHOSEN_TYPES") == null ||
                        data.getBundleExtra("CHOSEN_TYPES").getStringArrayList("CHOSEN_TYPES") == null)
                    break;

                ArrayList<String> chosenOnes = data.getBundleExtra("CHOSEN_TYPES").getStringArrayList("CHOSEN_TYPES");
                ArrayList<Types.locationTypes> chosenTypes = new ArrayList<Types.locationTypes>();
                for (Types.locationTypes type : Types.locationTypes.values())
                {
                    if (chosenOnes != null && chosenOnes.contains(type.identifier()))
                    {
                        chosenTypes.add(type);
                    }
                }
                HashMap<MarkerManager.CRITERIATYPE, Object> criteria = new HashMap<MarkerManager.CRITERIATYPE, Object>();
                criteria.put(PLACETYPE, chosenTypes);
                MarkerManager.getMarkerManager().filterMarkers(criteria);
                break;

            case (requestCode1):
            {
                getUserIdAndUserName(null);
            }
        }

    }

    private void promptWithAlertDlgBox(String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(false);
        builder.setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
    }

    protected void stopLocationUpdates()
    {
        if (mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }


    public void updateMyLocation(Location location)
    {
        mCurLoc = new LatLng(location.getLatitude(), location.getLongitude());
        if (!curLocationUpdated)
        {
            curLocationUpdated = true;
            mMarkerManager.removeAll(Types.locationTypes.BEST_LAST_KNOWN);
        }
        mMarkerManager.removeAll(Types.locationTypes.CURR_GPS_LOCATION);
        mMarkerManager.removeAll(Types.locationTypes.BEST_LAST_KNOWN);
        if (bLatestLocationObtained)
            showLocation(mCurLoc, Types.locationTypes.CURR_GPS_LOCATION, false, mCurZoomLevel);
        else
            showLocation(mCurLoc, Types.locationTypes.CURR_GPS_LOCATION, true, mCurZoomLevel);

        if(m_bMovingMode)
        {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(mCurLoc));
        }

        refreshNow();

    }

    public void onBackPressed()
    {
        moveTaskToBack(true);
    }

}
