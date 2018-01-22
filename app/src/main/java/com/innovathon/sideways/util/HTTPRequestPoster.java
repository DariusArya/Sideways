package com.innovathon.sideways.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HTTPRequestPoster {
    private static HttpURLConnection urlc;

    public static InputStream sendGetRequest(String endpoint, String requestParameters) throws Exception {
        return sendGetRequest(endpoint, requestParameters, null, -1);
    }

    public static StringBuffer sendGetRequestReturnString(String endpoint, String requestParameters) throws Exception {
        InputStream is = sendGetRequest(endpoint, requestParameters);
        return getString(is);
    }

    private static StringBuffer getString(InputStream is) throws Exception {
        StringBuffer sb = new StringBuffer();
        try {
            byte[] bs = new byte[30];
            int num = -1;
            while ((num = is.read(bs)) > 0) {
                for (byte b : bs)
                    sb.append((char) b);
            }
        } catch (Exception e) {
            throw e;
        }

        return sb;
    }

    /**
     * Sends an HTTP GET request to a url
     *
     * @param endpoint          - The URL of the server. (Example: " http://www.yahoo.com/search")
     * @param requestParameters - all the request parameters (Example: "param1=val1&param2=val2"). Note: This method will add the question mark (?) to the request - DO NOT add it yourself
     * @return - The response from the end point
     * @throws Exception
     */
    public static InputStream sendGetRequest(String endpoint, String requestParameters, String proxyserver, int port) throws Exception {
        try {
            String urlStr = endpoint;
            if (requestParameters != null && requestParameters.length() > 0)
                urlStr += "?q=" + requestParameters;

            URL url = new URL(urlStr);
            URLConnection conn = null;
            Proxy proxy = null;
            if (proxyserver != null)
                proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyserver, port));

            if (urlStr.startsWith("https://"))
                conn = (proxy == null ? (HttpsURLConnection) url.openConnection() : (HttpsURLConnection) url.openConnection(proxy));

            if (urlStr.startsWith("http://"))
                conn = (proxy == null ? url.openConnection() : url.openConnection(proxy));

            try {
                return conn.getInputStream();
            } catch (javax.net.ssl.SSLHandshakeException e) {
                String hostname = endpoint;
                hostname = hostname.substring("https://".length());
                if (hostname.contains("/"))
                    hostname = hostname.substring(0, hostname.indexOf("/"));
//                hostname = hostname + ":80";
                String[] str = new String[1];
//                hostname = "https://" + hostname;
                str[0] = hostname;
                if (proxyserver != null) {
                    System.setProperty("http.proxyHost", proxyserver);
                    System.setProperty("http.proxyPort", port + "");
                }

                HttpsURLConnection conns = MyHttpsClient.getHttpsUrlConnection(endpoint);

                return conns.getInputStream();
            }


        } catch (Exception e) {
            throw e;
        }


    }


    private static SSLContext getSSLContext() throws Exception {
        final SSLContext sslContext = SSLContext.getInstance("SSL");
        // Create a trust manager that does not validate certificate chains
        X509TrustManager trustManager = new X509TrustManager() {

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
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

    public static StringBuffer sendGetRequestReturnString(String endpoint, String requestParameters, String proxyserver, int port) throws Exception {
        return getString(sendGetRequest(endpoint, requestParameters, proxyserver, port));
    }

    /**
     * Reads data from the data reader and posts it to a server via POST request.
     * data - The data you want to send
     * endpoint - The server's address
     * output - writes the server's response to output
     *
     * @throws Exception
     */
    public static void postData(Reader data, String filename, String contenttype, URL endpoint, PrintStream out) throws Exception {
        HttpURLConnection urlc = null;
        try {
            postData(data, filename, endpoint, contenttype);
            displayServersResponse(out);
        } catch (IOException e) {
            throw new Exception("Connection error (is server running at " + endpoint + " ?): " + e);
        } finally {
            if (urlc != null)
                urlc.disconnect();
        }
    }

    /**
     * Reads data from the data reader and posts it to a server via POST request.
     * data - The data you want to send
     * endpoint - The server's address
     * output - writes the server's response to output
     *
     * @throws Exception
     */
    public static void postData(Reader data, String filename, URL endpoint, String contenttype) throws Exception {
        try {
            urlc = (HttpURLConnection) endpoint.openConnection();
            try {
                urlc.setRequestMethod("POST");
            } catch (ProtocolException e) {
                throw new Exception("Shouldn't happen: HttpURLConnection doesn't support POST??", e);
            }

            String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
            String CRLF = "\r\n"; // Line separator required by multipart/form-data.

            urlc.setDoOutput(true);
            urlc.setDoInput(true);
            urlc.setUseCaches(false);
            urlc.setAllowUserInteraction(false);
            urlc.setRequestProperty("Content-type", "text/xml; charset=" + "UTF-8");

            if (contenttype.equals("multipart/form-data"))
                urlc.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            OutputStream out = urlc.getOutputStream();
            String charset = "UTF-8";
            Writer writer = null;
            try {
                writer = new PrintWriter(new OutputStreamWriter(out, charset), true); // true = autoFlush, important!
                writer.append("--" + boundary).append(CRLF);
                writer.append("Content-Disposition: form-data; name=\"textFile\"; filename=\"" + filename + "\"").append(CRLF);
                writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
                writer.append(CRLF).flush();
                write(data, writer, CRLF);
                // End of multipart/form-data.
                writer.append("--" + boundary + "--").append(CRLF);
            } catch (IOException e) {
                throw new Exception("IOException while posting data", e);
            } finally {
                if (writer != null)
                    writer.close();
                if (out != null)
                    out.close();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private static void write(Reader reader, Writer writer, String CRLF) throws IOException {
        BufferedReader breader = new BufferedReader(reader);
        for (String line; (line = breader.readLine()) != null; ) {
            writer.append(line).append(CRLF);
        }
        writer.flush();
        if (breader != null)
            breader.close();

    }


    /**
     * Pipes everything from the reader to the writer via a buffer
     */
    private static void pipe(Reader reader, Writer writer) throws IOException {
        char[] buf = new char[1024];
        int read = 0;
        while ((read = reader.read(buf)) >= 0) {
            writer.write(buf, 0, read);
        }
        writer.flush();
    }

    private static void displayServersResponse(PrintStream out) throws Exception {
        InputStream in = null;
        try {
            in = urlc.getInputStream();
            Reader reader = new InputStreamReader(in);
            char[] cha = new char[1];
            while (reader.read(cha) > 0)
                out.print(cha[0]);
            reader.close();
        } catch (IOException e) {
            throw new Exception("IOException while reading response", e);
        } finally {
            if (in != null)
                in.close();
        }
    }

}



 
