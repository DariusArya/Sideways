package com.innovathon.sideways.util;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.webkit.WebView;

import com.github.scribejava.apis.GoogleApi20;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.innovathon.sideways.R;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Payman & Nahid on 2/12/2017.
 */

public class GoogleAuth
{
    private final String clientId, clientSecret;
    private final OAuth20Service service;
    private final String mAuthorizationUrl;
    public String authurl = null;
    private static Activity mMainActivity;

    public void login()
    {
        GoogleAuthWebViewClient webviewclient = new GoogleAuthWebViewClient();
        WebAct.setWebViewClient(webviewclient);
        Intent googleLogInIntent = new Intent(mMainActivity, WebAct.class);
        googleLogInIntent.putExtra("URL", mAuthorizationUrl);
        System.out.println("ready to launch");
        mMainActivity.startActivityForResult(googleLogInIntent, mMainActivity.getResources().getInteger(R.integer.rescodegauthcalled));
    }

    public class GoogleAuthWebViewClient extends Browser
    {
        @Override
        protected boolean overrideUrlLoading(WebView webview, String url)
        {
            String regex = "code=(.*)";
            String[] code = new String[1];
            Matcher m = Pattern.compile(regex).matcher(url);
            if (m.find())
            {
                code[0] = m.group(1);
                googleloginOauthHelper(code, service);
                return true;
            }
            else
            {
                webview.loadUrl(url);
                return true;
            }
        }
    }


//
    public GoogleAuth(Activity mAct)
    {
        mMainActivity = mAct;
        clientId = mMainActivity.getString(R.string.google_client_id);
        clientSecret = mMainActivity.getString(R.string.google_client_secret);
        String secretState = "secret" + new Random().nextInt(999_999);
        service = new ServiceBuilder()
                .apiKey(clientId)
                .apiSecret(clientSecret)
                .scope("profile") // replace with desired scope
                .state(secretState)
                .callback("http://localhost:20000/")
                .build(GoogleApi20.instance());
        final Map<String, String> additionalParams = new HashMap<>();
        additionalParams.put("access_type", "offline");
        //force to reget refresh token (if usera are asked not the first time)
        additionalParams.put("prompt", "consent");
        mAuthorizationUrl = service.getAuthorizationUrl(additionalParams);
    }
    private void googleloginOauthHelper(String[] codecontainer, final OAuth20Service service)
    {
        final String code = codecontainer[0];
        final Response[] responseContainer = {null};
        AsyncTask<?,?,?> googleloginprocess = new AsyncTask<Object, Object, Object>()
        {
            @Override
            protected Object doInBackground(Object... params)
            {
                try
                {
                    final OAuth2AccessToken accessToken = service.getAccessToken(code);
                    System.out.println("Got the Access Token!");
                    System.out.println("(if your curious it looks like this: " + accessToken
                            + ", 'rawResponse'='" + accessToken.getRawResponse() + "')");
                    System.out.println();

                    // Now let's go and ask for a protected resource!
                    System.out.println("Now we're going to access a protected resource...");
                    final OAuthRequest request = new OAuthRequest(Verb.GET, mMainActivity.getString(R.string.google_protected_res_url));
                    service.signRequest(accessToken, request);
                    final Response response = service.execute(request);
                    responseContainer[0] = response;
                    return response;

                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    return e;
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                    return e;
                }
                catch (ExecutionException e)
                {
                    e.printStackTrace();
                    return e;
                }
            }
        };

        googleloginprocess.execute();

    }

}
