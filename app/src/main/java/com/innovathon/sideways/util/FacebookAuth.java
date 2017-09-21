package com.innovathon.sideways.util;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.webkit.WebView;

import com.github.scribejava.apis.FacebookApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.extractors.OAuth2AccessTokenJsonExtractor;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.innovathon.sideways.R;
import com.innovathon.sideways.util.ssltools.NoSSLv3SocketFactory;
import com.innovathon.sideways.util.ssltools.SSLFix;

import net.smartam.leeloo.client.OAuthClient;
import net.smartam.leeloo.client.URLConnectionClient;
import net.smartam.leeloo.client.request.OAuthClientRequest;
import net.smartam.leeloo.client.response.OAuthJSONAccessTokenResponse;
import net.smartam.leeloo.common.exception.OAuthProblemException;
import net.smartam.leeloo.common.exception.OAuthSystemException;
import net.smartam.leeloo.common.message.types.GrantType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Payman & Nahid on 2/26/2017.
 */

public class FacebookAuth
{
    private final String app_id;
    private final String app_secret;

    private static String redirect_url ;
    private final Activity mMainActivity;
    private String mAuthorizationUrl;
    private OAuth2AccessToken mFacebookAccesstoken = null;
    private static final String PROTECTED_RESOURCE_URL = "https://graph.facebook.com/v2.8/me?fields=id,name,gender,email,age_range";
    private Intent mResultIntent;

    public FacebookAuth(Activity mAct)
    {
        mMainActivity = mAct;
        this.app_id = mMainActivity.getString(R.string.facebook_app_id);
        this.app_secret = mMainActivity.getString(R.string.facebook_app_secret);
        redirect_url = mMainActivity.getString(R.string.facebook_redirect_url);
        mAuthorizationUrl = mMainActivity.getString(R.string.facebook_login);
        mAuthorizationUrl = mAuthorizationUrl.replace("{app-id}",app_id).replace("{redirect-uri}",redirect_url);
    }

    public class FacebookAuthWebViewClient extends Browser
    {
        WebAct mWebAct;

        @Override
        public void setWebAct(WebAct webAct)
        {
            mWebAct = webAct;
        }

        @Override
        protected boolean overrideUrlLoading(WebView webview, String url)
        {
            String regex = "code=(.*)";
            String[] code = new String[1];
            Matcher m = Pattern.compile(regex).matcher(url);
            if (m.find())
            {
                code[0] = m.group(1);
                facebookloginOauthHelper(code, mWebAct);
                return true;
            }
            else
            {
                webview.loadUrl(url);
                return true;
            }
        }
    }

    private void facebookloginOauthHelper(final String[] codecontainer, final WebAct webAct)
    {

        AsyncTask<?,?,?> facebookloginprocess = new AsyncTask<Object, Object, Object>()
        {
            @Override
            protected Object doInBackground(Object... params)
            {
                try
                {
                    getAccessTokenFromCode(codecontainer[0]);
                    webAct.setResult(Activity.RESULT_OK, mResultIntent);
                    webAct.finish();
                }
                catch (OAuthProblemException e)
                {
                    e.printStackTrace();
                    webAct.setResult(Activity.RESULT_CANCELED, null);
                    webAct.finish();
                }
                catch (OAuthSystemException e)
                {
                    e.printStackTrace();
                    webAct.setResult(Activity.RESULT_CANCELED, null);
                    webAct.finish();
                }
                return null;
            }
        };

        HttpsURLConnection.setDefaultSSLSocketFactory(new NoSSLv3SocketFactory());
        SSLFix.trustAllCertificateAuthorities();
        facebookloginprocess.execute();
    }

    private void getPublicProfile()
    {
        final String secretState = "secret" + new Random().nextInt(999_999);
        final OAuth20Service service = new ServiceBuilder()
                .apiKey(app_id)
                .apiSecret(app_secret)
                .state(secretState)
                .callback(redirect_url)
                .build(FacebookApi.instance());
    }

    private void getAccessTokenFromCode(String code) throws OAuthSystemException, OAuthProblemException
    {
        OAuthClientRequest request = null;
          String urltoken = "https://graph.facebook.com/v2.8/oauth/access_token?" ;
        request = OAuthClientRequest.tokenLocation(urltoken)
                    .setGrantType(GrantType.AUTHORIZATION_CODE)
                    .setClientId(app_id)
                    .setClientSecret(app_secret)
                    .setRedirectURI(redirect_url)
                    .setCode(code)
                    .buildBodyMessage();

        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

        OAuthJSONAccessTokenResponse response = oAuthClient.accessToken(request);

        OAuth2AccessTokenJsonExtractor accesstokenextractor =  OAuth2AccessTokenJsonExtractor.instance();

        HashMap<String,String> headers = new HashMap<String,String>();
        String expiresin = response.getExpiresIn();
        String scope = response.getScope();
        String access_token = response.getAccessToken();
        String refresh_token = response.getRefreshToken();

        headers.put("access_token",access_token);
        headers.put("refresh_token",refresh_token);
        headers.put("expires_in",expiresin);
        headers.put("scope",scope);

        Response oauth_response = new Response(200,null, headers,response.getBody());
        try
        {
            mFacebookAccesstoken = accesstokenextractor.extract(oauth_response);
            final OAuthRequest request_for = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
            final String secretState = "secret" + new Random().nextInt(999_999);
            final OAuth20Service service = new ServiceBuilder()
                    .apiKey(app_id)
                    .apiSecret(app_secret)
                    .state(secretState)
                    .callback("http://localhost:10000/")
                    .build(FacebookApi.instance());

            service.signRequest(mFacebookAccesstoken, request_for);

            final Response newresponse = service.execute(request_for);

            mResultIntent = new Intent();
            String login_method_indicator_tag = mMainActivity.getResources().getString(R.string.login_method_indicator_tag);
            mResultIntent.putExtra(login_method_indicator_tag,"FACEBOOK");
            String result_tag = mMainActivity.getResources().getString(R.string.from_auth_to_launcher_acc_token);
            mResultIntent.putExtra(result_tag, access_token);
            result_tag = mMainActivity.getResources().getString(R.string.from_auth_to_launcher_secret);
            mResultIntent.putExtra(result_tag, mFacebookAccesstoken.getRawResponse());
            result_tag = mMainActivity.getResources().getString(R.string.init_fb_profile_resp);
            mResultIntent.putExtra(result_tag,newresponse.getBody());
//                                request = new OAuthRequest(Verb.GET, facebookurl+"gender");
//                                service.signRequest(mAccessToken, request);
//                                String genderresp = request.send().getBody();

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
        }


    }
    public void login()
    {
        FacebookAuthWebViewClient webviewclient = new FacebookAuthWebViewClient();
        WebAct.setWebViewClient(webviewclient);
        Intent facebookLogInIntent = new Intent(mMainActivity, WebAct.class);
        facebookLogInIntent.putExtra("URL", mAuthorizationUrl);
        System.out.println("ready to launch");
        mMainActivity.startActivityForResult(facebookLogInIntent, mMainActivity.getResources().getInteger(R.integer.rescodegauthcalled));
    }


}
