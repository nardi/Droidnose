package nl.nardilam.droidnose.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import nl.nardilam.droidnose.Event;
import nl.nardilam.droidnose.Orientation;
import nl.nardilam.droidnose.Timetable;
import nl.nardilam.droidnose.datetime.Day;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class MultiDayView extends HorizontalScrollView
{
	private static LayoutParams dayParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
	
	private final Timetable timetable;
	private final Day startDay;
	
	public MultiDayView(Context context, Timetable timetable, Day startDay)
	{
		super(context);
		
		this.timetable = timetable;
		this.startDay = startDay;
		
		this.setBackgroundColor(Color.RED);
		
		
		
		
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
	
	private Day getPrevNonEmptyDay(Day fromDay)
	{
		List<Event> dayEvents = new ArrayList<Event>();
		List<Event> allEvents = this.timetable.getEvents();
		if (!allEvents.isEmpty())
		{
			Event firstEvent = Collections.min(allEvents);
			if (fromDay.endTime.isBefore(firstEvent.startTime))
			{
				return fromDay;
			}
			
			Day currentDay = fromDay;
			do
			{
				dayEvents = this.timetable.startDuring(currentDay);
				if (dayEvents.isEmpty())
				{
					currentDay = currentDay.add(-1);
				}
			} while (dayEvents.isEmpty());
			return currentDay;
		}
		else
			return fromDay;
	}
}
