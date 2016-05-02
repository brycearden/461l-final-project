package finalproject.ee461l.journey;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

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
    private String waypointLatLong;

    private String prevRoute;
    private boolean useCurrentSpot;
    private String checkSpot;

    //used to store range of place search
    private int miles;
    private int meters;

    //used to store spinner selection
    private String resource;

    private boolean ready = false;

    private JSONObject[] nearbyPlaces;

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

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.places_choices_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new SpinnerActivity());

        spinner.setGravity(Gravity.CENTER_HORIZONTAL);

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

        prevRoute = intent.getStringExtra("JSONDirection");

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

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    public void findNearbyPlaces(View view) {
        System.out.println("finding new places");
        if (resource != null) {
            EditText mEdit   = (EditText)findViewById(R.id.distance);
            String value = mEdit.getText().toString();
            mEdit   = (EditText)findViewById(R.id.alongRoute);
            String alongRoute = mEdit.getText().toString();
            if (!value.equals("")) {
                miles = Integer.parseInt(value);
            } else {
                miles = 5;
            }
            meters = miles * 1609;
            if (!alongRoute.equals("")) {
                useCurrentSpot = false;
                int distance = Integer.parseInt(alongRoute);
                distance *= 1609;
                JSONObject directions = null;
                try {
                    directions = new JSONObject(prevRoute);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                parse(directions,distance);
            } else {
                useCurrentSpot = true;
            }
            new FindPlace(this).execute();
        }
    }
    private class FindPlace extends AsyncTask<String, Void, String> {
        String location;
        private ProgressDialog dialog;
        private Waypoint activity;

        public FindPlace(Waypoint obj) {
            dialog = new ProgressDialog(Waypoint.this);
            activity = obj;
        }

        protected String doInBackground(String... strings) {
            String httpData = "";
            JSONObject[] place = null;
            boolean finished = false;
            JSONObject places = null;
            JSONArray[] jsonArray = new JSONArray[3];
            int numResponses = 0;
            String token = null;
            int count = 0;
            try {
                while (!finished) {
                    String googlePlacesUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
                    if (numResponses == 0) {
                        if (!useCurrentSpot) {
                            googlePlacesUrl += "location=" + checkSpot;
                        } else {
                            googlePlacesUrl += "location=" + currentLocation;
                        }
                        googlePlacesUrl += "&radius=" + meters;
                        googlePlacesUrl += "&types=" + resource;
                    } else {
                        googlePlacesUrl += "&pagetoken=" + token;
                    }
                    googlePlacesUrl += "&key=AIzaSyCsGbBFaG5NIf40zDsMgEZw8nh65I5fMw8";

                    URL url = null;
                    url = new URL(googlePlacesUrl);


                    InputStream inputStream = null;
                    HttpURLConnection httpURLConnection = null;

                    httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.connect();
                    inputStream = httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuffer stringBuffer = new StringBuffer();
                    String line = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuffer.append(line);
                    }
                    httpData = stringBuffer.toString();
                    bufferedReader.close();
                    httpURLConnection.disconnect();

                    try {
                        places = new JSONObject(httpData);
                        if (places.has("next_page_token")) {
                            token = places.getString("next_page_token");
                        } else {
                            token = null;
                        }
                        if (token != null) {
                            jsonArray[numResponses] = places.getJSONArray("results");
                            numResponses += 1;
                        } else {
                            jsonArray[numResponses] = places.getJSONArray("results");
                            finished = true;
                        }
                        Thread.sleep(2000);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            try {
                int placesCount = 0;
                for (int i = 0; i < 3; i += 1) {
                    if (jsonArray[i] != null) {
                        placesCount += jsonArray[i].length();
                    }
                }
                place = new JSONObject[placesCount];
                int spot = 0;
                for (int i = 0; i < 3; i += 1) {
                    if (jsonArray[i] == null) {
                        i += 1;
                    } else {
                        for (int j = 0; j < jsonArray[i].length(); j += 1) {
                            place[spot] = jsonArray[i].getJSONObject(j);
                            spot += 1;
                        }
                    }
                }
                nearbyPlaces = place;
                ready = true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return httpData;
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Finding Waypoints, Please Wait...");
            dialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            if (dialog.isShowing()) dialog.dismiss();

            JSONObject places = null;
            try {
                places = new JSONObject(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (nearbyPlaces != null) {
                Intent displayPlaces = new Intent(activity, DisplayWaypointChoices.class);
                String[] names = new String[nearbyPlaces.length];
                String[] namesID = new String[nearbyPlaces.length];
                String[] namesLatLng = new String[nearbyPlaces.length];
                try {
                    for (int i = 0; i < nearbyPlaces.length; i += 1) {
                        names[i] = nearbyPlaces[i].getString("name");
                        namesID[i] = nearbyPlaces[i].getString("place_id");
                        Double lat,lng = 0.0;
                        lat = (Double )((JSONObject)((JSONObject)nearbyPlaces[i].get("geometry")).get("location")).get("lat");
                        lng = (Double )((JSONObject)((JSONObject)nearbyPlaces[i].get("geometry")).get("location")).get("lng");
                        namesLatLng[i] = lat + "," + lng;
                    }
                    displayPlaces.putExtra("places", names);
                    displayPlaces.putExtra("placesID", namesID);
                    displayPlaces.putExtra("placesLatLng", namesLatLng);
                    displayPlaces.putExtra("StartLocLatLng", startLatLong);
                    displayPlaces.putExtra("EndLocLatLng", endLatLong);

                    //Also add start/end place ids to intent
                    displayPlaces.putExtra("StartLocationId", startLocId);
                    displayPlaces.putExtra("EndLocationId", endLocId);
                    startActivityForResult(displayPlaces, 15);
                    //finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            //finish();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("onActivityResult");
        if (requestCode == 15) {
            if (resultCode == RESULT_OK) {
                String waypointID = data.getStringExtra("WaypointID");
                //Given the place ID, we need to get the latitude and longitude
                new PlaceSearch(this, data).execute(waypointID);
                //new TripRequest().execute(startLocId, endLocId, waypointID);
                Intent intent = new Intent(data);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                //Null directions
                System.out.println("error in code");
            }
        }
    }

    public void addWaypoint(View view) {
        System.out.println("made it");
        if(startLocId == null || endLocId == null || waypoint == null){return;}
        String waypointID = waypoint.getId();
        waypointLatLong = waypoint.getLatLng().toString();
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
                if (startId.equals("N/A")) {
                    String[] locs = new String[4];
                    String startString = startLatLong.substring(startLatLong.indexOf(" ")+2, startLatLong.lastIndexOf(")")); //"xx.xxxx,-xx.xxxx"
                    String[] startLoc = startString.split(","); //"xx.xxxx", "-xx.xxxx"
                    locs[0] = startLoc[0];
                    locs[1] = startLoc[1];

                    String endString = endLatLong.substring(endLatLong.indexOf(" ")+2, endLatLong.lastIndexOf(")")); //"xx.xxxx,-xx.xxxx"
                    String[] endLoc = endString.split(","); //"xx.xxxx", "-xx.xxxx"
                    locs[2] = endLoc[0];
                    locs[3] = endLoc[1];

                    url = new URL("https://maps.googleapis.com/maps/api/directions/json?origin="
                            + locs[0] + "," + locs[1] + "&destination="
                            + locs[2] + "," + locs[3] +
                            "&waypoints=place_id:" + waypointId + "&key=AIzaSyCsGbBFaG5NIf40zDsMgEZw8nh65I5fMw8");
                }
                else url = new URL("https://maps.googleapis.com/maps/api/directions/json?origin=place_id:" + startId +
                        "&destination=place_id:" + endId + "&waypoints=place_id:" + waypointId + "&key=AIzaSyCsGbBFaG5NIf40zDsMgEZw8nh65I5fMw8");
                System.out.println(url);
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
            intent.putExtra("WaypointLocationId", waypointId);

            //Finally add waypoint lat/lng
            LatLng wyptLatLng = waypoint.getLatLng();
            String latLng = wyptLatLng.toString();
            String latlng[] = latLng.split(",");
            latlng[0] = latlng[0].substring(latlng[0].indexOf('(')+1); //Latitude
            latlng[1] = latlng[1].substring(0, latlng[1].indexOf(')')); //Longitude
            latLng = latlng[0] + "," + latlng[1];
            intent.putExtra("WaypointLocLatLng", latLng);

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

    private class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            resource = (String) parent.getItemAtPosition(pos);
        }

        public void onNothingSelected(AdapterView<?> parent) {
            resource = null;
        }
    }

    /** Receives a JSONObject and returns a list of lists containing latitude and longitude */
    public void parse(JSONObject jObject, int distance){

        JSONArray jRoutes;
        JSONArray jLegs;
        JSONArray jSteps;
        int tempDist;
        int tempDist2;
        int stepDist;

        try {

            jRoutes = jObject.getJSONArray("routes");

            /** Traversing all routes */
            for(int i=0;i<jRoutes.length();i++){
                jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");

                /** Traversing all legs */
                for(int j=0;j<jLegs.length();j++){
                    jSteps = ( (JSONObject)jLegs.get(j) ).getJSONArray("steps");
                    int dist = (int)((JSONObject)((JSONObject)jLegs.get(j)).get("distance")).get("value");
                    System.out.println("Distance: "+dist);
                    if (dist > distance) {
                        /** Traversing all steps of polyline */
                        for (int k = 0; k < jSteps.length(); k++) {
                            int newDistVal = (int) ((JSONObject) ((JSONObject) jSteps.get(k)).get("distance")).get("value");
                            if (newDistVal > distance) {
                                String polyline = "";
                                polyline = (String) ((JSONObject) ((JSONObject) jSteps.get(k)).get("polyline")).get("points");
                                decodePoly(polyline, distance, newDistVal);
                                return;
                            } else {
                                distance -= newDistVal;
                            }

                        }
                    } else {
                        distance -= dist;
                    }
                }
            }

            //line=mMap.addPolyline(lineOptions);

        } catch (JSONException e) {
            e.printStackTrace();
        }catch (Exception e){
        }
    }

    private void decodePoly(String encoded, int newDistVal, int newDistVal2) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        int eachStep = newDistVal2/len;
        int numSteps = newDistVal/eachStep;
        String latLng = null;
        while (index < len && numSteps!=0) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            latLng = (((double) lat / 1E5) + "," +
                    ((double) lng / 1E5));
            numSteps -= 1;
        }
        checkSpot = latLng;
    }
}
