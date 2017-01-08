package at.fhjoanneum.holidaychecker_arlinda;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by arlinda on 05.01.2017.
 */

public class ListHolidays extends Activity {
    String[] dataValues;
    ArrayList<HashMap<String, String>> dataArray;
    public static String[] countryCodeValue;

    public class HttpHelper extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            StringBuilder out = new StringBuilder();
            try {
                // get the string parameter from execute()
                URL url = new URL(params[0]);

                // create Urlconnection
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                // read inputstrem
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    out.append(line);
                }
                //Log.i("INTERNET", out.toString());

                if (out != null) {
                    //JSONObject jsonObj = new JSONObject(out.toString());
                    JSONArray jsonArray = new JSONArray(out.toString());

                    // Getting JSON Array node
                    //JSONArray data = jsonArray.getJSONArray().get(0);

                    // looping through All Contacts
                    for (int i = 0; i < jsonArray.length(); i++) {
                        //JSONArray c = jsonArray.getJSONArray(i);
                        JSONObject obj = jsonArray.getJSONObject(i);
                        String localName = obj.getString("localName");
                        String englishName = obj.getString("englishName");

                        // Date node is JSON Object
                        JSONObject date = obj.getJSONObject("date");
                        String day = date.getString("day");
                        String month = date.getString("month");
                        String year = date.getString("year");
                        String dayOfWeek = date.getString("dayOfWeek");
                        //String dayOfWeekFormated = new SimpleDateFormat("EEE").format(dayOfWeek);

                        // tmp hash map for single value
                        HashMap<String, String> dataOfHoliday = new HashMap<>();

                        // adding each child node to HashMap key => value
                        dataOfHoliday.put("day", day);
                        dataOfHoliday.put("month", month);
                        dataOfHoliday.put("year", year);
                        dataOfHoliday.put("dayOfWeek", dayOfWeek);
                        dataOfHoliday.put("localName", localName);
                        dataOfHoliday.put("englishName", englishName);

                        //System.out.println("dataOfHoliday: " +dataOfHoliday);
                        // adding holidays to the list
                        dataArray.add(dataOfHoliday);
                        System.out.println("dataArray: " +dataArray);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return out.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            updateList();
        }
    }

    private void updateList(){
        ListView listView = (ListView) findViewById(R.id.list);
        if(dataArray != null){
            ListAdapter adapter = new SimpleAdapter(
                    ListHolidays.this,
                    dataArray,
                    R.layout.list_item,
                    new String[]{"dayOfWeek","day", "month", "localName", "englishName","year"},
                    new int[]{R.id.dayOfWeek, R.id.day, R.id.month, R.id.localName, R.id.englishName, R.id.year});

            //Toast.makeText(ListHolidays.this,"dataArray: "+ dataArray,Toast.LENGTH_SHORT).show();;
            //System.out.println("dataArray: "+ dataArray);
            // Assign adapter to ListView
            listView.setAdapter(adapter);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.holiday_listitem);

        //updateList();

        // Defined Array values to show in ListView
        dataValues = new String[]{};
        dataArray = new ArrayList<>();

        //Get user input from the other activity
        Bundle bundle = getIntent().getExtras();
        String spinnerValue = bundle.getString("SpinnerValue");
        String fromDateStr = bundle.getString("date");
        String toDateStr = bundle.getString("endDate");

        /*
        //format date
        SimpleDateFormat writeFormat = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat readFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.GERMANY);
        Date fromDate = new Date();
        Date toDate = new Date();

        try {
            fromDate = readFormat.parse(date);
            toDate = readFormat.parse(endDate);
            Log.i("Dateeeeee:", date);

        } catch (ParseException e) {
            e.printStackTrace();
        }*/

        /*
        String fromDateStr = readFormat.format(fromDate);
        String toDateStr = writeFormat.format(toDate);*/

        countryCodeValue = spinnerValue.split(" ");

        String getHolidaysApiUrl = "http://kayaposoft.com/enrico/json/v1.0/?action=getPublicHolidaysForDateRange&fromDate=" + fromDateStr +"&toDate=" + toDateStr +"&country=" +
                //countryCodeValue[2].toString()
                "aut"
                + "&region=District+Of+" +"";
        HttpHelper helper = new HttpHelper();
        helper.execute(getHolidaysApiUrl);

        /*
        Toast.makeText(ListHolidays.this,
                "\nLocation : "+
                        //countryCodeValue[2]
                        ""
                        + "\nSelected dates : " +fromDateStr + "\n" +toDateStr +
                        "\nValues " + dataArray,
                Toast.LENGTH_SHORT).show();*/

    }


}
