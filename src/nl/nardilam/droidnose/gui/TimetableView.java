package nl.nardilam.droidnose.gui;

import nl.nardilam.droidnose.Orientation;
import nl.nardilam.droidnose.Timetable;
import nl.nardilam.droidnose.datetime.Day;
import android.content.Context;
import android.widget.LinearLayout;

/*
 * Deze view geeft een deel van een rooster weer.
 * Het aantal dagen wat weergegeven wordt is afhankelijk van de
 * beschikbare ruimte, alleen de meest linkse dag wordt meegegeven.
 */
public class TimetableView extends LinearLayout
{	
	private final Timetable timetable;
	private final Day day;
	
	private LinearLayout hourLayout = null;
	private DateTitleView dummyDateView = null;
	private HourView hourView = null;
	private DayView dayView = null;
	
	public TimetableView(Context context, Timetable timetable, Day day)
	{
		super(context);
		
		this.timetable = timetable;
		this.day = day;
		
		this.setOrientation(Orientation.HORIZONTAL);
		
		hourLayout = new LinearLayout(context);
		hourLayout.setOrientation(Orientation.VERTICAL);
		this.addView(hourLayout);
		
		/*
		 * Deze is er alleen om de uren op de goede plaats te krijgen
		 */
		dummyDateView = new DateTitleView(context, null);
		hourLayout.addView(dummyDateView);
		
		hourView = new HourView(context, TimeLayout.DEFAULT_STARTHOUR, TimeLayout.DEFAULT_ENDHOUR);
		hourLayout.addView(hourView);
		
		MultiDayView dayView = new MultiDayView(context, timetable, day, hourView);
		hourView.addHourHeightListener(dayView);
		this.addView(dayView);
	}
}
