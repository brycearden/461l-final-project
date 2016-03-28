package finalproject.ee461l.journey;

import android.content.Intent;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.app.Activity;
import android.view.View;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by kevinrosen1 on 3/14/16.
 * This class will have voice control support functions
 * Eventually this will include a lot of the main functionality of the voice control that will
 *  be used in onActivityResult()
 */
public class VoiceSupport {

    //Text To Speech
    static boolean useTTS;
    static TextToSpeech speaker;
    JourneyHome journeyHome;

    public VoiceSupport(JourneyHome journeyHome){
        //Let's also set up the TTS engine
        speaker = journeyHome.getInstance();
    }


    public void startVoiceRecog(Intent data) {
        if (!useTTS) {
            //Cannot use speech engine in this case
            System.out.println("Failed to set up TTS");
            return;
        }

        //TTS is set up
        while (speaker.isSpeaking()) {} //Right now doing a busy wait, definitely a better way to do this though
        List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

        String phrase = results.get(0);
        phrase = phrase.toLowerCase();
        if (phrase.contains("request a stop") || phrase.contains("request stop") || phrase.contains("make a stop")
                || phrase.contains("make stop")) {
            //The user is asking to request a stop, so we need to handle appropriately
            VoiceSupport.tts("Would you like to stop for food, gas, sight-seeing, or other? Say 'cancel' to exit", "stopType");
            while (speaker.isSpeaking()) {}
            journeyHome.voiceComm("Choices: Food, Gas, Sight-seeing, Other", JourneyHome.VOICE_REQUEST);
        }
        else {
            //The user is not requesting a stop
            VoiceSupport.tts("To use me, say 'Request a Stop' or 'Make a Stop'. I will help with adding stops to your route", "help");
        }
    }

    public String voiceStopRequested(Intent data) {
        List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        String phrase = results.get(0);
        String stopType;
        phrase = phrase.toLowerCase();
        if (phrase.contains("food")) {
            stopType = "food";
        }
        else if (phrase.contains("gas")) {
            stopType = "gas";
        }
        else if (phrase.contains("sightseeing") || phrase.contains("sights")) {
            stopType = "sights";
        }
        else if (phrase.contains("other")) {
            stopType = "other";
        }
        else if (phrase.contains("cancel")) {
            VoiceSupport.tts("Cancelling stop addition...", "cancelStop");
            return new String();
        }
        else {
            VoiceSupport.tts("I'm sorry, please try again. Would you like to stop for food, gas, sight-seeing, or other? " +
                    "Say 'cancel' to exit", "help2");
            while (speaker.isSpeaking()) {}
            journeyHome.voiceComm("Choices: Food, Gas, Sight-seeing, Other", JourneyHome.VOICE_REQUEST);
            return new String();
        }

        //Go to next step of process
        VoiceSupport.tts("When would you like to stop? Either say a time or 'Within blank minutes'", "timeRequest");
        while (speaker.isSpeaking()) {}
        journeyHome.voiceComm("Say either a time (xx:xx) or 'Within __ minutes'", JourneyHome.VOICE_TIME);

        return stopType;
    }

