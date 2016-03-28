package finalproject.ee461l.journey;

import android.location.Location;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by kevinrosen1 on 3/14/16.
 * This class will have map helper functions.
 * These include Location Updates to calculations for Map Routes
 */
public class MapSupport {

    public static JSONArray getRouteSteps(JSONObject directions, boolean isFirstTime) {
        JSONArray steps = null;
        if (isFirstTime) {
            //No waypoints
            try {
                JSONArray routes = directions.getJSONArray("routes");

                //Need to get the legs[]
                JSONObject firstRoute = routes.optJSONObject(0); //If we look for more than 1 route, we'll need a loop
                JSONArray legs = firstRoute.getJSONArray("legs");

                //Need to get the steps[] now
                JSONObject firstLeg = legs.optJSONObject(0); //Once we add waypoints there will be more legs
                steps = firstLeg.getJSONArray("steps");
            }
            catch (JSONException e) {
                //JSON Error
            }
        }
        else {
            //At least 1 waypoint to worry about
        }
        return steps;
    }

    //Note: This function was written on our own, but a few pieces of the polyline decryption were cited from
    //      this GitHub repository: https://github.com/googlemaps/android-maps-utils, in the decode() function
    public static ArrayList<LatLng> convertPolyline(JSONArray steps) {
        ArrayList<LatLng> leg = new ArrayList<LatLng>();
        for (int i = 0; i < steps.length(); i++) {
            String points = "";
            try {
                points = steps.getJSONObject(i).getJSONObject("polyline").getString("points");
            }
            catch (JSONException e) {
                //JSON Error
            }
            double latitude = 0;
            double longitude = 0; //Out here b/c path uses relative lat/lng changes
            int index = 0;
            int length = points.length();
            while (index < length) {
                //Need to decode each character
                //Start with latitude
                int[] lat = getCoord(points, index);
                latitude += lat[0] / 1e5; //step 2
                index = lat[1];

                //Repeat with longitude
                int[] lng = getCoord(points, index);
                longitude += lng[0] / 1e5;
                index = lng[1];

                LatLng current = new LatLng(latitude, longitude);
                leg.add(current);
            }
        }

        return leg;
    }

    public static int[] getCoord(String points, int currIndex) {
        int result = 1;
        int shift = 0;
        int character = 0x20;
        int index = currIndex;
        while (character >= 0x1f) {
            //>=x1f is because every chunk gets ORd with 0x20 except the last one per code
            if (index >= points.length()) break;
            character = points.charAt(index);
            character -= 64; //step 10
            result += (character << shift); //step 7
            shift += 5; //5-bit chunks
            index++;
        }

        //Need to determine if original value was negative or not (step 5)
        //Since there is a left shift of 1 before any inversion, a positive # will always have
        //a 0 in the LSB
        if ((result & 1) == 1) result = ~(result >> 1); //RSHF on inside so MSB = 1
        else result = result >> 1; //step 4

        return new int[] {result, index};
    }

    public static void updateLocation(Location location) {
        LatLng currentLoc = new LatLng(location.getLatitude(), location.getLongitude());
        if (JourneyHome.marker != null) JourneyHome.marker.remove();
        JourneyHome.marker = JourneyHome.mMap.addMarker(new MarkerOptions().position(currentLoc).title("Current Location"));
        if (JourneyHome.firstUpdate) {
            JourneyHome.mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLoc));
            JourneyHome.firstUpdate = false;
        }
        JourneyHome.currentLocation = currentLoc;
    }

    public static LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }
}
