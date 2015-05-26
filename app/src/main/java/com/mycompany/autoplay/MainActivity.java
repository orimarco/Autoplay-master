package com.mycompany.autoplay;

import android.app.Activity;
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
import android.view.Window;
import android.view.WindowManager;
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

public class MainActivity extends Activity {
    MediaPlayer mediaPlayer;
    SpeechRecognitionHelper srh;
    int index;
    private List<String> playMatches;
    private List<String> backMatches;
    private List<String> forwardMatches;
    private List<String> pauseMatches;
    String[] songs;
    String[] splited;
    boolean wasPlaying;
    int numSongs;
    int playlistLength=0;

    static boolean isFirstTime = true;

    void callForward(){
        MediaPlayer.OnCompletionListener completionListener= new MediaPlayer.OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer arg0) {
                callForward();
            }
        };
        if (mediaPlayer != null)
            mediaPlayer.release();

        splited = songs[index].split("\\s+");
        index=(index+1)%numSongs;
        String str = spaces(splited);
        mediaPlayer = MediaPlayer.create(this, Uri.parse("http://storage.googleapis.com/autoplay_audio/" + str));
        mediaPlayer.setOnCompletionListener(completionListener);
        if(! mediaPlayer.isPlaying())
            mediaPlayer.start();
    }
    public void forward(View v) {

        callForward();

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = getIntent().getExtras();
        playlistLength = b.getInt("seekBarProgressInSeconds");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        if(isFirstTime) {
            index = 0;
            wasPlaying = false;
            srh = new SpeechRecognitionHelper();    //initialize speech recognizer
            readPools();    //initialize word matching pools

            new ServletPostAsyncTask().execute(new Pair<Context, String>(this, "1 2 3 4 5"));
            isFirstTime = false;
        }
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
            str = str + splited[i] + "%20";
            i++;
        }
        str = str + splited[i];
        return str;
    }

    public void recognize(View v) {
        if(mediaPlayer.isPlaying())
            wasPlaying = true;
        mediaPlayer.pause();
        srh.run(this);
    }

    public void playPause(View v) {
        if(mediaPlayer.isPlaying() || wasPlaying){
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
    }

    public void recognizedPlay(View v){
        if(wasPlaying)
            Toast.makeText(this, "Music is already playing!", Toast.LENGTH_LONG).show();
        mediaPlayer.start(); //start again anyway, because we paused before recognition
    }

    public void recognizedPause(View v){    //no need to pause, already paused...
        if(!wasPlaying)
            Toast.makeText(this, "Music is already paused!", Toast.LENGTH_LONG).show();
        wasPlaying = false;
    }

    public void back(View v) {
        MediaPlayer.OnCompletionListener completionListener= new MediaPlayer.OnCompletionListener(){
            @Override
            public void onCompletion(MediaPlayer arg0) {
                callForward();
            }
        };
        if (mediaPlayer != null)
            mediaPlayer.release();
        index=(index-2);
        if(index<0)
            index=index+numSongs;
        splited = songs[index].split("\\s+");
        index=(index+1)%numSongs;
        String str = spaces(splited);
        mediaPlayer = MediaPlayer.create(this, Uri.parse("http://storage.googleapis.com/autoplay_audio/" + str));
        mediaPlayer.setOnCompletionListener(completionListener);
        if(!mediaPlayer.isPlaying())
            mediaPlayer.start();
    }

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

        analyze(matches);
        super.onActivityResult(requestCode, resultCode, data);
    }

    void analyze(ArrayList input) {
        View v = this.getWindow().getDecorView().findViewById(android.R.id.content);
        if (matchesPlay(input))
            recognizedPlay(v);
        else if (matchesPause(input))
            recognizedPause(v);
        else if (matchesForward(input))
            forward(v);
        else if (matchesBack(input))
            back(v);

        if(wasPlaying)
            mediaPlayer.start();
        wasPlaying = false; //initialize back

    }

    private boolean matchesPlay(ArrayList inputArray) {
        for (Object word : inputArray) {
            if (playMatches.contains(((String)word).toLowerCase()))
                return true;
        }
        return false;
    }

    private boolean matchesPause(ArrayList inputArray) {
        for (Object word : inputArray) {
            if (pauseMatches.contains(((String)word).toLowerCase()))
                return true;
        }
        return false;
    }

    private boolean matchesForward(ArrayList inputArray) {
        for (Object word : inputArray) {
            if (forwardMatches.contains(((String)word).toLowerCase()))
                return true;
        }
        return false;
    }

    private boolean matchesBack(ArrayList inputArray) {
        for (Object word : inputArray) {
            if (backMatches.contains(((String)word).toLowerCase()))
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
                nameValuePairs.add(new BasicNameValuePair("playlistLength", Integer.toString(playlistLength)));
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
            MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener(){
                @Override
                public void onCompletion(MediaPlayer arg0) {
                    callForward();
                }
            };
            if (mediaPlayer == null) {
                songs = result.split("@");
                numSongs=songs.length - 1;
                splited = songs[index].split("\\s+");
                index=(index+1)%numSongs;
                String str = spaces(splited);
                Uri uri = Uri.parse("http://storage.googleapis.com/autoplay_audio/" + str);
                mediaPlayer = MediaPlayer.create(context, uri);

                if(mediaPlayer != null) {
                    mediaPlayer.setOnCompletionListener(completionListener);
                    mediaPlayer.pause();
                }
            }

        }
    }
}
