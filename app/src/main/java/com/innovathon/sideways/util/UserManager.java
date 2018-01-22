package com.innovathon.sideways.util;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.innovathon.sideways.R;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Created by Payman & Nahid on 2/8/2017.
 */

public class UserManager {

    final public static int NONE = -1000;
    final public static int REGD_NOTLOGGED = 1001;
    final public static int REGD_LOGGED_AS_GUEST = 1002;
    final public static int REGD_LOGGED_AS_FB_USER = 1003;
    final public static int REGD_LOGGED_AS_GPLUS_USER = 1004;
    final public static String[] account_types = {"GUEST", "GOOGLE+", "FACEBOOK", "SIDEWAYS"};
    private static final int REGD_LOGGED_AS_SIDEWAYS_USER = 1005;
    private static final int REGD_LOGGED_AS_GUEST_USER = 1000;
    private static UserManager theOnlyUserManager = null;
    private static Activity mAct = null;
    private static String account_type = null;
    private static int mStatus = NONE;
    private static String mService = null;
    private final String mPrefName;
    String clientId = null;
    String clientSecret = null;
    private String mAuth_method;
    private String mLoginMethod, mAccessToken, mResponse, mProfile;
    private boolean mLoggedin;
    private String mKeyForAccessToken, mKeyForLoginMethod, mKeyForResponse, mKeyForProfile;

    private UserManager(Activity main_) {
        mAct = main_;
        mPrefName = mAct.getString(R.string.app_name);
        mAuth_method = mAct.getString(R.string.login_method_indicator_tag);
        mStatus = NONE;

        mKeyForAccessToken = mAct.getString(R.string.access_token);
        mKeyForLoginMethod = mAct.getString(R.string.key_login_method);
        mKeyForResponse = mAct.getString(R.string.key_response);
        mKeyForProfile = mAct.getString(R.string.key_profile);
    }

    static public UserManager getAUserManager(Activity main) {
        if (theOnlyUserManager == null)
            theOnlyUserManager = new UserManager(main);


        return theOnlyUserManager;
    }

    static public UserManager getTheOnlyUserManager() {

        return theOnlyUserManager;
    }

    public static void setService(String servicename) {
        mService = servicename;
    }

    public static UserManager getTheOnlyUserManager(Activity mainActivity) {
        if (theOnlyUserManager != null)
            return theOnlyUserManager;
        else
            return getAUserManager(mainActivity);
    }

    public int status() {
        String auth = retrieveAuthMethod();
        if (auth == null) {
            return NONE;
        }

        if (auth.equalsIgnoreCase(account_types[0]))
            mStatus = REGD_LOGGED_AS_GUEST_USER;

        if (auth.equalsIgnoreCase(account_types[1]))
            mStatus = REGD_LOGGED_AS_GPLUS_USER;

        if (auth.equalsIgnoreCase(account_types[2]))
            mStatus = REGD_LOGGED_AS_FB_USER;

        if (auth.equalsIgnoreCase(account_types[3]))
            mStatus = REGD_LOGGED_AS_SIDEWAYS_USER;

        return mStatus;
    }

    public void getUser() {
        mLoggedin = false;
        SharedPreferences settings;
        settings = mAct.getSharedPreferences(mPrefName, Context.MODE_PRIVATE); //1
        String auth = retrieveAuthMethod();
        if (auth.equalsIgnoreCase(account_types[2])) {
            User.name = settings.getString(mAct.getString(R.string.facebook_name_lable), null);
            User.id = settings.getString(mAct.getString(R.string.facebook_id_lable), null);
            User.gender = settings.getString(mAct.getString(R.string.genderlabel), null);
            User.email = settings.getString(mAct.getString(R.string.facebook_email_lable), null);
            User.age = settings.getString(mAct.getString(R.string.facebook_age_lable), "");
            User.phonenumber = settings.getString(mAct.getString(R.string.phonenumber), "");
            mLoggedin = true;
        }

        if (auth.equalsIgnoreCase(account_types[3])) {
            User.name = settings.getString(mAct.getString(R.string.name_lable), null);
            User.id = settings.getString(mAct.getString(R.string.id_lable), null);
            User.gender = settings.getString(mAct.getString(R.string.genderlabel), null);
            User.email = settings.getString(mAct.getString(R.string.email_lable), null);
            User.age = settings.getString(mAct.getString(R.string.age_lable), null);
            User.phonenumber = settings.getString(mAct.getString(R.string.phonenumber), "");
            mLoggedin = true;
        }


    }

