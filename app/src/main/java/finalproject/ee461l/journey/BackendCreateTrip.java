package finalproject.ee461l.journey;

import android.os.AsyncTask;

import com.google.android.gms.location.places.Place;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by kevinrosen1 on 4/25/16.
 */
public class BackendCreateTrip extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... params) {
        String userEmail = "";
        String startPlace = "";
        String endPlace = "";
        String result = "";
        int currentPlace = 0;
        for (int i = 0; i < 3; currentPlace++, i++) {
            switch (currentPlace) {
                case 0:
                    userEmail = params[i];
                    break;
                case 1:
                    //StartPlace
                    startPlace = params[i];
                    break;
                case 2:
                    //EndPlace
                    endPlace = params[i];
            }
        }
        boolean createUser = false;
        URL url = null;
        try {
            //Check to see if the user exists
            url = new URL("http://journey-1236.appspot.com/api/user/" + userEmail);
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            if (request.getResponseCode() != HttpURLConnection.HTTP_OK) {
                System.out.println("User does not exist, response code: " + request.getResponseCode());
                //Create User necessary
                createUser = true;
            }
            else {
                System.out.println("User does exist");
            }
            request.disconnect();
            //If we need to create the User object, do so here
            if (createUser) {
                url = new URL("http://journey-1236.appspot.com/api/user/" + userEmail);
                request = (HttpURLConnection) url.openConnection();
                request.setRequestMethod("POST");
                request.setDoOutput(true);
                request.setDoInput(true);
                request.setRequestProperty("Content-Type", "application/json");
                request.setChunkedStreamingMode(0);
                request.connect();

                JSONObject user = new JSONObject();
                user.put("isleader", true);
                byte[] data = user.toString().getBytes("UTF-8");

                DataOutputStream output = new DataOutputStream(request.getOutputStream());
                writeStream(output, data);
                output.close();
            }
            //Create the trip
            url = new URL("http://journey-1236.appspot.com/api/trip/new");
            request = (HttpURLConnection) url.openConnection();
            request.setRequestMethod("POST");
            request.setDoOutput(true);
            request.setDoInput(true);
            request.setRequestProperty("Content-Type", "application/json");
            request.setChunkedStreamingMode(0);
            request.connect();

            JSONObject trip = new JSONObject();
            trip.put("active", true);
            trip.put("startloc", startPlace);
            trip.put("endloc", endPlace);
            byte[] data = trip.toString().getBytes("UTF-8");

            DataOutputStream output = new DataOutputStream(request.getOutputStream());
            writeStream(output, data);
            output.close();

            InputStream in = new BufferedInputStream(request.getInputStream());
            result = readStream(in);
            request.disconnect();

            //Finally, associate this user with this trip
            String tripId = getTripId(result);
            url = new URL("http://journey-1236.appspot.com/api/user/trip/" + userEmail);
            request = (HttpURLConnection) url.openConnection();
            request.setRequestMethod("PUT");
            request.setDoOutput(true);
            request.setDoInput(true);
            request.setRequestProperty("Content-Type", "application/json");
            request.setChunkedStreamingMode(0);
            request.connect();

            trip = new JSONObject();
            trip.put("trip_id", tripId);
            data = trip.toString().getBytes("UTF-8");

            output = new DataOutputStream(request.getOutputStream());
            writeStream(output, data);
            output.close();

            in = new BufferedInputStream(request.getInputStream());
            result = readStream(in);

            request.disconnect();
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
        System.out.println("Returned JSON Object: " + result);
    }

    private void writeStream(DataOutputStream output, byte[] data) throws JSONException, IOException {
        output.write(data);
        output.flush();
        output.close();
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

    protected String getTripId(String result) throws JSONException {
        JSONObject trip = new JSONObject(result);
        String tripId = trip.getString("key");
        System.out.println("Trip ID: " + tripId);
        return tripId;
    }
}
