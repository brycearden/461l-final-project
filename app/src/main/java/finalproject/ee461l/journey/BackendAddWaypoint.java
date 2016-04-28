package finalproject.ee461l.journey;

import android.os.AsyncTask;

/**
 * Created by kevinrosen1 on 4/27/16.
 */
public class BackendAddWaypoint extends AsyncTask<String, Void, String> {

    private BackendFunctionality backend;

    public BackendAddWaypoint() {
        backend = BackendFunctionality.getInstance();
    }

    @Override
    protected String doInBackground(String... params) {
        String email = params[0];
        return null;
    }
}
