package com.innovathon.sideways.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.People;
import com.google.api.services.people.v1.model.Birthday;
import com.google.api.services.people.v1.model.CoverPhoto;
import com.google.api.services.people.v1.model.Gender;
import com.google.api.services.people.v1.model.Name;
import com.google.api.services.people.v1.model.Person;
import com.innovathon.sideways.R;
import com.innovathon.sideways.main.MainActivity;
import com.innovathon.sideways.ssltools.NoSSLv3SocketFactory;
import com.innovathon.sideways.util.ssltools.SSLFix;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class LoginPanel extends ActivitySendingInfo implements GoogleApiClient.OnConnectionFailedListener {
    private static final int RC_SIGN_IN = 2000;
    UserManager mUserMan = null;
    private CallbackManager  mFacebookCallbackManager;
    private LoginButton mFacebookSignInButton;
    private String mLoginMethod, mAccessToken, mResponse, mProfile;
    private Context mContext;
    private Activity mAct;
    private String mPrefName;
    private boolean registered = false;
    private GoogleSignInOptions gso;
    private static final String TAG = "LoginPanel";

    Button button;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        mContext = this;
        mAct = this;
        HttpsURLConnection.setDefaultSSLSocketFactory(new NoSSLv3SocketFactory());
        mUserMan = UserManager.getAUserManager(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        super.onCreate(savedInstanceState);


        //amir 7/5/1396

//        facebook login with facebook api
//        initialization of facebook api
//        FacebookSdk.sdkInitialize(getApplicationContext());
//        mFacebookCallbackManager = CallbackManager.Factory.create();
//      end of initialization


//        setupCallBacksForFacebookLoginButton();

        setContentView(R.layout.activity_login_options);

        findViewById(R.id.btn_login).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sidewayslogin();
                    }
                }

        );

        findViewById(R.id.facebook_login_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        facebooklogin();
                    }
                }

        );

        findViewById(R.id.google_login_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        googlelogin();
                    }
                }

        );

        findViewById(R.id.guest_login_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sidewaysloginAsGuest();
                    }
                }