    private String retrieveAuthMethod() {
        SharedPreferences settings;
        String text;
        settings = mAct.getSharedPreferences(mPrefName, Context.MODE_PRIVATE); //1
        text = settings.getString(mKeyForLoginMethod, null); //2
        return text;
    }


    public void saveLogInInformation(Intent data) {
        mLoginMethod = data.getStringExtra(mAct.getString(R.string.login_method_indicator_tag));
        mAccessToken = data.getStringExtra(mAct.getString(R.string.from_auth_to_launcher_acc_token));
        mResponse = data.getStringExtra(mAct.getString(R.string.from_auth_to_launcher_secret));
        mProfile = data.getStringExtra(mAct.getString(R.string.init_fb_profile_resp));

        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = mAct.getSharedPreferences(mPrefName, Context.MODE_PRIVATE); //1
        editor = settings.edit(); //2

        editor.putString(mKeyForLoginMethod, mLoginMethod);
        editor.putString(mKeyForResponse, mResponse);
        editor.putString(mKeyForProfile, mProfile);
        editor.putString(mKeyForAccessToken, mAccessToken); //3

        String idlabel = mAct.getString(R.string.id_lable);
        String namelabel = mAct.getString(R.string.name_lable);
        String agelabel = mAct.getString(R.string.age_lable);
        String emaillabel = mAct.getString(R.string.email_lable);
        String genderlabel = mAct.getString(R.string.gender_label);
        String yoblabel = mAct.getString(R.string.yob_label);
        String phone_label = mAct.getString(R.string.phonenumber);

        if (mLoginMethod != null && mLoginMethod.equalsIgnoreCase("sideways")) {
            if (User.phonenumber != null)
            {
                editor.putString(phone_label, User.phonenumber);
            }
            if (User.name != null) {
                editor.putString(namelabel, User.name);
            }
            if (User.id != null) {
                editor.putString(idlabel, User.id);
            }

            if (User.yob != null) {
                editor.putString(yoblabel, User.yob);
            }

            if (User.email != null)
                editor.putString(emaillabel, User.email);

            if (User.gender != null)
                editor.putString(genderlabel, User.gender);
        }

        if (mProfile != null && mProfile.trim().length() > 0) {
            if (mLoginMethod != null)
                if (mLoginMethod.equalsIgnoreCase("facebook")) {
                    JSONParser parser = new JSONParser();
                    Object object = null;
                    try {
                        object = parser.parse(mProfile);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (object != null) {
                        JSONObject jobj = (JSONObject) object;
                        int num = jobj.keySet().size();

                        String value = null;

                        value = (String) jobj.get(idlabel);
                        if (value != null) {
                            User.id = value;
                            editor.putString(idlabel, User.id);
                        }

                        if ((value = (String) jobj.get(namelabel)) != null) {
                            User.name = value;
                            editor.putString(namelabel, User.name);
                        }

                        if ((value = (String) jobj.get(agelabel)) != null) {
                            User.age = value;
                            editor.putString(agelabel, User.age);
                        }

                        if ((value = (String) jobj.get(emaillabel)) != null) {
                            User.email = value;
                            editor.putString(emaillabel, User.email);
                        }

                        if ((value = (String) jobj.get(genderlabel)) != null) {
                            User.gender = value;
                            editor.putString(genderlabel, User.gender);
                        }
                    }
                }
        }

        editor.commit(); //4
    }

    public void logUserIn() {
        getUser();
    }

    public void saveLogInInformation() {
        SharedPreferences settings;
        SharedPreferences.Editor editor;
        settings = mAct.getSharedPreferences(mPrefName, Context.MODE_PRIVATE); //1
        editor = settings.edit(); //2
        String idlabel = mAct.getString(R.string.id_lable);
        String namelabel = mAct.getString(R.string.name_lable);
        String agelabel = mAct.getString(R.string.age_lable);
        String emaillabel = mAct.getString(R.string.email_lable);
        String genderlabel = mAct.getString(R.string.gender_label);

        editor.putString(idlabel, User.id);
        editor.putString(namelabel, User.name);
        editor.putString(agelabel, User.age);
        editor.putString(emaillabel, User.email);
        editor.putString(genderlabel, User.gender);
        editor.commit();

    }
}
