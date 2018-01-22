package com.innovathon.sideways.util;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.innovathon.sideways.R;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class WebAct extends Activity {


    static protected String mUrl;
    static WebViewClient mWebViewClient = null;
    private WebView mWebView;

    public WebAct() {

    }

    public WebAct(String url) {
        mUrl = url;
    }

    public static void setUrl(String url) {
        mUrl = url;
    }

    public static void setWebViewClient(Browser webviewclient) {
        mWebViewClient = webviewclient;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getExtras() != null) {
                mUrl = intent.getStringExtra("URL");
            }
        } else {
            if (savedInstanceState != null) {
                mUrl = savedInstanceState.getString("URL");
            }
        }
        setContentView(R.layout.activity_web);
        mWebView = (WebView) findViewById(R.id.web_view);
        if (mWebViewClient instanceof Browser) {
            ((Browser) mWebViewClient).setWebAct(this);
        }
        if (mUrl != null) {
            mWebView.setWebChromeClient(new WebChromeClient());
            mWebView.setWebViewClient(mWebViewClient);
            mWebView.getSettings().setLoadsImagesAutomatically(true);
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
            mWebView.loadUrl(mUrl);
        }


    }
}
