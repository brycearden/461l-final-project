package finalproject.ee461l.journey;

import android.content.Intent;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.view.View;

import java.util.Calendar;

/**
 * Created by kevinrosen1 on 3/14/16.
 * This class will have voice control support functions
 * Eventually this will include a lot of the main functionality of the voice control that will
 *  be used in onActivityResult()
 */
public class VoiceSupport {
    public static void tts(String phrase, String id) {
        if (Build.VERSION.SDK_INT >= 21) {
            JourneyHome.speaker.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, id);
        }
        else {
            JourneyHome.speaker.speak(phrase, TextToSpeech.QUEUE_FLUSH, null);
        }
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
