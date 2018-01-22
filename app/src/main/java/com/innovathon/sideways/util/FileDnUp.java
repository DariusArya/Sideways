package com.innovathon.sideways.util;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

public class FileDnUp {
    static String proxyserver = null;
    static int port = -1;


    public static String getProxyserver() {
        return proxyserver;
    }

    public static void setProxyserver(String proxyserver_) {
        proxyserver = proxyserver_;
    }

    public static int getPort() {
        return port;
    }

    public static void setPort(int port_) {
        port = port_;
    }


    /**
     * Downloading one file from <code><em>from</em></code> url to  <code><em>to file</em></code>
     *
     * @param from the url to request the file from
     * @param to   the file name to save the file as
     * @throws IOException
     */
    public static void downloadFile(String from, String to, String proxyserver, int port, Context context) throws IOException {
        InputStream is;
        try {

            if (proxyserver == null)
                is = HTTPRequestPoster.sendGetRequest(from, null);
            else
                is = HTTPRequestPoster.sendGetRequest(from, null, proxyserver, port);

            File toFile = new File(to);
            FileOutputStream toFileStream = new FileOutputStream(toFile);

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = is.read(buffer)) != -1)
                toFileStream.write(buffer, 0, bytesRead); // write

            toFileStream.close();
        } catch (Exception e) {

        }
    }

    public static boolean downloadFile(String from, String to) throws IOException {
        InputStream is;
        try {
            is = HTTPRequestPoster.sendGetRequest(from, null);
            File toFile = new File(to);
            FileOutputStream toFileStream = new FileOutputStream(toFile);

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = is.read(buffer)) != -1)
                toFileStream.write(buffer, 0, bytesRead); // write

            toFileStream.close();
            return true;
        } catch (Exception e) {
            System.out.print(e.getMessage());
            return false;
        }


    }

    public static String getRemoteText(String from) throws IOException {
        InputStream is;
        try {
            is = HTTPRequestPoster.sendGetRequest(from, null);
            byte[] buffer = new byte[4096];
            int bytesRead = -1;

            StringBuffer buf = new StringBuffer();

            while ((bytesRead = is.read(buffer)) != -1) {
                for (byte b : buffer) {
                    buf.append((char) b);
                }
            }

            return buf.toString();
        } catch (Exception e) {
//			System.out.print(e.getMessage());
            return null;
        }


    }

    public static String getRemoteChars(String from, int num) throws IOException {
        InputStream is;
        try {
            is = HTTPRequestPoster.sendGetRequest(from, null);
            byte[] buffer = new byte[4096];

            int bytesRead = -1;

            StringBuffer buf = new StringBuffer();
            int count = 0;
            while ((bytesRead = is.read(buffer)) != -1) {
                for (byte b : buffer) {
                    buf.append((char) b);
                    count++;
                    if (count == num)
                        break;
                }

                if (count == num)
                    break;
            }

            return buf.toString();
        } catch (Exception e) {
            System.out.print(e.getMessage());
            return null;
        }


    }

    private static void waitABit(int i) {
        try {
            Thread.sleep(i);
        } catch (Exception e) {

        }
    }

    public static void downloadFiles(String[] from, String[] to) throws IOException {
        if (from.length != to.length)
            for (String f : from)
                for (String t : to)
                    downloadFile(f, t);
        else {
            int k = 0;
            for (String f : from)
                downloadFile(f, to[k++]);
        }

    }

    public static void uploadFile(String file, String urldest) throws Exception {
        Reader data = new FileReader(new File(file));
        URL endpoint = new URL(urldest);
        HTTPRequestPoster.postData(data, file, "multipart/form-data", endpoint, System.out);
    }

    public static void setProxyserverPort(String addr, int p) {
        setProxyserver(addr);
        setPort(p);

    }

}
