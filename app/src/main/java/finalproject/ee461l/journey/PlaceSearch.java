package finalproject.ee461l.journey;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

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
 * Created by kevinrosen1 on 5/2/16.
 */
public class PlaceSearch extends AsyncTask<String, Void, String> {

    private ProgressDialog dialog;
    private Waypoint activity;
    private OnUpdated update;
    private Intent data;

    public PlaceSearch(Waypoint activity, OnUpdated update, Intent data) {
        dialog = new ProgressDialog(activity);
        this.activity = activity;
        this.data = data;
        this.update = update;
    }

    @Override
    protected String doInBackground(String... params) {
        String placeId = params[0];
        String result = null;
        URL url = null;
        try {
            url = new URL("https://maps.googleapis.com/maps/api/place/details/json?placeid=" + placeId
                    + "&key=AIzaSyCsGbBFaG5NIf40zDsMgEZw8nh65I5fMw8");
            HttpsURLConnection request = (HttpsURLConnection) url.openConnection();
            String responseMessage = request.getResponseMessage();
            InputStream in = new BufferedInputStream(request.getInputStream());
            result = readStream(in);

            //Parse the JSON
            JSONObject json = new JSONObject(result);
            String status = json.getString("status");
            if (!status.equals("OK")) return null;

            //Valid status. Let's get the Lat/Lng
            JSONObject placeInfo = json.getJSONObject("result");
            JSONObject geometry = placeInfo.getJSONObject("geometry");
            JSONObject location = geometry.getJSONObject("location");
            double lat = location.getDouble("lat");
            double lng = location.getDouble("lng");
            LatLng coords = new LatLng(lat, lng);
            result = coords.toString();
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
    protected void onPostExecute(String result) {
        if (dialog.isShowing()) dialog.dismiss();
        Intent intent = new Intent(data);
        intent.putExtra("WaypointLatLng", result);
        update.onUpdated(0, result);
        /*
        if (result != null) activity.setResult(activity.RESULT_OK, intent);
        else activity.setResult(activity.RESULT_CANCELED, intent);
        activity.finish();
        */
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Please Wait...");
        dialog.show();
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
