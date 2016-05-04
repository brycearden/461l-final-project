package finalproject.ee461l.journey;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * Created by kevinrosen1 on 4/27/16.
 */
public class BackendAddWaypoint extends AsyncTask<String, Void, String> {

    /*
    Preconditions: -Trip has already been created/joined
                   -User has already been created and assigned isTripLeader
     */

    private BackendFunctionality backend;

    public BackendAddWaypoint() {
        backend = BackendFunctionality.getInstance();
    }

    @Override
    protected String doInBackground(String... params) {
        String email = params[0];
        String waypoint = params[1];
        try {
            HttpURLConnection request = backend.searchForUser(email);
            if (request.getResponseCode() != HttpURLConnection.HTTP_OK) {
                System.out.println("User does not exist, response code: " + request.getResponseCode());
                request.disconnect();
                return null;
            }
            InputStream in = new BufferedInputStream(request.getInputStream());
            String json = backend.readStream(in);
            request.disconnect();

            //Valid user object returned; We need to find the trip id
            String result = null;
            JSONObject user = new JSONObject(json);
            JSONArray tripIds = user.getJSONArray("trip_ids");
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

                    JSONObject trip = new JSONObject(json);
                    boolean isActive = trip.getBoolean("active");
                    if (isActive) {
                        //Found the trip we want to use
                        result = json;
                        break;
                    }
                }
            }

            if (result == null) return null;
            JSONObject trip = new JSONObject(result);
            String tripId = trip.getString("key");

            //With the trip_id, we can now add the waypoint to the correct trip
            request = backend.createWaypoint(waypoint);
            if (request.getResponseCode() != HttpURLConnection.HTTP_OK) {
                System.out.println("Failed to create Waypoint, response code: " + request.getResponseCode());
                request.disconnect();
                return null;
            }
            in = new BufferedInputStream(request.getInputStream());
            json = backend.readStream(in);
            request.disconnect();
            System.out.println("Waypoint Object: " + json);

            JSONObject waypointObj = new JSONObject(json);
            String waypointId = waypointObj.getString("key");

            //Finally, add the waypoint to the trip
            request = backend.connectWaypointTrip(tripId, waypointId);
            if (request.getResponseCode() != HttpURLConnection.HTTP_OK) {
                System.out.println("Failed to create Waypoint, response code: " + request.getResponseCode());
                request.disconnect();
                return null;
            }
            in = new BufferedInputStream(request.getInputStream());
            json = backend.readStream(in);
            request.disconnect();
            System.out.println("Returned JSON: " + json);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
