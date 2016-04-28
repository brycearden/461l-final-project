package finalproject.ee461l.journey;

import android.app.ProgressDialog;
import android.os.AsyncTask;

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

/**
 * Created by kevinrosen1 on 4/25/16.
 */
public class BackendSearchForUser extends AsyncTask<String, Void, String> {

    private OnTaskCompleted listener;
    private BackendFunctionality backend;
    private ProgressDialog dialog;
    private String start;
    private String end;

    public BackendSearchForUser(OnTaskCompleted listener, JoinTrip trip) {
        this.listener = listener;
        backend = BackendFunctionality.getInstance();
        dialog = new ProgressDialog(trip);
    }

    @Override
    protected String doInBackground(String... params) {
        String result = "";
        String targetEmail = params[0];
        String userEmail = params[1];
        URL url = null;
        try {
            HttpURLConnection request = backend.searchForUser(targetEmail);
            if (request.getResponseCode() != HttpURLConnection.HTTP_OK) {
                System.out.println("User does not exist, response code: " + request.getResponseCode());
                request.disconnect();
            }
            else {
                //We need to see if this user has any trips
                InputStream in = new BufferedInputStream(request.getInputStream());
                String json = backend.readStream(in); //This is a JSONObject. Let's get the trip ids
                request.disconnect();

                JSONObject user = new JSONObject(json);
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

                        JSONObject trip = new JSONObject(json);
                        boolean isActive = trip.getBoolean("active");
                        if (isActive) {
                            //Found the trip we want to use
                            result = json;
                            break;
                        }
                    }
                }

                if (result.equals("")) return result;
                //We have a valid Trip JSON Object. We need to get the actual Route JSON from Google
                JSONObject trip = new JSONObject(result);
                start = trip.getString("startloc");
                end = trip.getString("endloc");
                String[] coords = getLatLng(start, end);
                url = new URL("https://maps.googleapis.com/maps/api/directions/json?origin="
                        + coords[0] + "," + coords[1] + "&destination="
                        + coords[2] + "," + coords[3] + "&key=AIzaSyCsGbBFaG5NIf40zDsMgEZw8nh65I5fMw8");
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                in = new BufferedInputStream(conn.getInputStream());
                result = backend.readStream(in);
                conn.disconnect();

                //Finally, we need to associate this user with this trip
                request = backend.searchForUser(userEmail);
                if (request.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    //We need to create this user. NOTE: isleader will need to be false!
                    request.disconnect();
                    request = backend.createUser(userEmail, false);
                }
                else {
                    //We just need to make sure that the user is not the trip leader
                    request.disconnect();
                    request = backend.userSetIsLeader(userEmail, false);
                }
                in = new BufferedInputStream(request.getInputStream());
                System.out.println("Created User: " + backend.readStream(in));
                request.disconnect();

                request = backend.connectUserTrip(userEmail, trip.getString("key"));
                request.disconnect();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
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
        System.out.println("Result: " + result);
        if (dialog.isShowing()) dialog.dismiss();
        listener.onTaskCompleted(result, start, end);
    }

    protected String[] getLatLng(String start, String end) { //Format: "lat/lng: (xx.xxxx,-xx.xxxx)"
        String[] strings = new String[4];
        String startString = start.substring(start.indexOf(" ")+2, start.lastIndexOf(")")); //"xx.xxxx,-xx.xxxx"
        String[] startLoc = startString.split(","); //"xx.xxxx", "-xx.xxxx"
        strings[0] = startLoc[0];
        strings[1] = startLoc[1];

        String endString = end.substring(end.indexOf(" ")+2, end.lastIndexOf(")")); //"xx.xxxx,-xx.xxxx"
        String[] endLoc = endString.split(","); //"xx.xxxx", "-xx.xxxx"
        strings[2] = endLoc[0];
        strings[3] = endLoc[1];
        return strings;
    }
}
