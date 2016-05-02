package finalproject.ee461l.journey;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
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

    private boolean isTripActive;
    private boolean isLeader;

    private String prevRoute;

    //hack for nested activity
    protected static String startLocationId;
    protected static String endLocationId;
    protected static String startLatLng;
    protected static String endLatLng;
    protected static String result;
    protected static boolean dataReady;
    protected static boolean dataCorrect;
    protected static boolean usingHack;

    //OnActivityResult Constants
    public static final int START_TRIP = 0;
    public static final int JOIN_TRIP = 1;
    public static final int VOICE_START = 2;
    public static final int VOICE_REQUEST = 3;
    public static final int VOICE_TIME = 4;
    public static final int VOICE_DISTANCE = 5;
    public static final int VOICE_CALC = 4;
    public static final int ADD_WAYPOINT = 6;

    //Permissions Constants
    public static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 10;

    //Google sign-in
    public static final int GOOGLE_ACCT_SIGNIN = 50;
    private boolean isSignedIn;

    //Intent Constants
    public static final String CURRENT_LOCATION = "finalproject.ee461l.journey.CURRENT_LOCATION";
    public static final String START_LOCATION = "finalproject.ee461l.journey.START_LOCATION";
    public static final String END_LOCATION = "finalproject.ee461l.journey.END_LOCATION";
    public static final String START_LATLNG = "finalproject.ee461l.journey.START_LATLONG";
    public static final String END_LATLNG = "finalproject.ee461l.journey.END_LATLNG";

    //Waypoint adding
    protected String stopType;
    protected int timeToStop;
    protected int distanceFromRoute;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    GoogleApiClient client;
    GoogleSignInOptions options;


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
        options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        map = new MapSupport(this, manager, client);
        voice = new VoiceSupport(this);
        // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(map)
                .addOnConnectionFailedListener(map)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(Auth.GOOGLE_SIGN_IN_API, options)
                .addApi(AppIndex.API).build();

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
        isTripActive = false;
        isLeader = false;
    }

    @Override
    protected void onStart() {
        client.connect();
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "JourneyHome Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://finalproject.ee461l.journey/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    protected void onStop() {
        client.disconnect();
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "JourneyHome Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://finalproject.ee461l.journey/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
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
        if (!isSignedIn) {
            //We do not want to allow trip creation unless the user is signed in
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Login Required")
                    .setMessage("You must sign in with your Google Account before creating a Trip. "
                            + "Log in from the Navigation Drawer.")
                    .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.out.println("User clicked okay");
                        }
                    })
                    .show();
            return;
        }
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
        if (!isSignedIn) {
            //We do not want to allow trip creation unless the user is signed in
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Login Required")
                    .setMessage("You must sign in with your Google Account before joining a Trip. "
                            + "Log in from the Navigation Drawer.")
                    .setNeutralButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.out.println("User clicked okay");
                        }
                    })
                    .show();
            return;
        }
        Intent intent = new Intent(this, JoinTrip.class);
        intent.putExtra("UserEmail", nav.getUserEmail());
        startActivityForResult(intent, JourneyHome.JOIN_TRIP);
    }

    public void stopHandler(View view) {
        Intent waypointIntent = new Intent(this, Waypoint.class);
        //If we decide to pass values from this screen, we do that here
        waypointIntent.putExtra(CURRENT_LOCATION, map.currentLocation.toString());

        String startLocId = map.getStartLocID();
        String endLocId = map.getEndLocID();
        String endLatLong = map.getEndLatLng();
        String startLatLong = map.getStartLatLng();

        waypointIntent.putExtra(START_LOCATION, startLocId);
        waypointIntent.putExtra(END_LOCATION, endLocId);
        waypointIntent.putExtra(START_LATLNG, startLatLong);
        waypointIntent.putExtra(END_LATLNG, endLatLong);
        waypointIntent.putExtra("JSONDirection", prevRoute);

        startActivityForResult(waypointIntent, JourneyHome.ADD_WAYPOINT);
    }

    public void toggleDrawer(View view) {
        nav.toggleDrawer();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == JourneyHome.START_TRIP) {
            // From Start Handler
            if (resultCode == RESULT_OK) {
                //It worked
                isTripActive = true;
                isLeader = true;
                journeyStartTrip(data);
                Toast.makeText(this, "Successfully Created a Trip", Toast.LENGTH_LONG).show();
            }
            else {
                //Null directions
                Toast.makeText(this, "Failed to Create a Trip. Please try again", Toast.LENGTH_LONG).show();
            }
        }
        else if (requestCode == JourneyHome.JOIN_TRIP) {
            if (resultCode == RESULT_OK) {
                //It worked
                isTripActive = true;
                isLeader = false;
                boolean isWaypoint = data.getBooleanExtra("isWaypoint", false);
                if (!isWaypoint) journeyStartTrip(data);
                else journeyStartWaypointTrip(data);
                Toast.makeText(this, "Successfully Joined an Active Trip", Toast.LENGTH_LONG).show();
            }
            else {
                //Didn't work\
                Toast.makeText(this, "Failed to Join a Trip. Ensure that you entered the Host's email correctly", Toast.LENGTH_LONG).show();
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
        else if (requestCode == JourneyHome.ADD_WAYPOINT) {
            if (!usingHack) {
                if (resultCode == RESULT_OK) {
                    System.out.println(data.getStringExtra("JSONDirections"));
                    map.mMap.clear();
                    data.putExtra("isCaravanTrip", map.getCaravanTrip());
                    data.putExtra("numWaypoints", map.numWaypoints+1);
                    journeyStartWaypointTrip(data);
                }
            } else {
                usingHack = false;
                while(!dataReady){}
                dataReady = false;
                if (dataCorrect) {
                    Intent intent = new Intent(data);
                    intent.putExtra("isCaravanTrip", map.getCaravanTrip());
                    intent.putExtra("numWaypoints", map.numWaypoints+1);
                    map.mMap.clear();
                    journeyStartWaypointTrip(intent);
                }
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
        map.setCaravanTrip(data.getBooleanExtra("isCaravanTrip", false));

        prevRoute = data.getStringExtra("JSONDirections");

        //Now we will deal with the route directions
        JSONObject directions = null;
        try {
            directions = new JSONObject(data.getStringExtra("JSONDirections"));
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

        //If we are caravaning the trip, we need to post to the backend
        if (map.getCaravanTrip() && isLeader) map.postTripToBackend(nav.getUserEmail());
    }

    public void journeyStartWaypointTrip(Intent data) {
        //We will start by changing the buttons on screen
        adjustView();
        map.setIds(data);
        map.setCaravanTrip(data.getBooleanExtra("isCaravanTrip", false));
        map.numWaypoints = data.getIntExtra("numWaypoints", 0);

        int numWaypoints = 1;

        prevRoute = data.getStringExtra("JSONDirections");

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

            //multiple steps for waypoints
            JSONArray steps1 = map.getRouteSteps(directions, true);
            JSONArray steps2 = map.getRouteSteps(directions, false);

            //Need to convert polyline points into legitimate points
            //Reverse engineering this: https://developers.google.com/maps/documentation/utilities/polylinealgorithm
            List<LatLng> leg1 = map.convertPolyline(steps1);
            List<LatLng> leg2 = map.convertPolyline(steps2);

            //Next we need to create a PolylineOptions object and give it all of the points in the step
            PolylineOptions options = new PolylineOptions();
            for (LatLng coord : leg1) {
                options.add(coord);
            }
            for (LatLng coord : leg2) {
                options.add(coord);
            }

            //Finally, we add the polyline to the map
            map.mMap.addPolyline(options);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Finally we will adjust the zoom to the appropriate level
        map.adjustMapZoom(data);

        //Note: Need waypoint lat/lng coordinates for this!
        if (map.getCaravanTrip() && isLeader) map.postWaypointToBackend(nav.getUserEmail(), data.getStringExtra("WaypointLatLng"));
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

        //Need to create a Directions bar at the bottom
        RelativeLayout dirLayout = map.addDirectionsToLayout();

        //Now we need to deal with Layout parameters
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        speech.setLayoutParams(params);
        speech.getBackground().setAlpha(0);
        speech.setId(R.id.speech_button);
        layout.addView(speech);
        layout.addView(dirLayout);
    }

    @Override
    public void onBackPressed() {
        FragmentManager manager = getFragmentManager();
        if (manager.getBackStackEntryCount() > 0) {
            manager.popBackStack();
            if (manager.getBackStackEntryCount() == 1) {
                //This is the last thing in the stack; therefore it is the MapFragment. Let's restore buttons
                Button button = (Button) JourneyHome.this.findViewById(R.id.start_trip);
                button.setVisibility(View.VISIBLE);
                ImageButton menu = (ImageButton) JourneyHome.this.findViewById(R.id.menu);
                menu.setVisibility(View.VISIBLE);
                if (JourneyHome.this.findViewById(R.id.join_trip) != null) {
                    //Trip not started yet
                    button = (Button) JourneyHome.this.findViewById(R.id.join_trip);
                    button.setVisibility(View.VISIBLE);
                }
                else {
                    //Trip started
                    ImageButton ibutton = (ImageButton) JourneyHome.this.findViewById(R.id.speech_button);
                    ibutton.setVisibility(View.VISIBLE);
                    RelativeLayout dirs = (RelativeLayout) JourneyHome.this.findViewById(R.id.directions_button);
                    dirs.setVisibility(View.VISIBLE);
                }
            }
        }
        else {
            if (isTripActive) {
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("End Trip")
                        .setMessage("Are you sure you want to exit this trip? If you created this trip it will be deleted.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (map.getCaravanTrip()) map.deleteTripFromBackend(nav.getUserEmail(), true);
                                else map.deleteTripFromBackend(nav.getUserEmail(), false);
                            }

                        })
                        .setNegativeButton("No", null)
                        .show();
            }
            else super.onBackPressed();
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
                    Button button = (Button) JourneyHome.this.findViewById(R.id.start_trip);
                    button.setVisibility(View.GONE);
                    ImageButton menu = (ImageButton) JourneyHome.this.findViewById(R.id.menu);
                    menu.setVisibility(View.GONE);
                    if (JourneyHome.this.findViewById(R.id.join_trip) != null) {
                        //Trip not started yet
                        button = (Button) JourneyHome.this.findViewById(R.id.join_trip);
                        button.setVisibility(View.GONE);
                    }
                    else {
                        //Trip started
                        ImageButton ibutton = (ImageButton) JourneyHome.this.findViewById(R.id.speech_button);
                        ibutton.setVisibility(View.GONE);
                        RelativeLayout dirs = (RelativeLayout) JourneyHome.this.findViewById(R.id.directions_button);
                        dirs.setVisibility(View.GONE);
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
