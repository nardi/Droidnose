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
			
			/*
			 * Verzamel alle dagen waarop wat gebeurt
			 */
			Day lastDay = this.startDay;
			if (!timetable.getEvents().isEmpty())
			{
				List<Event> events = this.timetable.getEvents();
				
				for (Event e : events)
				{
					Day eventDay = e.startTime.getDay();
					if (!this.dayList.contains(eventDay))
						this.dayList.add(eventDay);
					lastDay = eventDay;
				}
			}
			
			/*
			 * Als er nog niet genoeg dagen zijn om het scherm te vullen,
			 * neem nog een paar extra lege
			 */
			while (this.dayList.size() < daysOnScreen)
			{
				lastDay = lastDay.add(1);
				this.dayList.add(lastDay);
			}
			
			Context context = this.getContext();
			LayoutParams dayParams = new LayoutParams(this.dayWidth, LayoutParams.WRAP_CONTENT);
			
			for (Day day : multiDayView.dayList)
			{
				DayView dayView = new DayView(context, timetable, day, hourView);
				dayView.setHourHeight(multiDayView.getHourHeight());
				multiDayView.dayViews.add(dayView);
				multiDayView.layout.addView(dayView, dayParams);
			}
			
			int startDayIndex = this.dayList.indexOf(this.startDay);
			if (startDayIndex == -1)
			{
				startDayIndex = this.dayList.indexOf(this.getNextNonEmptyDay(this.startDay, 1));
			}
			this.setScrollBars(startDayIndex + daysOnScreen - 1);
			
			final int startDayStep = startDayIndex;
			this.scrollView.post(new Runnable()
			{
				public void run()
				{
					multiDayView.scrollView.scrollToStep(startDayStep);
					multiDayView.scrollView.setVisibility(VISIBLE);
				}
			});
		}
	}
	
	private Day getNextNonEmptyDay(Day fromDay, int direction)
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
				currentDay = currentDay.add(direction);
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
			int rightDayViewIndex = step + multiDayView.daysOnScreen - 1;
			multiDayView.setScrollBars(rightDayViewIndex);
			
			multiDayView.setStartDay(multiDayView.dayList.get(step));
		}
	}
}
