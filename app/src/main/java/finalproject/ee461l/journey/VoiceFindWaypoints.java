package finalproject.ee461l.journey;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by kevinrosen1 on 5/2/16.
 */
public class VoiceFindWaypoints extends AsyncTask<Void, Void, JSONObject[]> {
    //FUTURE: Try to use this in Waypoint Activity instead of nested class?

    private ProgressDialog dialog;
    private JourneyHome home;

    public VoiceFindWaypoints(JourneyHome home) {
        this.home = home;
        dialog = new ProgressDialog(home);
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Finding Waypoints, Please Wait...");
        dialog.show();
    }

    @Override
    protected JSONObject[] doInBackground(Void... params) {
        LatLng currentLocationObj = home.map.currentLocation;
        int distance = home.distanceFromRoute;
        String stopType = home.stopType;

        JSONArray[] jsonArray = new JSONArray[3];
        boolean finished = false;
        int numResponses = 0;
        String token = null;
        URL url = null;

        JSONObject[] nearbyPlaces = null;

        //Let's get currentLocation into the correct form
        String currentLocation = currentLocationObj.toString();
        String waypointString = currentLocation.substring(currentLocation.indexOf(" ")+2, currentLocation.lastIndexOf(")")); //"xx.xxxx,-xx.xxxx"
        String[] current = waypointString.split(","); //"xx.xxxx", "-xx.xxxx"
        currentLocation = current[0] + "," + current[1];

        try {
            while (!finished) {
                String googlePlacesUrl = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
                if (numResponses == 0) {
                    googlePlacesUrl += "location=" + currentLocation;
                    googlePlacesUrl += "&radius=" + distance;
                    googlePlacesUrl += "&types=" + stopType;
                } else {
                    googlePlacesUrl += "&pagetoken=" + token;
                }
                googlePlacesUrl += "&key=AIzaSyCsGbBFaG5NIf40zDsMgEZw8nh65I5fMw8";

                url = new URL(googlePlacesUrl);

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.connect();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuffer stringBuffer = new StringBuffer();
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuffer.append(line);
                }
                String json = stringBuffer.toString();
                bufferedReader.close();
                httpURLConnection.disconnect();

                JSONObject places = new JSONObject(json);
                if (places.has("next_page_token")) {
                    token = places.getString("next_page_token");
                } else {
                    token = null;
                }
                if (token != null) {
                    jsonArray[numResponses] = places.getJSONArray("results");
                    numResponses += 1;
                } else {
                    jsonArray[numResponses] = places.getJSONArray("results");
                    finished = true;
                }
                Thread.sleep(2000);
            }

            int placesCount = 0;
            for (int i = 0; i < 3; i += 1) {
                if (jsonArray[i] != null) {
                    placesCount += jsonArray[i].length();
                }
            }

            JSONObject[] place = new JSONObject[placesCount];
            int spot = 0;
            for (int i = 0; i < 3; i += 1) {
                if (jsonArray[i] == null) {
                    i += 1;
                } else {
                    for (int j = 0; j < jsonArray[i].length(); j += 1) {
                        place[spot] = jsonArray[i].getJSONObject(j);
                        spot += 1;
                    }
                }
            }
            nearbyPlaces = place;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return nearbyPlaces;
    }

    @Override
    protected void onPostExecute(JSONObject[] result) {
        if (dialog.isShowing()) dialog.dismiss();
        if (result != null) {
            Intent displayPlaces = new Intent(home, DisplayWaypointChoices.class);
            String[] names = new String[result.length];
            String[] namesID = new String[result.length];
            String[] namesLatLng = new String[result.length];
            try {
                for (int i = 0; i < result.length; i += 1) {
                    names[i] = result[i].getString("name");
                    namesID[i] = result[i].getString("place_id");
                    Double lat,lng = 0.0;
                    lat = (Double )((JSONObject)((JSONObject)result[i].get("geometry")).get("location")).get("lat");
                    lng = (Double )((JSONObject)((JSONObject)result[i].get("geometry")).get("location")).get("lng");
                    namesLatLng[i] = lat + "," + lng;
                }
                displayPlaces.putExtra("places", names);
                displayPlaces.putExtra("placesID", namesID);
                displayPlaces.putExtra("placesLatLng", namesLatLng);
                displayPlaces.putExtra("StartLocLatLng", home.map.getStartLatLng());
                displayPlaces.putExtra("EndLocLatLng", home.map.getEndLatLng());

                //Also add start/end place ids to intent
                displayPlaces.putExtra("StartLocationId", home.map.getStartLocID());
                displayPlaces.putExtra("EndLocationId", home.map.getEndLocID());
                home.startActivityForResult(displayPlaces, home.VOICE_ADD_WAYPOINT);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
