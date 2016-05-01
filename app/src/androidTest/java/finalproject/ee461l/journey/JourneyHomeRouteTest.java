package finalproject.ee461l.journey;

import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by kevinrosen1 on 4/24/16.
 */
public class JourneyHomeRouteTest {
    private TestFunctions tests;

    @Before
    public void setup() throws UiObjectNotFoundException, InterruptedException {
        tests = TestFunctions.getInstance();
        tests.setupApplication();

        //Log in
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        tests.logInOut(true);
        tests.startTripActivity();
        tests.currentLocation();
        tests.endLocation();
        tests.beginTrip();
        Thread.sleep(2000); //Sleep enough so that the fragment can be stable before testing
    }

    @After
    public void teardown() throws InterruptedException {
        tests.device.pressBack();
        Thread.sleep(500);
    }

    @Test
    public void testLayout() throws UiObjectNotFoundException, InterruptedException {
        Thread.sleep(1000); //Needs a bit more time to focus to make sure start/end pins are there
        //Menu button
        UiObject menu = tests.device.findObject(new UiSelector()
                .resourceId("finalproject.ee461l.journey:id/menu"));
        Assert.assertTrue(menu.exists());

        //Add Stop button
        UiObject stop = tests.device.findObject(new UiSelector()
                .text("Add Stop to Route"));
        Assert.assertTrue(stop.exists());

        //Voice button
        UiObject speech = tests.device.findObject(new UiSelector()
                .resourceId("finalproject.ee461l.journey:id/speech_button"));
        Assert.assertTrue(speech.exists());

        //Directions
        UiObject dir = tests.device.findObject(new UiSelector()
                .text("Route Directions"));
        Assert.assertTrue(dir.exists());

        //Start pin
        Thread.sleep(1000); //1 additional second should be enough. Sometimes it takes a while to focus
        UiObject start = tests.device.findObject(new UiSelector()
                .description("Current Location"));
        if (!start.exists()) start = tests.device.findObject(new UiSelector()
                .description("Start Location. "));
        Assert.assertTrue(start.exists()); //Fails for some reason?

        //End pin
        UiObject end = tests.device.findObject(new UiSelector()
                .description("End Location. "));
        Assert.assertTrue(end.exists());
    }

    @Test
    public void directions() throws UiObjectNotFoundException {
        UiObject dir = tests.device.findObject(new UiSelector()
                .className("android.widget.RelativeLayout")
                .clickable(true)
                .childSelector(new UiSelector()
                        .text("Route Directions"))
        );
        Assert.assertTrue(dir.exists());
        dir.click();

        //Make sure we are on a new activity
        dir = tests.device.findObject(new UiSelector()
                .resourceId("finalproject.ee461l.journey:id/action_bar")
                .childSelector(new UiSelector()
                        .text("Route Directions"))
        );
        Assert.assertTrue(dir.exists());
        tests.device.pressBack();
    }

    @Test
    public void testHelp() throws UiObjectNotFoundException {
        tests.displayHelp();
        tests.device.pressBack();

        //We now need to confirm that the Map is displayed again
        UiObject trip = tests.device.findObject(new UiSelector()
                .text("Add Stop to Route"));
        Assert.assertTrue(trip.exists());
        tests.device.pressBack();
    }

    @Test
    public void voiceComm() throws UiObjectNotFoundException {
        UiObject voice = tests.device.findObject(new UiSelector()
                .resourceId("finalproject.ee461l.journey:id/speech_button"));
        Assert.assertTrue(voice.exists());
        voice.click();

        UiObject prompt = tests.device.findObject(new UiSelector()
                .textStartsWith("Start Stop Request Process")
                .resourceId("com.google.android.googlequicksearchbox:id/intent_api_text"));
        if (!prompt.exists()) prompt = tests.device.findObject(new UiSelector()
                .textStartsWith("Didn't catch that")
                .resourceId("com.google.android.googlequicksearchbox:id/intent_api_text"));
        Assert.assertTrue(prompt.exists());

        //Would test the conversation, but UIAutomator is incapable of running voice tests
        tests.device.pressBack();
    }

    @Test
    public void addStopToRoute() throws UiObjectNotFoundException {
        //Populate when Gordie finishes
    }
}
