package com.innovathon.sideways.main;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.innovathon.sideways.R;

public class UserCommentActivity extends AppCompatActivity
{
    Activity mThisAct ;
    String result_tag = null;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_comment);
        mThisAct = this;
        result_tag = getString(R.string.user_comment_tag);
        View okbutton = (View) findViewById(R.id.usercommentok);
        Intent ucomment_intent = getIntent();
        if (ucomment_intent.getExtras() != null)
        {
            String usersprevcomments = ucomment_intent.getExtras().getString(getResources().getString(R.string.users_previous_comments));
            if (usersprevcomments != null && !usersprevcomments.isEmpty())
            {
                ((EditText) findViewById(R.id.usercommenttext)).setText(usersprevcomments);
            }
        }
        okbutton.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                View ucomtextview = findViewById(R.id.usercommenttext);
                String ucomtext = "";
                if (ucomtextview instanceof EditText)
                    ucomtext  = ((EditText)ucomtextview).getText().toString();
                if (ucomtextview instanceof TextView)
                    ucomtext  = ((TextView)ucomtextview).getText().toString();

                Intent resultIntent = new Intent();
                resultIntent.putExtra(result_tag, ucomtext);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();

            }
        });

        View cancelbutton = findViewById(R.id.usercommentcancel);
        cancelbutton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    }


}
