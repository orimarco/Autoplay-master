package com.mycompany.autoplay;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Pair;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ori marcovitch on 6/12/15.
 */
class ServerLogSendRequest extends AsyncTask<Pair<Context, String>, Void, String> {
    private Context context;
    String CompletedSongs;
    String UnCompletedSongs;
    String android_id;

    ServerLogSendRequest(String _CompletedSongs, String _UnCompletedSongs, String _android_id){
        CompletedSongs = _CompletedSongs;
        UnCompletedSongs = _UnCompletedSongs;
        android_id = _android_id;
    }
    @Override
    protected String doInBackground(Pair<Context, String>... params) {

        context = params[0].first;
        String name = params[0].second;

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost("http://perudo-909.appspot.com/david");
        try {
            // Add name data to request
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("CompletedSongs", CompletedSongs));
            nameValuePairs.add(new BasicNameValuePair("UnCompletedSongs", UnCompletedSongs));
            nameValuePairs.add(new BasicNameValuePair("android_id", android_id));
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpClient.execute(httpPost);
            if (response.getStatusLine().getStatusCode() == 200) {
                return EntityUtils.toString(response.getEntity());
            }
            return "Error: " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase();

        } catch (ClientProtocolException e) {
            return e.getMessage();
        } catch (IOException e) {
            return e.getMessage();
        }


    }

    @Override
    protected void onPostExecute(String result) {
    }

}
