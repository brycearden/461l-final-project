package finalproject.ee461l.journey;

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
public class SearchForUser extends AsyncTask<String, Void, String> {

    private OnTaskCompleted listener;
    private String start;
    private String end;

    public SearchForUser(OnTaskCompleted listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... params) {
        String result = "";
        String email = params[0]; //Only 1 parameter here
        URL url = null;
        try {
            url = new URL("http://journey-1236.appspot.com/api/user/" + email);
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            if (request.getResponseCode() != HttpURLConnection.HTTP_OK) {
                System.out.println("User does not exist, response code: " + request.getResponseCode());
                request.disconnect();
            }
            else {
                //We need to see if this user has any trips
                InputStream in = new BufferedInputStream(request.getInputStream());
                String json = readStream(in); //This is a JSONObject. Let's get the trip ids
                request.disconnect();

                //Delete some trips (Note: This is how you delete a trip
                /*
                url = new URL("http://journey-1236.appspot.com/api/trip/5732568548769792");
                request = (HttpURLConnection) url.openConnection();
                request.setRequestMethod("DELETE");
                request.connect();

                in = new BufferedInputStream(request.getInputStream());
                System.out.println("Delete result: " + readStream(in));
                request.disconnect();
                */

                JSONObject user = new JSONObject(json);
                JSONArray tripIds = user.getJSONArray("trip_ids");
                System.out.println("Trip IDs: " + tripIds);
                for (int i = 0; i < tripIds.length(); i++) {
                    //We need to see if this trip is active or not
                    url = new URL("http://journey-1236.appspot.com/api/trip/" + tripIds.get(i));
                    request = (HttpURLConnection) url.openConnection();
                    if (request.getResponseCode() != HttpURLConnection.HTTP_OK) {
                        System.out.println("Trip does not exist, response code: " + request.getResponseCode());
                        request.disconnect();
                    }
                    else {
                        //Trip exists. Let's see if it is active or not
                        in = new BufferedInputStream(request.getInputStream());
                        json = readStream(in); //This is a JSONObject. Let's get the trip ids
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
                result = readStream(in);
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

    protected void onPostExecute(String result) {
        System.out.println("Result: " + result);
        listener.onTaskCompleted(result, start, end);
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
