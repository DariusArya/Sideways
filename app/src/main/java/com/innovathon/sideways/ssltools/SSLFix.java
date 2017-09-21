package com.innovathon.sideways.ssltools;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by Payman on 8/18/2016.
 */
public class SSLFix
{
    public static boolean removeSSLv3()
    {
        SSLContext sslcontext = null;
        try
        {
            sslcontext = SSLContext.getInstance("TLSv1");
            sslcontext.init(null,null,null);
            SSLSocketFactory NoSSLv3Factory = new NoSSLv3SocketFactory(sslcontext.getSocketFactory());
            HttpsURLConnection.setDefaultSSLSocketFactory(NoSSLv3Factory);
            return true;
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
            return false;
        }
        catch (KeyManagementException e)
        {
            e.printStackTrace();
            return false;
        }


    }

    public static void trustAllCertificateAuthorities()
    {// Install the all-trusting trust manager

        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager()
        {
            @Override
            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException
            {

            }

            @Override
            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException
            {

            }

            public java.security.cert.X509Certificate[] getAcceptedIssuers()
            {
                return new java.security.cert.X509Certificate[] {};
            }


        } };
        try
        {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
