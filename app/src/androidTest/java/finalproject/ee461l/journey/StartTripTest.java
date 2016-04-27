package finalproject.ee461l.journey;

import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by kevinrosen1 on 4/24/16.
 */
public class StartTripTest {

    public static final String START_LOC = "3700";
    public static final String END_LOC = "2810"; //These are just beginnings of addresses; will pick from dropdown

    private TestFunctions tests;

    @Before
    public void setup() throws UiObjectNotFoundException {
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
    }

    @Test
    public void useCurrentLocation() throws UiObjectNotFoundException {
        tests.currentLocation();

        //Verify that button worked
        UiObject text = tests.device.findObject(new UiSelector()
                .resourceId("finalproject.ee461l.journey:id/place_autocomplete_search_input"));
        Assert.assertTrue(text.getText().equals("Current Location"));
    }

    @Test
    public void generateTripNothingEntered() throws UiObjectNotFoundException {
        tests.beginTrip();

        //Activity should not change
        UiObject startTrip = tests.device.findObject(new UiSelector()
                .text("Start Road Trip"));
        Assert.assertTrue(startTrip.exists());
    }

    @Test
    public void generateTripOnePlaceEntered() throws UiObjectNotFoundException {
        tests.currentLocation();
        tests.beginTrip();

        //Activity should not change
        UiObject startTrip = tests.device.findObject(new UiSelector()
                .text("Start Road Trip"));
        Assert.assertTrue(startTrip.exists());
    }

    @Test
    public void generateTripNoCurrentLocation() throws UiObjectNotFoundException, InterruptedException {
        tests.startLocation();
        tests.endLocation();
        tests.beginTrip();

        //Activity should change
        Thread.sleep(1500);
        UiObject stopTripButton = tests.device.findObject(new UiSelector()
                .resourceId("finalproject.ee461l.journey:id/start_trip")
                .text("Add Stop to Route"));
        Assert.assertTrue(stopTripButton.exists());
    }

    @Test
    public void generateTripCurrentLocation() throws UiObjectNotFoundException, InterruptedException {
        tests.currentLocation();
        tests.endLocation();
        tests.beginTrip();

        //Activity should change
        Thread.sleep(1500);
        UiObject stopTripButton = tests.device.findObject(new UiSelector()
                .resourceId("finalproject.ee461l.journey:id/start_trip")
                .text("Add Stop to Route"));
        Assert.assertTrue(stopTripButton.exists());
    }

    @Test
    public void generateTripCurrentLocationCaravan() throws UiObjectNotFoundException, InterruptedException {
        tests.currentLocation();
        tests.endLocation();
        tests.caravanTrip();
        tests.beginTrip();

        //Activity should change
        Thread.sleep(1500);
        UiObject stopTripButton = tests.device.findObject(new UiSelector()
                .resourceId("finalproject.ee461l.journey:id/start_trip")
                .text("Add Stop to Route"));
        Assert.assertTrue(stopTripButton.exists());

        //Need to close app and delete the trip
        tests.device.pressBack();
        UiObject exitButton = tests.device.findObject(new UiSelector()
                .text("Yes")
                .resourceId("android:id/button1"));
        Assert.assertTrue(exitButton.exists());
        exitButton.click();
        Thread.sleep(5000);
    }
}
