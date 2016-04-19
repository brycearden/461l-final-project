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

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;

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

public class JourneyHome extends FragmentActivity {


    private HelpFragment helpFragment;

    protected VoiceSupport voice;
    protected MapSupport map;
    protected NavDrawerSupport nav;

    //OnActivityResult Constants
    public static final int START_TRIP = 0;
    public static final int JOIN_TRIP = 1;
    public static final int VOICE_START = 2;
    public static final int VOICE_REQUEST = 3;
    public static final int VOICE_TIME = 4;
    public static final int VOICE_DISTANCE = 5;
    public static final int VOICE_CALC = 4;

    //Permissions Constants
    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 10;

    //Google sign-in
    public static final int GOOGLE_ACCT_SIGNIN = 50;
    private boolean isSignedIn;

    //Intent Constants
    public static final String CURRENT_LOCATION = "finalproject.ee461l.journey.CURRENT_LOCATION";

    //Waypoint adding
    protected String stopType;
    protected int timeToStop;
    protected int distanceFromRoute;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    GoogleApiClient client;


    //only reason this is in


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_journey_home);

        //Initialize fragments
        //mapFragment = MapFragment.newInstance();
        helpFragment = HelpFragment.getInstance();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        // Note: Doing it this way may be slower?
        FragmentManager manager = getFragmentManager();

        //Configure Google sign-in options
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        map = new MapSupport(this, manager, client);
        voice = new VoiceSupport(this);
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(map)
                .addOnConnectionFailedListener(map)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(Auth.GOOGLE_SIGN_IN_API, options)
                .build();

        map.setClient(client);
        //Navigation Drawer
        DrawerLayout mDrawerLayout = (DrawerLayout) this.findViewById(R.id.drawer_layout);
        ListView mDrawerList = (ListView) this.findViewById(R.id.left_drawer);
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new NavClickListener());
        //Rest of setup
        //GeneralSupport.navDrawer(mDrawerList, mDrawerLayout, this);
        nav = new NavDrawerSupport(this, mDrawerList, mDrawerLayout);

        //Finally, initialize some globals
        stopType = "";
        timeToStop = 0;
        distanceFromRoute = 0;
        isSignedIn = false;
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



    /**
     * This function gets called when the "Start Road Trip" button is pressed
     * It will begin a new Activity
     *
     * @param view
     */
    public void startHandler(View view) {
        Intent intent = new Intent(this, StartTrip.class);
        //If we decide to pass values from this screen, we do that here
        intent.putExtra(CURRENT_LOCATION, map.currentLocation.toString());
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
        else if (requestCode == GOOGLE_ACCT_SIGNIN) {
            isSignedIn = nav.signIn(data, this);
        }
    }

    public void journeyStartTrip(Intent data) {
        //We will start by changing the buttons on screen
        adjustView();
        map.setIds(data);

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

        //Finally we will adjust the zoom to the appropriate level
        map.adjustMapZoom(data);
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

    @Override
    public void onBackPressed() {
        FragmentManager manager = getFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            manager.popBackStack();
        }
        else {
            super.onBackPressed();
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
                    if (!isSignedIn) {
                        //Log In
                        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(client);
                        startActivityForResult(signInIntent, GOOGLE_ACCT_SIGNIN);
                    }
                    else {
                        //Log out
                        //NOTE: We have to confirm that onConnected was called before running this!
                        isSignedIn = nav.signOut(JourneyHome.this, client);
                    }
                    break;
                case 1:
                    //Settings
                    System.out.println("Settings selected");
                    break;
                case 2:
                    //Help
                    //First we need to remove whatever buttons are on the screen
                    if (JourneyHome.this.findViewById(R.id.join_trip) != null) {
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
                    manager.beginTransaction().addToBackStack("home").replace(R.id.home_view, helpFragment).commit();
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
