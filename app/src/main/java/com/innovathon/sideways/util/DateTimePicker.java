package com.innovathon.sideways.util;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.innovathon.sideways.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimePicker extends AppCompatActivity
{

    View mTimePicker;
    CalendarView mDatePicker;
    TextView mBack2Date ;
    final static private int CHOOSING_DATE = 1;
    final static private int CHOOSING_TIME = 2;
    Activity thisact;

    int mState = CHOOSING_DATE;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_time_picker);

        thisact = this;

        mTimePicker = findViewById(R.id.timePicker);
        if (mTimePicker != null)
        {
            mTimePicker.setVisibility(View.INVISIBLE);
        }

        mBack2Date = (TextView) findViewById(R.id.backtodate);
        if (mBack2Date != null)
        {
            mBack2Date.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    mTimePicker.setVisibility(View.INVISIBLE);
                    mBack2Date.setVisibility(View.INVISIBLE);
                    TextView donebut = (TextView) findViewById(R.id.donedatebut);
                    donebut.setText(thisact.getString(R.string.donedatebuttext));
                }
            });
            mBack2Date.setVisibility(View.INVISIBLE);
        }

        mDatePicker = (CalendarView) findViewById(R.id.calendarView) ;
        initializeCalendar();
//        Calendar c = Calendar.getInstance();
//        c.setTimeInMillis(System.currentTimeMillis());
//        mDatePicker.setDate(System.currentTimeMillis(), true, true);
        View donebut =  findViewById(R.id.donedatebut);
        if (donebut != null)
            donebut.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                  if (mState == CHOOSING_DATE)
                      handleDoneDateButton();
                   if (mState == CHOOSING_TIME)
                      handleDone();
                }
            });
    }

    private void handleDone()
    {
        finish();
    }

    String mDateOfEvent, mTimeOfEvent;

    public void handleDoneDateButton()
    {
        String strDate = getSelectedDate();
        EditText doe = (EditText) findViewById(R.id.date_of_event);
        doe.setText(strDate);

        if (mTimePicker != null)
        {
            mTimePicker.setVisibility(View.VISIBLE);
        }

        if (mBack2Date != null)
        {
            mBack2Date.setVisibility(View.VISIBLE);
        }


        mState = CHOOSING_TIME;

        TextView donebut = (TextView) findViewById(R.id.donedatebut);
        donebut.setText("Done.");



    }

    private String getSelectedDate()
    {
        long selecteddate = mDatePicker.getDate();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(selecteddate);
        Date d  = c.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM/dd/YYYY", Locale.US);
        sdf.setCalendar(c);
        String strDate = sdf.format(d);
        return strDate;
    }

    public void initializeCalendar()
    {

        // sets whether to show the week number.
        mDatePicker.setShowWeekNumber(false);

        // sets the first day of week according to Calendar.
        // here we set Monday as the first day of the Calendar
        mDatePicker.setFirstDayOfWeek(1);

        mDatePicker.setFocusable(true);
        mDatePicker.setFocusableInTouchMode(true);
        mDatePicker.findFocus();

//        //The background color for the selected week.
//        mDatePicker.setSelectedWeekBackgroundColor(getResources().getColor(R.color.green));
//
//        //sets the color for the dates of an unfocused month.
//        mDatePicker.setUnfocusedMonthDateColor(getResources().getColor(R.color.transparent));
//
//        //sets the color for the separator line between weeks.
//        mDatePicker.setWeekSeparatorLineColor(getResources().getColor(R.color.transparent));
//
//        //sets the color for the vertical bar shown at the beginning and at the end of the selected date.
//        mDatePicker.setSelectedDateVerticalBar(R.color.darkgreen);

        //sets the listener to be notified upon selected date change.
        mDatePicker.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            //show the selected date as a toast
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int day)
            {
                Toast.makeText(getApplicationContext(), day + "/" + (month+1) + "/" + year, Toast.LENGTH_LONG).show();
            }
        });

       
    }

}
