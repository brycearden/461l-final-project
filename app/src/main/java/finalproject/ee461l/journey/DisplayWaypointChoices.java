package finalproject.ee461l.journey;

import android.app.ListActivity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;

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
 * Created by gordiewhite on 4/30/16.
 */
public class DisplayWaypointChoices extends ListActivity{

    private String startLatLong;
    private String endLatLong;
    private String startLocId;
    private String endLocId;
    private String[] placesID;
    private String[] placesLatLng;
    private String waypointLatLng;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String[] places = (String[]) intent.getExtras().get("places");
        placesID = (String[]) intent.getExtras().get("placesID");
        placesLatLng = (String[]) intent.getExtras().get("placesLatLng");
        startLocId = intent.getExtras().getString("StartLocationId");
        endLocId = intent.getExtras().getString("EndLocationId");
        startLatLong = intent.getExtras().getString("StartLocLatLng");
        endLatLong = intent.getExtras().getString("EndLocLatLng");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, places);
        setListAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        String item = (String) getListAdapter().getItem(position);
        Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
        //Intent intent = new Intent();
        String waypointID = placesID[position];
        //intent.putExtra("choice", position);
        //setResult(RESULT_OK, intent);
        waypointLatLng = placesLatLng[position];
        //intent.putExtra("choice", position);
        //setResult(RESULT_OK, intent);
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
            intent.putExtra("WaypointLatLng", waypointLatLng);

            intent.putExtra("WaypointID", waypointId);

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
