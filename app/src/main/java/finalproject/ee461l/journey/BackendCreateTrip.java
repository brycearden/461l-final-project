package finalproject.ee461l.journey;

import android.app.ProgressDialog;
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

    private BackendFunctionality backend;
    private ProgressDialog dialog;

    public BackendCreateTrip(JourneyHome home) {
        backend = BackendFunctionality.getInstance();
        dialog = new ProgressDialog(home);
    }

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
            HttpURLConnection request = backend.searchForUser(userEmail);
            if (request.getResponseCode() != HttpURLConnection.HTTP_OK) {
                System.out.println("User does not exist, response code: " + request.getResponseCode());
                //Create User necessary
                request.disconnect();
                request = backend.createUser(userEmail, true);
            }
            else {
                //We need to make sure the user is the trip leader
                request.disconnect();
                request = backend.userSetIsLeader(userEmail, true);
            }
            request.disconnect();
            //If we need to create the User object, do so here
            if (createUser) {
                /*
                InputStream in = new BufferedInputStream(request.getInputStream());
                result = backend.readStream(in);
                System.out.println("Result of Creating User: " + result);
                */
            }
            //Create the trip
            request = backend.createTrip(startPlace, endPlace);

            InputStream in = new BufferedInputStream(request.getInputStream());
            result = backend.readStream(in);
            request.disconnect();

            //Finally, associate this user with this trip
            String tripId = getTripId(result);
            request = backend.connectUserTrip(userEmail, tripId);

            in = new BufferedInputStream(request.getInputStream());
            result = backend.readStream(in);
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

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Please Wait...");
        dialog.show();
    }

    @Override
    protected void onPostExecute(String result) {
        System.out.println("Returned JSON Object: " + result);
        if (dialog.isShowing()) dialog.dismiss();
    }

    protected String getTripId(String result) throws JSONException {
        JSONObject trip = new JSONObject(result);
        String tripId = trip.getString("key");
        System.out.println("Trip ID: " + tripId);
        return tripId;
    }
}
