package com.example.nadavlotan.autodemo;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Random;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

// Imports the Google Cloud client library


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextToSpeech textToSpeech;
    Socket mSocket;
    String serverText = "Welcome to ABC. How can I help you?";
    JSONObject params = new JSONObject();
    private boolean initialized;
    private String queuedText;
    private String TAG = "TTS";
    String mostRecentUtteranceID;
    TextView serverTextView;

    private void ttsInit() {
        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (textToSpeech.getEngines().size() == 0) {
                    Toast.makeText(MapsActivity.this, "No Engines Installed", Toast.LENGTH_LONG).show();
                } else {
                    if (status == TextToSpeech.SUCCESS) {
                        ttsInitialized();
                    }
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {


                if (textToSpeech.getEngines().size() == 0) {
                    Toast.makeText(MapsActivity.this, "No Engines Installed", Toast.LENGTH_LONG).show();
                } else {
                    if (status == TextToSpeech.SUCCESS) {

                        ttsInitialized();
                    }
                }
            }
        });

        Button restartButton = findViewById(R.id.restartButton);
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                params.remove("userMessage");
                try {
                    params.put("userMessage", "restart");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mSocket.emit("userMessage", params);
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        TextView locationTextView = findViewById(R.id.locationInfoTextView);
        locationTextView.setText("Ayalon Darom (Route no. 20)");
        locationTextView.setVisibility(View.VISIBLE);

        ImageButton micImageButton = (ImageButton) findViewById(R.id.micImageButton);
        micImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSpeechInput();
            }
        });

        serverTextView = (TextView) findViewById(R.id.serverTextView);

        final TextView enterTextView = (TextView) findViewById(R.id.enterTextView);

        Button sendTextButton = (Button) findViewById(R.id.sendTextButton);
        sendTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                params.remove("userMessage");
                try {
                    params.put("userMessage", enterTextView.getText());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                mSocket.emit("userMessage", params);
                Toast.makeText(MapsActivity.this, "Emitted Succesfully", Toast.LENGTH_LONG).show();
            }
        });


        // Initialize and sets the TextFetcher socket to connect and wait for input
        TextFetcher textFetcher = new TextFetcher();
        mSocket = textFetcher.getmSocket();
        mSocket.on("serverMessage", onServerMessage);
        mSocket.on("receivingData", onReceivingData);
        mSocket.connect();
    }

    private void ttsInitialized() {
        // *** set UtteranceProgressListener AFTER tts is initialized ***
        textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {


            @Override
            public void onStart(String utteranceId) {

            }

            @Override
            // this method will always called from a background thread.
            public void onDone(String utteranceId) {

                // only respond to the most recent utterance
                if (!utteranceId.equals(mostRecentUtteranceID)) {
                    Log.i("XXX", "onDone() blocked: utterance ID mismatch.");
                    return;
                } // else continue...


                boolean wasCalledFromBackgroundThread = (Thread.currentThread().getId() != 1);
                Log.i("XXX", "was onDone() called on a background thread? : " + wasCalledFromBackgroundThread);

                Log.i("XXX", "onDone working.");

                boolean firstCon = !serverText.equals("Ok, I'm now checking your pulse, please remain still for a few seconds.");
                boolean secondCon = !serverText.equals("Pulse rate successfully checked");
                boolean thirdCon = !serverText.equals("To help me define your injury ,answer the next 5 questions");

                boolean understand = serverText.equals("I understand.");
                boolean finish = serverText.equals("Ok, mada are already on their way to you.") || serverText.equals("I understand, i've successfully informed Mada.") || serverText.equals("All right, i've successfully informed Mada.");

                Log.d("ServerText", Boolean.toString(firstCon));
//                Log.d("ServerText", serverText);
                Log.d("ServerText", Boolean.toString(secondCon));
                Log.d("ServerText", serverText);
                Log.d("ServerText", Boolean.toString(thirdCon));
                Log.d("ServerText", serverText);

//                if (firstCon) {
//                    params.remove("userMessage");
//                    try {
//                        params.put("userMessage", " ");
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    mSocket.emit("userMessage");
//                }

                if (finish) {
                    params.remove("userMessage");
                    try {
                        params.put("userMessage", "nn");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mSocket.emit("getData");
                }

                if (understand) {
//                    ttsInit();
                    params.remove("userMessage");
                    try {
                        params.put("userMessage", "pain");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mSocket.emit("userMessage", params);
                }

                if (firstCon) {
                    if (secondCon) {
                        if (thirdCon) {
                            if (!understand) {
                                if (!finish) {
                                    ttsInit();
                                    getSpeechInput();
                                }
                            }
                        }
                    }
                }


                // for demonstration only... avoid references to
                // MainActivity (unless you use a WeakReference)
                // inside the onDone() method, as it
                // can cause a memory leak.
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // *** toast will not work if called from a background thread ***
//                        Toast.makeText(MapsActivity.this, "onDone working.", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(String utteranceId) {

            }
        });

        // set Language
        textToSpeech.setLanguage(Locale.US);

        // set unique utterance ID for each utterance
        mostRecentUtteranceID = (new Random().nextInt() % 9999999) + ""; // "" is String force

        // set params
        // *** this method will work for more devices: API 19+ ***
        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, mostRecentUtteranceID);


    }

    public void ttsSpeak() {
        textToSpeech.speak(serverText, TextToSpeech.QUEUE_ADD, null, mostRecentUtteranceID);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.emit("closeConnection", "restart");
    }


    /**
     *
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        float zoomLevel = 15.0f;

        // Add a marker in Sydney and move the camera
        LatLng location = new LatLng(32.155573, 34.814040);
        mMap.addMarker(new MarkerOptions().position(location).title("Marker in your location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel));
    }

    public void getSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.UK);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.UK);
        if (serverText != "Ok, I'm now checking your pulse, please remain still for a few seconds.") {
            if (serverText != "Pulse rate successfully checked") {
                if (serverText != "To help me define your injury ,answer the next 5 questions") {
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, 10);
                    } else {
                        Toast.makeText(this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        TextView enterTextView = (TextView) findViewById(R.id.enterTextView);

        switch (requestCode) {
            case 10:
                if (resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    serverText = result.get(0);
                    enterTextView.setText(serverText);

                    Toast.makeText(this, "text successfully saved: " + serverText, Toast.LENGTH_SHORT).show();

                    params.remove("userMessage");
                    try {
                        params.put("userMessage", enterTextView.getText());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mSocket.emit("userMessage", params);
                }
//                break;
        }
    }

    /**
     * OnServerMessage
     * <p>
     * Gets a message from the server for the event serverMessage and updated the serverText
     */
    private Emitter.Listener onServerMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MapsActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    boolean cont = false;
                    serverText = (String) args[0];
                    serverTextView.setText(serverText);
//                    Toast.makeText(MapsActivity.this, serverText, Toast.LENGTH_LONG).show();
                    ttsInitialized();
                    ttsSpeak();
                }
            });
        }
    };

    private Emitter.Listener onReceivingData = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MapsActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    serverText = (String) args[0];
//                    Toast.makeText(MapsActivity.this, serverText, Toast.LENGTH_LONG).show();
//                    ttsInitialized();
//                    ttsSpeak();
                }
            });
        }
    };
}