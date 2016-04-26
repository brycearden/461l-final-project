package finalproject.ee461l.journey;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

public class JoinTrip extends AppCompatActivity implements OnTaskCompleted {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_trip);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Let's get the intent from JourneyHome
        Intent intent = getIntent();
    }

    public void searchForUser(View view) {
        EditText emailView = (EditText) findViewById(R.id.enter_email);
        String email = emailView.getText().toString().toLowerCase();
        if (!email.contains("@") || !email.contains(".")) return;
        //Quite possibly a valid email. Let's go through the checks
        new SearchForUser(this).execute(email);
    }

    @Override
    public void onTaskCompleted(String result, String start, String end) {
        //SearchForUser is done. lets handle this
        Intent intent = new Intent();
        intent.putExtra("JSONDirections", result);
        intent.putExtra("StartLocLatLng", start);
        intent.putExtra("EndLocLatLng", end);
        //Also add start/end place ids to intent
        intent.putExtra("StartLocationId", "N/A");
        intent.putExtra("EndLocationId", "N/A");
        //Attach caravan info
        intent.putExtra("isCaravanTrip", true);
        if (result != null && result != "") setResult(RESULT_OK, intent);
        else setResult(RESULT_CANCELED, intent);
        finish();
    }
}
