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
import nl.nardilam.droidnose.Utils;
import nl.nardilam.droidnose.datetime.Day;
import android.content.Context;
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
	
	private final ContextCallback<Timetable.DayEvents, DayView> onUpdate = new ContextCallback<Timetable.DayEvents, DayView>()
	{
		public void onResult(Timetable.DayEvents result, DayView context)
		{
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
		
		DateTitleView dtView = new DateTitleView(context, day);
		this.addView(dtView);
		
		if (this.errorUpdating != null)
		{
			TextView errorText = new TextView(context);
			errorText.setText("Er is een fout opgetreden:\n\n"
					  		+ Utils.niceException(this.errorUpdating) + "\n\n"
					  		+ "Raak deze tekst aan om het opnieuw te proberen.");
			errorText.setGravity(Gravity.CENTER);
			//errorText.setEllipsize(TruncateAt.MIDDLE);
			errorText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			final int padding = Utils.dpToPx(40);
			errorText.setPadding(padding, 0, padding, 0);
			errorText.setClickable(true);
			errorText.setOnClickListener(new View.OnClickListener()
			{
				@Override
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
			List<Event> dayEvents = this.timetable.startDuring(day, onUpdate);
			
			if (timetable.getUpdatingDays().contains(day))
			{
				this.addView(new LoadingView(context));
			}
			else
			{
				if (dayScrollView != null)
					dayScrollView.unlink();
				dayScrollView = new LinkedScrollView(context);
				dayScrollView.linkTo(this.hourView.getScrollView());
				this.addView(dayScrollView);
				
				RelativeLayout layout = new RelativeLayout(context);
				layout.setPadding(0, hourHeight / 2, 0, 0);
				int numHours = this.getEndHour() - this.getStartHour() + 1;
				layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, numHours * hourHeight));
				dayScrollView.addView(layout);
				
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
						
						double eventStart = this.day.startTime.timeTo(event.startTime).inHours();
						double emptyGroupTime = eventStart - groupStart;
						
						LayoutParams params = (LayoutParams)ev.getLayoutParams();
						params.topMargin = (int)(emptyGroupTime * hourHeight);
						ev.setLayoutParams(params);
						
						horizontalEvents.addView(ev);
					}
					
					layout.addView(horizontalEvents, groupParams);
				}
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
