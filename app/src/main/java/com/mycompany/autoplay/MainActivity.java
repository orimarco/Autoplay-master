package com.mycompany.autoplay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
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
    private List<String> makeSomeNoiseMatches;
    private List<String> volumeUpMatches;
    private List<String> volumeDownMatches;
    private List<String> muteMatches;
    String CompletedSongs="";
    String UnCompletedSongs="";
    String[] songs;
    String[] singers;
    String[] albums;
    String android_id;
    boolean ended = false;
    String lastSongName="";             // name of song, if there are encodeSpaces need to be seperated by split("\\s+")
    boolean wasPlaying = false;
    int numSongs;
    int playlistLength=0;

    MediaPlayer.OnCompletionListener completionListener= new MediaPlayer.OnCompletionListener(){
        @Override
        public void onCompletion(MediaPlayer arg0) {
            callForward();
        }
    };

    void callForward(){

        if (mediaPlayer != null)
            mediaPlayer.release();
        if(ended){
            CompletedSongs+= "\n\t\t" + lastSongName ;
        }
        ended = true;
        index = (index + 1) % numSongs;
        lastSongName=songs[index];
        mediaPlayer = MediaPlayer.create(this, Uri.parse("http://storage.googleapis.com/autoplay_audio/" + getCurrentSongPath()));
        mediaPlayer.setOnCompletionListener(completionListener);
        if(wasPlaying)
            playMusic();
        setSongDetailsText();
    }

    private void setSongDetailsText() {
        String songNameSpaces=songs[index];
        String songSingerSpaces=singers[index];
        String songAlbumSpaces=albums[index];
        TextView songName = (TextView) findViewById(R.id.songName);
        songName.setText(songNameSpaces.split(".mp3")[0]);
        TextView artistName = (TextView) findViewById(R.id.artistName);
        artistName.setText(songSingerSpaces);
        TextView albumName = (TextView) findViewById(R.id.albumName);
        albumName.setText(songAlbumSpaces);
    }

    public void forward(View v) {
        ended =false;
        UnCompletedSongs += "\n\t\t" + lastSongName;
        callForward();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        android_id = android.provider.Settings.Secure.getString(this.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID); //
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null){
            Bundle b = getIntent().getExtras();
            playlistLength = b.getInt("seekBarProgressInSeconds");
            index = 0;
            srh = new SpeechRecognitionHelper();    //initialize speech recogniאzer
            readPools();    //initialize word matching pools

            new ServerPlaylistRequest().execute(new Pair<Context, String>(this, "1 2 3 4 5"));
            PhoneStateListener phoneStateListener = new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incomingNumber) {
                    if (state == TelephonyManager.CALL_STATE_RINGING) {
                        pauseMusicForSpeechRecognition();
                    } else if(state == TelephonyManager.CALL_STATE_IDLE) {
                        if(wasPlaying)
                            playMusic();
                    } else if(state == TelephonyManager.CALL_STATE_OFFHOOK) {
                        pauseMusicForSpeechRecognition();
                    }
                    super.onCallStateChanged(state, incomingNumber);
                }
            };
            TelephonyManager mgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
