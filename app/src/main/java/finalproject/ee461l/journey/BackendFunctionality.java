package finalproject.ee461l.journey;

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
import java.util.ArrayList;

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

    protected JSONArray getWaypoints(String tripId) throws JSONException, IOException {
        HttpURLConnection request = getListOfWaypoints(tripId);
        if (request.getResponseCode() != HttpURLConnection.HTTP_OK) {
            System.out.println("Could not get list of Waypoints, response code: " + request.getResponseCode());
            request.disconnect();
            return null;
        }
        //We have a list of waypoints. Let's get them all
        InputStream in = new BufferedInputStream(request.getInputStream());
        String json = readStream(in);
        request.disconnect();

        //Returns a list of waypoint objects. Let's get them
        JSONObject list = new JSONObject(json);
        JSONArray waypointIds = list.getJSONArray("waypoints");
        return waypointIds;
    }

    protected HttpURLConnection getListOfWaypoints(String tripId) throws IOException {
        URL url = new URL("http://journey-1236.appspot.com/api/trip/waypoint/list/" + tripId);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        return request;
    }

    protected HttpURLConnection getWaypoint(String waypointId) throws IOException {
        URL url = new URL("http://journey-1236.appspot.com/api/waypoint/" + waypointId);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        return request;
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

        InputStream in = new BufferedInputStream(request.getInputStream());
        System.out.println("Isleader: " + readStream(in));

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

        InputStream in = new BufferedInputStream(request.getInputStream());
        System.out.println(readStream(in));

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

    protected HttpURLConnection createWaypoint(String latlng) throws IOException, JSONException {
        URL url = new URL("http://journey-1236.appspot.com/api/waypoint/new");
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.setRequestMethod("POST");
        request.setDoOutput(true);
        request.setDoInput(true);
        request.setRequestProperty("Content-Type", "application/json");
        request.setChunkedStreamingMode(0);
        request.connect();

        System.out.println("Waypoint String: " + latlng);
        String waypointString = null;
        if (latlng.indexOf(")") != -1) waypointString = latlng.substring(latlng.indexOf(" ")+2, latlng.lastIndexOf(")")); //"xx.xxxx,-xx.xxxx"
        else waypointString = latlng;
        String[] waypoint = waypointString.split(","); //"xx.xxxx", "-xx.xxxx"

        JSONObject trip = new JSONObject();
        trip.put("lat", waypoint[0]);
        trip.put("lon", waypoint[1]);
        byte[] data = trip.toString().getBytes("UTF-8");

        DataOutputStream output = new DataOutputStream(request.getOutputStream());
        writeStream(output, data);
        output.close();

        return request;
    }

    protected HttpURLConnection deleteWaypoint(String waypointId) throws IOException {
        URL url = new URL("http://journey-1236.appspot.com/api/waypoint/" + waypointId);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.setRequestMethod("DELETE");
        request.connect();
        return request;
    }

    protected HttpURLConnection deleteTrip(String tripId) throws IOException, JSONException {
        //We need to deal w/ waypoints first
        JSONArray waypoints = getWaypoints(tripId);
        if (waypoints != null && waypoints.length() != 0) {
            for (int i = 0; i < waypoints.length(); i++) {
                //Need to disassociate w/ trip and delete
                System.out.println(waypoints.getString(i));
                JSONObject waypoint = new JSONObject(waypoints.getString(i));
                HttpURLConnection request = disconnectWaypointTrip(tripId, waypoint.getString("key"));
                if (request.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    System.out.println("Failed to disconnect a Waypoint from trip, response code: " + request.getResponseCode());
                }
                request.disconnect();

                request = deleteWaypoint(waypoint.getString("key"));
                if (request.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    System.out.println("Failed to delete a Waypoint, response code: " + request.getResponseCode());
                }
                request.disconnect();
            }
        }
        URL url = new URL("http://journey-1236.appspot.com/api/trip/" + tripId);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.setRequestMethod("DELETE");
        request.connect();
        return request;
    }

    protected HttpURLConnection connectWaypointTrip(String tripId, String waypointId) throws IOException, JSONException {
        URL url = new URL("http://journey-1236.appspot.com/api/trip/waypoint/" + tripId);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.setRequestMethod("PUT");
        request.setDoOutput(true);
        request.setDoInput(true);
        request.setRequestProperty("Content-Type", "application/json");
        request.setChunkedStreamingMode(0);
        request.connect();

        JSONObject trip = new JSONObject();
        trip.put("waypoint_id", waypointId);
        byte[] data = trip.toString().getBytes("UTF-8");

        DataOutputStream output = new DataOutputStream(request.getOutputStream());
        writeStream(output, data);
        output.close();

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

    protected HttpURLConnection disconnectWaypointTrip(String tripId, String waypointId) throws IOException, JSONException {
        URL url = new URL("http://journey-1236.appspot.com/api/trip/waypoint/remove/" + tripId);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.setRequestMethod("PUT");
        request.setDoOutput(true);
        request.setDoInput(true);
        request.setRequestProperty("Content-Type", "application/json");
        request.setChunkedStreamingMode(0);
        request.connect();

        JSONObject trip = new JSONObject();
        trip.put("waypoint_id", waypointId);
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
