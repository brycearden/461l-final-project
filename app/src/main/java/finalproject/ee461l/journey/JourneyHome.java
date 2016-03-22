package finalproject.ee461l.journey;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class JourneyHome extends FragmentActivity implements
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    //Google Maps
    private static LocationRequest mLocationRequest;
    //To be used in MapSupport
    static GoogleMap mMap;
    static boolean firstUpdate;
    static LatLng currentLocation;
    static Marker marker;

    //Fragments
    private MapFragment mapFragment;
    private HelpFragment helpFragment;

    //Text To Speech
    private static boolean useTTS;
    static TextToSpeech speaker;

    //OnActivityResult Constants
    private static final int START_TRIP = 0;
    private static final int JOIN_TRIP = 1;
    private static final int VOICE_START = 2;
    private static final int VOICE_REQUEST = 3;
    private static final int VOICE_TIME = 4;
    private static final int VOICE_DISTANCE = 5;
    private static final int VOICE_CALC = 4;

    //Waypoint adding
    private String stopType;
    private int timeToStop;
    private int distanceFromRoute;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    private TextToSpeech getInstance() {
        if (speaker != null) return speaker;
        return new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    //Failed to set up TTS engine
                    JourneyHome.speaker.setLanguage(Locale.US);
                } else {
                    JourneyHome.useTTS = false;
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_home);

        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        //Initialize fragments
        mapFragment = MapFragment.newInstance();
        helpFragment = HelpFragment.getInstance();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        FragmentManager manager = getFragmentManager();
        manager.beginTransaction().add(R.id.home_view, mapFragment).commit();
        mapFragment.getMapAsync(this);
        JourneyHome.firstUpdate = true;
        JourneyHome.useTTS = true;

        //Navigation Drawer
        ArrayList<String> navItems = new ArrayList<String>();
        navItems.add("Log In");
        navItems.add("Settings");
        navItems.add("Help");
        navItems.add("About");

        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, navItems));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new NavClickListener());

        //Handling navigation drawer open/close
        mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                System.out.println("Just Opened the drawer");
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                System.out.println("Just Closed the drawer");
            }
        });

        //Let's also set up the TTS engine
        JourneyHome.speaker = getInstance();

        //Finally, initialize some globals
        stopType = "";
        timeToStop = 0;
        distanceFromRoute = 0;
        marker = null;
    }

    @Override
    protected void onStart() {
        client.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        client.disconnect();
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(JourneyHome.currentLocation));
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLocationRequest = MapSupport.createLocationRequest();
        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(client, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {
                //Need to implement!
                //URL: https://developer.android.com/training/location/change-location-settings.html
                System.out.println("Result of PendingResult: " + locationSettingsResult);
            }
        });

        com.google.android.gms.location.LocationListener listener = new com.google.android.gms.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                MapSupport.updateLocation(location);
            }
        };
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                client, mLocationRequest, listener);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        System.out.println("Connection suspended. Cause: " + cause);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // An unresolvable error has occurred and a connection to Google APIs
        // could not be established. Display an error message, or handle
        // the failure silently

        // ...
        System.out.println("Connection Error");
    }

    /**
     * This function gets called when the "Start Road Trip" button is pressed
     * It will begin a new Activity
     *
     * @param view
     */
    public void startHandler(View view) {
        Intent intent = new Intent(this, StartTrip.class);
        //If we decide to pass values from this screen, we do that here
        startActivityForResult(intent, JourneyHome.START_TRIP);
    }

    /**
     * This function gets called when the "Join Road Trip" button is pressed
     * It will begin a new Activity
     *
     * @param view
     */
    public void joinHandler(View view) {
        Intent intent = new Intent(this, JoinTrip.class);
        //If we decide to pass values from this screen, we do that here
        startActivity(intent);
    }

    public void stopHandler(View view) {
        //Do nothing right now
        System.out.println("Called for a stop");
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == JourneyHome.START_TRIP) {
            // From Start Handler
            if (resultCode == RESULT_OK) {
                //It worked
                //We will start by changing the buttons on screen
                adjustView();

                //Now we will deal with the route directions
                JSONObject directions = null;
                try {
                    directions = new JSONObject(data.getStringExtra("JSONDirections"));
                    System.out.println(directions);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String valid = null;
                try {
                    valid = directions.getString("status");
                    if (!valid.equals("OK")) return;
                    //For this particular function, we do not need to worry about waypoints
                    JSONArray steps = MapSupport.getRouteSteps(directions, true);

                    //Need to convert polyline points into legitimate points
                    //Reverse engineering this: https://developers.google.com/maps/documentation/utilities/polylinealgorithm
                    List<LatLng> leg = MapSupport.convertPolyline(steps);

                    //Next we need to create a PolylineOptions object and give it all of the points in the step
                    PolylineOptions options = new PolylineOptions();
                    for (LatLng coord : leg) {
                        options.add(coord);
                    }

                    //Finally, we add the polyline to the map
                    mMap.addPolyline(options);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                //Null directions
            }
        }
        else if (requestCode == JourneyHome.VOICE_START) {
            //Voice recognition
            if (resultCode == RESULT_OK) {
                //Currently just going to repeat what was said here
                if (!JourneyHome.useTTS) {
                    //Cannot use speech engine in this case
                    System.out.println("Failed to set up TTS");
                    return;
                }

                //TTS is set up
                while (speaker.isSpeaking()) {} //Right now doing a busy wait, definitely a better way to do this though
                List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                String phrase = results.get(0);
                phrase = phrase.toLowerCase();
                if (phrase.contains("request a stop") || phrase.contains("request stop") || phrase.contains("make a stop")
                        || phrase.contains("make stop")) {
                    //The user is asking to request a stop, so we need to handle appropriately
                    VoiceSupport.tts("Would you like to stop for food, gas, sight-seeing, or other? Say 'cancel' to exit", "stopType");
                    while (speaker.isSpeaking()) {}
                    voiceComm("Choices: Food, Gas, Sight-seeing, Other", JourneyHome.VOICE_REQUEST);
                }
                else {
                    //The user is not requesting a stop
                    VoiceSupport.tts("To use me, say 'Request a Stop' or 'Make a Stop'. I will help with adding stops to your route", "help");
                }
            }
        }
        else if (requestCode == JourneyHome.VOICE_REQUEST) {
            //User requested a type of stop
            if (resultCode == RESULT_OK) {
                List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String phrase = results.get(0);
                phrase = phrase.toLowerCase();
                if (phrase.contains("food")) {
                    stopType = "food";
                }
                else if (phrase.contains("gas")) {
                    stopType = "gas";
                }
                else if (phrase.contains("sightseeing") || phrase.contains("sights")) {
                    stopType = "sights";
                }
                else if (phrase.contains("other")) {
                    stopType = "other";
                }
                else if (phrase.contains("cancel")) {
                    VoiceSupport.tts("Cancelling stop addition...", "cancelStop");
                    return;
                }
                else {
                    VoiceSupport.tts("I'm sorry, please try again. Would you like to stop for food, gas, sight-seeing, or other? " +
                            "Say 'cancel' to exit", "help2");
                    while (speaker.isSpeaking()) {}
                    voiceComm("Choices: Food, Gas, Sight-seeing, Other", JourneyHome.VOICE_REQUEST);
                    return;
                }

                //Go to next step of process
                VoiceSupport.tts("When would you like to stop? Either say a time or 'Within blank minutes'", "timeRequest");
                while (speaker.isSpeaking()) {}
                voiceComm("Say either a time (xx:xx) or 'Within __ minutes'", JourneyHome.VOICE_TIME);
            }
        }
        else if (requestCode == JourneyHome.VOICE_TIME) {
            //User requested a time
            if (resultCode == RESULT_OK) {
                List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String phrase = results.get(0);
                phrase = phrase.toLowerCase();
                if (phrase.contains("o'clock") || phrase.contains("oclock")) {
                    //Time "on the hour"
                    int hour = VoiceSupport.getTimeOnHour(phrase);

                    //Current time
                    int currentTime = VoiceSupport.getCurrentTime();
                    if (currentTime > 779) hour += 720; //Make sure in the same time; this may need to change at some point?

                    timeToStop = hour - currentTime; //This gets us an approximate time
                    if (timeToStop <= 0) {
                        VoiceSupport.tts("I'm sorry, the time you have asked for has already passed. Please try again. " +
                                "Either say a time or 'Within blank minutes'", "help3");
                        while (speaker.isSpeaking()) {}
                        voiceComm("Say either a time (xx:xx) or 'Within __ minutes'", JourneyHome.VOICE_TIME);
                        return;
                    }
                    System.out.println("Calculated time to stop: " + timeToStop);
                }
                else if (phrase.contains(":")) {
                    //Time with minutes included
                    int time = VoiceSupport.getTime(phrase);

                    //Current time
                    int currentTime = VoiceSupport.getCurrentTime();
                    if (currentTime > 779) time += 720;

                    timeToStop = time - currentTime;
                    if (timeToStop <= 0) {
                        VoiceSupport.tts("I'm sorry, the time you have asked for has already passed. Please try again. " +
                                "Either say a time or 'Within blank minutes'", "help3");
                        while (speaker.isSpeaking()) {}
                        voiceComm("Say either a time (xx:xx) or 'Within __ minutes'", JourneyHome.VOICE_TIME);
                        return;
                    }
                    System.out.println("Calculated time to stop: " + timeToStop);
                }
                else if (phrase.contains("within") || phrase.contains("with in")) {
                    timeToStop = VoiceSupport.getTimeWithin(phrase);
                    if (timeToStop == -1) {
                        //There was an issue with parsing an integer
                        VoiceSupport.tts("I'm sorry, there was an issue processing your request. Please try again. " +
                                "Either say a time or 'Within blank minutes'", "help3");
                        while (speaker.isSpeaking()) {}
                        voiceComm("Say either a time (xx:xx) or 'Within __ minutes'", JourneyHome.VOICE_TIME);
                        return;
                    }
                    System.out.println("Calculated time to stop: " + timeToStop);
                }
                else {
                    VoiceSupport.tts("I'm sorry, please try again. Either say a time or 'Within blank minutes'", "repeatTime");
                    while (speaker.isSpeaking()) {}
                    voiceComm("Say either a time (xx:xx) or 'Within __ minutes'", JourneyHome.VOICE_TIME);
                    return;
                }

                //This means we now have a time. If distance is not defined, we will ask. Otherwise, move to calculation
                if (distanceFromRoute == 0) {
                    VoiceSupport.tts("How far off your route are you willing to go?", "distance");
                    while (speaker.isSpeaking()) {}
                    voiceComm("Distance in miles or kilometers", JourneyHome.VOICE_DISTANCE);
                }
                else {
                    VoiceSupport.tts("Finding stops, please wait...", "calculating");
                    while (speaker.isSpeaking()) {}
                    //voiceComm("Say either a time (xx:xx) or 'Within __ minutes'", JourneyHome.VOICE_TIME);
                }
            }
        }
        else if (requestCode == JourneyHome.VOICE_DISTANCE) {
            //Distance request
            if (resultCode == RESULT_OK) {
                List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String phrase = results.get(0);
                phrase = phrase.toLowerCase();
                if (phrase.contains("mile")) {
                    int index = phrase.indexOf("mile");
                    double calcDistance = VoiceSupport.getDistance(phrase, index);
                    if (calcDistance == -1) {
                        //There was an issue
                        VoiceSupport.tts("I'm sorry, there was an issue processing your request. Please try again. " +
                                "How far off your route are you willing to go?", "help3");
                        while (speaker.isSpeaking()) {}
                        voiceComm("Distance in miles or kilometers", JourneyHome.VOICE_DISTANCE);
                        return;
                    }

                    //We need to convert into meters
                    distanceFromRoute = (int) (calcDistance * 1609);
                }
                else if (phrase.contains("kilometer") || phrase.contains("kilometre")) {
                    int index = phrase.indexOf("kilometer");
                    if (index == -1) index = phrase.indexOf("kilometre");
                    double calcDistance = VoiceSupport.getDistance(phrase, index);
                    if (calcDistance == -1) {
                        //There was an issue
                        VoiceSupport.tts("I'm sorry, there was an issue processing your request. Please try again. " +
                                "How far off your route are you willing to go?", "help3");
                        while (speaker.isSpeaking()) {}
                        voiceComm("Distance in miles or kilometers", JourneyHome.VOICE_DISTANCE);
                        return;
                    }

                    //Convert to meters
                    distanceFromRoute = (int) (calcDistance * 1000);
                }
                else {
                    VoiceSupport.tts("I'm sorry, please try again. How far off your route are you willing to go?", "help4");
                    while (speaker.isSpeaking()) {}
                    voiceComm("Distance in miles or kilometers", JourneyHome.VOICE_DISTANCE);
                    return;
                }

                //Now we go to calculations
                System.out.println("Distance: " + distanceFromRoute);
            }
        }
    }

    public void adjustView() {
        ViewGroup layout = (RelativeLayout) JourneyHome.this.findViewById(R.id.journey_layout);
        Button startButton = (Button) JourneyHome.this.findViewById(R.id.start_trip);
        View joinButton = (Button) JourneyHome.this.findViewById(R.id.join_trip);
        layout.removeView(joinButton);

        //Will simply change the text and activity of the start button to "Add Stop"
        startButton.setText("Add Stop to Route");
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopHandler(v);
            }
        });

        //Need to also add a speech button
        ImageButton speech = new ImageButton(JourneyHome.this);
        //Image edited from: http://3.bp.blogspot.com/-WOpREKAmsfY/Ua2uLrKiGuI/AAAAAAABJu0/yJt8I49pO5o/s640/chrome-iphone-voice-search-2.png
        speech.setImageResource(R.drawable.google_microphone_logo);
        speech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                voiceComm("Start Stop Request Process by saying 'Request Stop' or 'Make Stop'", JourneyHome.VOICE_START);
            }
        });

        //Now we need to deal with Layout parameters
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        speech.setLayoutParams(params);
        speech.getBackground().setAlpha(0);
        speech.setId(R.id.speech_button);
        layout.addView(speech);
    }

    public void voiceComm(String helpText, int resultId) {
        //Start voice recognition activity
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, helpText);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, resultId);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            mMap.setMyLocationEnabled(true);
            mMap.moveCamera(CameraUpdateFactory.zoomTo(10));
        } catch (SecurityException e) {
            //TODO: Add something here?
        }
    }

    /**
     * This class serves as the onClickListener for our navigation drawer
     */
    private class NavClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            DrawerLayout drawer = (DrawerLayout) JourneyHome.this.findViewById(R.id.drawer_layout);
            drawer.closeDrawer(Gravity.LEFT);
            switch (position) {
                case 0:
                    //Log In
                    AlertDialog.Builder builder = new AlertDialog.Builder(JourneyHome.this);
                    LayoutInflater inflater = JourneyHome.this.getLayoutInflater();
                    builder.setView(inflater.inflate(R.layout.login, null))
                            .setPositiveButton("Log In", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //Log in here
                                    EditText emailView = (EditText) JourneyHome.this.findViewById(R.id.email);
                                    EditText passView = (EditText) JourneyHome.this.findViewById(R.id.password);
                                    Editable emailEdit = emailView.getText();
                                    Editable passEdit = passView.getText();
                                    String email = emailEdit.toString();
                                    String pass = passEdit.toString();
                                    System.out.println("Email: " + email + ", Password: " + pass);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .setTitle("Log Into Journey");
                    builder.show();
                    break;
                case 1:
                    //Settings
                    System.out.println("Settings selected");
                    break;
                case 2:
                    //Help
                    //First we need to remove whatever buttons are on the screen
                    if (JourneyHome.this.findViewById(R.id.start_trip) != null) {
                        //Trip not started yet
                        Button button = (Button) JourneyHome.this.findViewById(R.id.start_trip);
                        button.setVisibility(View.GONE);
                        button = (Button) JourneyHome.this.findViewById(R.id.join_trip);
                        button.setVisibility(View.GONE);
                    }
                    else {
                        //Trip started
                        Button button = (Button) JourneyHome.this.findViewById(R.id.start_trip);
                        button.setVisibility(View.GONE);
                        ImageButton ibutton = (ImageButton) JourneyHome.this.findViewById(R.id.speech_button);
                        ibutton.setVisibility(View.GONE);
                    }

                    FragmentManager manager = getFragmentManager();
                    manager.beginTransaction().replace(R.id.home_view, helpFragment).commit();
                    System.out.println("Help selected");
                    break;
                case 3:
                    //About
                    System.out.println("About selected");
                    break;
            }
        }
    }
}
