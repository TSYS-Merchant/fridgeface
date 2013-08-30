
package com.fridgeface.webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.util.Log;

import com.fridgeface.constants.IntentExtras;
import com.fridgeface.utils.LogHelper;

public class ServerRequestHandler extends HttpServerThread {
    public static final String TAG = ServerRequestHandler.class.getName();

    public ServerRequestHandler(SocketListenerService service, Socket socket) throws IOException {
        super(service, socket);
    }

    @Override
    protected BasicHttpResponse onPost(HttpRequest request) {
        String uri;

        uri = request.getRequestLine().getUri();
        if (uri.startsWith("/poke.json")) {
            JSONObject json = new JSONObject();
            try {
                json.put("success", false);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            HashMap<String, String> postVars = new HashMap<String, String>();
            try {
                BasicHttpEntityEnclosingRequest entityRequest =
                        new BasicHttpEntityEnclosingRequest(request.getRequestLine());
                mConnection.receiveRequestEntity(entityRequest);
                HttpEntity entity = entityRequest.getEntity();

                InputStream is = entity.getContent();
                char[] buffer = new char[2048];
                StringWriter sw = new StringWriter();
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(is, "UTF-8"), 2048);
                while (reader.ready() || entity.isStreaming()) {
                    int n = reader.read(buffer);
                    if (n > 0) {
                        sw.write(buffer, 0, n);
                    }
                }
                is.close();
                String postBody = sw.toString();

                String[] params = postBody.split("&");
                for (String param : params) {
                    String[] paramNvp = param.split("=");
                    if (!paramNvp[0].equals("")) {
                        String value;
                        if (paramNvp.length > 1) {
                            value = URLDecoder.decode(paramNvp[1], "UTF-8");
                        } else {
                            value = "";
                        }
                        postVars.put(URLDecoder.decode(paramNvp[0], "UTF-8"), value);
                    }
                }

                if (postVars.containsKey("verb")) {
                    if (postVars.get("verb").equals("mood")) {
                        Float mood = null;
                        try {
                            mood = Float.parseFloat(postVars.get("mood"));
                        } catch (NumberFormatException e) {
                        }

                        if (mood != null) {
                            mood = Math.min(1f, Math.max(-1f, mood));
                            LogHelper.i("Got mood from webservice: " + mood);
                            Intent intent = new Intent(IntentExtras.ACTION_POKE);
                            intent.putExtra(IntentExtras.EXTRA_MOOD, mood);
                            mService.sendBroadcast(intent);

                            json.put("mood", mood);
                            json.put("success", true);
                        } else {
                            json.put("message", "Please specify a mood in the range of -1 to 1.");
                        }
                    } else if (postVars.get("verb").equals("speak")) {
                        if (postVars.containsKey("phrase")
                                && !postVars.get("phrase").equals("")) {
                            String phrase = postVars.get("phrase");
                            String voice = postVars.get("voice");
                            String pitch = postVars.get("pitch");
                            String rate = postVars.get("rate");
                            LogHelper.i("Speaking text: " + phrase + " / " + voice + " / " + pitch
                                    + " / " + rate);
                            Intent intent = new Intent(IntentExtras.ACTION_POKE);
                            intent.putExtra(IntentExtras.TTS_PHRASE, phrase);
                            intent.putExtra(IntentExtras.TTS_VOICE, voice);
                            intent.putExtra(IntentExtras.TTS_PITCH, pitch);
                            intent.putExtra(IntentExtras.TTS_RATE, rate);
                            mService.sendBroadcast(intent);
                            json.put("success", true);
                        } else {
                            json.put("message", "Please specify a phrase.");
                        }
                    } else {
                        json.put("message", "Verb not recognized.");
                    }
                }
            } catch (HttpException e) {
                Log.e(TAG, "HTTP exception", e);
            } catch (IOException e) {
                Log.e(TAG, "HTTP exception", e);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            BasicHttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
            response.addHeader("Content-Type", "application/json");
            try {
                response.setEntity(new StringEntity(json.toString()));
                response.addHeader("Content-Length", json.toString().length() + "");
            } catch (UnsupportedEncodingException ignored) {
            }

            return response;
        } else {
            return super.onPost(request);
        }
    }
}
