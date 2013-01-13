package nl.nardilam.droidnose;

import java.util.Calendar;
import nl.nardilam.droidnose.datetime.Day;
import nl.nardilam.droidnose.datetime.TimeUtils;

import android.os.Build;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class ChooseDateActivity extends ContextActivity
{
	public static final String YEAR = "nl.nardilam.droidnose.Year";
	public static final String MONTH = "nl.nardilam.droidnose.Month"; 
	public static final String DAY = "nl.nardilam.droidnose.Day";
	
	private final ChooseDateActivity activity = this;
	
    @SuppressLint("NewApi")
	public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        RelativeLayout layout = new RelativeLayout(this);
        
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(Orientation.VERTICAL);
        
        final DatePicker datePicker = new DatePicker(this);	
		datePicker.setId(1);
        
		if (Build.VERSION.SDK_INT >= 11)
		{
			if (Utils.isInPortraitMode())
			{
				datePicker.setCalendarViewShown(true);
				datePicker.setSpinnersShown(false);
				
				if (Build.VERSION.SDK_INT >= 12)
				{
					CalendarView cv = datePicker.getCalendarView();
					cv.setFirstDayOfWeek(Calendar.MONDAY);
					cv.setShowWeekNumber(true);
				}
			}
			else
			{
				datePicker.setCalendarViewShown(false);
				datePicker.setSpinnersShown(true);
			}
			
			//float density = Utils.getScreenDensity();
			//datePicker.setScaleX(density);
	        //datePicker.setScaleY(density);
		}
        
        Day selectedDay = createDayFromIntent(this.getIntent());
        datePicker.init(selectedDay.year, selectedDay.month - 1, selectedDay.day, null);
        
        RelativeLayout.LayoutParams dateParams =
				new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		dateParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        
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
				activity.setResult(Activity.RESULT_OK, createIntentFromDay(selectedDay, new Intent()));
				activity.finish();
			}
        };
        finished.setOnClickListener(onFinished);
        
        RelativeLayout.LayoutParams finishedParams =
				new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		finishedParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		finishedParams.addRule(RelativeLayout.BELOW, datePicker.getId());
		
		layout.addView(datePicker, dateParams);
		layout.addView(finished, finishedParams);
		
        this.setContentView(layout);
    }
    
    public static Intent createIntentFromDay(Day day, Intent emptyIntent)
    {
    	emptyIntent.putExtra(YEAR, day.year);
    	emptyIntent.putExtra(MONTH, day.month);
    	emptyIntent.putExtra(DAY, day.day);
    	return emptyIntent;
    }
    
    public static Day createDayFromIntent(Intent intent)
    {
    	Day today = Day.today();
        int year = intent.getIntExtra(YEAR, today.year);
        int monthOfYear = intent.getIntExtra(MONTH, today.month);
        int dayOfMonth = intent.getIntExtra(DAY, today.day);
        return new Day(year, monthOfYear, dayOfMonth, TimeUtils.CET);
    }
}
