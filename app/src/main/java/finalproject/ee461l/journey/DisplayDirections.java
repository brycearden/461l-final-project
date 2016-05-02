package finalproject.ee461l.journey;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class DisplayDirections extends AppCompatActivity {

    private List<String> directions;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_directions);

        //Get list of directions
        Intent intent = getIntent();
        directions = intent.getStringArrayListExtra(MapSupport.DIRECTIONS_ARRAY);

        //Display as a scrollable list
        showDirections();
    }

    private void showDirections() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.directions_layout);
        //For now, add a textview for each direction
        for (int i = 0; i < directions.size(); i++) {
            String step = directions.get(i);
            TextView view = new TextView(this);
            view.setText(Html.fromHtml(step));
            view.setTextSize(2, 18);
            view.setPadding(50, 5, 0, 5);
            view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            layout.addView(view);

            //Now we will add a bottom border
            View border = new View(this);
            border.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
            border.setBackgroundColor(Color.BLACK);
            border.setPadding(50, 5, 0, 5);
            layout.addView(border);
        }
    }
}
