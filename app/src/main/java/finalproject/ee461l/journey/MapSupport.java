package finalproject.ee461l.journey;

import android.*;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.OnMapReadyCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by kevinrosen1 on 3/14/16.
 * This class will have map helper functions.
 * These include Location Updates to calculations for Map Routes
 */
public class MapSupport implements com.google.android.gms.location.LocationListener,
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    protected JourneyHome journeyHome;

    //Google Maps
    protected LocationRequest mLocationRequest;
    protected GoogleMap mMap;
    protected boolean firstUpdate;
    protected LatLng currentLocation;
    protected Marker marker;
    protected GoogleApiClient client;

    private ArrayList<LatLng> route;

    //Place IDs
    private String startLocationId;
    private String endLocationId;

    //Fragments
    private MapFragment mapFragment;

    //Text instructions for the route
    private ArrayList<String> directions;

    //Constants
    public static final String DIRECTIONS_ARRAY = "finalproject.ee461l.journey.DIRECTIONS_ARRAY";

    public MapSupport(JourneyHome home, FragmentManager manager, GoogleApiClient client){
        journeyHome = home;

        this.client = client;

        mapFragment = MapFragment.newInstance();
        setupMap(manager, mapFragment, home);

        marker = null;
        startLocationId = null;
        endLocationId = null;

        directions = new ArrayList<String>();
    }

    public void setClient(GoogleApiClient client){
        this.client = client;
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        mLocationRequest = createLocationRequest();
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

        //Now we need to check app permissions
        if (ActivityCompat.checkSelfPermission(journeyHome, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(journeyHome, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
            //TODO: Maybe check for the other dangerous permissions here?
            if (ActivityCompat.shouldShowRequestPermissionRationale(journeyHome, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                //TODO: show user why we need these permissions?
            }
            else {
                ActivityCompat.requestPermissions(journeyHome,
                        new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                        },
                        JourneyHome.MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            }
            return;
        }

        //com.google.android.gms.location.LocationListener listener = map;
        LocationServices.FusedLocationApi.requestLocationUpdates(
                client, mLocationRequest, this);
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
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
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

    public void onLocationChanged(Location location) {
        updateLocation(location);
    }

    public void setupMap(FragmentManager manager, MapFragment mapFragment, JourneyHome home) {
        manager.beginTransaction().add(R.id.home_view, mapFragment).commit();
        mapFragment.getMapAsync(this);
        firstUpdate = true;
        home.voice.useTTS = true;
    }

    public JSONArray getRouteSteps(JSONObject directions, boolean isFirstTime) {
        JSONArray steps = null;
        if (isFirstTime) {
            //No waypoints
            try {
                JSONArray routes = directions.getJSONArray("routes");

                //Need to get the legs[]
                JSONObject firstRoute = routes.optJSONObject(0); //If we look for more than 1 route, we'll need a loop
                JSONArray legs = firstRoute.getJSONArray("legs");

                //Need to get the steps[] now
                JSONObject firstLeg = legs.optJSONObject(0); //Once we add waypoints there will be more legs
                steps = firstLeg.getJSONArray("steps");
            }
            catch (JSONException e) {
                //JSON Error
            }
        }
        else {
            //At least 1 waypoint to worry about
        }
        return steps;
    }

    //Note: This function was written on our own, but a few pieces of the polyline decryption were cited from
    //      this GitHub repository: https://github.com/googlemaps/android-maps-utils, in the decode() function
    public ArrayList<LatLng> convertPolyline(JSONArray steps) {
        ArrayList<LatLng> leg = new ArrayList<LatLng>();
        for (int i = 0; i < steps.length(); i++) {
            String points = "";
            String instruction = "";
            try {
                points = steps.getJSONObject(i).getJSONObject("polyline").getString("points");
                //We will also get the HTML instructions for each step
                instruction = steps.getJSONObject(i).getString("html_instructions");
            }
            catch (JSONException e) {
                //JSON Error
            }
            //Let's first store the instruction
            directions.add(instruction);
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
        route = leg;
        return leg;
    }

    public static int[] getCoord(String points, int currIndex) {
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

    public void updateLocation(Location location) {
        LatLng currentLoc = new LatLng(location.getLatitude(), location.getLongitude());
        if (marker != null) marker.remove();
        marker = mMap.addMarker(new MarkerOptions().position(currentLoc).title("Current Location"));
        if (firstUpdate) {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLoc));
            firstUpdate = false;
        }
        currentLocation = currentLoc;
    }

    public LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    public void setIds(Intent data) {
        startLocationId = data.getExtras().getString("StartLocationId");
        endLocationId = data.getExtras().getString("EndLocationId");
    }

    public void adjustMapZoom(Intent data) {
        //First, let's get the latitude and longitude of the Start and End locations
        String startData = data.getExtras().getString("StartLocLatLng");
        String endData = data.getExtras().getString("EndLocLatLng");

        //Start with StartLoc
        LatLng startLoc = null;
        if (startData.equals("Current")) startLoc = currentLocation;
        else {
            String latlng[] = startData.split(",");
            latlng[0] = latlng[0].substring(latlng[0].indexOf('(')+1); //Latitude
            latlng[1] = latlng[1].substring(0, latlng[1].indexOf(')')); //Longitude
            startLoc = new LatLng(Double.parseDouble(latlng[0]), Double.parseDouble(latlng[1]));
        }
        System.out.println("Start: " + startLoc + ", " + startData);

        //EndLoc
        String latlng[] = endData.split(",");
        latlng[0] = latlng[0].substring(latlng[0].indexOf('(')+1); //Latitude
        latlng[1] = latlng[1].substring(0, latlng[1].indexOf(')')); //Longitude
        LatLng endLoc = new LatLng(Double.parseDouble(latlng[0]), Double.parseDouble(latlng[1]));
        System.out.println("End: " + endData + ", " + endLoc);

        //Now adjust camera zoom
        LatLngBounds.Builder builder = new LatLngBounds.Builder().include(startLoc).include(endLoc);
        for (int i = 0; i < route.size(); i++) {
            builder.include(route.get(i));
        }
        final LatLngBounds bounds = builder.build();
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 225), 2000, null);
            }
        });
    }

    public RelativeLayout addDirectionsToLayout() {
        RelativeLayout dirLayout = new RelativeLayout(journeyHome);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        dirLayout.setLayoutParams(params);
        dirLayout.setBackgroundColor(Color.WHITE);
        dirLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewDirections(v);
            }
        });

        TextView text = new TextView(journeyHome);
        text.setText("Route Directions");
        text.setTextColor(Color.argb(200, 255, 0, 0));
        text.setTextSize(2, 18); //18sp
        text.setPadding(0, 50, 0, 50);
        RelativeLayout.LayoutParams directionParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        directionParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        text.setLayoutParams(directionParams);
        dirLayout.addView(text);

        ImageView image = new ImageView(journeyHome);
        image.setImageResource(R.drawable.ic_directions_black_24dp);
        image.setPadding(50, 0, 0, 0);
        directionParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        directionParams.addRule(RelativeLayout.CENTER_VERTICAL);
        image.setLayoutParams(directionParams);
        dirLayout.addView(image);

        return dirLayout;
    }

    private void viewDirections(View view) {
        //Will open a new activity that will show the user directions
        Intent intent = new Intent(journeyHome, DisplayDirections.class);
        intent.putStringArrayListExtra(DIRECTIONS_ARRAY, directions);
        journeyHome.startActivity(intent);
    }
}
