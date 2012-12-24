package nl.nardilam.droidnose.gui;

import java.util.ArrayList;
import java.util.List;
import nl.nardilam.droidnose.Event;
import nl.nardilam.droidnose.Orientation;
import nl.nardilam.droidnose.Timetable;
import nl.nardilam.droidnose.TimetableActivity;
import nl.nardilam.droidnose.Utils;
import nl.nardilam.droidnose.datetime.Day;
import android.content.Context;
import android.widget.LinearLayout;

public class MultiDayView extends TimeLayout
{
	private static final int MIN_WIDTH_PER_DAY = Utils.dpToPx(300);
	
	private final MultiDayView multiDayView = this;
	
	private final Timetable timetable;
	private final HourView hourView;
	
	private DayScrollView scrollView = null;
	private LinearLayout layout = null;
	private List<DayView> dayViews = null;
	
	private int containerWidth;
	private int daysOnScreen;
	private int dayWidth;
	
	private Day startDay;
	private List<Day> dayList;
	
	public MultiDayView(Context context, Timetable timetable, Day startDay, HourView hourView)
	{
		super(context);
		
		this.timetable = timetable;
		this.hourView = hourView;
		
		this.startDay = startDay;
		
		this.scrollView = new DayScrollView(context);
		this.addView(this.scrollView);
		
		this.layout = new LinearLayout(context);
		this.layout.setOrientation(Orientation.HORIZONTAL);
		this.scrollView.addView(this.layout);
		
		this.dayList = new ArrayList<Day>();
		this.dayViews = new ArrayList<DayView>();
		
		/*
		 * Als er dingen voor negenen of na vijfen gebeuren,
		 * moet de HourView hiervan op de hoogte zijn om zich uit te breiden
		 */
		int startHour = TimeLayout.DEFAULT_STARTHOUR,
			endHour =  TimeLayout.DEFAULT_ENDHOUR;
		for (Event e : timetable.getEvents())
		{
			Day day = e.startTime.getDay();
			
			double startTime = day.startTime.timeTo(e.startTime).inHours();
			startHour = (int)Math.min(startHour, Math.round(startTime));
			
			double endTime = day.startTime.timeTo(e.endTime).inHours();
			endHour = (int)Math.max(endHour, Math.round(endTime));
		}
		hourView.setHourRange(startHour, endHour);
	}
	
	private void setStartDay(Day day)
	{
		this.startDay = day;
		if (this.getContext() instanceof TimetableActivity)
		{
			TimetableActivity activity = (TimetableActivity)this.getContext();
			activity.currentState.startDay = this.startDay;
		}
	}
	
	protected void update()
	{
		if (this.containerWidth != 0)
		{
			this.daysOnScreen = Math.max(1, this.containerWidth / MIN_WIDTH_PER_DAY);
			this.dayWidth = containerWidth / daysOnScreen;
			
			this.layout.removeAllViews();
			this.scrollView.setStepSize(dayWidth);
			this.scrollView.setVisibility(INVISIBLE);
			
			this.dayList.clear();
			this.dayViews.clear();
			
			this.fillDays();
			
			final int startDayIndex = daysOnScreen;
			this.setScrollBars(startDayIndex + daysOnScreen - 1);
			this.scrollView.post(new Runnable()
			{
				public void run()
				{
					multiDayView.scrollView.scrollToStep(startDayIndex);
					multiDayView.scrollView.setVisibility(VISIBLE);
				}
			});
		}
	}
	
	private int fillDays()
	{
		if (this.dayList.isEmpty())
		{
			this.startDay = this.getFirstNonEmptyDay(this.startDay, 1);
			this.addDay(this.startDay);
		}
		
		int startDayIndex = this.dayList.indexOf(this.startDay);
		int preDays = Math.max(daysOnScreen - startDayIndex, 0);
		int numDays = this.dayList.size();
		int postDays = Math.max((2 * daysOnScreen) - (numDays - startDayIndex), 0);
		
		for (Day day : this.getNonEmptyDays(dayList.get(0), -1, preDays))
			this.addDay(day, 0);
		
		for (Day day : this.getNonEmptyDays(dayList.get(dayList.size() - 1), 1, postDays))
			this.addDay(day);
		
		return preDays;
	}
	
