package com.mycompany.autoplay;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBarActivity;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;




public class MainActivity extends ActionBarActivity {
    MediaPlayer mediaPlayer;
    SpeechRecognitionHelper srh;
    List<Integer> songsIds;
    int index;
    private List<String> playMatches;
    private List<String> backMatches;
    private List<String> forwardMatches;
    private List<String> pauseMatches;
    String[] songs;
    String[] splited;


    void callforward(){
        MediaPlayer.OnCompletionListener completionListener= new MediaPlayer.OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer arg0) {
                callforward();
            }
        };
        if (mediaPlayer != null)
            mediaPlayer.release();

        splited = songs[index++].split("\\s+");
        String str = spaces(splited);
        mediaPlayer = MediaPlayer.create(this, Uri.parse("http://storage.googleapis.com/autoplay_audio/" + str));
        mediaPlayer.setOnCompletionListener(completionListener);
        mediaPlayer.start();
    }
    public void forward(View v) {

       callforward();

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        index = 0;


//        mediaPlayer = MediaPlayer.create(this, songsIds.get(index));
//        File f = Environment.getExternalStorageDirectory();
//        ArrayList<String> names = new ArrayList<String>(Arrays.asList(f.list()));
//        Toast.makeText(this, names.toString(), Toast.LENGTH_LONG).show();
//        mediaPlayer = MediaPlayer.create(this,Uri.parse("http://dimechimes.com/media/Rocky_Soundtrack_-_Eye_Of_The_Tiger.mp3"));
//        mediaPlayer = MediaPlayer.create(this,Uri.parse("https://00e9e64bac29d4aaf6ceea8798876fe4889f1f7c783eea7404-apidata.googleusercontent.com/download/storage/v1_internal/b/autoplay_audio/o/02-Move%20It.mp3?qk=AD5uMEscWeeOP-iRR4tfcTkrBltzjjHmcTCHrDGpYQyymTZd6QKFMzAiZ38S44yoPLVvGE-Oq_RouDT1EguQbIv_fABtrUZVTReNwq96a-6vS1feRV_9Rxim9eYkGv-DR4Z3QTf9clalRM0fYB5M61YZ2HWGgS-KFS1CNq4lNq5-fhDch7iKTK2jeTEbzfSSZfLNtffXP29z2oUDHum5jX5RQx9oQns4zH_ss6RRh8ZEakTS0ethvpFFVr9JsnfuxiY6Hnt83T_4SJW6N7wO9D2fsPCh6VRVPBv3hF8j1h2cTipMzsKIVBeVdH5Psxo9Twlm8JiAw3-RpjlOwq66FeeArM2DL5w1GKeA1S51ecTQ69Tlfnsrd2Boril6QZHDbdHFuoKRFSDbbWNfiBEmGZe0mtRuG21fkpTgHgpQaUZvwLWrZNGe7T6Xw_PmgV_l6YcP4PFNyQR96X8WFYukuWRvqTtMbAWTOk9CPg_fnpHz0ZByXDppKYRJSV75_2ft85qwWD1sT-ZPbgz9D0ysIlzRaaOg-EIiPq9KmLVZMnGzIMK9CaNLBaSdU7XjGqj9DxKlq4VJWj6NtLplfkqzdNtQ44P-cVupqR-lC8cuHV30FYRoK3vvzyd7H3we1T16_CTK1YEQ5-FiddQ3giMNodYvaYZFlm4UVV3-8xYzDmSl-uj2hq8Irlesja6KE7BvFU7xcSOZGe_e8fLzf3dUVrD5M6ZJYzj-pQ"));
//        mediaPlayer = MediaPlayer.create(this,Uri.parse("http://storage.googleapis.com/autoplay_audio/12-Why.mp3"));
//        mediaPlayer = MediaPlayer.create(this, Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/Music/Flares.mp3"));
//        srh = new SpeechRecognitionHelper();
//        readPools();
//        View v = this.getWindow().getDecorView().findViewById(android.R.id.content);

//        mediaPlayer.start();
//        play(v);
//        pause(v);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    String spaces(String[] splited) {
        int length;
        int i = 0;
        String str = "";
        length = splited.length;
        while (length > 1) {
            length--;
            str = str + splited[i] + " ";
            i++;
        }
        str = str + splited[i];
        return str;
    }

    public void recognize(View v) {
        mediaPlayer.pause();
        srh.run(this);
    }

    public void playPause(View v) {
            if(mediaPlayer.isPlaying()){
                mediaPlayer.pause();
            } else {
                mediaPlayer.start();
            }
    }



    public void back(View v) {
        MediaPlayer.OnCompletionListener completionListener= new MediaPlayer.OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer arg0) {
                callforward();
            }
        };
        if (mediaPlayer != null)
            mediaPlayer.release();
        index-=2;
        splited = songs[index++].split("\\s+");
        String str = spaces(splited);
        mediaPlayer = MediaPlayer.create(this, Uri.parse("http://storage.googleapis.com/autoplay_audio/" + str));
        mediaPlayer.setOnCompletionListener(completionListener);
        mediaPlayer.start();
    }

