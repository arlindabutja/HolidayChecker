package at.fhjoanneum.holidaychecker_arlinda;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
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
    public String spinnerValue;
    public String fromDateStr;
    public String toDateStr;
    public TextView errorMessage;

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

                if (out != null) {

                    JSONArray jsonArray = new JSONArray(out.toString());

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
                        if(dayOfWeek == "1") {
                            dayOfWeek = "Mon.";
                        } else if (dayOfWeek == "2") {
                            dayOfWeek = "Tue.";
                        } else if (dayOfWeek == "3") {
                            dayOfWeek = "Wed.";
                        } else if (dayOfWeek == "4") {
                            dayOfWeek = "Thu.";
                        }else if (dayOfWeek == "5") {
                            dayOfWeek = "Fri.";
                        }else if (dayOfWeek == "6") {
                            dayOfWeek = "Sat.";
                        }else if (dayOfWeek == "7") {
                            dayOfWeek = "Sun.";
                        }

                        if(month == "1") {
                            month = "Jan.";
                        } else if (month == "2") {
                            month = "Feb.";
                        } else if (month == "3") {
                            month = "Mar.";
                        } else if (month == "4") {
                            month = "Apr.";
                        }else if (month == "5") {
                            month = "May.";
                        }else if (month == "6") {
                            month = "Jun.";
                        }else if (month == "7") {
                            month = "Jul.";
                        }else if (month == "8") {
                            month = "Aug.";
                        }else if (month == "9") {
                            month = "Sept.";
                        }else if (month == "10") {
                            month = "Oct.";
                        }else if (month == "11") {
                            month = "Nov.";
                        }else if (month == "12") {
                            month = "Dec.";
                        }

                        // tmp hash map for single value
                        HashMap<String, String> dataOfHoliday = new HashMap<>();

                        // adding each child node to HashMap key => value
                        dataOfHoliday.put("day", day);
                        dataOfHoliday.put("month", month);
                        dataOfHoliday.put("year", year);
                        dataOfHoliday.put("dayOfWeek", dayOfWeek);
                        dataOfHoliday.put("localName", localName);
                        dataOfHoliday.put("englishName", englishName);

                        // adding holidays to the list
                        dataArray.add(dataOfHoliday);
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
            showErrorMessage();
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

            // Assign adapter to ListView
            listView.setAdapter(adapter);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.holiday_listitem);

        errorMessage = (TextView) findViewById(R.id.errorMessage);
        errorMessage.setVisibility(View.GONE);

        // Defined Array values to show in ListView
        dataValues = new String[]{};
        dataArray = new ArrayList<>();

        //Get user input from the other activity
        Bundle bundle = getIntent().getExtras();
        spinnerValue = bundle.getString("SpinnerValue");
        fromDateStr = bundle.getString("date");
        toDateStr = bundle.getString("endDate");

        String getHolidaysApiUrl = "http://kayaposoft.com/enrico/json/v1.0/?action=getPublicHolidaysForDateRange&fromDate="
                + fromDateStr +"&toDate="
                + toDateStr
                +"&country=" + spinnerValue;
        //+ "&region=District+Of+" +"";
        HttpHelper helper = new HttpHelper();
        helper.execute(getHolidaysApiUrl);
    }

    public void showErrorMessage(){

        if (errorMessage.getVisibility() == View.VISIBLE){
            errorMessage.setVisibility(View.GONE);
        }

        if (dataArray.size() == 0 || dataArray == null) {
            errorMessage.setVisibility(View.VISIBLE);
            errorMessage.setText("There are no holidays for the selected country: " + spinnerValue +
                    " on the selected date range " + fromDateStr + " / " +toDateStr);
        } else {
            errorMessage.setVisibility(View.GONE);
        }

    }


}