    public static void tts(String phrase, String id) {
        if (Build.VERSION.SDK_INT >= 21) {
            speaker.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, id);
        }
        else {
            speaker.speak(phrase, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public int voiceStopTime(Intent data) {
        //Still could use some condensing
        List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        String phrase = results.get(0);
        int timeToStop;
        phrase = phrase.toLowerCase();
        if (phrase.contains("o'clock") || phrase.contains("oclock")) {
            //Time "on the hour"
            int hour = VoiceSupport.getTimeOnHour(phrase);

            //Current time
            int currentTime = VoiceSupport.getCurrentTime();
            if (currentTime > 779) hour += 720; //Make sure in the same time; this may need to change at some point?

            timeToStop = hour - currentTime; //This gets us an approximate time
            if (timeToStop <= 0) {
                VoiceSupport.tts("I'm sorry, the time you have asked for has already passed. Please try again. " +
                        "Either say a time or 'Within blank minutes'", "help3");
                while (speaker.isSpeaking()) {}
                journeyHome.voiceComm("Say either a time (xx:xx) or 'Within __ minutes'", JourneyHome.VOICE_TIME);
                return timeToStop;
            }
            System.out.println("Calculated time to stop: " + timeToStop);
        }
        else if (phrase.contains(":")) {
            //Time with minutes included
            int time = VoiceSupport.getTime(phrase);

            //Current time
            int currentTime = VoiceSupport.getCurrentTime();
            if (currentTime > 779) time += 720;

            timeToStop = time - currentTime;
            if (timeToStop <= 0) {
                VoiceSupport.tts("I'm sorry, the time you have asked for has already passed. Please try again. " +
                        "Either say a time or 'Within blank minutes'", "help3");
                while (speaker.isSpeaking()) {}
                journeyHome.voiceComm("Say either a time (xx:xx) or 'Within __ minutes'", JourneyHome.VOICE_TIME);
                return timeToStop;
            }
            System.out.println("Calculated time to stop: " + timeToStop);
        }
        else if (phrase.contains("within") || phrase.contains("with in")) {
            timeToStop = VoiceSupport.getTimeWithin(phrase);
            if (timeToStop == -1) {
                //There was an issue with parsing an integer
                VoiceSupport.tts("I'm sorry, there was an issue processing your request. Please try again. " +
                        "Either say a time or 'Within blank minutes'", "help3");
                while (speaker.isSpeaking()) {}
                journeyHome.voiceComm("Say either a time (xx:xx) or 'Within __ minutes'", JourneyHome.VOICE_TIME);
                return timeToStop;
            }
            System.out.println("Calculated time to stop: " + timeToStop);
        }
        else {
            VoiceSupport.tts("I'm sorry, please try again. Either say a time or 'Within blank minutes'", "repeatTime");
            while (speaker.isSpeaking()) {}
            journeyHome.voiceComm("Say either a time (xx:xx) or 'Within __ minutes'", JourneyHome.VOICE_TIME);
            return journeyHome.timeToStop;
        }

        //This means we now have a time. If distance is not defined, we will ask. Otherwise, move to calculation
        if (journeyHome.distanceFromRoute == 0) {
            VoiceSupport.tts("How far off your route are you willing to go?", "distance");
            while (speaker.isSpeaking()) {}
            journeyHome.voiceComm("Distance in miles or kilometers", JourneyHome.VOICE_DISTANCE);
        }
        else {
            VoiceSupport.tts("Finding stops, please wait...", "calculating");
            while (speaker.isSpeaking()) {}
            //voiceComm("Say either a time (xx:xx) or 'Within __ minutes'", JourneyHome.VOICE_TIME);
        }
        return timeToStop;
    }

    public int voiceStopDistance(Intent data) {
        //Still could use some condensing
        List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
        String phrase = results.get(0);
        int distanceFromRoute;
        phrase = phrase.toLowerCase();
        if (phrase.contains("mile")) {
            int index = phrase.indexOf("mile");
            double calcDistance = VoiceSupport.getDistance(phrase, index);
            if (calcDistance == -1) {
                //There was an issue
                VoiceSupport.tts("I'm sorry, there was an issue processing your request. Please try again. " +
                        "How far off your route are you willing to go?", "help3");
                while (speaker.isSpeaking()) {}
                journeyHome.voiceComm("Distance in miles or kilometers", JourneyHome.VOICE_DISTANCE);
                return -1;
            }

            //We need to convert into meters
            distanceFromRoute = (int) (calcDistance * 1609);
        }
        else if (phrase.contains("kilometer") || phrase.contains("kilometre")) {
            int index = phrase.indexOf("kilometer");
            if (index == -1) index = phrase.indexOf("kilometre");
            double calcDistance = VoiceSupport.getDistance(phrase, index);
            if (calcDistance == -1) {
                //There was an issue
                VoiceSupport.tts("I'm sorry, there was an issue processing your request. Please try again. " +
                        "How far off your route are you willing to go?", "help3");
                while (speaker.isSpeaking()) {}
                journeyHome.voiceComm("Distance in miles or kilometers", JourneyHome.VOICE_DISTANCE);
                return -1;
            }

            //Convert to meters
            distanceFromRoute = (int) (calcDistance * 1000);
        }
        else {
            VoiceSupport.tts("I'm sorry, please try again. How far off your route are you willing to go?", "help4");
            while (speaker.isSpeaking()) {}
            journeyHome.voiceComm("Distance in miles or kilometers", JourneyHome.VOICE_DISTANCE);
            return -1;
        }

        //Now we go to calculations
        System.out.println("Distance: " + distanceFromRoute);
        return distanceFromRoute;
    }

    public static int getCurrentTime() {
        Calendar calendar = Calendar.getInstance();
        int currentHour = calendar.get(Calendar.HOUR);
        int currentMinute = calendar.get(Calendar.MINUTE);
        int currentTime = (currentHour * 60) + currentMinute;

        return currentTime;
    }

    public static int getTimeOnHour(String phrase) {
        int index = phrase.indexOf("o'clock");
        if (index == -1) index = phrase.indexOf("oclock");
        index -= 1; //phrased like "*time* o'clock", so this will skip the space and go to the time
        String substring = phrase.substring(0, index);
        int startIndex = substring.lastIndexOf(" ");
        String time = substring.substring(startIndex + 1);
        int hour = 0;
        try {
            hour = Integer.parseInt(time);
        }
        catch (NumberFormatException e) {
            //Time showed up as a string, so we need to parse appropriately
            hour = VoiceSupport.convertTimeToInt(time);
        }
        hour = hour * 60;

        return hour;
    }

    public static int getTime(String phrase) {
        int colon = phrase.indexOf(":");
        String hourSub = phrase.substring(0, colon);
        String minuteSub = phrase.substring(colon+1);

        //Start with hours
        int space = hourSub.lastIndexOf(" ");
        int hour = 0;
        if (space == -1) {
            hour = Integer.parseInt(hourSub.substring(0));
        }
        else {
            hour = Integer.parseInt(hourSub.substring(space+1));
        }

        //Minutes
        space = minuteSub.indexOf(" ");
        int minutes = 0;
        if (space == -1) {
            minutes = Integer.parseInt(minuteSub.substring(0));
        }
        else {
            minutes = Integer.parseInt(minuteSub.substring(0, space));
        }

        //We will get time in minutes to make calculations easier
        int time = (hour * 60) + minutes;

        return time;
    }

    public static int getTimeWithin(String phrase) {
        int within = phrase.indexOf("within");
        if (within == -1) within = phrase.indexOf("with in");
        boolean minutes = true;
        int unit = phrase.indexOf("minute");
        if (unit == -1) {
            //Not minutes, so must be hours
            unit = phrase.indexOf("hour");
            minutes = false;
        }

        int time = 0;
        try {
            time = Integer.parseInt(phrase.substring(within+7, unit-1)); //within+7 is the start of the num
        }
        catch (NumberFormatException e) {
            //the time must have been listed as a word, so convert
            System.out.println("Need to convert: " + phrase);
            return -1;
        }

        if (!minutes) time = time * 60;

        return time;
    }

    public static double getDistance(String phrase, int index) {
        String sub = phrase.substring(0, index-1); //cuts off the space before mile too
        int space = sub.lastIndexOf(" ");
        String distance = "";
        if (space == -1) {
            distance = sub;
        }
        else {
            distance = sub.substring(space + 1);
        }

        double calcDistance = 0;
        try {
            calcDistance = Double.parseDouble(distance);
        }
        catch (NumberFormatException e) {
            System.out.println("Exception with parsing. Distance: " + distance + ", phrase: " + phrase);
            return -1;
        }

        return calcDistance;
    }

    public static int convertTimeToInt(String time) {
        int hour = 0;
        switch (time) {
            case "one":
                hour = 1;
                break;
            case "two":
                hour = 2;
                break;
            case "three":
                hour = 3;
                break;
            case "four":
                hour = 4;
                break;
            case "five":
                hour = 5;
                break;
            case "six":
                hour = 6;
                break;
            case "seven":
                hour = 7;
                break;
            case "eight":
                hour = 8;
                break;
            case "nine":
                hour = 9;
                break;
            case "ten":
                hour = 10;
                break;
            case "eleven":
                hour = 11;
                break;
            case "twelve":
                hour = 12;
                break;
            default:
                break;
        }
        return hour;
    }
}
