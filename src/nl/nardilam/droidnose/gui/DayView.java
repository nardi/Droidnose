package nl.nardilam.droidnose.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.nardilam.droidnose.ContextCallback;
import nl.nardilam.droidnose.Event;
import nl.nardilam.droidnose.Orientation;
import nl.nardilam.droidnose.Timetable;
import nl.nardilam.droidnose.datetime.Day;
import android.content.Context;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class DayView extends TimeLayout
{
	private final Timetable timetable;
	private final Day day;
	private final HourView hourView;
	
	private LinkedScrollView dayScrollView = null;
	
	public void setVerticalScrollBarEnabled(boolean enabled)
	{
		this.dayScrollView.setVerticalScrollBarEnabled(enabled);
	}
	
	public DayView(Context context, Timetable timetable, Day day, HourView hourView)
	{
		super(context, hourView.getStartHour(), hourView.getEndHour());
		
		this.timetable = timetable;
		this.day = day;
		this.hourView = hourView;
		
		this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
		this.onUpdate.setContext(this);
	}
	
	private final ContextCallback<List<Event>, TimeLayout> onUpdate = new ContextCallback<List<Event>, TimeLayout>()
	{
		public void onResult(List<Event> result, TimeLayout context)
		{
			context.update();
		}

		public void onError(Exception e)
		{
			//show message or something
		}
	};
	
	protected void update()
	{
		this.removeAllViews();
		
		Context context = this.getContext();
		int hourHeight = this.getHourHeight();
		
		DateTitleView dtView = new DateTitleView(context, day);
		this.addView(dtView);
		
		dayScrollView = new LinkedScrollView(context);
		dayScrollView.linkTo(this.hourView.getScrollView());
		this.addView(dayScrollView);
		
		RelativeLayout layout = new RelativeLayout(context);
		layout.setPadding(0, hourHeight / 2, 0, 0);
		int numHours = this.getEndHour() - this.getStartHour() + 1;
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, numHours * hourHeight));
		dayScrollView.addView(layout); 
		
		List<Event> dayEvents = this.timetable.startDuring(day, onUpdate);
		
		if (dayEvents.isEmpty() && timetable.getUpdatingDays().contains(day))
		{
			ProgressBar pBar = new ProgressBar(context);
	        pBar.setIndeterminate(true);
	        
	        RelativeLayout.LayoutParams params =
					new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			params.addRule(RelativeLayout.CENTER_IN_PARENT);
			
	        layout.addView(pBar, params);
		}
		else
		{			
			List<Set<Event>> eventGroups = this.groupEvents(dayEvents);
			
			for (Set<Event> eventGroup : eventGroups)
			{
				Event firstEvent = Collections.min(eventGroup);
				double groupStart = this.day.startTime.timeTo(firstEvent.startTime).inHours();
				
				LinearLayout horizontalEvents = new LinearLayout(context);
			    horizontalEvents.setOrientation(Orientation.HORIZONTAL);
				
				RelativeLayout.LayoutParams groupParams =
						new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
				
				groupParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
				
				double emptyTime = groupStart - this.getStartHour();
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
						break;
					}	
				}
			}
			
			eventsToCheck.removeAll(eventGroup);
			eventGroupsList.add(eventGroup);
		}
		
		return eventGroupsList;
	}
}
