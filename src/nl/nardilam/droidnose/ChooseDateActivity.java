package nl.nardilam.droidnose;

import java.util.Calendar;
import java.util.TimeZone;
import nl.nardilam.droidnose.datetime.Day;
import nl.nardilam.droidnose.datetime.TimeUtils;

import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.RelativeLayout;
import android.support.v4.app.NavUtils;

public class ChooseDateActivity extends Activity
{
	public static final int DATE_REQUEST = 0;
	public static final String YEAR = "nl.nardilam.droidnose.YEAR";
	public static final String MONTH = "nl.nardilam.droidnose.MONTH"; 
	public static final String DAY = "nl.nardilam.droidnose.DAY";
	
	private final ChooseDateActivity activity = this;
	
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        RelativeLayout layout = new RelativeLayout(this);
        
        final DatePicker datePicker = new DatePicker(this);	
        datePicker.setId(1);
		RelativeLayout.LayoutParams dpParams =
				new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		dpParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		
		if (Build.VERSION.SDK_INT >= 11)
		{
			datePicker.setCalendarViewShown(true);
			datePicker.setSpinnersShown(false);
		}
		if (Build.VERSION.SDK_INT >= 12)
		{
			CalendarView cv = datePicker.getCalendarView();
			cv.setFirstDayOfWeek(Calendar.MONDAY);
		}
        
        Button finished = new Button(this);
        finished.setText("Klaar!");
        View.OnClickListener onFinished = new View.OnClickListener()
        {
			public void onClick(View view)
			{
				Day selectedDay = new Day(datePicker.getYear(),
						datePicker.getMonth() + 1,
						datePicker.getDayOfMonth(),
						TimeUtils.CET);
				activity.setResult(RESULT_OK, createIntentFromDay(selectedDay, new Intent()));
				activity.finish();
			}
        };
        finished.setOnClickListener(onFinished);
        
        RelativeLayout.LayoutParams finParams =
				new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		finParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		finParams.addRule(RelativeLayout.BELOW, datePicker.getId());
        
        Day selectedDay = createDayFromintent(this.getIntent());
        datePicker.init(selectedDay.year, selectedDay.month - 1, selectedDay.day, null);
        
        layout.addView(datePicker, dpParams);
        layout.addView(finished, finParams);
        
        this.setContentView(layout);
    }
    
    public static Intent createIntentFromDay(Day day, Intent emptyIntent)
    {
    	emptyIntent.putExtra(YEAR, day.year);
    	emptyIntent.putExtra(MONTH, day.month);
    	emptyIntent.putExtra(DAY, day.day);
    	return emptyIntent;
    }
    
    public static Day createDayFromintent(Intent intent)
    {
    	Day today = Day.today();
        int year = intent.getIntExtra(YEAR, today.year);
        int monthOfYear = intent.getIntExtra(MONTH, today.month);
        int dayOfMonth = intent.getIntExtra(DAY, today.day);
        return new Day(year, monthOfYear, dayOfMonth, TimeUtils.CET);
    }
}
