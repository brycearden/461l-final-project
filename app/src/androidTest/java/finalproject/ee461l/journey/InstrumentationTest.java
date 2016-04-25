package finalproject.ee461l.journey;

import android.app.Fragment;
import android.app.FragmentManager;

import com.google.android.gms.maps.MapFragment;

import org.junit.Assert;

/**
 * Created by kevinrosen1 on 4/25/16.
 */
public class InstrumentationTest extends android.test.ActivityInstrumentationTestCase2<JourneyHome> {

    JourneyHome home;
    Fragment active;

    public InstrumentationTest() {
        super(JourneyHome.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        home = (JourneyHome) getActivity();
        active = home.getFragmentManager().findFragmentById(R.id.home_view);
    }

    public void testOnCreate() {
        //Objects that are instantiated
        Assert.assertNotNull(home.voice);
        Assert.assertNotNull(home.map);
        Assert.assertNotNull(home.nav);
        Assert.assertNotNull(home.client);
        //Variables that are instantiated
        Assert.assertTrue(home.stopType.equals(""));
        Assert.assertTrue(home.timeToStop == 0);
        Assert.assertTrue(home.distanceFromRoute == 0);
        //Fragment that is up
        Assert.assertNotNull(active);
        Assert.assertTrue(active.getClass() == MapFragment.class);
    }
}
