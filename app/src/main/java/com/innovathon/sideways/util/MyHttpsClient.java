package com.innovathon.sideways.util;

import android.util.Log;

import java.net.URL;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

@SuppressWarnings("deprecation")
public class MyHttpsClient {

    public static String Cert_Storage_Directory = null;

    public static HttpsURLConnection getHttpsUrlConnection(String endpoint) {
        try {
            SSLContext sslcontext = getSSLContext();


            // Tell the URLConnection to use a SocketFactory from our SSLContext
            URL url = new URL(endpoint);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setSSLSocketFactory(sslcontext.getSocketFactory());
            return urlConnection;
        } catch (Exception e) {
            Log.e("Wihapp", "Can't get a HttpsUrlConnection", e);
            return null;
        }


    }

    private static SSLContext getSSLContext() throws Exception {
        final SSLContext sslContext = SSLContext.getInstance("SSL");
        // Create a trust manager that does not validate certificate chains
        X509TrustManager trustManager = new X509TrustManager() {

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

        };
        final TrustManager[] trustAllCerts = new TrustManager[1];
        trustAllCerts[0] = trustManager;
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        return sslContext;
    }

}
