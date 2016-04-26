package finalproject.ee461l.journey;

import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import java.util.ArrayList;

/**
 * Created by kevinrosen1 on 4/2/16.
 */
public class NavDrawerSupport {
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private String userEmail;

    public NavDrawerSupport(Context context, ListView list, DrawerLayout drawer) {
        mDrawerList = list;
        mDrawerLayout = drawer;
        userEmail = "";

        navDrawer(context);
    }

    public ArrayList<String> navDrawerItems(boolean login) {
        ArrayList<String> navItems = new ArrayList<String>();
        if (login) navItems.add("Log In");
        else navItems.add("Log Out");
        navItems.add("Settings");
        navItems.add("Help");
        navItems.add("About");
        return navItems;
    }

    public void navDrawer(Context context) {
        ArrayList<String> navItems = navDrawerItems(true);


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

    public void updateNavList(Context context, boolean login) {
        ArrayList<String> navItems = navDrawerItems(login);
        mDrawerList.setAdapter(new ArrayAdapter<String>(context,
                R.layout.drawer_list_item, navItems));
    }

    public void toggleDrawer() {
        //The only time button can be pressed is when drawer is closed, so this will simply open the drawer
        mDrawerLayout.openDrawer(mDrawerList);
    }

    public String getUserEmail() { return userEmail; }

    //Google Sign in functionality
    public boolean signIn(Intent data, Context context) {
        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        if (result.isSuccess()) {
            //Successfully signed in
            GoogleSignInAccount acct = result.getSignInAccount();
            System.out.println(acct.getDisplayName() + ", " + acct.getEmail());
            userEmail = acct.getEmail();
            //We also need to update the nav drawer to allow the user to log out
            updateNavList(context, false);
            return true;
        }
        else {
            //Failure
            System.out.println("Failed to sign user in");
            return false;
        }
    }

    public boolean signOut(Context context, GoogleApiClient client) {
        Auth.GoogleSignInApi.signOut(client).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        //User has logged out
                        System.out.println("User is now logged out of the app");
                    }
                }
        );
        updateNavList(context, true);
        userEmail = "";
        return false;
    }
}
