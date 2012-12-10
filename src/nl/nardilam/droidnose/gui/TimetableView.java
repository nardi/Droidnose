package nl.nardilam.droidnose.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.nardilam.droidnose.Event;
import nl.nardilam.droidnose.Orientation;
import nl.nardilam.droidnose.Timetable;
import nl.nardilam.droidnose.Utils;
import nl.nardilam.droidnose.datetime.Day;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.LinearLayout.LayoutParams;

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
	private final Day startDay;
	
	private LinearLayout hourLayout = null;
	private DateTitleView dummyDateView = null;
	private HourScrollView hourScrollView = null;
	private HourView hourView = null;
	private DayScrollView dayScrollView = null;
	private LinearLayout dayLayout = null;
	private DayView dayView = null;
	
	private int daysOnScreen = 1;
	
	public TimetableView(Context context, Timetable timetable, Day startDay)
	{
		super(context);
		
		this.timetable = timetable;
		this.startDay = startDay;
		
		this.setOrientation(Orientation.HORIZONTAL);
		
		hourLayout = new LinearLayout(context);
		hourLayout.setOrientation(Orientation.VERTICAL);
		//hourLayout.setBackgroundColor(Color.YELLOW);
		this.addView(hourLayout);
		
		dummyDateView = new DateTitleView(context, null);
		//dummyDateView.setBackgroundColor(Color.MAGENTA);
		hourLayout.addView(dummyDateView);
		
		hourScrollView = new HourScrollView(context);
		hourLayout.addView(hourScrollView);
		
		int startHour = DEFAULT_STARTHOUR, endHour = DEFAULT_ENDHOUR;
		hourView = new HourView(context, startHour, endHour);
		hourScrollView.addView(hourView);
		
		//dayScrollView = new DayScrollView(context);
		//this.addView(dayScrollView);
		
		Day firstDay = this.getNextNonEmptyDay(startDay);
		dayView = new DayView(context, timetable, firstDay, hourView, hourScrollView);
		this.addView(dayView);
		
		/* LayoutParams dayParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
		
		LinearLayout dayLayout = new LinearLayout(context);
		dayLayout.setOrientation(Orientation.HORIZONTAL);
		dayLayout.setBackgroundColor(Color.BLUE);
		this.addView(dayLayout);
		
		DayView dayView = new DayView(context, timetable, startDay, 9, 17, null);
		dayView.setHourHeight(60);
		dayLayout.addView(dayView, dayParams);
		
		dayView = new DayView(context, timetable, startDay.add(1), 9, 17, null);
		dayView.setHourHeight(60);
		dayLayout.addView(dayView, dayParams);
		
		dayView = new DayView(context, timetable, startDay.add(2), 9, 17, null);
		dayView.setHourHeight(60);
		dayLayout.addView(dayView, dayParams); */
	}
	
	private Day getNextNonEmptyDay(Day fromDay)
	{
		List<Event> dayEvents = new ArrayList<Event>();
		List<Event> allEvents = this.timetable.getEvents();
		if (!allEvents.isEmpty())
		{
			Event lastEvent = Collections.max(allEvents);
			//Event lastEvent = timetable.getEvents().get(timetable.getEvents().size() - 1);
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
	
	/* protected void createDayViews(int width)
	{
		int daysGenerated = this.daysOnScreen * 3;
		int widthPerDay = width / this.daysOnScreen;
		
		dayLayout = new LinearLayout(this.getContext());
		dayLayout.setOrientation(Orientation.HORIZONTAL);
		dayScrollView.addView(dayLayout, new LayoutParams(widthPerDay * daysGenerated, LayoutParams.WRAP_CONTENT));
		
		Day nextDay = startDay;
		for (int i = 0; i < this.daysOnScreen * 3; i++)
		{
			nextDay = this.getFirstNonEmptyDay(nextDay);
			DayView dayView = new DayView(this.getContext(), timetable, nextDay, hourView, hourScrollView);
			dayView.setHourHeight(hourView.getHourHeight());
			dayLayout.addView(dayView, dayParams);
			nextDay = nextDay.add(1);
		}
	} */
	
	/*
	public TimetableView(Context context, Timetable timetable, Day day)
	{
		super(context);

		this.setOrientation(Orientation.HORIZONTAL);
		this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		List<Event> events = new ArrayList<Event>();
		if (!timetable.getEvents().isEmpty())
		{
			Day orgDay = day;
			//Event lastEvent = Collections.max(timetable.getEvents());
			Event lastEvent = timetable.getEvents().get(timetable.getEvents().size() - 1);
			do
			{
				events = timetable.startDuring(day);
				if (events.isEmpty())
					day = day.add(1);
				if (day.startTime.isAfter(lastEvent.startTime))
				{
					day = orgDay;
					break;
				}
			} while (events.isEmpty());
		}
		
		hourLayout = new LinearLayout(context);
		hourLayout.setOrientation(Orientation.VERTICAL);
		hourLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		this.addView(hourLayout);
		
		/*
		 * Deze is er alleen om de uren op de goede plaats te krijgen
		 */
		/* dummyDateView = new DateTitleView(context, null);
		dummyDateView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		hourLayout.addView(dummyDateView);
		
		hourScrollView = new HourScrollView(context);
		hourScrollView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		hourLayout.addView(hourScrollView);
		
		int startHour = DEFAULT_STARTHOUR, endHour = DEFAULT_ENDHOUR;
		if (!events.isEmpty())
		{
			startHour = (int)Math.min(9, day.startTime.timeTo(events.get(0).startTime).inHours());
			endHour = (int)Math.ceil(Math.max(17, day.startTime.timeTo(events.get(events.size() - 1).startTime).inHours()));
		}
		hourView = new HourView(context, startHour, endHour);
		hourView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		hourScrollView.addView(hourView);		
		
		divider = new DividerView(context, Utils.dipToPx(2), Utils.dipToPx(8), Color.BLACK & 0xAAFFFFFF);
		this.addView(divider);
		
		dayScrollView = new DayScrollView(context);
		dayScrollView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		this.addView(dayScrollView);
		
		dayView = new DayView(context, timetable, day, hourView);
		dayView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		dayScrollView.addView(dayView);
	}
	*/
	
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
		
	private class DayScrollView extends HorizontalScrollView
	{
		public DayScrollView(Context context)
		{
			super(context);
		}	
		
		private boolean measured = false;
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
		{
			int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
			int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
			
			if (!measured)
			{
				daysOnScreen = parentWidth / 300;
				
				//createDayViews(parentWidth / daysOnScreen);
				
				measured = true;
			}
			
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}
}
