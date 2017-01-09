package at.fhjoanneum.holidaychecker_arlinda;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.PopupWindow;
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
    public static EditText setDateRange;

    private String myCurrentLoc;

    //Calendar
    String fromDateStr;
    String toDateStr;

    //Location variables
    private GoogleApiClient mGoogleApiClient;
    private boolean mRequestLocationUpdates = false;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private int MY_PERMISSIONS_REQUEST;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        lblLocation = (TextView) findViewById(R.id.lblLocation);
        if (checkPlayServices()){
            buildGoogleApiClient();
            createLocationRequest();
        }

        //Spinner func.
        addListenerOnButton();
        setDateRange = (EditText) findViewById(R.id.selectDateRange);
        System.out.println("setDate: "+ setDateRange.getText());

        setDateRange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //To show current date in the datepicker
                /*
                Calendar mcurrentDate=Calendar.getInstance();
                int mYear=mcurrentDate.get(Calendar.YEAR);
                int mMonth=mcurrentDate.get(Calendar.MONTH);
                int mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker=new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                    //      Your code   to get date and time
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                // set day of month , month and year value in the edit text
                                selectDateRange.setText(dayOfMonth + "/"
                                        + (monthOfYear + 1) + "/" + year);

                            }
                    }
                },mYear, mMonth, mDay);
                mDatePicker.setTitle("Select date");
                mDatePicker.show();*/

                Intent i=new Intent(MainActivity.this, CalendarActivity.class);
                startActivity(i);

                //CalendarActivity.updateEditTextView();
            }
        });

        checkPermissions();
    }

    @Override
    protected void onStart() {

        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {

        super.onStop();
        checkPlayServices();
        if (mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }

    private void displayLocation() {

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longtitude = mLastLocation.getLongitude();

            Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.ENGLISH);
            try {
                List<Address> addresses = geoCoder.getFromLocation(latitude, longtitude, 1);

                String add = "";
                String fullAdd = "";

                if (addresses.size() > 0)
                {
                    add = addresses.get(0).getCountryName();


                    String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String country = addresses.get(0).getCountryName();
                    String postalCode = addresses.get(0).getPostalCode();
                    String knownName = addresses.get(0).getFeatureName();
                    fullAdd = address +"--"+ city +"--"+ state +"--"+ country +"--"+ postalCode +"--"+ knownName +"\n";
                }

                myCurrentLoc=add;
                //System.out.println("add" +add);
                lblLocation.setText(fullAdd);

                FileOutputStream fileout=openFileOutput("mytextfile.txt", MODE_PRIVATE);
                OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
                outputWriter.write(fullAdd);
                outputWriter.close();
                
                File file = getFileStreamPath("mytextfile.txt");
                String path = file.getPath();

            }
            catch (IOException e1) {
                e1.printStackTrace();
            }
        } else {
            if(myCurrentLoc != null){
                lblLocation.setText("The GPS is disabled or the device is not supported");
            }
        }
    }

    protected synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
    }

    protected void createLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        displayLocation();
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
        Toast.makeText(getApplicationContext(), "Location changed", Toast.LENGTH_SHORT).show();
        displayLocation();
    }

    // get the selected dropdown list value
    public void addListenerOnButton() {

        spinner1 = (Spinner) findViewById(R.id.spinner1);
        String[] countryName;
        List<String> listNames = new ArrayList<String>();
        List<String> listCode = new ArrayList<String>();

        String[] countryArrays = getResources().getStringArray(R.array.countries_array);
        for (int i = 0; i < countryArrays.length; i++) {
            String country = countryArrays[i];
            countryName = country.split("-");
            listNames.add(countryName[0]);
            listCode.add(countryName[1]);
        }


        ArrayAdapter<String> dataAdapterNames = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item, listNames);

        //ArrayAdapter<String> dataAdapterCode = new ArrayAdapter<String>(this,
        //        android.R.layout.simple_spinner_dropdown_item, countryArrays);

        // Assign adapter to ListView
        spinner1.setAdapter(dataAdapterNames);

        btnSubmit = (Button) findViewById(R.id.buttonSubmit);
        buttonShowLocation = (Button) findViewById(R.id.buttonShowLocation);

        buttonShowLocation.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                displayLocation();
                System.out.println("myCurrentLoc:"+myCurrentLoc);

                System.out.println("lblLocation: "+lblLocation.getText());

                if(myCurrentLoc != null){
                    System.out.println("myCurrentLoc Inside:"+myCurrentLoc);
                    int index = getIndex(spinner1,myCurrentLoc);
                    spinner1.setSelection(index);
                }
               // displayContacts();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Get the data selected by the user and executed the userinput
                Bundle bundleCalendar = getIntent().getExtras();

                if(bundleCalendar != null){
                    fromDateStr = bundleCalendar.getString("fromDateStr");
                    toDateStr= bundleCalendar.getString("toDateStr");
                    Intent intent = new Intent(MainActivity.this, ListHolidays.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("SpinnerValue",spinner1.getSelectedItem().toString());
                    bundle.putString("date",fromDateStr);
                    bundle.putString("endDate",toDateStr);
                    intent.putExtras(bundle);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "Please select a date range!", Toast.LENGTH_SHORT).show();
                }

            }

        });
    }

    public int getIndex(Spinner spinner, String myString){
        int index = 0;
        for (int i=0;i<spinner.getCount();i++){
            String country = spinner.getItemAtPosition(i).toString().trim();
            if (country.equalsIgnoreCase(myString)){
                index = i;
                break;
            }
        }
        return index;
    }


    public void displayContacts() {
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Toast.makeText(MainActivity.this, "Name: " + name + ", Phone No: " + phoneNo, Toast.LENGTH_SHORT).show();
                        updateContact(name, "1111111");
                    }
                    pCur.close();
                }
            }
        }
    }

    public void updateContact(String name, String phone) {
        ContentResolver cr = getContentResolver();

        String where = ContactsContract.Data.DISPLAY_NAME + " = ? AND " +
                ContactsContract.Data.MIMETYPE + " = ? AND " +
                String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE) + " = ? ";
        String[] params = new String[] {name,
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE_HOME)};

        Cursor phoneCur = getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, where, params, null);

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        if ( (null == phoneCur)  ) {
            Toast.makeText(MainActivity.this, "Contact does not exist", Toast.LENGTH_SHORT).show();
        } else {
            ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(where, params)
                    .withValue(ContactsContract.CommonDataKinds.Phone.DATA, phone)
                    .build());
        }

        phoneCur.close();

        try {
            cr.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Toast.makeText(MainActivity.this, "Updated the phone number of" + name +" to: " + phone, Toast.LENGTH_SHORT).show();
    }


    public void checkPermissions(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED)
        {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST);

        }

    }

}
