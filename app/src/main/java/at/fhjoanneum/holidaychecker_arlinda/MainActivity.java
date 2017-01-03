package at.fhjoanneum.holidaychecker_arlinda;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.squareup.timessquare.CalendarPickerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends Activity  implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    //GUI
    private Button buttonShowLocation, btnSubmit;
    private TextView lblLocation;
    private Spinner spinner1;
    private CalendarPickerView calendar;

    private String myCurrentLoc;

    //Location variables
    private GoogleApiClient mGoogleApiClient;
    private boolean mRequestLocationUpdates = false;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private static int UPDATE_INTERVAL = 10000;
    private static int FASTEST_INTERVAL = 5000;
    private static int DISPLACEMENT = 10;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private int MY_PERMISSIONS_REQUEST_LOCATION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lblLocation = (TextView) findViewById(R.id.lblLocation);
        if (checkPlayServices()){
            buildGoogleApiClient();
            createLocationRequest();
        }
        //displayLocation();
       // addListenerOnSpinnerItemSelection();
        addListenerOnButton();

        /* Calendar */
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);

        calendar = (CalendarPickerView) findViewById(R.id.calendar_view);
        Date today = new Date();
        calendar.init(today, nextYear.getTime())
                .withSelectedDate(today)
                .inMode(CalendarPickerView.SelectionMode.RANGE);
        calendar.highlightDates(getHolidays());

    }

    private ArrayList<Date> getHolidays(){
        //SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyyy");
        //Date dateInString =  new Date();
        Date date = new Date();
        /*
        try {
            date = sdf.parse(dateInString);
        } catch (ParseException e) {
            e.printStackTrace();
        }*/
        ArrayList<Date> holidays = new ArrayList<>();
        holidays.add(date);
        return holidays;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
        if (mGoogleApiClient.isConnected() && mRequestLocationUpdates)
            startLocationUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();
        checkPlayServices();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);

        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longtitude = mLastLocation.getLongitude();

            Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
            try {
                List<Address> addresses = geoCoder.getFromLocation(latitude, longtitude, 1);

                String add = "";
                if (addresses.size() > 0)
                {
                    add = addresses.get(0).getCountryName();
                }

                myCurrentLoc=add;
                lblLocation.setText(myCurrentLoc);
            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
        } else {
            if(myCurrentLoc !=""){
                lblLocation.setText("The GPS is disabled or the device is not supported");
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        return true;
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        if (mRequestLocationUpdates){
            startLocationUpdates();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        Toast.makeText(getApplicationContext(), "Location changed", Toast.LENGTH_SHORT);
        displayLocation();
    }

    private int getIndex(Spinner spinner, String myString)
    {
        int index = 0;

        for (int i=0;i<spinner.getCount();i++){
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString)){
                index = i;
                break;
            }
        }
        return index;
    }

    // get the selected dropdown list value
    public void addListenerOnButton() {

        spinner1 = (Spinner) findViewById(R.id.spinner1);
        btnSubmit = (Button) findViewById(R.id.buttonSubmit);
        buttonShowLocation = (Button) findViewById(R.id.buttonShowLocation);

        buttonShowLocation.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                displayLocation();
                if(myCurrentLoc != ""){
                    int index = getIndex(spinner1, myCurrentLoc);
                    spinner1.setSelection(index);
                }

            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Date date = calendar.getSelectedDate();
                Date endDate  = calendar.getSelectedDates().get(calendar.getSelectedDates().size() -1);

                

                /*
                Toast.makeText(MainActivity.this,
                                "\nLocation : "+ String.valueOf(spinner1.getSelectedItem()) + "\nSelected dates : " +date +endDate,
                        Toast.LENGTH_SHORT).show();
                */
            }

        });


        
    }

}
