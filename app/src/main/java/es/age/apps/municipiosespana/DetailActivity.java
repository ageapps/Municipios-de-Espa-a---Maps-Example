package es.age.apps.municipiosespana;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Adrián García Espinosa on 25/3/16.
 */
public class DetailActivity extends AppCompatActivity {

    public static String EXTRA_NAME = "map_name";
    public static String EXTRA_X = "map_x";
    public static String EXTRA_Y = "map_y";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get Extras
        String name = getIntent().getStringExtra(EXTRA_NAME);
        String x = getIntent().getStringExtra(EXTRA_X);
        String y = getIntent().getStringExtra(EXTRA_Y);

        // SetUp Views
        TextView title = (TextView) findViewById(R.id.title);
        TextView txt_x = (TextView) findViewById(R.id.text_x);
        TextView txt_y = (TextView) findViewById(R.id.text_y);

        title.setText(name);
        txt_x.setText("Coordinate x: " + x);
        txt_y.setText("Coordinate y: " + y);

        // Set Activity Title
        getSupportActionBar().setTitle("Municipio - " + name);
    }

}
