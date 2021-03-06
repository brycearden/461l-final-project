package finalproject.ee461l.journey;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;

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

public class StartTrip extends AppCompatActivity {

    private boolean caravan;
    protected Place startLoc;
    protected Place endLoc;

    //For current location
    private LocationSupport loc;
    private PlaceAutocompleteFragment startFragment;
    private boolean usingCurrentLoc;
    private String currentLocation;
    private String currentLocPlaceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_trip);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        //Google Places
        //Should just be able to use the client from JourneyHome?
        /*
        GoogleApiClient mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, null)
                .build();
                */

        //Let's set up the Places Listener
        startFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.new_waypoint);
        PlaceAutocompleteFragment finishFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.finish_place);

        startFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                System.out.println("Place: " + place.getName());
                startLoc = place;
                usingCurrentLoc = false;
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                System.out.println("An error occurred: " + status);
            }
        });

        finishFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                System.out.println("Place: " + place.getName());
                endLoc = place;
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                System.out.println("An error occurred: " + status);
            }
        });

        startFragment.setHint("Select Starting Destination");
        finishFragment.setHint("Select Final Destination");

        caravan = false;
        usingCurrentLoc = false;
    }

    public void caravanCheck(View view) {
        caravan = ((CheckBox) view).isChecked();
    }

    public void beginTrip(View view) {
        if ((usingCurrentLoc && loc.getId() == null) || (!usingCurrentLoc && startLoc == null) || endLoc == null) return;
        new TripRequest().execute(startLoc, endLoc);
    }

    public void useCurrentLoc(View view) {
        //User wants current location to be used
        startFragment.setText("Current Location");
        usingCurrentLoc = true;
    }

    private class TripRequest extends AsyncTask<Place, Void, String> {
        private String startId;
        private String endId;
        private BackendFunctionality backend;
        private ProgressDialog dialog;

        public TripRequest() {
            backend = BackendFunctionality.getInstance(); //Using for readStream function
            dialog = new ProgressDialog(StartTrip.this);
        }

        protected String doInBackground(Place... places) {
            Place startPlace = null;
            Place endPlace = null;
            String result = "";
            HttpsURLConnection request = null;

            int currentPlace = 0;
            for (int i = 0; i < 2; currentPlace++, i++) {
                if (currentPlace == 0) {
                    //StartPlace
                    startPlace = places[i];
                } else {
                    //EndPlace
                    endPlace = places[i];
                }
            }
            try {
                URL url = null;
                if (usingCurrentLoc) {
                    url = new URL("https://maps.googleapis.com/maps/api/directions/json?origin=place_id:" + loc.getId() +
                            "&destination=place_id:" + endPlace.getId() + "&key=AIzaSyCsGbBFaG5NIf40zDsMgEZw8nh65I5fMw8");
                    startId = loc.getId();
                } else {
                    url = new URL("https://maps.googleapis.com/maps/api/directions/json?origin=place_id:" + startPlace.getId() +
                            "&destination=place_id:" + endPlace.getId() + "&key=AIzaSyCsGbBFaG5NIf40zDsMgEZw8nh65I5fMw8");
                    startId = startPlace.getId();
                }
                endId = endPlace.getId();
                request = (HttpsURLConnection) url.openConnection();
                String responseMessage = request.getResponseMessage();
                InputStream in = new BufferedInputStream(request.getInputStream());
                result = backend.readStream(in);
                request.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                request.disconnect();
            }
            return result;
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Please Wait...");
            dialog.show();
        }

        @Override
        protected void onPostExecute(String result) {
            JSONObject directions = null;
            try {
                directions = new JSONObject(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (dialog.isShowing()) dialog.dismiss();

            Intent intent = new Intent();
            intent.putExtra("JSONDirections", result);
            if (usingCurrentLoc) intent.putExtra("StartLocLatLng", "Current");
            else intent.putExtra("StartLocLatLng", startLoc.getLatLng().toString());
            intent.putExtra("EndLocLatLng", endLoc.getLatLng().toString());
            //Also add start/end place ids to intent
            intent.putExtra("StartLocationId", startId);
            intent.putExtra("EndLocationId", endId);
            //Attach caravan info
            intent.putExtra("isCaravanTrip", caravan);
            if (directions != null) setResult(RESULT_OK, intent);
            else setResult(RESULT_CANCELED, intent);
            finish();
        }
    }
}
