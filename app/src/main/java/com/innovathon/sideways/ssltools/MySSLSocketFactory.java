package com.innovathon.sideways.ssltools;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by Payman on 8/18/2016.
 */
public class MySSLSocketFactory extends SSLSocketFactory
{

    SSLContext sslContext = SSLContext.getInstance("TLS");

    public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException
    {
           TrustManager tm = new X509TrustManager()
           {
               public void checkClientTrusted(X509Certificate[] chain, String authType)
               {
               }

               public void checkServerTrusted(X509Certificate[] chain, String authType)
               {
               }

               public X509Certificate[] getAcceptedIssuers()
               {
                  return null;
               }
           };

        sslContext.init(null, new TrustManager[] { tm }, null);
    }

    @Override
    public String[] getDefaultCipherSuites()
    {
        return new String[0];
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return new String[0];
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException
    {
        return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
    }

    @Override
    public Socket createSocket() throws IOException
    {
        return sslContext.getSocketFactory().createSocket();
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException
    {
        return null;
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException
    {
       return null;
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException
    {
        return null;
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException
    {
        return null;
    }
}