	private List<Day> getNonEmptyDays(Day fromDay, int searchDirection, int amount)
	{
		List<Day> days = new ArrayList<Day>();
		Day currentDay = fromDay;
		for (int i = 0; i < amount; i++)
		{
			currentDay = currentDay.add(searchDirection);
			currentDay = this.getFirstNonEmptyDay(currentDay, searchDirection);
			days.add(currentDay);
		}
		return days;
	}
		
	private Day getFirstNonEmptyDay(Day fromDay, int searchDirection)
	{
		List<Event> allEvents = this.timetable.getEvents();
		if (!allEvents.isEmpty())
		{
			Event firstEvent = allEvents.get(0);
			Event lastEvent = allEvents.get(allEvents.size() - 1);
			
			/*
			 * Als de dag buiten het rooster ligt, return hem om
			 * geen oneindige loop te krijgen (en toch wat te laten zien)
			 */
			if (fromDay.startTime.isAfter(lastEvent.startTime)
			 || fromDay.endTime.isBefore(firstEvent.startTime))
			{
				return fromDay;
			}
			
			/*
			 * Blijf anders 1 bij de dag optellen totdat er iets plaatsvindt
			 */
			Day currentDay = fromDay;
			List<Event> dayEvents = timetable.startDuring(currentDay);
			while (dayEvents.isEmpty())
			{
				currentDay = currentDay.add(searchDirection);
				dayEvents = timetable.startDuring(currentDay);
			}
			return currentDay;
		}
		/*
		 * Als er een leeg rooster gegeven wordt, geven
		 * we de opgegeven dag maar terug
		 */
		else
		{
			return fromDay;
		}
	}
	
	private void addDay(Day day)
	{
		this.addDay(day, this.dayList.size());
	}
	
	private void addDay(Day day, int location)
	{
		this.dayList.add(location, day);
		DayView dayView = this.makeDayView(day);
		this.dayViews.add(location, dayView);
		this.layout.addView(dayView, location);		
	}
	
	private DayView makeDayView(Day day)
	{
		DayView dayView = new DayView(this.getContext(), timetable, day, hourView);
		dayView.setHourHeight(this.getHourHeight());
		LayoutParams dayParams = new LayoutParams(this.dayWidth, LayoutParams.WRAP_CONTENT);
		dayView.setLayoutParams(dayParams);
		return dayView;
	}
	
	public void setScrollBars(int rightDayViewIndex)
	{
		for (DayView d : this.dayViews)
			d.setVerticalScrollBarEnabled(false);
		
		if (this.dayViews.size() > rightDayViewIndex && rightDayViewIndex >= 0)
		{
			DayView rightDayView = this.dayViews.get(rightDayViewIndex);
			rightDayView.setVerticalScrollBarEnabled(true);
		}
	}
	
	private class DayScrollView extends StepScrollView
	{
		public DayScrollView(Context context)
		{
			super(context);
			
			this.setHorizontalScrollBarEnabled(false);
			this.setHorizontalFadingEdgeEnabled(false);
		}	
		
		private boolean measured = false;
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
		{
			int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
			
			if (!measured)
			{
				multiDayView.containerWidth = parentWidth;
				multiDayView.update();
				
				measured = true;
			}
			
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}

		protected void onStepChange(int step)
		{
			multiDayView.setStartDay(multiDayView.dayList.get(step));
			int dayOffset = multiDayView.fillDays();
			
			int rightDayViewIndex = dayOffset + step + multiDayView.daysOnScreen - 1;
			multiDayView.setScrollBars(rightDayViewIndex);
			
			this.scrollToStep(step + dayOffset);
		}
	}
}
