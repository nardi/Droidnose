package nl.nardilam.droidnose.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import nl.nardilam.droidnose.ContextCallback;
import nl.nardilam.droidnose.Event;
import nl.nardilam.droidnose.Orientation;
import nl.nardilam.droidnose.Timetable;
import nl.nardilam.droidnose.Utils;
import nl.nardilam.droidnose.datetime.Day;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DayView extends TimeLayout
{
	private final Timetable timetable;
	private final Day day;
	private final HourView hourView;
	
	private DateTitleView dtView = null;
	private LinkedScrollView dayScrollView = null;
	private Exception errorUpdating = null;
	
	public void setVerticalScrollBarEnabled(boolean enabled)
	{
		if (this.dayScrollView != null)
			this.dayScrollView.setVerticalScrollBarEnabled(enabled);
	}
	
	public DayView(Context context, Timetable timetable, Day day, HourView hourView)
	{
		super(context, hourView.getStartHour(), hourView.getEndHour());
		
		this.timetable = timetable;
		this.day = day;
		this.hourView = hourView;
		
		this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		this.onUpdate.setContext(this);
	}
	
	private final ContextCallback<Timetable.EventCollection, DayView> onUpdate = new ContextCallback<Timetable.EventCollection, DayView>()
	{
		public void onResult(Timetable.EventCollection result, DayView context)
		{
			Log.v("DayView", "Update finished for " + day.toString());
			context.errorUpdating = null;
			context.update();
		}

		public void onError(Exception e, DayView context)
		{
			context.errorUpdating = e;
			context.update();
		}
	};
	
	protected void update()
	{
		this.removeAllViews();
		
		Context context = this.getContext();
		int hourHeight = this.getHourHeight();
		
		dtView = new DateTitleView(context, day);
		this.addView(dtView);
		
		List<Event> dayEvents = this.timetable.startDuring(day, onUpdate);
		if (this.errorUpdating != null && dayEvents.isEmpty())
		{
			TextView errorText = new TextView(context);
			errorText.setText("Er is een fout opgetreden:\n\n"
					  		+ Utils.niceException(this.errorUpdating) + "\n\n"
					  		+ "Raak deze tekst aan om het opnieuw te proberen.");
			errorText.setGravity(Gravity.CENTER);
			errorText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			final int padding = Utils.dpToPx(40);
			errorText.setPadding(padding, 0, padding, 0);
			errorText.setClickable(true);
			errorText.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View v)
				{
					errorUpdating = null;
					timetable.updateIfNeeded(onUpdate, day);
					update();
				}
			});
			this.addView(errorText);
		}
		else
		{			
			if (timetable.getUpdatingDays().contains(day))
			{
				this.addView(new LoadingView(context));
			}
			else
			{
				if (this.errorUpdating != null)
					this.dtView.setError(true);
				
				if (dayScrollView != null)
					dayScrollView.unlink();
				dayScrollView = new LinkedScrollView(context);
				dayScrollView.linkTo(this.hourView.getScrollView());
				this.addView(dayScrollView);
				
				RelativeLayout layout = this.makeLayout(dayEvents, this.getStartHour());
				layout.setPadding(0, hourHeight / 2, 0, 0);
				int numHours = this.getEndHour() - this.getStartHour() + 1;
				LayoutParams params = (LayoutParams)layout.getLayoutParams();
				params.width = LayoutParams.MATCH_PARENT;
				params.height = numHours * hourHeight;
				layout.setLayoutParams(params);
				dayScrollView.addView(layout);
			}
		}
	}
	
	private RelativeLayout makeLayout(Collection<Event> events, double startHour)
	{
		Context context = this.getContext();
		int hourHeight = this.getHourHeight();
		
		RelativeLayout layout = new RelativeLayout(context);
		layout.setLayoutParams(new LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.5f));
		
		List<SortedSet<Event>> horizontalEventGroups = this.groupHorizontalEvents(events);
		SortedSet<Event> lastHEG = null;
		
		for (SortedSet<Event> horizontalEventGroup : horizontalEventGroups)
		{
			Event firstEvent = Collections.min(horizontalEventGroup);
			double groupStart = this.day.startTime.timeTo(firstEvent.startTime).inHours();
			
			LinearLayout horizontalEvents = new LinearLayout(context);
		    horizontalEvents.setOrientation(Orientation.HORIZONTAL);
			
			RelativeLayout.LayoutParams groupParams =
					new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			
			groupParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			
			double emptyTime = groupStart - startHour;
			groupParams.topMargin = (int)(emptyTime * hourHeight);
			
			List<SortedSet<Event>> verticalEventGroups = this.groupVerticalEvents(horizontalEventGroup);
			
			for (SortedSet<Event> verticalEventGroup : verticalEventGroups)
			{
				Event event = verticalEventGroup.first();
				double eventStart = this.day.startTime.timeTo(event.startTime).inHours();
				double emptyGroupTime = eventStart - groupStart;
				if (verticalEventGroup.size() == 1)
				{
					EventView ev = new EventView(context, event);
					ev.setHourHeight(hourHeight);
					if (lastHEG != null)
					{
						boolean topBorder = true;
						for (Event e : lastHEG)
							topBorder = !event.startTime.equals(e.endTime);
						ev.setBorder(true, topBorder, true, true);
					}
					
					LayoutParams params = (LayoutParams)ev.getLayoutParams();
					params.topMargin = (int)(emptyGroupTime * hourHeight);
					ev.setLayoutParams(params);
					
					horizontalEvents.addView(ev);
				}
				else
				{
					RelativeLayout groupLayout = this.makeLayout(verticalEventGroup, eventStart);
					groupLayout.setPadding(0, (int)emptyGroupTime, 0, 0);
					horizontalEvents.addView(groupLayout);
				}
			}
			
			layout.addView(horizontalEvents, groupParams);
			
			lastHEG = horizontalEventGroup;
		}
		
		return layout;
	}
	
	/*
	 * Een EventGroup is een aantal Events die tegelijk plaatsvinden.
	 * Deze moeten daarom naast elkaar verschijnen en dus in een
	 * horizontale LinearLayout geplaatst worden, vandaar deze groepering. 
	 */
	private List<SortedSet<Event>> groupHorizontalEvents(Collection<Event> events)
	{
		List<Event> eventsToCheck = new ArrayList<Event>(events);
		List<SortedSet<Event>> eventGroupsList = new ArrayList<SortedSet<Event>>();
		
		while (!eventsToCheck.isEmpty())
		{
			Event firstEvent = eventsToCheck.get(0);
			SortedSet<Event> eventGroup = new TreeSet<Event>();
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
	
	private List<SortedSet<Event>> groupVerticalEvents(Collection<Event> events)
	{
		List<Event> eventsToCheck = new ArrayList<Event>(events);
		List<SortedSet<Event>> eventGroupsList = new ArrayList<SortedSet<Event>>();
		
		while (!eventsToCheck.isEmpty())
		{
			Event firstEvent = eventsToCheck.get(0);
			SortedSet<Event> eventGroup = new TreeSet<Event>();
			eventGroup.add(firstEvent);
			
			Event currentEvent = firstEvent;
			for (Event candidate : eventsToCheck)
			{
				if (!candidate.startTime.isBefore(currentEvent.endTime))
				{
					eventGroup.add(candidate);
					currentEvent = candidate;
				}
			}
			
			eventsToCheck.removeAll(eventGroup);
			eventGroupsList.add(eventGroup);
		}
		
		return eventGroupsList;
	}
}
