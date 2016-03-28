package finalproject.ee461l.journey;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
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
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class JourneyHome extends FragmentActivity implements
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    private HelpFragment helpFragment;



    protected VoiceSupport voice;
    protected MapSupport map;

    //OnActivityResult Constants
    public static final int START_TRIP = 0;
    public static final int JOIN_TRIP = 1;
    public static final int VOICE_START = 2;
    public static final int VOICE_REQUEST = 3;
    public static final int VOICE_TIME = 4;
    public static final int VOICE_DISTANCE = 5;
    public static final int VOICE_CALC = 4;

    //Permissions Constants
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 10;

    //Waypoint adding
    protected String stopType;
    protected int timeToStop;
    protected int distanceFromRoute;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    GoogleApiClient client;


    protected TextToSpeech getInstance() {
        if (voice.speaker != null) return voice.speaker;
        return new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    //Failed to set up TTS engine
                    voice.speaker.setLanguage(Locale.US);
                } else {
                    voice.useTTS = false;
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_home);


        //Initialize fragments
        //mapFragment = MapFragment.newInstance();
        helpFragment = HelpFragment.getInstance();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        // Note: Doing it this way may be slower?
        FragmentManager manager = getFragmentManager();

        map = new MapSupport(this, manager);
        voice = new VoiceSupport(this);
        //Navigation Drawer
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new NavClickListener());
        //Rest of setup
        GeneralSupport.navDrawer(mDrawerList, mDrawerLayout, this);



        //Finally, initialize some globals
        stopType = "";
        timeToStop = 0;
        distanceFromRoute = 0;
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
        map.mMap.moveCamera(CameraUpdateFactory.newLatLng(map.currentLocation));
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        map.mLocationRequest = map.createLocationRequest();
        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder().addLocationRequest(map.mLocationRequest);
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

        //Now we need to check app permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //TODO: Maybe check for the other dangerous permissions here?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                //TODO: show user why we need these permissions?
            }
            else {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        },
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            }
            return;
        }

        //com.google.android.gms.location.LocationListener listener = map;
        LocationServices.FusedLocationApi.requestLocationUpdates(
                client, map.mLocationRequest, map);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION:
                //Request for fine location
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    //com.google.android.gms.location.LocationListener listener = GeneralSupport.getLocationListener();
                    try {
                        LocationServices.FusedLocationApi.requestLocationUpdates(
                                client, map.mLocationRequest, map);
                    }
                    catch (SecurityException e) {
                        //This should not happen since the permission has been granted!
                        System.out.println("Permission was granted but still failed to get location?");
                    }
                } else {
                    // Permission denied
                    //TODO: Implement some handling of this situation
                    System.out.println("Permission denied");
                }
                return;
        }
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
                journeyStartTrip(data);
            }
            else {
                //Null directions
                System.out.println("Returned null directions from StartTrip");
            }
        }
        else if (requestCode == JourneyHome.VOICE_START) {
            //Voice recognition
            if (resultCode == RESULT_OK) {
                voice.startVoiceRecog(data);
            }
        }
        else if (requestCode == JourneyHome.VOICE_REQUEST) {
            //User requested a type of stop
            if (resultCode == RESULT_OK) {
                stopType = voice.voiceStopRequested(data);
            }
        }
        else if (requestCode == JourneyHome.VOICE_TIME) {
            //User requested a time
            if (resultCode == RESULT_OK) {
                timeToStop = voice.voiceStopTime(data);
            }
        }
        else if (requestCode == JourneyHome.VOICE_DISTANCE) {
            //Distance request
            if (resultCode == RESULT_OK) {
                distanceFromRoute = voice.voiceStopDistance(data);
            }
        }
    }

    public void journeyStartTrip(Intent data) {
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
            JSONArray steps = map.getRouteSteps(directions, true);

            //Need to convert polyline points into legitimate points
            //Reverse engineering this: https://developers.google.com/maps/documentation/utilities/polylinealgorithm
            List<LatLng> leg = map.convertPolyline(steps);

            //Next we need to create a PolylineOptions object and give it all of the points in the step
            PolylineOptions options = new PolylineOptions();
            for (LatLng coord : leg) {
                options.add(coord);
            }

            //Finally, we add the polyline to the map
            map.mMap.addPolyline(options);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    public void voiceComm(String helpText, int resultId) {
        //Start voice recognition activity
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, helpText);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        startActivityForResult(intent, resultId);
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
        map.mMap = googleMap;

        try {
            map.mMap.setMyLocationEnabled(true);
            map.mMap.moveCamera(CameraUpdateFactory.zoomTo(10));
        } catch (SecurityException e) {
            //TODO: Add something here?
        }
    }


    /**
     * This class serves as the onClickListener for our navigation drawer
     */
    class NavClickListener implements ListView.OnItemClickListener {
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
