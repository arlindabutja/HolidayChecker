package at.fhjoanneum.holidaychecker_arlinda;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.timessquare.CalendarPickerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by test on 06.01.2017.
 */

public class CalendarActivity extends Activity {

    private String fromDateStr;
    private String toDateStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar);

        Calendar minDate = Calendar.getInstance();
        minDate.add(Calendar.YEAR, -1);
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(java.util.Calendar.YEAR, 7);

        final CalendarPickerView calendar = (CalendarPickerView) findViewById(R.id.calendar_view2);

        Date today = new Date();
        calendar.init(minDate.getTime(), nextYear.getTime())
                .withSelectedDate(today)
                .inMode(CalendarPickerView.SelectionMode.RANGE);

        //Displaying all selected dates while clicking on a button
        Button goBack = (Button) findViewById(R.id.btn_show_dates);
        goBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get the date range given from user
                Date date = calendar.getSelectedDate();
                Date endDate  = calendar.getSelectedDates().get(calendar.getSelectedDates().size() -1);

                //format date
                SimpleDateFormat writeFormat = new SimpleDateFormat("dd-MM-yyyy");
                fromDateStr = writeFormat.format(date);
                toDateStr = writeFormat.format(endDate);

                //Get the last selected country name
                Bundle bundle = getIntent().getExtras();
                String countryNamePr = bundle.getString("LastSelectedCountry");
                System.out.println("LastSelectedCountry Calendar "+countryNamePr);
                //Create new Intent Object, and specify class
                Intent intent = new Intent();
                intent.setClass(CalendarActivity.this,MainActivity.class);
                intent.putExtra("fromDateStr",fromDateStr);
                intent.putExtra("toDateStr",toDateStr);
                intent.putExtra("lastSelectedCountry", countryNamePr);
                startActivity(intent);
            }
        });

    }

}
