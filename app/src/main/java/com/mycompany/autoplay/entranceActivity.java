package com.mycompany.autoplay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;


public class entranceActivity extends Activity {

    private SeekBar seekBar;
    private TextView textView;
    int seekBarProgressInMinutes = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrance);
        initializeVariables();

        // Initialize the textview with '0'.

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                seekBarProgressInMinutes = progresValue;
                setMinutesInText(seekBarProgressInMinutes);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setMinutesInText(seekBarProgressInMinutes);
                startMainActivity();
            }
        });
    }

    // A private method to help us initialize our variables.
    private void initializeVariables() {
        seekBar = (SeekBar) findViewById(R.id.chooseLengthBar);
        textView = (TextView) findViewById(R.id.progressText);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_entrance, menu);
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

    private void setMinutesInText(int minutes){
        textView.setText(minutes / 60 + "h" + minutes % 60 + "m");
    }

    private void startMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        Bundle b = new Bundle();
        b.putInt("seekBarProgressInSeconds", seekBarProgressInMinutes * 60);
        intent.putExtras(b);
        startActivity(intent);
        finish();
    }
}
