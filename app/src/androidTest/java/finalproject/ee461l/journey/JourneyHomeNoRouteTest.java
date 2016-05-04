package finalproject.ee461l.journey;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.RunWith;

/**
 * Created by kevinrosen1 on 4/24/16.
 */
@RunWith(AndroidJUnit4.class)
public class JourneyHomeNoRouteTest {

    private TestFunctions tests;

    @Before
    public void setup() throws UiObjectNotFoundException, InterruptedException {
        tests = TestFunctions.getInstance();
        tests.setupApplication();

        //Log in
        Thread.sleep(500);
        tests.logInOut(true);
    }

    @After
    public void teardown() throws InterruptedException {
        tests.device.pressBack();
        Thread.sleep(500);
    }

    @Test
    public void testLogIn() throws UiObjectNotFoundException {
        //Now let's verify that login worked
        tests.menu();

        UiObject logout = tests.device.findObject(new UiSelector()
                .className("android.widget.ListView")
                .childSelector(new UiSelector()
                        .text("Log Out"))
        );
        Assert.assertTrue(logout.exists());
    }

    @Test
    public void testLogOut() throws UiObjectNotFoundException {
        //Now start log out procedure
        tests.logInOut(false);

        //Verify that it worked
        tests.menu();

        UiObject login = tests.device.findObject(new UiSelector()
                .className("android.widget.ListView")
                .childSelector(new UiSelector()
                        .text("Log In"))
        );
        Assert.assertTrue(login.exists());
    }

    @Test
    public void testStartTrip() throws UiObjectNotFoundException {
        tests.startTripActivity();
        //We should now be in the StartTrip activity. Let's verify
        UiObject startText = tests.device.findObject(new UiSelector()
                .text("Start Road Trip"));
        Assert.assertTrue(startText.exists());
        tests.device.pressBack();
    }

    @Test
    public void testJoinTrip() throws UiObjectNotFoundException {
        tests.joinTripActivity();
        //We should now be in the JoinTrip activity. Let's verify
        UiObject startText = tests.device.findObject(new UiSelector()
                .text("Join Road Trip"));
        Assert.assertTrue(startText.exists());
        tests.device.pressBack();
    }

    @Test
    public void testHelp() throws UiObjectNotFoundException {
        tests.displayHelp();
        tests.device.pressBack();

        //We now need to confirm that the Map is displayed again
        UiObject trip = tests.device.findObject(new UiSelector()
                .text("Start Road Trip"));
        Assert.assertTrue(trip.exists());
        tests.device.pressBack();
    }
}
