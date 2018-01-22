package com.innovathon.sideways.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.innovathon.sideways.R;
import com.innovathon.sideways.main.MainActivity;


public class UserRegistrationActivity extends Activity implements AdapterView.OnItemSelectedListener {


    private static final int USER_CHOSE_SIDEWAYS = 2000;
    private Context context;
    private Intent mResultIntent;
    private String mYearSelected;

    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        mYearSelected = parent.getItemAtPosition(pos).toString();
        User.yob = mYearSelected;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Toast.makeText(this, "You must select a year for your year of birth.", Toast.LENGTH_LONG);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_info_gui);
        context = this;
//        String myphonenumber =  getPhoneNumber();
//        TextView phonenumberview = (TextView) findViewById(R.id.phonenumberview);
//
//        String phonenumberlabel = getResources().getString(R.string.phonenumber);
//        phonenumberview.setText(phonenumberlabel+"    " + myphonenumber);

        findViewById(R.id.submission_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                User.yob = getUsersEntryForYoB();
                if (((CheckBox) findViewById(R.id.male_checkbox)).isChecked())
                    User.gender = "male";
                else
                    User.gender = "female";

//               String user_email_username = ((EditText) findViewById(R.id.user_name)).getText().toString();
//               String user_email_domain = ((EditText) findViewById(R.id.domain_address)).getText().toString();

//               String user_email = user_email_username.trim() + "@" + user_email_domain.trim();
                String user_email = getIntent().getStringExtra(getString(R.string.email_lable));
                User.email = user_email;

                User.phonenumber = getIntent().getStringExtra(getString(R.string.phonenumber));
                String phonenumberprefix = getResources().getString(R.string.phonenumber);
                if (User.phonenumber.startsWith(phonenumberprefix)) {
                    User.phonenumber = User.phonenumber.substring(phonenumberprefix.length()).trim();
                }
                String msg = "";
                if (User.email == null || !User.email.matches(".*?@.*?\\..*?")) {
                    msg = getResources().getString(R.string.avalidemailaddress);
                }

                if (!((CheckBox) findViewById(R.id.male_checkbox)).isChecked() && !((CheckBox) findViewById(R.id.female_checkbox)).isChecked()) {
                    msg += "\n" + getResources().getString(R.string.checkapptgenderbox);
                }
                String username = ((EditText) findViewById(R.id.mynameis)).getText().toString();
                if (username == null || username.isEmpty()) {
                    msg += "You should enter a name for yourself";
                } else
                    User.name = username;

                User.password = getIntent().getStringExtra(getString(R.string.password));

                if (msg.isEmpty()) {
                    registerUserWithSideways();

                } else {
                    alertUser(msg);
                }
            }
        });

        findViewById(R.id.male_checkbox).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((CheckBox) findViewById(R.id.female_checkbox)).setChecked(!((CheckBox) findViewById(R.id.male_checkbox)).isChecked());
            }
        });

        findViewById(R.id.female_checkbox).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((CheckBox) findViewById(R.id.male_checkbox)).setChecked(!((CheckBox) findViewById(R.id.female_checkbox)).isChecked());
            }
        });

        Spinner spinner = (Spinner) findViewById(R.id.year_of_birth);
// Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.valid_years_of_birth, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(this);

    }

    private void registerUserWithSideways() {
        User.id = User.email + "#" + User.phonenumber;
        User.id_type = getResources().getString(R.string.app_name).toUpperCase();

        UserManager userManager = UserManager.getTheOnlyUserManager();
        Intent data = new Intent();
        data.putExtra(getString(R.string.login_method_indicator_tag), getString(R.string.app_name));
        userManager.saveLogInInformation(data);
        mResultIntent = new Intent(UserRegistrationActivity.this, MainActivity.class);
        String login_method_indicator_tag = getResources().getString(R.string.login_method_indicator_tag);
        mResultIntent.putExtra(login_method_indicator_tag, User.id_type);
        startActivity(mResultIntent);
    }

    private void alertUser(String msg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        // set title
        alertDialogBuilder.setTitle("Insufficient Information");

        // set dialog message
        alertDialogBuilder
                .setMessage(msg)
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private String getUsersEntryForYoB() {
        return mYearSelected;
    }


}
