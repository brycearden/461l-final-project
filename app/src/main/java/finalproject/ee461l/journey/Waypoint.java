package finalproject.ee461l.journey;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by gordiewhite on 4/18/16.
 */
public class Waypoint extends AppCompatActivity {

    private String startLocId;
    private String endLocId;
    private String endLatLong;
    private String startLatLong;
    private Place waypoint;

    //For current location
    private LocationSupport loc;
    private PlaceAutocompleteFragment waypointFragment;
    private boolean usingCurrentLoc;
    private String currentLocation;
    private String currentLocPlaceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_waypoint);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Let's get the intent from JourneyHome
        Intent intent = getIntent();
        String location = intent.getExtras().getString(JourneyHome.CURRENT_LOCATION);
        String latlng[] = location.split(",");
        latlng[0] = latlng[0].substring(latlng[0].indexOf('(')+1); //Latitude
        latlng[1] = latlng[1].substring(0, latlng[1].indexOf(')')); //Longitude
        currentLocation = latlng[0] + "," + latlng[1];

        //Create LocationSupport object
        loc = new LocationSupport();
        loc.getCurrentLocationInfo(currentLocation);

        //get start and end location id for route
        startLocId = intent.getExtras().getString(JourneyHome.START_LOCATION);
        endLocId = intent.getExtras().getString(JourneyHome.END_LOCATION);
        startLatLong = intent.getExtras().getString(JourneyHome.START_LATLNG);
        endLatLong = intent.getExtras().getString(JourneyHome.END_LATLNG);

        //Let's set up the Places Listener
        waypointFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.new_waypoint);

        waypointFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                System.out.println("Place: " + place.getName());
                waypoint = place;
                usingCurrentLoc = false;
            }

            @Override

            public void onError(Status status) {
                // TODO: Handle the error.
                System.out.println("An error occurred: " + status);
            }
        });

        waypointFragment.setHint("Select Waypoint");

        usingCurrentLoc = false;
    }

    public void addWaypoint(View view) {
        System.out.println("made it");
        if(startLocId == null || endLocId == null || waypoint == null){return;}
        String waypointID = waypoint.getId();
        new TripRequest().execute(startLocId, endLocId, waypointID);
    }
    private class TripRequest extends AsyncTask<String, Void, String> {
        private String startId;
        private String endId;
        private String waypointId;

        Place startLoc;
        Place endLoc;

        protected String doInBackground(String... strings) {
            String result = "";

            int currentPlace = 0;
            for (int i = 0; i < 3; currentPlace++, i++) {
                if (currentPlace == 0) {
                    //StartPlace
                    startId = strings[i];
                } else if (currentPlace == 1){
                    //EndPlace
                    endId = strings[i];
                } else {
                    waypointId = strings[i];
                }
            }
            System.out.println(strings[2]);
            waypointId = strings[2];
            System.out.println(waypointId);
            try {
                URL url = null;
                url = new URL("https://maps.googleapis.com/maps/api/directions/json?origin=place_id:" + startId +
                        "&destination=place_id:" + endId + "&waypoints=place_id:" + waypointId + "&key=AIzaSyCsGbBFaG5NIf40zDsMgEZw8nh65I5fMw8");
                HttpsURLConnection request = (HttpsURLConnection) url.openConnection();
                String responseMessage = request.getResponseMessage();
                InputStream in = new BufferedInputStream(request.getInputStream());
                result = readStream(in);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        protected void onPostExecute(String result) {
            JSONObject directions = null;
            try {
                directions = new JSONObject(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent();
            intent.putExtra("JSONDirections", result);
            intent.putExtra("StartLocLatLng", startLatLong);
            intent.putExtra("EndLocLatLng", endLatLong);

            //Also add start/end place ids to intent
            intent.putExtra("StartLocationId", startId);
            intent.putExtra("EndLocationId", endId);

            if (directions != null) setResult(RESULT_OK, intent);
            else setResult(RESULT_CANCELED, intent);
            finish();
        }

        protected String readStream(InputStream in) throws IOException {
            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                builder.append(line);
            }
            in.close();
            return builder.toString();
        }
    }
}
