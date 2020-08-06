package com.example.nadavlotan.autodemo;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class IncomingCall extends AppCompatActivity {
    MediaPlayer mp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        mp = MediaPlayer.create(IncomingCall.this, R.raw.iphone_x);
        mp.setLooping(true);
//        mp.start();

        Button answerButton = (Button) findViewById(R.id.answerButton);
        Button declineButton = (Button) findViewById(R.id.declineButton);

        answerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.stop();
            }
        });

        declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.stop();
            }
        });
    }
}
