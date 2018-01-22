package com.innovathon.sideways.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.innovathon.sideways.R;
import com.innovathon.sideways.main.MainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class RegisterPanel extends ActivitySendingInfo implements LoaderCallbacks<Cursor>
{

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]
{
            "foo@example.com:hello", "bar@example.com:world"
    };
    int state = 0;
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private String mPhoneNumber;
    private String mPrefName;
    private String congratmessageForEmailVerification;
    private String mEmailaddress;
    private String congratmessageForSendingEmailForVerification;
    private String congratmessageForCompletionOfRegistration;
    private String mPassword;
    private Activity mAct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAct = this;
        setContentView(R.layout.activity_register_panel);
        mPrefName = getString(R.string.app_name);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPhoneNumber = getIntent().getStringExtra(getString(R.string.phonenumber));
        mPasswordView = (EditText) findViewById(R.id.password);
//        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener()
//        {
//            @Override
//            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent)
//            {
//                if (id == R.id.login || id == EditorInfo.IME_NULL)
//                {
//                    attemptLogin();
//                    return true;
//                }
//                return false;
//            }
//        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
{
                submit();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void submit()
    {
        if (state == 2)
            registerEmailPassword();

        if (state == 0)
            sendStuffInStartApp();
//            verifyEmailWithTempPassword();

        if (state == 1)
            checkTempPasswordAgainstSentPhrase();
    }

    private void sendStuffInStartApp()
    {
        HashMap<String, String> info = new HashMap<String, String>();
        AutoCompleteTextView textview = (AutoCompleteTextView) findViewById(R.id.email);
        String emailaddress = textview.getText().toString();
        User.phonenumber = mPhoneNumber;
        User.email = emailaddress;
        User.id = User.email + "#" + User.phonenumber;
        User.id_type = getResources().getString(R.string.app_name).toUpperCase();
        info.put(getString(R.string.phonenumber), mPhoneNumber);
        info.put("`" + getString(R.string.AL) + "`", "'" + User.id + '@' + User.id_type + "'");


        info.put(getString(R.string.email_lable), emailaddress);

        state = 4;
        congratmessageForCompletionOfRegistration = "Thank you very much, You are registered now. \n Let's go sideways.";

        String urlregister = getString(R.string.baseurl) + getString(R.string.registerphp);

        postInfo(info, urlregister, congratmessageForCompletionOfRegistration);

    }

    private void registerEmailPassword()
    {
        AutoCompleteTextView textview = (AutoCompleteTextView) findViewById(R.id.email);
        String emailaddress = textview.getText().toString();

        EditText passwordview = (EditText) findViewById(R.id.password);
        String password = passwordview.getText().toString();

        EditText passwordconfirmview = (EditText) findViewById(R.id.passwordconfirm);
        String passwordconfirm = passwordview.getText().toString();

        if (password == null || password.isEmpty())
        {
            prompt(this, "You have to choose a password");
            return;
        }

        if (password.trim().length() < 3)
        {
            prompt(this, "Your password should be at least 3 letters long");
            return;
        }

        if (passwordconfirm == null || passwordconfirm.isEmpty())
        {
            prompt(this, "Please confirm your chosen password, by repeating it in the lower window.");
            return;
        }

        if (!passwordconfirm.equals(password))
        {
            prompt(this, "Your second entry doesn't match the first one.");
            return;
        }

        HashMap<String, String> info = new HashMap<String, String>();

        info.put(getString(R.string.phonenumber), mPhoneNumber);
        info.put(getString(R.string.email_lable), emailaddress);
        info.put(getString(R.string.password), password);
        mPassword = password;
        congratmessageForCompletionOfRegistration = "Thank you very much, You are registered now. \n Let's get to know each other a little.";

        String urlregister = getString(R.string.baseurl) + getString(R.string.registerphp);

        postInfo(info, urlregister, congratmessageForCompletionOfRegistration);
    }


    private void verifyEmailWithTempPassword()
    {
        HashMap<String, String> info = new HashMap<String, String>();
        AutoCompleteTextView textview = (AutoCompleteTextView) findViewById(R.id.email);
        String emailaddress = textview.getText().toString();
        info.put("emailaddress", emailaddress);
        congratmessageForSendingEmailForVerification = "Please check your email and enter the temporary password below, then you can choose your \n" +
                "permanent password.";

        String verifyemail = getString(R.string.baseurl) + getString(R.string.verifyemail);

        postInfo(info, verifyemail, congratmessageForSendingEmailForVerification);
    }

    private void checkTempPasswordAgainstSentPhrase()
    {
        HashMap<String, String> info = new HashMap<String, String>();
        AutoCompleteTextView textview = (AutoCompleteTextView) findViewById(R.id.email);
        String emailaddress = textview.getText().toString();
        info.put(getString(R.string.email_lable), emailaddress);

        EditText passwordview = (EditText) findViewById(R.id.password);
        String password = passwordview.getText().toString();

        info.put("temppassword", password);
        congratmessageForEmailVerification = "Congratualtions ! Your email is now verified. Please enter your permanent password and confirm it.\nYou only have to do it once";

        String verifyemail = getString(R.string.baseurl) + getString(R.string.checktemppassword);

        postInfo(info, verifyemail, congratmessageForEmailVerification);
    }

    @Override
    public void doThisForMessage(String msg)
    {

        if (state == 4 && msg.trim().endsWith("success"))
        {
            AutoCompleteTextView textview = (AutoCompleteTextView) findViewById(R.id.email);
            mEmailaddress = textview.getText().toString();
            User.phonenumber = mPhoneNumber;
            User.email = mEmailaddress;
            registerUserWithSideways();
            launchSideways(true);
            return;
        }
//        msg.contains(congratmessageForSendingEmailForVerification
        if (state == 0 && msg.endsWith("success"))
        {
            EditText pw = (EditText) findViewById(R.id.password);
            pw.setVisibility(View.VISIBLE);
            pw.invalidate();
            state = 1;
            return;
        }

        if (state == 1 && msg.endsWith("true"))
        {
            AutoCompleteTextView textview = (AutoCompleteTextView) findViewById(R.id.email);
            mEmailaddress = textview.getText().toString();
            EditText pw = (EditText) findViewById(R.id.password);
            pw.setText(" ".subSequence(0, 1));
            TextInputLayout layoutUser;
            layoutUser = (TextInputLayout) findViewById(R.id.passwordlayout);
            layoutUser.setHint(getResources().getString(R.string.perm_password_hint));
            pw.setVisibility(View.VISIBLE);
            pw.invalidate();

            state = 2;
            EditText pw2 = (EditText) findViewById(R.id.passwordconfirm);
            pw2.setVisibility(View.VISIBLE);
            return;
        }

        if (state == 2 && msg.contains("success"))
        {
//            Intent intent = new Intent(RegisterPanel.this, LoginPanel.class);
//            intent.putExtra(getString(R.string.email_lable), mEmailaddress);
//            intent.putExtra(getString(R.string.password), mPassword);
//            intent.putExtra(getString(R.string.phonenumber), mPhoneNumber);
//
//            startActivity(intent);
        }


    }

    private void registerUserWithSideways()
    {
        User.id = User.email + "#" + User.phonenumber;
        User.id_type = getResources().getString(R.string.app_name).toUpperCase();

        UserManager userManager = UserManager.getTheOnlyUserManager();
        if (userManager == null)
            userManager = UserManager.getAUserManager(this);
        Intent data = new Intent();
        data.putExtra(getString(R.string.login_method_indicator_tag), getString(R.string.app_name));
        userManager.saveLogInInformation(data);
    }

    private void launchSideways(Boolean loggedin)
    {
        Intent intent = new Intent(this, MainActivity.class);
        if (loggedin == null)
            loggedin = false;
        intent.putExtra(getString(R.string.isloggedintag), loggedin);
        intent.putExtra("ALREADY_REGISTERED", true);

        startActivity(intent);
        finish();
    }

    private void populateAutoComplete()
    {
        if (!mayRequestContacts())
        {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts()
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS))
        {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener()
                    {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        if (requestCode == REQUEST_READ_CONTACTS)
        {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin()
    {
        if (mAuthTask != null)
        {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password))
        {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email))
        {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }
        else
            if (!isEmailValid(email))
            {
                mEmailView.setError(getString(R.string.error_invalid_email));
                focusView = mEmailView;
                cancel = true;
            }

        if (cancel)
        {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }
        else
        {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email)
    {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password)
    {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show)
    {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
        {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd(Animator animation)
                {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
    {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader)
    {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(RegisterPanel.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(" Do you want to cancel? ");
        builder.setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mAct.finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                }).show();
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try
            {
                // Simulate network access.
                Thread.sleep(2000);
            }
            catch (InterruptedException e)
            {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS)
            {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail))
                {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success)
        {
            mAuthTask = null;
            showProgress(false);

            if (success)
            {
                finish();
            }
            else
            {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled()
        {
            mAuthTask = null;
            showProgress(false);
        }
    }

}

