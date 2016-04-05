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
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by kevinrosen1 on 4/5/16.
 */
public class LocationSupport {
    private String currentLocPlaceId;

    public LocationSupport() {
        currentLocPlaceId = null;
    }

    public String getId() {return currentLocPlaceId;}

    public void getCurrentLocationInfo(String currentLocation) {
        new LocationRequest().execute(currentLocation);
    }

    public void currentLoc(String result) {
        //Run when activity is started so that the activity can run faster
        JSONObject json = null;
        try {
            json = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        currentLocPlaceId = getLocId(json);
    }

    public String getLocId(JSONObject json) {
        String status = null;
        String place_id = null;
        try {
            System.out.println(json);
            status = json.getString("status");
            if (!status.equals("OK")) return "";

            //Valid JSON object returned
            JSONArray results = json.getJSONArray("results");
            JSONObject firstResult = results.optJSONObject(0); //Hopefully the most accurate result
            place_id = firstResult.getString("place_id");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return place_id;
    }

    private class LocationRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String location = null;
            String result = null;
            if (params[0] != null) location = params[0];
            try {
                URL url = new URL("https://maps.googleapis.com/maps/api/geocode/json?latlng=" + location +
                        "&key=AIzaSyCsGbBFaG5NIf40zDsMgEZw8nh65I5fMw8");
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
            currentLoc(result);
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
