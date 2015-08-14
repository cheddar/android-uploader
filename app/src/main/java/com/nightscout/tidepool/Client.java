package com.nightscout.tidepool;

import android.net.http.AndroidHttpClient;
import android.util.Base64;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 *
 */
public class Client implements Closeable {
    private static final String TAG = "TidepoolClient";

    private final AndroidHttpClient httpClient;
    private final String host;
    private final String uploadHost;

    public Client(String host, String uploadHost) {
        this.host = host;
        this.uploadHost = uploadHost;

        httpClient = AndroidHttpClient.newInstance("Nightscout uploader v0");
    }

    public void close() throws IOException {
        httpClient.close();
    }

    public String login(String username, String password) {
        HttpPost post = new HttpPost(makeURL("/auth/login"));

        post.setHeader("Authorization", "Basic " + Base64.encodeToString(
                String.format("%s:%s", username, password).getBytes(),
                Base64.NO_WRAP
        ));

        try {
            HttpResponse response = httpClient.execute(post);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                return response.getFirstHeader("x-tidepool-session-token").getValue();
            } else {
                Log.w(TAG, String.format("Got unexpected response code[%s] when logging in.", statusCode));
            }
        } catch (IOException e) {
            Log.w(TAG, String.format("Problem logging in as user[%s]", username), e);
        }
        return null;
    }

    public void postDatum(String token, Map<String, Object> event) {
        Log.i(TAG, String.format("Posting event[%s]", event));

        JSONObject obj = new JSONObject();

        for (Map.Entry<String, Object> entry : event.entrySet()) {
            try {
                obj.put(entry.getKey(), entry.getValue());
            } catch (JSONException e) {
                Log.w(TAG, String.format("Unable to set key-value[%s]", entry), e);
            }
        }

        HttpPost post = null;
        try {
            post = new HttpPost("https://" + uploadHost + "/data");
            post.setHeader("x-tidepool-session-token", token);
            post.setHeader("content-type", "application/json");
            post.setEntity(new StringEntity(obj.toString()));
        } catch (UnsupportedEncodingException e) {
            Log.w(TAG, "Unable to build up request", e);
        }

        Log.i(TAG, String.format("Issuing post to [%s]", post.getURI()));
        for (Header header : post.getAllHeaders()) {
            Log.i(TAG, header.toString());
        }

        try {
            HttpResponse httpResponse = httpClient.execute(post);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            Log.i(TAG, String.format("Got response[%s]", statusCode));
            if (statusCode == 400) {
                InputStream is = httpResponse.getEntity().getContent();
                byte[] bytes = new byte[16 * 1024];
                int numRead = is.read(bytes);
                Log.i(TAG, new String(bytes, 0, numRead));
            }
        } catch (IOException e) {
            Log.w(TAG, "Problem posting data", e);
        }
    }

    private String makeURL(String path) {
        return String.format("https://%s%s", host, path);
    }
}
