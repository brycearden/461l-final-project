package finalproject.ee461l.journey;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by kevinrosen1 on 5/1/16.
 */
public class BackendCheckForUpdate extends AsyncTask<String, Void, String> {

    BackendFunctionality backend;
    OnUpdated update;
    int numWaypoints;

    public BackendCheckForUpdate(OnUpdated update, int numWaypoints) {
        backend = BackendFunctionality.getInstance();
        this.update = update;
        this.numWaypoints = numWaypoints;
    }

    @Override
    protected String doInBackground(String... params) {
        String email = params[0];
        //First let's get the user
        String result = null;
        URL url = null;
        try {
            HttpURLConnection request = backend.searchForUser(email);
            if (request.getResponseCode() != HttpURLConnection.HTTP_OK) {
                System.out.println("User does not exist, response code: " + request.getResponseCode());
                request.disconnect();
                return null;
            }
            //User exists, let's find the active trip they are on
            InputStream in = new BufferedInputStream(request.getInputStream());
            String json = backend.readStream(in); //This is a JSONObject. Let's get the trip ids
            request.disconnect();

            JSONObject user = new JSONObject(json);
            JSONObject trip = null;
            JSONArray tripIds = user.getJSONArray("trip_ids");
            System.out.println("Trip IDs: " + tripIds);
            for (int i = 0; i < tripIds.length(); i++) {
                //We need to see if this trip is active or not
                request = backend.searchForTrip(tripIds.getString(i));
                if (request.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    System.out.println("Trip does not exist, response code: " + request.getResponseCode());
                    request.disconnect();
                }
                else {
                    //Trip exists. Let's see if it is active or not
                    in = new BufferedInputStream(request.getInputStream());
                    json = backend.readStream(in); //This is a JSONObject. Let's get the trip ids
                    request.disconnect();

                    trip = new JSONObject(json);
                    boolean isActive = trip.getBoolean("active");
                    if (isActive) {
                        //Found the trip we want to use
                        break;
                    }
                    else trip = null;
                }
            }
            if (trip == null) {
                System.out.println("Could not find an active trip");
                return null;
            }

            //We have the active trip. Let's see if there has been an update since we last checked
            System.out.println("Found trip: " + trip.getString("key"));
            JSONArray waypointIds = trip.getJSONArray("waypoint_ids");
            System.out.println("Waypoints: " + waypointIds + ", numWaypoints: " + numWaypoints);
            if (waypointIds.length() <= numWaypoints) return null;

            //This means we have at least 1 additional waypoint in this trip. We need to do an update
            String waypointUrl = "";
            for (int i = 0; i < waypointIds.length(); i++) {
                request = backend.getWaypoint(waypointIds.getString(i));
                if (request.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    System.out.println("Waypoint does not exist, response code: " + request.getResponseCode());
                    request.disconnect();
                    continue;
                }
                //Valid ID. Let's add to string
                request.disconnect();
                in = new BufferedInputStream(request.getInputStream());
                json = backend.readStream(in); //This is a JSONObject. Let's get the trip ids
                request.disconnect();
                System.out.println("Waypoint: " + json);

                //Let's make sure that this waypoint is indeed associated with our trip first
                JSONObject way = new JSONObject(json);
                String connectedTrip = way.getString("trip");
                //if (connectedTrip == null || connectedTrip.equals("null") || !connectedTrip.equals(trip.getString("key"))) continue;

                if (waypointUrl == "") waypointUrl = "&waypoints=";

                String lat = way.getString("lat");
                String lon = way.getString("lon");
                waypointUrl += (lat + "%2C");
                waypointUrl += lon;
                if (i != (waypointIds.length() - 1)) waypointUrl += "%7C"; //All lat/lngs except the last one end w/ this
            }
            if (waypointUrl.equals("")) return null;

            //Now we have the waypoint URL, so let's get the new route
            String start = trip.getString("startloc");
            String end = trip.getString("endloc");
            String[] coords = backend.getLatLng(start, end);
            url = new URL("https://maps.googleapis.com/maps/api/directions/json?origin="
                    + coords[0] + "," + coords[1] + "&destination="
                    + coords[2] + "," + coords[3]
                    + waypointUrl + "&key=AIzaSyCsGbBFaG5NIf40zDsMgEZw8nh65I5fMw8");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            in = new BufferedInputStream(conn.getInputStream());
            result = backend.readStream(in);
            conn.disconnect();

            //Finally, let's update our numWaypoints value
            numWaypoints = waypointIds.length();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != null) update.onUpdated(numWaypoints, result);
    }
}
