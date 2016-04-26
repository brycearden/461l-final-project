package finalproject.ee461l.journey;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by kevinrosen1 on 4/26/16.
 */
public class BackendDeleteTrip extends AsyncTask<String, Void, Void> {
    private OnTaskCompleted listener;

    public BackendDeleteTrip(OnTaskCompleted listener) {
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(String... params) {
        String userEmail = params[0];
        String startLoc = params[1];
        String endLoc = params[2];
        URL url = null;
        try {
            url = new URL("http://journey-1236.appspot.com/api/user/" + userEmail);
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            if (request.getResponseCode() != HttpURLConnection.HTTP_OK) {
                System.out.println("User does not exist, response code: " + request.getResponseCode());
                request.disconnect();
                return null;
            }

            //User exists; we need to see if they are the trip leader first
            InputStream in = new BufferedInputStream(request.getInputStream());
            String json = readStream(in); //This is a JSONObject. Let's get the trip ids
            request.disconnect();

            JSONObject user = new JSONObject(json);
            boolean isLeader = user.getBoolean("isleader");
            if (!isLeader) {
                System.out.println("User is not the trip leader. Therefore not deleting the trip.");
                return null;
            }

            //Will try to find the trip associated with this start/end location
            JSONObject trip = null;
            JSONArray tripIds = user.getJSONArray("trip_ids");
            for (int i = 0; i < tripIds.length(); i++) {
                url = new URL("http://journey-1236.appspot.com/api/trip/" + tripIds.get(i));
                request = (HttpURLConnection) url.openConnection();
                if (request.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    System.out.println("Trip does not exist, response code: " + request.getResponseCode());
                    request.disconnect();
                    trip = null;
                    continue;
                }

                //Trip exists. We need to get start/end loc and compare
                in = new BufferedInputStream(request.getInputStream());
                json = readStream(in); //This is a JSONObject. Let's get the trip ids
                request.disconnect();

                trip = new JSONObject(json);
                String start = trip.getString("startloc");
                if (!start.equals(startLoc)) {
                    System.out.println("Trip does not match Start Location");
                    trip = null;
                    continue;
                }
                String end = trip.getString("endloc");
                if (!end.equals(endLoc)) {
                    System.out.println("Trip does not match End Location");
                    trip = null;
                    continue;
                }
                else {
                    //This trip is a match
                    break;
                }
            }

            if (trip == null) return null;

            //We have a matching trip, so let's first disassociate user and trip
            String tripId = trip.getString("key");
            url = new URL("http://journey-1236.appspot.com/api/user/trip/remove/" + userEmail);
            request = (HttpURLConnection) url.openConnection();
            request.setRequestMethod("PUT");
            request.setDoOutput(true);
            request.setDoInput(true);
            request.setRequestProperty("Content-Type", "application/json");
            request.setChunkedStreamingMode(0);
            request.connect();

            trip = new JSONObject();
            trip.put("trip_id", tripId);
            byte[] data = trip.toString().getBytes("UTF-8");

            DataOutputStream output = new DataOutputStream(request.getOutputStream());
            writeStream(output, data);
            output.close();

            if (request.getResponseCode() != HttpURLConnection.HTTP_OK) {
                System.out.println("Trip not successfully removed. There was an issue with user disassociation. Code: "
                        + request.getResponseCode());
                request.disconnect();
                return null;
            }

            in = new BufferedInputStream(request.getInputStream());
            json = readStream(in);
            request.disconnect();
            System.out.println("Delete: " + json);

            //Finally, we will delete the trip object
            url = new URL("http://journey-1236.appspot.com/api/trip/" + tripId);
            request = (HttpURLConnection) url.openConnection();
            request.setRequestMethod("DELETE");
            request.connect();

            in = new BufferedInputStream(request.getInputStream());
            System.out.println("Delete result: " + readStream(in));
            request.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected void onPostExecute(Void test) {
        System.out.println("onPostExecute reached");
        listener.onTaskCompleted("", "", "");
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
}
