package nl.nardilam.droidnose.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import nl.nardilam.droidnose.Event;
import nl.nardilam.droidnose.Orientation;
import nl.nardilam.droidnose.Timetable;
import nl.nardilam.droidnose.datetime.Day;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.LinearLayout.LayoutParams;

public class DayView extends TimeLayout
{
	private final Timetable timetable;
	private final Day day;
	
	private final LinkedScrollView lsv;
	
	public DayView(Context context, Timetable timetable, Day day, HourView hourView, LinkedScrollView lsv)
	{
		this(context, timetable, day, hourView.startHour, hourView.endHour, lsv);
	}
	
	public DayView(Context context, Timetable timetable, Day day, int startHour, int endHour, LinkedScrollView lsv)
	{
		super(context, startHour, endHour);
		
		this.timetable = timetable;
		this.day = day;
		
		this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
		this.lsv = lsv;
	}
	
	protected void update()
	{
		this.removeAllViews();
		
		Context context = this.getContext();
		int hourHeight = this.getHourHeight();
		
		DateTitleView dtView = new DateTitleView(context, day);
		//dtView.setBackgroundColor(Color.CYAN);
		this.addView(dtView);
		
		LinkedScrollView dayScrollView = new LinkedScrollView(context);
		dayScrollView.linkTo(this.lsv);
		this.addView(dayScrollView);
		
		RelativeLayout layout = new RelativeLayout(context);
		layout.setPadding(0, hourHeight / 2, 0, 0);
		int numHours = this.endHour - this.startHour + 1;
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, numHours * hourHeight));
		//layout.setBackgroundColor(Color.GREEN);
		dayScrollView.addView(layout); 
		
		List<Set<Event>> eventGroups = this.groupEvents(this.timetable.startDuring(day));
		
		for (Set<Event> eventGroup : eventGroups)
		{
			Event firstEvent = Collections.min(eventGroup);
			double groupStart = this.day.startTime.timeTo(firstEvent.startTime).inHours();
			
			LinearLayout horizontalEvents = new LinearLayout(context);
		    horizontalEvents.setOrientation(Orientation.HORIZONTAL);
			
			RelativeLayout.LayoutParams groupParams =
					new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			
			groupParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			
			double emptyTime = groupStart - this.startHour;
			groupParams.topMargin = (int)(emptyTime * hourHeight);
			
			for (Event event : eventGroup)
			{
				EventView ev = new EventView(context, event);
				ev.setHourHeight(hourHeight);
				horizontalEvents.addView(ev);
			}
			
			layout.addView(horizontalEvents, groupParams);
		}
	}
	
	/*
	 * Een EventGroup is een aantal Events die tegelijk plaatsvinden.
	 * Deze moeten daarom naast elkaar verschijnen en dus in een
	 * horizontale LinearLayout geplaatst worden, vandaar deze groepering. 
	 */
	private List<Set<Event>> groupEvents(Collection<Event> events)
	{
		List<Event> eventsToCheck = new ArrayList<Event>(events);
		List<Set<Event>> eventGroupsList = new ArrayList<Set<Event>>();
		
		while (!eventsToCheck.isEmpty())
		{
			Event firstEvent = eventsToCheck.get(0);
			Set<Event> eventGroup = new HashSet<Event>();
			eventGroup.add(firstEvent);
			
			for (Event candidate : eventsToCheck)
			{
				for (Event member : eventGroup)
				{
					if (candidate.startsDuring(member))
					{
						eventGroup.add(candidate);
					}
						
				}
			}
			
			eventsToCheck.removeAll(eventGroup);
			eventGroupsList.add(eventGroup);
		}
		
		return eventGroupsList;
	}
	
	/*
	protected void update()
	{
		this.removeAllViews();
		
		Context context = this.getContext();
		
		DateTitleView dtView = new DateTitleView(context, day);
		dtView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		this.addView(dtView);
		
		View emptyPadding = new View(context);
		emptyPadding.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, this.getHourHeight() / 2));
		this.addView(emptyPadding);
		
		double currentHour = this.startHour;
		ArrayList<Event> tempEventList = new ArrayList<Event>(events);
		for (Event event : tempEventList)
		{
			LinearLayout horizontal = new LinearLayout(context);
		    horizontal.setOrientation(Orientation.HORIZONTAL);
		    horizontal.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		    this.addView(horizontal);
			
			currentHour += this.addEvent(event, currentHour, horizontal, tempEventList);
		}
	}
	
	private double addEvent(Event event, double currentHour, LinearLayout addTo, List<Event> otherEvents)
	{
	    double startHour = event.startTime.getDay().startTime.timeTo(event.startTime).inHours();
		
		Context context = this.getContext();
		int hourHeight = this.getHourHeight();
		
		LinearLayout vertical = new LinearLayout(context);
		vertical.setOrientation(Orientation.VERTICAL);
		vertical.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		addTo.addView(vertical);
		
		double emptyTime = startHour - currentHour;
		if (emptyTime != 0)
		{
			View empty = new View(context);
			LinearLayout.LayoutParams params = 
				new LayoutParams(this.getWidth(), (int)(hourHeight * emptyTime));
			empty.setLayoutParams(params);
			empty.setBackgroundColor(Color.CYAN);
			vertical.addView(empty);
		}
		
		double eventTime = event.startTime.timeTo(event.endTime).inHours();
		EventView v = new EventView(context, event);
		v.setHourHeight(hourHeight);
		vertical.addView(v);   
		
		double viewLength = emptyTime + eventTime;
		otherEvents.remove(event);
		for (Event e2 : otherEvents)
        {
            if (e2.startsDuring(event))
            {
            	viewLength = Math.max(viewLength, this.addEvent(e2, currentHour, addTo, otherEvents));
            }
        }
		return viewLength;
	}
	*/
}