//register_button

        );
        findViewById(R.id.register_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        registerbutton();
                    }
                }

        );

        mPrefName = getString(R.string.app_name);
    }

    private void sidewayslogin()
    {
        String email = getContent(R.id.input_email);
        String password = getContent(R.id.input_password);

        if (email == null || email.isEmpty())
        {
            prompt(this, "You must enter an email address.");
            return;
        }
        if(password == null || password.isEmpty())
        {
            prompt(this, "You must enter your password");
            return;
        }

        HashMap<String,String> cred = new HashMap<String,String>();
        cred.put(getString(R.string.email_lable),email);
        cred.put(getString(R.string.password ),password);
        this.postInfo(cred, getString(R.string.baseurl) + getString(R.string.loginurl),"");
    }

    private String getContent(int id)
    {
        String ret = null;
        Editable text = ((EditText) findViewById(id)).getText();
        if (text != null)
            ret = text.toString();

        if (ret == null || ret.isEmpty())
        {
            return ret;
        }

        return ret;
    }

    @Override
    public void doThisForMessage(String msg)
    {
        if(msg != null && !msg.isEmpty())
        {
            if (msg.toLowerCase().endsWith("success"))
            {
                msg = msg.substring(0, msg.indexOf("success"));
                msg = msg.trim();
                try
                {
                    JSONParser parser = new JSONParser();
                    Object object = parser.parse(msg);
                    if (object instanceof JSONArray)
                    {
                        JSONArray jsonarray = (JSONArray) object;
                        object = jsonarray.get(jsonarray.size()-1);
                    }
                    JSONObject jsonobject = (JSONObject) object;
                    User.id = (String) jsonobject.get("id");
                    User.gender = (String) jsonobject.get("gender");
                    User.name = (String) jsonobject.get("firstname") + " " + (String)jsonobject.get("lastname");
                    User.yob = (String) jsonobject.get("yob");
                    User.age = (String) jsonobject.get("age");
                    getEditor();
                    editor.putString(getString(R.string.id_lable),User.id);
                    editor.putString(getString(R.string.name_lable),User.name);
                    editor.putString(getString(R.string.gender_label),User.gender);
                    editor.putString(getString(R.string.age_lable),User.age);
                    editor.putString(getString(R.string.email_lable),User.email);
                    editor.putString(getString(R.string.phonenumber),User.phonenumber);
                    editor.putString(getString(R.string.key_login_method), mPrefName);
                    editor.commit();
                    registered = true;

                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
                launchSideways(true);
            }
            else
            {
                ((EditText) findViewById(R.id.input_email)).setText("");
                ((EditText) findViewById(R.id.input_password)).setText("");
                prompt(this, "Please enter the correct email and password.");
            }
        }
    }
    private SharedPreferences.Editor getEditor()
    {
        if (editor == null)
        {
            SharedPreferences settings;
            settings = mContext.getSharedPreferences(mPrefName, Context.MODE_PRIVATE); //1
            editor = settings.edit(); //2
        }

        return editor;
    }
    private void launchSideways(boolean b)
    {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra (getString(R.string.isloggedintag),b);
        intent.putExtra("ALREADY_REGISTERED",b);
        startActivity(intent);
    }

    private void registerbutton()
     {
         Intent intent = new Intent(LoginPanel.this, PhoneNumberVerificationActivity.class);
//         intent.putExtra("PHONENUMBER",)
        startActivity(intent);
//         Intent intent = new Intent(LoginPanel.this, RegisterPanel.class);
//         startActivity(intent);
     }


    private void sidewaysloginAsGuest()
    {
        Intent authorizationIntent = new Intent(mAct, MainActivity.class);
        startActivityForResult(authorizationIntent, getResources().getInteger(R.integer.USER_CHOICE_SIDEWAYS));

        Toast.makeText(LoginPanel.this,
                "You chose to sign in as guest, \nyou won't be able to post.", Toast.LENGTH_LONG).show();
    }

    private void googlelogin()
    {
        googleSignIn();

//        GoogleAuth gAuth = new GoogleAuth(this);
//
//        gAuth.login();
    }
    private GoogleApiClient client;
    private void googleSignIn()
    {
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(Scopes.PLUS_LOGIN)) // "https://www.googleapis.com/auth/plus.login"
                .requestScopes(new Scope(Scopes.PLUS_ME)) // "https://www.googleapis.com/auth/plus.me"
                .requestScopes(new Scope(Scopes.PROFILE))
                .requestProfile()
                .requestId()
                .build();
        client = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addApi(Auth.CREDENTIALS_API)
                .build();

        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(client);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }



    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {

    }

    public void facebooklogin()
    {
        FacebookAuth fAuth = new FacebookAuth(this);

        fAuth.login();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        String email = getUsersEmailAddress();
        User.email = email;

        String phonenumber = getUsersPhoneNumber();
        User.phonenumber = phonenumber;
        // Check which request we're responding to
        int code = Activity.RESULT_OK;
        if (requestCode == 100)
        {
            UserManager userManager = UserManager.getTheOnlyUserManager();
            if (userManager == null)
                userManager = UserManager.getAUserManager(this);
            userManager.saveLogInInformation(data);
            finish();
        }

        if (requestCode == RC_SIGN_IN)
        {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result != null)
                handleSignInResult(result);
        }

        if (requestCode == getResources().getInteger(R.integer.USER_CHOICE_SIDEWAYS))
        {
            UserManager userManager = UserManager.getTheOnlyUserManager();
            if (userManager == null)
                userManager = UserManager.getAUserManager(this);
            userManager.saveLogInInformation();
            finish();
        }

    }

    // Retrieve and save the url to the users Cover photo if they have one
    private class GetCoverPhotoAsyncTask extends AsyncTask<String, Intent, Void>
    {
        HttpTransport httpTransport = new NetHttpTransport();
        JacksonFactory jsonFactory = new JacksonFactory();
        Intent resultIntent = null;
        public GetCoverPhotoAsyncTask(Intent r)
        {
            resultIntent = r;
        }

        // Retrieved from the sigin result of an authorized GoogleSignIn
        String personEmail;

        @Override
        protected Void doInBackground(String... params) {
            personEmail = params[0];
            Person userProfile = null;
            Collection<String> scopes = new ArrayList<>(Collections.singletonList(Scopes.PROFILE));

            GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(LoginPanel.this, scopes);
            credential.setSelectedAccount(new Account(personEmail, "com.google"));

            People service = new People.Builder(httpTransport, jsonFactory, credential)
                    .setApplicationName(getString(R.string.app_name)) // your app name
                    .build();

            // Get info. on user
            try
            {
                userProfile = service.people().get("people/me")
                        .setFields("names,nicknames,emailAddresses,genders,birthdays")
                        .execute();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage(), e);
            }

            // Get whatever you want
            if (userProfile != null)
            {
                List<Name> names = userProfile.getNames();
                if (names != null && !names.isEmpty())
                {
                    User.name = names.get(0).getDisplayName();
                }
                List<Gender> genders = userProfile.getGenders();
                if (genders != null && !genders.isEmpty())
                {
                    User.gender = genders.get(0).getFormattedValue();
                }
                String agerange = userProfile.getAgeRange();
                if (agerange != null)
                    User.agerange = agerange;
                List<?> ageranges = userProfile.getAgeRanges();
                if (ageranges != null && !ageranges.isEmpty())
                {
                    User.agerange = ageranges.get(0).toString();
                }

                List<Birthday> birthdays = userProfile.getBirthdays();
                if (birthdays != null && !birthdays.isEmpty())
                {
                    User.yob = birthdays.get(0).getDate().getYear().toString();
                }

                List<CoverPhoto> covers = userProfile.getCoverPhotos();
                if (covers != null && covers.size() > 0)
                {
                    CoverPhoto cover = covers.get(0);
                    if (cover != null)
                    {
                        // save url to cover photo here, load at will
                        //Prefs.setPersonCoverPhoto(cover.getUrl());
                    }
                }
            }
            String login_method_indicator_tag = mAct.getResources().getString(R.string.login_method_indicator_tag);
            resultIntent.putExtra(login_method_indicator_tag,"GOOGLE");
            String result_tag = mAct.getResources().getString(R.string.from_auth_to_launcher_acc_token);
            setResult(Activity.RESULT_OK, resultIntent);


            UserManager.getTheOnlyUserManager().saveLogInInformation(resultIntent);

            Intent mResultIntent = new Intent(LoginPanel.this, MainActivity.class);

            mResultIntent.putExtra(login_method_indicator_tag,User.id_type);
            startActivity(mResultIntent);
            return null;
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Intent intent = getIntent();
        System.out.println();
    }


    private void setupCallBacksForFacebookLoginButton()
    {
//        mFacebookSignInButton = (com.facebook.login.widget.LoginButton)findViewById(R.id.facebook_login_button);
//        mFacebookSignInButton.setBackgroundResource(R.drawable.loginwithfacebook);
//        mFacebookSignInButton.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
//        mFacebookSignInButton.registerCallback(mFacebookCallbackManager,
//                new FacebookCallback<LoginResult>()
//                {
//                    @Override
//                    public void onSuccess(final LoginResult loginResult)
//                    {
//                        //TODO: Use the Profile class to get information about the current user.
//                        handleSignInResult(new Callable<Void>()
//                        {
//                            @Override
//                            public Void call() throws Exception
//                            {
//                                LoginManager.getInstance().logOut();
//                                return null;
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onCancel()
//                    {
//                        handleSignInResult(null);
//                    }
//
//                    @Override
//                    public void onError(FacebookException error)
//                    {
//                        Log.d(LoginPanel.class.getCanonicalName(), error.getMessage());
//                        handleSignInResult(null);
//                    }
//                }
//        );

    }

    private void handleSignInResult(Callable<Void> callable)
    {
        Toast.makeText(this, "You handled the Signin Result Eh?" , Toast.LENGTH_LONG);
    }

    private void handleSignInResult(GoogleSignInResult result)
    {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess())
        {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            Intent resultIntent = new Intent();
            resultIntent.putExtra(getString(R.string.google_signin_success), true);
            String login_method_indicator_tag = getResources().getString(R.string.login_method_indicator_tag);
            resultIntent.putExtra(login_method_indicator_tag,"GOOGLE");
            String result_tag = getResources().getString(R.string.from_auth_to_launcher_acc_token);
            resultIntent.putExtra(result_tag, acct.getId());
            resultIntent.putExtra(getString(R.string.name_lable), acct.getDisplayName());

            result_tag = getResources().getString(R.string.from_auth_to_launcher_secret);
            resultIntent.putExtra(result_tag, acct.getServerAuthCode());
            HashMap<String,String> profile_hashmap = new HashMap<String,String>();
            GetCoverPhotoAsyncTask googleprofileprocess = new GetCoverPhotoAsyncTask(resultIntent);

            HttpsURLConnection.setDefaultSSLSocketFactory(new com.innovathon.sideways.util.ssltools.NoSSLv3SocketFactory());
            SSLFix.trustAllCertificateAuthorities();
            googleprofileprocess.execute(acct.getEmail());

        }
        else
        {
            Toast.makeText(this, "Couldn't sign you in, try agagin." , Toast.LENGTH_LONG);

        }
    }

    public String getUsersEmailAddress()
    {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        Account[] accounts = AccountManager.get(mContext).getAccounts();
        for (Account account : accounts)
        {
            if (pattern.matcher(account.name).matches())
            {

                String emailAddress = account.name;
                return emailAddress;
            }
        }

        return null;
    }

    public String getUsersPhoneNumber()
    {
        String usersPhoneNumber = null;

        TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String MyPhoneNumber = "0000000000";

        try {
            usersPhoneNumber = mngr.getLine1Number();
        } catch (NullPointerException ex) {
        }

        if (MyPhoneNumber.equals("")) {
            usersPhoneNumber = mngr.getSubscriberId();
        }
        String ret = usersPhoneNumber;
        if (ret.startsWith(User.country_code)) {
            ret = ret.substring(User.country_code.length());
        }

        ret = String.format("%s-%s-%s", ret.substring(0, 3), ret.substring(3, 6),
                ret.substring(6, 10));

        return ret;


    }
}


