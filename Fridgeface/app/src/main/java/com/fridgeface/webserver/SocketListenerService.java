package com.fridgeface.webserver;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.fridgeface.R;
import com.fridgeface.utils.LogHelper;

import cz.msebera.android.httpclient.conn.util.InetAddressUtils;

@SuppressWarnings("unused")
public class SocketListenerService extends Service {
    private static final String TAG = SocketListenerService.class.getName();
    public static final String COMMAND_START_SERVER = "com.fridgeface.webserver.COMMAND_START_SERVER";
    public static final String COMMAND_STOP_SERVER = "com.fridgeface.webserver.COMMAND_STOP_SERVER";
    public static final String ACTION_SERVER_STARTED = "com.fridgeface.webserver.ACTION_SERVER_STARTED";
    public static final String ACTION_SERVER_STOPPED = "com.fridgeface.webserver.ACTION_SERVER_STOPPED";
    public static final String EXTRA_COMMAND = "command";
    public static final String EXTRA_NOTIFICATION = "notification";
    public static final String EXTRA_REQUEST_HANDLER = "request_handler";
    public static final String EXTRA_PORT = "port";
    public static final String EXTRA_HTTPS = "https";
    public static final String EXTRA_PACKAGE = "package";
    public static final String CONNECTION_TYPE_NOT_CONNECTED = "not_connected";
    public static final String CONNECTION_TYPE_CELLULAR = "cellular";
    public static final String CONNECTION_TYPE_WIFI = "wifi";
    private String mIp;
    private int mPort = 6789;
    private boolean mHttps = false;
    private boolean mIsStarting = false;
    private ServerSocket mListener = null;
    private Thread mListenerThread = null;
    public BroadcastReceiver networkStateReceiver;
    private Class<HttpServerThread> mRequestHandler = HttpServerThread.class;
    private Notification mNotification = null;

    public SocketListenerService() {
    }

    public void onCreate() {
        super.onCreate();
        mIp = getCurrentIp();
        networkStateReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String ip = getCurrentIp();
                if (ip != null && !ip.equals(mIp)) {
                    restartServer();
                }
            }
        };
        mNotification = new Notification();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent.hasExtra(EXTRA_NOTIFICATION)) {
            mNotification = intent.getParcelableExtra(EXTRA_NOTIFICATION);
        }

        if (intent.hasExtra(EXTRA_REQUEST_HANDLER)) {
            mRequestHandler = (Class)intent.getSerializableExtra(EXTRA_REQUEST_HANDLER);
        }

        if (intent.hasExtra(EXTRA_PORT)) {
            mPort = intent.getIntExtra(EXTRA_PORT, 6789);
        }

        if (intent.hasExtra(EXTRA_HTTPS)) {
            mHttps = intent.getBooleanExtra(EXTRA_HTTPS, false);
        }

        if (intent.hasExtra(EXTRA_COMMAND)) {
            String command = intent.getStringExtra(EXTRA_COMMAND);
            LogHelper.i("command received: " + command);
            if (command.equals(COMMAND_START_SERVER)) {
                startServer();
            } else if (command.equals(COMMAND_STOP_SERVER)) {
                stopServer();
            }
        }

        return Service.START_REDELIVER_INTENT;
    }

    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkStateReceiver);
    }

    private void startServer() {
        LogHelper.i("Server starting");
        if (!isRunning() && !mIsStarting) {
            mIsStarting = true;
            mIp = getCurrentIp();
            if (mIp == null) {
                (new Handler(getMainLooper())).postDelayed(new Thread() {
                    public void run() {
                        startServer();
                    }
                }, 250L);
            } else {
                try {
                    mListener = null;
                    mListener = new ServerSocket();
                    mListener.setReuseAddress(true);
                    mListener.bind(new InetSocketAddress(mIp, mPort));
                    mListenerThread = new Thread() {
                        public void run() {
                            while (isRunning() && mListener.isBound() && !mListener.isClosed()) {
                                try {
                                    Socket client = mListener.accept();
                                    ((HttpServerThread)mRequestHandler.getConstructors()[0]
                                            .newInstance(SocketListenerService.this, client)).start();
                                } catch (IOException var3) {
                                    ;
                                } catch (IllegalArgumentException var4) {
                                    var4.printStackTrace();
                                } catch (SecurityException var5) {
                                    var5.printStackTrace();
                                } catch (InstantiationException var6) {
                                    var6.printStackTrace();
                                } catch (IllegalAccessException var7) {
                                    var7.printStackTrace();
                                } catch (InvocationTargetException var8) {
                                    var8.printStackTrace();
                                }
                            }
                        }
                    };
                    mListenerThread.start();
                    IntentFilter filter = new IntentFilter();
                    filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
                    registerReceiver(networkStateReceiver, filter);
                    if (mNotification != null) {
                        mNotification = new NotificationCompat.Builder(SocketListenerService.this)
                                .setContentTitle(getString(R.string.app_name))
                                .setContentText("http://" + mIp + ":" + mPort)
                                .build();
                        startForeground(1, mNotification);
                    }

                    onServerStarted(mIp, mPort);
                    sendBroadcast((new Intent("com.fridgeface.webserver.ACTION_SERVER_STARTED"))
                            .putExtra(EXTRA_PACKAGE, getPackageName()).putExtra("ip", mIp).putExtra("port", mPort));
                } catch (IOException var3) {
                    Toast.makeText(this, "Unable to start server. Please try changing the port number.",
                            Toast.LENGTH_LONG).show();
                }
            }

            mIsStarting = false;
        }
    }

    private void stopServer() {
        if (isRunning()) {
            try {
                mListener.close();
                mListener = null;
                mListenerThread = null;
                stopForeground(true);
                unregisterReceiver(networkStateReceiver);
                sendBroadcast((new Intent(COMMAND_STOP_SERVER))
                        .putExtra(EXTRA_PACKAGE, getPackageName()));
                onServerStopped();
            } catch (IOException var2) {
                Log.e(TAG, "Exception closing listener", var2);
            } catch (IllegalArgumentException var3) {
                Log.e(TAG, "Exception closing listener (receiver not registered?)", var3);
            }
        }
    }

    private void restartServer() {
        if (isRunning()) {
            Log.i(TAG, "Restarting server...");
            stopServer();
            startServer();
        }
    }

    public boolean isRunning() {
        return mListener != null;
    }

    public String getIp() {
        return mIp;
    }

    public int getPort() {
        return mPort;
    }

    public String getUrl() {
        StringBuffer sb = new StringBuffer();
        if (mHttps) {
            sb.append("https://");
        } else {
            sb.append("http://");
        }

        sb.append(mIp).append(":").append(mPort);
        return sb.toString();
    }

    public String getConnectionType() {
        String connectionType = "not_connected";
        ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null) {
            if (ni.getType() != 1 && ni.getType() != 9) {
                connectionType = "cellular";
            } else {
                connectionType = "wifi";
            }
        }

        return connectionType;
    }

    protected void onServerStarted(String ip, int port) {
    }

    protected void onServerStopped() {
    }

    private String getCurrentIp() {
        String ip = null;

        try {
            Enumeration ex = NetworkInterface.getNetworkInterfaces();

            while (ex.hasMoreElements()) {
                NetworkInterface intf = (NetworkInterface)ex.nextElement();
                Enumeration enumIpAddr = intf.getInetAddresses();

                while (enumIpAddr.hasMoreElements()) {
                    InetAddress inetAddress = (InetAddress)enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() &&
                            InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {
                        ip = inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException var6) {
            Log.e(TAG, "Exception getting IP", var6);
        }

        return ip;
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }
}
