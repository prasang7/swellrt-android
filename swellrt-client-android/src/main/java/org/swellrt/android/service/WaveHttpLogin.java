package org.swellrt.android.service;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class WaveHttpLogin {

    public final static String TAG = "WaveHttpLogin";

    public class Response {

        public String sessionId;
        public String userId;

    }

    public static String WAVE_SESSION_COOKIE = "WSESSIONID";

    private static String LOGIN_CTX = "auth/signin?r=none";
    private static String CHARSET = "utf-8";


    private String host;
    private String username;
    private String password;

    public WaveHttpLogin(String host, String username, String password) {
        this.host = host;
        this.username = username;
        this.password = password;
    }

    public Response execute() {

        Response response = new Response();

        String urlStr = host.endsWith("/") ? host + LOGIN_CTX : host + "/" + LOGIN_CTX;
        String queryStr = "";
        try {
            queryStr = "address=" + URLEncoder.encode(username, "UTF-8") + "&password="
                    + URLEncoder.encode(password, CHARSET) + "&signIn="
                    + URLEncoder.encode("Sign+in", CHARSET);

        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Login Error: "+e.getMessage());
        }

        HttpURLConnection connection = null;

        try {

            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();

            connection.setDoOutput(true);
            connection.setRequestProperty("Accept-Charset", CHARSET);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset="
                    + CHARSET);

            OutputStream out = connection.getOutputStream();
            out.write(queryStr.getBytes(CHARSET));


            if (connection.getResponseCode() != 200) {
                Log.e(TAG,
                        "HTTP Login response error " + connection.getResponseCode() + ", "
                                + connection.getResponseMessage());
            } else {

                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        connection.getInputStream()));

                String responseText = reader.readLine();

                JsonParser jp = new JsonParser();
                JsonElement responseJson = jp.parse(responseText);
                response.userId = responseJson.getAsJsonObject().get("participantId").getAsString();
                response.sessionId = responseJson.getAsJsonObject().get("sessionId").getAsString();

                if (response.sessionId == null)
                    Log.e(TAG, "Login error: Session not found in HTTP response");
            }


        } catch (Exception e) {
            Log.e(TAG, "Login error: "+e.getMessage());
        } finally {
            connection.disconnect();
        }

        return response;

    }

}
