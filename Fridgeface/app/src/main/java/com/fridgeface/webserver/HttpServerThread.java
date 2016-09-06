package com.fridgeface.webserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import android.util.Log;
import android.webkit.MimeTypeMap;

import cz.msebera.android.httpclient.HttpException;
import cz.msebera.android.httpclient.HttpRequest;
import cz.msebera.android.httpclient.HttpVersion;
import cz.msebera.android.httpclient.entity.InputStreamEntity;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.DefaultBHttpServerConnection;
import cz.msebera.android.httpclient.message.BasicHttpResponse;

/*package */ class HttpServerThread extends Thread {
    private static final String TAG = HttpServerThread.class.getName();

    private static final int BUFFER_SIZE = 256000;
    final SocketListenerService mService;
    final DefaultBHttpServerConnection mConnection;

    HttpServerThread(SocketListenerService service, Socket socket) throws IOException {
        this.mService = service;
        this.mConnection = new DefaultBHttpServerConnection(BUFFER_SIZE);
        this.mConnection.bind(socket);
    }

    private BasicHttpResponse onGet(HttpRequest request) {
        String uri = request.getRequestLine().getUri();
        if(uri.endsWith("/")) {
            uri = uri + "index.html";
        }

        BasicHttpResponse response;
        try {
            InputStream is = this.mService.getAssets().open("htdocs" + uri);
            response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
            String e = MimeTypeMap.getFileExtensionFromUrl(uri);
            String contentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(e);
            if(contentType == null) {
                if(e.equalsIgnoreCase("js")) {
                    contentType = "text/javascript";
                } else if(!e.equalsIgnoreCase("html") && !e.equalsIgnoreCase("htm")) {
                    if(e.equalsIgnoreCase("css")) {
                        contentType = "text/css";
                    } else {
                        contentType = "text/plain";
                    }
                } else {
                    contentType = "text/html";
                }
            }

            response.addHeader("Content-Type", contentType);
            response.setEntity(new InputStreamEntity(is, 256000L));
        } catch (IOException e) {
            Log.e(TAG, "IO Exception", e);
            response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 404, "Not Found");

            try {
                response.addHeader("Content-Type", "text/html");
                response.setEntity(new StringEntity("404 - Not Found"));
            } catch (UnsupportedEncodingException ignored) {

            }
        }

        return response;
    }

    protected BasicHttpResponse onPost(HttpRequest request) {
        return this.onGet(request);
    }

    public void run() {
        BasicHttpResponse response = null;

        try {
            HttpRequest e = this.mConnection.receiveRequestHeader();
            String method = e.getRequestLine().getMethod();
            if(method.equalsIgnoreCase("get")) {
                response = this.onGet(e);
            } else if(method.equalsIgnoreCase("post")) {
                response = this.onPost(e);
            }

            if(response != null) {
                this.mConnection.sendResponseHeader(response);
                this.mConnection.sendResponseEntity(response);
                this.mConnection.flush();
                this.mConnection.shutdown();
            }
        } catch (HttpException var4) {
            Log.e(TAG, "HTTP Exception", var4);
        } catch (IOException var5) {
            Log.e(TAG, "IO Exception", var5);
        }

    }
}
