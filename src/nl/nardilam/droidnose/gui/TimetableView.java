package nl.nardilam.droidnose.gui;

import java.util.ArrayList;
import java.util.List;
import nl.nardilam.droidnose.Event;
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
	private static final int DEFAULT_STARTHOUR = 9;
	private static final int DEFAULT_ENDHOUR = 17;
	
	private final Timetable timetable;
	private final Day day;
	
	private LinearLayout hourLayout = null;
	private DateTitleView dummyDateView = null;
	private HourScrollView hourScrollView = null;
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
		
		hourScrollView = new HourScrollView(context);
		hourLayout.addView(hourScrollView);
		
		int startHour = DEFAULT_STARTHOUR, endHour = DEFAULT_ENDHOUR;
		hourView = new HourView(context, startHour, endHour);
		hourScrollView.addView(hourView);
		
		Day firstDay = this.getNextNonEmptyDay(day);
		dayView = new DayView(context, timetable, firstDay, hourView, hourScrollView);
		this.addView(dayView);
	}
	
	private Day getNextNonEmptyDay(Day fromDay)
	{
		List<Event> dayEvents = new ArrayList<Event>();
		List<Event> allEvents = this.timetable.getEvents();
		if (!allEvents.isEmpty())
		{
			Event lastEvent = allEvents.get(allEvents.size() - 1);
			if (fromDay.startTime.isAfter(lastEvent.startTime))
			{
				return fromDay;
			}
			
			Day currentDay = fromDay;
			do
			{
				dayEvents = timetable.startDuring(currentDay);
				if (dayEvents.isEmpty())
				{
					currentDay = currentDay.add(1);
				}
			} while (dayEvents.isEmpty());
			return currentDay;
		}
		else
			return fromDay;
	}
	
	/*
	 * Een speciale ScrollView-klasse die de hourHeight van de
	 * hourView en de dayViews set zodra deze bekend is.
	 * (Deze is afhankelijk van de grootte van de parentview.)
	 */
	private class HourScrollView extends LinkedScrollView
	{
		public HourScrollView(Context context)
		{
			super(context);
			
			this.setVerticalScrollBarEnabled(false);
		}	
		
		private boolean measured = false;
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
		{
			int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
			int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
			
			if (!measured)
			{
				if (parentWidth > parentHeight)
					hourView.setAvailableHeight(parentWidth);
				else
					hourView.setAvailableHeight(parentHeight);
				
				dayView.setHourHeight(hourView.getHourHeight());
				
				measured = true;
			}
			
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}
}
