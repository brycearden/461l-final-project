package finalproject.ee461l.journey;

import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;

import org.junit.Assert;

/**
 * Created by kevinrosen1 on 4/24/16.
 */
public class TestFunctions {

    public static final String LOGIN_EMAIL = "larrybarnson@gmail.com";

    protected UiDevice device;
    private static TestFunctions tests;

    private TestFunctions() {}

    public static synchronized TestFunctions getInstance() {
        if (tests == null) tests = new TestFunctions();
        return tests;
    }

    /*
    General Functions (for use in all tests)
     */
    public UiDevice setupApplication() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        // Launch the app
        Context context = InstrumentationRegistry.getContext();
        final Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage("finalproject.ee461l.journey");
        // Clear out any previous instances
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        // Wait for the app to appear
        device.wait(Until.hasObject(By.pkg("finalproject.ee461l.journey").depth(0)),
                5000);

        return device;
    }

    public void logInOut(boolean isLogin) throws UiObjectNotFoundException {
        //Now run the login procedure
        menu();

        UiObject action = device.findObject(new UiSelector()
                .className("android.widget.ListView")
                .childSelector(new UiSelector()
                        .text(isLogin ? "Log In" : "Log Out"))
        );
        Assert.assertTrue(action.exists());
        action.click();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!isLogin) return; //If we are logging out we are done

        UiObject user = device.findObject(new UiSelector()
                .text(LOGIN_EMAIL));
        if (user.exists()) user.click(); //If the user didn't log out previously, this will not exist

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
    Activity functions (used to switch activities)
     */
    public void startTripActivity() throws UiObjectNotFoundException {
        UiObject startTrip = device.findObject(new UiSelector()
                .resourceId("finalproject.ee461l.journey:id/start_trip"));
        Assert.assertTrue(startTrip.exists());
        startTrip.click();

        //If we are prompted to log in, there was an issue
        UiObject loginPopup = device.findObject(new UiSelector()
                .text("Login Required")
                .resourceId("android:id/alertTitle"));
        Assert.assertFalse(loginPopup.exists());
    }

    public void joinTripActivity() throws UiObjectNotFoundException {
        UiObject joinTrip = device.findObject(new UiSelector()
                .resourceId("finalproject.ee461l.journey:id/join_trip"));
        Assert.assertTrue(joinTrip.exists());
        joinTrip.click();

        //If we are prompted to log in, there was an issue
        UiObject loginPopup = device.findObject(new UiSelector()
                .text("Login Required")
                .resourceId("android:id/alertTitle"));
        Assert.assertFalse(loginPopup.exists());
    }

    /*
    JourneyHome functions (pre/post route addition)
     */
    public void menu() throws UiObjectNotFoundException {
        UiObject menu = device.findObject(new UiSelector()
                .resourceId("finalproject.ee461l.journey:id/menu"));
        Assert.assertTrue(menu.exists());
        menu.click();
    }

    public void displayHelp() throws UiObjectNotFoundException {
        menu();
        UiObject action = device.findObject(new UiSelector()
                .className("android.widget.ListView")
                .childSelector(new UiSelector()
                        .text("Help"))
        );
        Assert.assertTrue(action.exists());
        action.click();

        UiObject help = device.findObject(new UiSelector()
                .text("Journey Help"));
        Assert.assertTrue(help.exists());
    }

    /*
    StartTripTest Functions
     */
    public void currentLocation() throws UiObjectNotFoundException {
        UiObject currLoc = tests.device.findObject(new UiSelector()
                .resourceId("finalproject.ee461l.journey:id/use_current_loc"));
        Assert.assertTrue(currLoc.exists());
        currLoc.click();
    }

    public void beginTrip() throws UiObjectNotFoundException {
        UiObject begin = tests.device.findObject(new UiSelector()
                .text("Begin Road Trip With These Settings"));
        Assert.assertTrue(begin.exists());
        begin.click();
    }

    public void caravanTrip() throws UiObjectNotFoundException {
        UiObject caravan = device.findObject(new UiSelector()
                .resourceId("finalproject.ee461l.journey:id/caravan_box"));
        Assert.assertTrue(caravan.exists());
        caravan.click();
    }

    public void startLocation() throws UiObjectNotFoundException {
        UiObject box = device.findObject(new UiSelector()
                .resourceId("finalproject.ee461l.journey:id/place_autocomplete_search_input")
                .text("Select Starting Destination"));
        Assert.assertTrue(box.exists());
        box.click();

        UiObject search = device.findObject(new UiSelector()
                .text("Search")
                .resourceId("com.google.android.gms:id/edit_text"));
        Assert.assertTrue(search.exists());
        search.setText(StartTripTest.START_LOC);
        try {
            //Give search time to perform
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        selectPlace();
    }

    public void endLocation() throws UiObjectNotFoundException {
        UiObject box = device.findObject(new UiSelector()
                .resourceId("finalproject.ee461l.journey:id/place_autocomplete_search_input")
                .text("Select Final Destination"));
        Assert.assertTrue(box.exists());
        box.click();

        UiObject search = device.findObject(new UiSelector()
                .text("Search")
                .resourceId("com.google.android.gms:id/edit_text"));
        Assert.assertTrue(search.exists());
        search.setText(StartTripTest.END_LOC);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        selectPlace();
    }

    public void selectPlace() throws UiObjectNotFoundException {
        //Select the first location
        UiObject place = device.findObject(new UiSelector()
                .className("android.support.v7.widget.RecyclerView")
                .resourceId("com.google.android.gms:id/list")
                .childSelector(new UiSelector().index(0))
        );
        Assert.assertTrue(place.exists());
        place.click();
    }
}
