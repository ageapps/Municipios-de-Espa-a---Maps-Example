package es.age.apps.municipiosespana;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Adrián García Espinosa on 25/3/16.
 */

public class MapsActivity extends AppCompatActivity implements
        GoogleMap.OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private static final String TAG = MapsActivity.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private List<Marker> mMarkers;
    private Location mLastLocation;
    private ArrayList<String> names;
    private String[] location_x;
    private String[] location_y;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        toolbar.setTitle(getResources().getString(R.string.title_activity_maps));
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // First we need to check availability of play services
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();
        }


        // SetUp XML resources
        names = new ArrayList<String>(); // Better use of List to find the index of a name
        String[] s = getResources().getStringArray(R.array.municipios);
        for (int i = 0; i< s.length; i++){
            names.add(s[i]);
        }
        location_x= getResources().getStringArray(R.array.municipios_x);
        location_y = getResources().getStringArray(R.array.municipios_y);
        mMarkers = new ArrayList<Marker>();

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if( !locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("GPS not found");  // GPS not found
            builder.setMessage("Want to enable GPS"); // Want to enable?
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
            });
            builder.setNegativeButton("No", null);
            builder.create().show();
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, names);
        //Find the AutoCompleteTextView control
        AutoCompleteTextView acmpTxt = (AutoCompleteTextView) findViewById(R.id.autocmp);
        //Set the number of characters the user must type before the drop down list is shown
        acmpTxt.setThreshold(3);
        //Set the adapter
        acmpTxt.setAdapter(adapter);
        //Set OnItemClickListener
        acmpTxt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //Hide the keyboard to see better the map
                hideKeyboard();

                String item = (String) parent.getItemAtPosition(position);
                int index = names.indexOf(item);

                //Show Toast with item selected
                Toast.makeText(MapsActivity.this,item, Toast.LENGTH_SHORT).show();

                //Move camera to selected item
                CameraPosition cameraPosition = CameraPosition.builder()
                        .target(new LatLng(Double.parseDouble(location_x[index]),
                                Double.parseDouble(location_y[index])))
                                .zoom(12f)
                                .bearing(0.0f)
                                .tilt(0.0f)
                                .build();

                mMap.animateCamera(CameraUpdateFactory
                        .newCameraPosition(cameraPosition), null);
                //Show info from selected item
                mMarkers.get(index).showInfoWindow();

            }
        });

    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).
                    hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode,this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(this,
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Enable map controls
        mMap.getUiSettings().setZoomControlsEnabled(true);
        // Add markers for each location
        for (int i = 0; i < names.size(); i++) {
            LatLng coords = new LatLng(Double.parseDouble(location_x[i]),Double.parseDouble(location_y[i]));
            Marker m = mMap.addMarker(new MarkerOptions().position(coords).title(names.get(i)));
            mMarkers.add(m);
        }

        // Set marker titles clickable
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                //get library from marker
                String name = names.get(mMarkers.indexOf(marker));
                String x = location_x[mMarkers.indexOf(marker)];
                String y = location_y[mMarkers.indexOf(marker)];
                Intent i = new Intent(MapsActivity.this, DetailActivity.class);
                i.putExtra(DetailActivity.EXTRA_NAME,name);
                i.putExtra(DetailActivity.EXTRA_X, x);
                i.putExtra(DetailActivity.EXTRA_Y,y);
                MapsActivity.this.startActivity(i);
            }
        });



        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
        displayLocation();
    }
    @Override
    public void onDestroy() {
        this.getFragmentManager().beginTransaction().addToBackStack("map");
        super.onDestroy();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission((AppCompatActivity) this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPlayServices();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }
    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {

        // Once connected with google api, get the location
        displayLocation();
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    /**
     * Method to display the location on UI
     * */
    private void displayLocation() {

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);



        if (mLastLocation != null) {
            CameraPosition position = CameraPosition.builder()
                    .target( new LatLng( mLastLocation.getLatitude(),
                            mLastLocation.getLongitude() ) )
                    .zoom( 8f )
                    .bearing( 0.0f )
                    .tilt( 0.0f )
                    .build();

            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), null);
        }
    }
}
