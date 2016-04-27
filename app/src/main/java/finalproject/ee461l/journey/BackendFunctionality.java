package finalproject.ee461l.journey;

import org.json.JSONException;
import org.json.JSONObject;

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
public class BackendFunctionality {

    private static BackendFunctionality backend;

    private BackendFunctionality() {}

    public static synchronized BackendFunctionality getInstance() {
        if (backend == null) backend = new BackendFunctionality();
        return backend;
    }

    protected void writeStream(DataOutputStream output, byte[] data) throws JSONException, IOException {
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

    protected HttpURLConnection searchForUser(String email) throws IOException {
        URL url = new URL("http://journey-1236.appspot.com/api/user/" + email);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        return request;
    }

    protected HttpURLConnection searchForTrip(String tripId) throws IOException {
        URL url = new URL("http://journey-1236.appspot.com/api/trip/" + tripId);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        return request;
    }

    protected HttpURLConnection userSetIsLeader(String email, boolean isLeader) throws IOException, JSONException {
        URL url = new URL("http://journey-1236.appspot.com/api/user/" + email);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.setRequestMethod("PUT");
        request.setDoOutput(true);
        request.setDoInput(true);
        request.setRequestProperty("Content-Type", "application/json");
        request.setChunkedStreamingMode(0);
        request.connect();

        JSONObject user = new JSONObject();
        user.put("isleader", isLeader);
        byte[] data = user.toString().getBytes("UTF-8");

        DataOutputStream output = new DataOutputStream(request.getOutputStream());
        writeStream(output, data);
        output.close();

        return request;
    }

    protected HttpURLConnection createUser(String email, boolean isLeader) throws IOException, JSONException {
        URL url = new URL("http://journey-1236.appspot.com/api/user/" + email);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.setRequestMethod("POST");
        request.setDoOutput(true);
        request.setDoInput(true);
        request.setRequestProperty("Content-Type", "application/json");
        request.setChunkedStreamingMode(0);
        request.connect();

        JSONObject user = new JSONObject();
        user.put("isleader", isLeader);
        byte[] data = user.toString().getBytes("UTF-8");

        DataOutputStream output = new DataOutputStream(request.getOutputStream());
        writeStream(output, data);
        output.close();

        return request;
    }

    protected HttpURLConnection createTrip(String startPlace, String endPlace) throws IOException, JSONException {
        URL url = new URL("http://journey-1236.appspot.com/api/trip/new");
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
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

        return request;
    }

    protected HttpURLConnection deleteTrip(String tripId) throws IOException {
        URL url = new URL("http://journey-1236.appspot.com/api/trip/" + tripId);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.setRequestMethod("DELETE");
        request.connect();
        return request;
    }

    protected HttpURLConnection connectUserTrip(String email, String tripId) throws IOException, JSONException {
        URL url = new URL("http://journey-1236.appspot.com/api/user/trip/" + email);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.setRequestMethod("PUT");
        request.setDoOutput(true);
        request.setDoInput(true);
        request.setRequestProperty("Content-Type", "application/json");
        request.setChunkedStreamingMode(0);
        request.connect();

        JSONObject trip = new JSONObject();
        trip.put("trip_id", tripId);
        byte[] data = trip.toString().getBytes("UTF-8");

        DataOutputStream output = new DataOutputStream(request.getOutputStream());
        writeStream(output, data);
        output.close();

        return request;
    }

    protected HttpURLConnection disconnectUserTrip(String email, String tripId) throws IOException, JSONException {
        URL url = new URL("http://journey-1236.appspot.com/api/user/trip/remove/" + email);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.setRequestMethod("PUT");
        request.setDoOutput(true);
        request.setDoInput(true);
        request.setRequestProperty("Content-Type", "application/json");
        request.setChunkedStreamingMode(0);
        request.connect();

        JSONObject trip = new JSONObject();
        trip.put("trip_id", tripId);
        byte[] data = trip.toString().getBytes("UTF-8");

        DataOutputStream output = new DataOutputStream(request.getOutputStream());
        writeStream(output, data);
        output.close();

        return request;
    }
}
