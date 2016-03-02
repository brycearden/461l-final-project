package finalproject.ee461l.journey;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class JourneyHome extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static boolean firstUpdate;
    private static LatLng currentLocation;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey_home);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        JourneyHome.firstUpdate = true;

        //Navigation Drawer
        ArrayList<String> navItems = new ArrayList<String>();
        navItems.add("Log In");
        navItems.add("Settings");

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
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(JourneyHome.currentLocation));
    }

    private class NavClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {
            System.out.println("Testing the clicking of a button, with position " + position);
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
        startActivityForResult(intent, 0);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            // From Start Handler
            if (resultCode == RESULT_OK) {
                //It worked
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
                    JSONArray routes = directions.getJSONArray("routes");

                    //Need to get the legs[]
                    JSONObject firstRoute = routes.optJSONObject(0); //If we look for more than 1 route, we'll need a loop
                    JSONArray legs = firstRoute.getJSONArray("legs");

                    //Need to get the steps[] now
                    JSONObject firstLeg = legs.optJSONObject(0); //Once we add waypoints there will be more legs
                    JSONArray steps = firstLeg.getJSONArray("steps");

                    //Need to convert polyline points into legitimate points
                    //Reverse engineering this: https://developers.google.com/maps/documentation/utilities/polylinealgorithm
                    List<LatLng> leg = new ArrayList<LatLng>();
                    for (int i = 0; i < steps.length(); i++) {
                        String points = steps.getJSONObject(i).getJSONObject("polyline").getString("points");
                        double latitude = 0;
                        double longitude = 0; //Out here b/c path uses relative lat/lng changes
                        int index = 0;
                        int length = points.length();
                        while (index < length) {
                            //Need to decode each character
                            //Start with latitude
                            int[] lat = getCoord(points, index);
                            latitude += lat[0] / 1e5; //step 2
                            index = lat[1];

                            //Repeat with longitude
                            int[] lng = getCoord(points, index);
                            longitude += lng[0] / 1e5;
                            index = lng[1];

                            LatLng current = new LatLng(latitude, longitude);
                            leg.add(current);
                        }
                    }

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
    }

    public int[] getCoord(String points, int currIndex) {
        int result = 1;
        int shift = 0;
        int character = 0x20;
        int index = currIndex;
        while (character >= 0x1f) {
            //>=x1f is because every chunk gets ORd with 0x20 except the last one per code
            if (index >= points.length()) break;
            character = points.charAt(index);
            character -= 64; //step 10
            result += (character << shift); //step 7
            shift += 5; //5-bit chunks
            index++;
        }

        //Need to determine if original value was negative or not (step 5)
        //Since there is a left shift of 1 before any inversion, a positive # will always have
        //a 0 in the LSB
        if ((result & 1) == 1) result = ~(result >> 1); //RSHF on inside so MSB = 1
        else result = result >> 1; //step 4

        return new int[] {result, index};
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

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                updateLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

        // Register the listener with the Location Manager to receive location updates
        try {
            mMap.setMyLocationEnabled(true);
            mMap.moveCamera(CameraUpdateFactory.zoomTo(10));
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        } catch (SecurityException e) {
        }

        // Add a marker in Sydney and move the camera
        /*
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        */
    }

    public void updateLocation(Location location) {
        LatLng currentLoc = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(currentLoc).title("Current Location"));
        if (JourneyHome.firstUpdate) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLoc));
            JourneyHome.firstUpdate = false;
        }
        JourneyHome.currentLocation = currentLoc;
    }
}