//            TODO: if(mgr != null) {
//                mgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
//            }
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
    }

    protected void onStop() {
        super.onStop();
        if(CompletedSongs != "" || UnCompletedSongs != ""){
            new ServerLogSendRequest(CompletedSongs, UnCompletedSongs, android_id).execute(new Pair<Context, String>(this, "1 2 3 4 5"));
            CompletedSongs = "";
            UnCompletedSongs = "";
        }
    }


    @Override
    public void onBackPressed(){
        Intent intent = new Intent(this, entranceActivity.class);
        startActivity(intent);
        finish();
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

    protected String encodeSpaces(String[] splited) {
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
        pauseMusicForSpeechRecognition();
        srh.run(this);
    }

    public void playPause(View v) {
        if(mediaPlayer.isPlaying()){
            pauseMusic();
        } else {
            playMusic();
        }
    }

    public void recognizedPlay(View v){
        if(wasPlaying)
            Toast.makeText(this, "Music is already playing!", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, "Play", Toast.LENGTH_LONG).show();
        playMusic(); //start again anyway, because we paused before recognition
    }

    public void recognizedPause(View v){    //no need to pause, already paused...
        if(!wasPlaying)
            Toast.makeText(this, "Music is already paused!", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, "Paused", Toast.LENGTH_LONG).show();
        wasPlaying = false;
        setButtonToPlay();
    }

    public void back(View v) {
        UnCompletedSongs += "\n\t\t" + lastSongName;
        if (mediaPlayer != null)
            mediaPlayer.release();
        index = (index + numSongs - 1) % numSongs;  //cyclic decrease index
        lastSongName=songs[index];
        String str = getCurrentSongPath();
        mediaPlayer = MediaPlayer.create(this, Uri.parse("http://storage.googleapis.com/autoplay_audio/" + str));
        mediaPlayer.setOnCompletionListener(completionListener);
        if(wasPlaying)
            playMusic();
        setSongDetailsText();

    }

    private String getCurrentSongPath() {
        String[] splited = songs[index].split("\\s+");
        return encodeSpaces(splited);
    }

    // Activity Results handler- This is used to take care of result of speech recognition
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
        }
        if(matches != null)
            analyze((String)matches.get(0));
        else    //if speech recognition was cancelled for some reason...
            if(wasPlaying)
                playMusic();
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void analyze(String input) {
        if(input == null)
            return;
        View v = this.getWindow().getDecorView().findViewById(android.R.id.content);
        if (matchesPlay(input))
            recognizedPlay(v);
        else if (matchesPause(input))
            recognizedPause(v);
        else if (matchesForward(input))
            forward(v);
        else if (matchesBack(input))
            back(v);
        else if(matchesMakeSomeNoise(input))
            raiseVolumeToMax();
        else if(matchesVolumeUp(input))
            raiseVolume();
        else if(matchesVolumeDown(input))
            decreaseVolume();
        else if(matchesMute(input))
            muteVolume();
        else if(matchesSpecialVolumeUp(input))
            updateSpecialVolumeUp(input);
        else if(matchesSpecialVolumeDown(input))
            updateSpecialVolumeDown(input);
        else
            Toast.makeText(this,"Couldn't understand you...\nPlease try again!",Toast.LENGTH_SHORT).show();
        if(wasPlaying)
            playMusic();
    }

    private void raiseVolumeToMax() {
        AudioManager audioManager =
                (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int val =audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, val,AudioManager.FLAG_PLAY_SOUND);
        Toast.makeText(this, "Volume set to maximum", Toast.LENGTH_LONG).show();
    }

    private void muteVolume() {
        AudioManager audioManager =
                (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0,AudioManager.FLAG_PLAY_SOUND);
        Toast.makeText(this, "Music muted", Toast.LENGTH_LONG).show();

    }

    private void raiseVolume() {
        AudioManager audioManager =
                (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
    }

    private void decreaseVolume() {
        AudioManager audioManager =
                (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_LOWER * 2, AudioManager.FLAG_SHOW_UI);
    }

    private void updateSpecialVolumeUp(String input) {
        String[] splited = input.toLowerCase().split(" ");
        int count = 0;
        for(String word : splited)
            if(word.equals("up"))
                count++;
        AudioManager audioManager =
                (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        for(int i = 0 ; i < count; ++i)
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
    }

    private void updateSpecialVolumeDown(String input) {
        String[] splited = input.toLowerCase().split(" ");
        int count = 0;
        for(String word : splited)
            if(word.equals("down"))
                count++;
        AudioManager audioManager =
                (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        for(int i = 0 ; i < count; ++i)
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                 AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
    }

    private boolean matchesPlay(String input) {
        return playMatches.contains(input.toLowerCase());
    }

    private boolean matchesPause(String input) {
        return pauseMatches.contains(input.toLowerCase());
    }

    private boolean matchesForward(String input) {
        return forwardMatches.contains(input.toLowerCase());
    }

    private boolean matchesBack(String input) {
        return backMatches.contains(input.toLowerCase());
    }

    private boolean matchesMakeSomeNoise(String input) {
        return makeSomeNoiseMatches.contains(input.toLowerCase());
    }

    private boolean matchesVolumeUp(String input) {
        return volumeUpMatches.contains(input.toLowerCase());
    }

    private boolean matchesVolumeDown(String input) {
        return volumeDownMatches.contains(input.toLowerCase());
    }

    private boolean matchesMute(String input) {
        return muteMatches.contains(input.toLowerCase());
    }
    private boolean matchesSpecialVolumeUp(String input) {
        String[] splited = input.toLowerCase().split(" ");
        if(splited != null && splited.length >= 3)
            return splited[0].equals("volume") && splited[1].equals("up") && splited[2].equals("up");
        return false;
    }

    private boolean matchesSpecialVolumeDown(String input) {
        String[] splited = input.toLowerCase().split(" ");
        if(splited != null && splited.length >= 3)
            return splited[0].equals("volume") && splited[1].equals("down") && splited[2].equals("down");
        return false;
    }

    private void readPools() {
        playMatches = readPool("playMatches.txt");
        backMatches = readPool("backMatches.txt");
        forwardMatches = readPool("forwardMatches.txt");
        pauseMatches = readPool("pauseMatches.txt");
        makeSomeNoiseMatches = readPool("makeSomeNoiseMatches.txt");
        volumeUpMatches = readPool("volumeUpMatches.txt");
        volumeDownMatches = readPool("volumeDownMatches.txt");
        muteMatches = readPool("muteMatches.txt");
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

    private void setButtonToPlay(){
        ImageButton ib = (ImageButton)findViewById(R.id.btnPlayPause);
        ib.setImageResource(R.drawable.btn_play);

    }

    private void setButtonToPause(){
        ImageButton ib = (ImageButton)findViewById(R.id.btnPlayPause);
        ib.setImageResource(R.drawable.btn_pause);
    }


    /*always call this fot play music, it does all the things around, like changing buttons etc.*/
    private void playMusic(){
        setSongDetailsText();
        setButtonToPause();
        mediaPlayer.start();
        wasPlaying = true;
    }

    /*always call this fot pause music, it does all the things around, like changing buttons etc.*/
    void pauseMusic(){
        setSongDetailsText();
        setButtonToPlay();
        wasPlaying = false;
        mediaPlayer.pause();
    }

    void pauseMusicForSpeechRecognition(){
        setButtonToPlay();
        if(mediaPlayer.isPlaying()){
            wasPlaying = true;
            mediaPlayer.pause();
        }
    }
    class ServerPlaylistRequest extends AsyncTask<Pair<Context, String>, Void, String> {
        public static final String playlistReqUrl = "http://perudo-909.appspot.com/hello";
        private Context context;

        @Override
        protected String doInBackground(Pair<Context, String>... params) {
            context = params[0].first;
            String name = params[0].second;

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(playlistReqUrl);
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
            if (mediaPlayer == null) {
                songs = result.split("#")[0].split("@");
                singers = result.split("#")[1].split("@");
                albums = result.split("#")[2].split("@");
                numSongs = songs.length - 1;
                lastSongName = songs[index];
                Uri uri = Uri.parse("http://storage.googleapis.com/autoplay_audio/" + getCurrentSongPath());
                mediaPlayer = MediaPlayer.create(context, uri);
                mediaPlayer.setOnCompletionListener(completionListener);
                setSongDetailsText();
            }
        }
    }
}