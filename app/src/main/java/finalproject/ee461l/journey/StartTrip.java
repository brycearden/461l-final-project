package finalproject.ee461l.journey;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class StartTrip extends AppCompatActivity {

    private boolean caravan;
    private Place startLoc;
    private Place endLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_trip);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Let's get the intent from JourneyHome
        Intent intent = getIntent();

        //Google Places
        GoogleApiClient mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, null)
                .build();

        //Let's set up the Places Listener
        PlaceAutocompleteFragment startFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.start_place);
        PlaceAutocompleteFragment finishFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.finish_place);

        startFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                System.out.println("Place: " + place.getName());
                startLoc = place;
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
    }

    public void caravanCheck(View view) {
        caravan = ((CheckBox) view).isChecked();
    }

    public void beginTrip(View view) {
        new TripRequest().execute(startLoc, endLoc);

    }

    private class TripRequest extends AsyncTask<Place, Void, String> {
        protected String doInBackground(Place... places) {
            Place startPlace = null;
            Place endPlace = null;
            String result = "";

            int currentPlace = 0;
            for (int i = 0; i < 2; currentPlace++, i++) {
                if (currentPlace == 0) {
                    //StartPlace
                    startPlace = places[i];
                }
                else {
                    //EndPlace
                    endPlace = places[i];
                }
            }
            try {
                URL url = new URL("https://maps.googleapis.com/maps/api/directions/json?origin=place_id:" + startPlace.getId() +
                        "&destination=place_id:" + endPlace.getId() + "&key=AIzaSyCsGbBFaG5NIf40zDsMgEZw8nh65I5fMw8");
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