//    private List<Integer> getSongsIdsList(){
//        Field[] fields=R.raw.class.getFields();
//        List<Integer> ids= new ArrayList<>();
//        for (Field field : fields) {
//            try {
//                ids.add(field.getInt(field));
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            }
//        }
//        return ids;
//    }

    // Activity Results handler
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ArrayList matches = null;
        // if it’s speech recognition results
        // and process finished ok
        if (requestCode == 2 && resultCode == RESULT_OK) {

            // receiving a result in string array
            // there can be some strings because sometimes speech recognizing inaccurate
            // more relevant results in the beginning of the list
            matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            // in “matches” array we holding a results... let’s show the most relevant
            if (matches.size() > 0)
                Toast.makeText(this, (String) matches.get(0), Toast.LENGTH_LONG).show();
        }

        //analyze(matches);
        super.onActivityResult(requestCode, resultCode, data);
    }

  /*  void analyze(ArrayList input) {
        View v = this.getWindow().getDecorView().findViewById(android.R.id.content);
        if (matchesPlay(input))
            play(v);
        else if (matchesPause(input))
            pause(v);
        else if (matchesForward(input))
            forward(v);
        else if (matchesBack(input))
            back(v);
    }*/

    private boolean matchesPlay(ArrayList inputArray) {
        for (Object word : inputArray) {
            if (playMatches.contains(word))
                return true;
        }
        return false;
    }

    private boolean matchesPause(ArrayList inputArray) {
        for (Object word : inputArray) {
            if (pauseMatches.contains(word))
                return true;
        }
        return false;
    }

    private boolean matchesForward(ArrayList inputArray) {
        for (Object word : inputArray) {
            if (forwardMatches.contains(word))
                return true;
        }
        return false;
    }

    private boolean matchesBack(ArrayList inputArray) {
        for (Object word : inputArray) {
            if (backMatches.contains(word))
                return true;
        }
        return false;
    }


    private void readPools() {
        playMatches = readPool("playMatches.txt");
        backMatches = readPool("backMatches.txt");
        forwardMatches = readPool("forwardMatches.txt");
        pauseMatches = readPool("pauseMatches.txt");
    }

    private List<String> readPool(String poolFile) {
        List<String> words = new ArrayList<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getAssets().open(poolFile), "UTF-8"));
            // do reading, usually loop until end of file reading
            String mLine = reader.readLine();
            while (mLine != null) {
                words.add(mLine);
                mLine = reader.readLine();
            }
        } catch (IOException e) {
            //TODO: log the exception or something
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    //TODO: log the exception or something
                }
            }
        }
        return words;
    }

    public void onclick(View v) {
        new ServletPostAsyncTask().execute(new Pair<Context, String>(this, "1 2 3 4 5"));
    }

    class ServletPostAsyncTask extends AsyncTask<Pair<Context, String>, Void, String> {
        private Context context;

        @Override
        protected String doInBackground(Pair<Context, String>... params) {
            context = params[0].first;
            String name = params[0].second;

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("http://perudo-909.appspot.com/hello");
            try {
                // Add name data to request
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("name", name));
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

    MediaPlayer.OnCompletionListener completionListener= new MediaPlayer.OnCompletionListener(){
        @Override
        public void onCompletion(MediaPlayer arg0) {
        callforward();
        }
    };

            if (mediaPlayer == null) {
                songs = result.split("@");
                splited = songs[index++].split("\\s+");
                String str = spaces(splited);
                mediaPlayer = MediaPlayer.create(context, Uri.parse("http://storage.googleapis.com/autoplay_audio/" + str));
                mediaPlayer.setOnCompletionListener(completionListener);
                mediaPlayer.start();
            }

        }
    }
}
