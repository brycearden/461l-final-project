package finalproject.ee461l.journey;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.location.Location;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.maps.MapFragment;

import java.util.ArrayList;

/**
 * Created by kevinrosen1 on 3/24/16.
 */
public class GeneralSupport {
    public static ArrayList<String> navDrawerItems() {
        ArrayList<String> navItems = new ArrayList<String>();
        navItems.add("Log In");
        navItems.add("Settings");
        navItems.add("Help");
        navItems.add("About");
        return navItems;
    }

    public static void navDrawer(ListView mDrawerList, DrawerLayout mDrawerLayout, Context context) {
        ArrayList<String> navItems = GeneralSupport.navDrawerItems();


        mDrawerList.setAdapter(new ArrayAdapter<String>(context,
                R.layout.drawer_list_item, navItems));

        //Handling navigation drawer open/close
        mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                System.out.println("Just Opened the drawer");
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                System.out.println("Just Closed the drawer");
            }
        });
    }
/*
    public static com.google.android.gms.location.LocationListener getLocationListener() {
        return new com.google.android.gms.location.LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                MapSupport.updateLocation(location);
            }
        };
    }
*/
}
