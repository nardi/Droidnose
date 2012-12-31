package nl.nardilam.droidnose;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.os.AsyncTask;

import nl.nardilam.droidnose.datetime.Day;
import nl.nardilam.droidnose.datetime.Duration;
import nl.nardilam.droidnose.datetime.Time;
import nl.nardilam.droidnose.datetime.TimeUtils;
import nl.nardilam.droidnose.datetime.Week;
import nl.nardilam.droidnose.datetime.WeekDay;

public abstract class Timetable implements Serializable
{    
	private static final long serialVersionUID = 1L;
	
	private final Timetable timetable = this;
	
	private List<Event> eventList;
	protected Map<Day, Time> updateLog;
	private transient final Map<Day, Callback<List<Event>>> updatesInProgress = new HashMap<Day, Callback<List<Event>>>();
	
    protected Timetable(List<Event> events)
    {    	
    	this.setEvents(new ArrayList<Event>(events));
        this.updateLog = new HashMap<Day, Time>();
    }
    
    public void updateIfNeeded(Callback<List<Event>> whenDone, Day... days)
    {
    	if (days.length != 0)
    	{
    		Time now = Time.now();
    		ArrayList<Day> daysToUpdate = new ArrayList<Day>();
	    	for (Day day : days)
	    	{
	    		Time lastUpdate = this.updateLog.get(day);
	        	if ((lastUpdate == null
	        	|| !lastUpdate.add(Duration.hours(24)).isAfter(now))
	        	&& !this.updatesInProgress.containsKey(day))
	        	{
	        		daysToUpdate.add(day);
	        	}
	    	}
	    	this.update(whenDone, daysToUpdate);
	    }
    }
    
    protected void update(final Callback<List<Event>> whenDone, final List<Day> daysToUpdate)
    {
    	if (!daysToUpdate.isEmpty())
    	{
       		final Time updateTime = Time.now();
       		for (Day day : daysToUpdate)
       		{
       			this.updatesInProgress.put(day, whenDone);
       		}
    		new EventsDownloader(daysToUpdate, new Callback<List<Event>>()
    		{
				public void onResult(List<Event> newEvents)
				{
					if (newEvents != null)
					{
						List<Event> events = new ArrayList<Event>(timetable.getEvents());
						Iterator<Event> eventIterator = events.listIterator();
						while (eventIterator.hasNext())
						{
							Event e = eventIterator.next();
				    		for (Day day : daysToUpdate)
				    		{
				    			if (e.startsDuring(day))
				    				eventIterator.remove();
				    		}
				    	}
				    	events.addAll(newEvents);
				    	timetable.setEvents(events);
				    	
				    	for (Day day : daysToUpdate)
				    	{
				    		timetable.updateLog.put(day, updateTime);
				    		timetable.updatesInProgress.remove(day);
				    	}
					}
			    	
			    	whenDone.onResult(newEvents);
				}

				public void onError(Exception e)
				{
					whenDone.onError(e);
				}
    		}).execute();
    	}
    }
    
    private class EventsDownloader extends AsyncTask<Void, Void, List<Event>>
    {
    	private final Callback<List<Event>> callback;
    	private final List<Day> daysToUpdate;
    	private Exception fatalException = null;
    	
    	public EventsDownloader(List<Day> daysToUpdate, Callback<List<Event>> callback)
    	{
    		this.daysToUpdate = daysToUpdate;
    		this.callback = callback;
    	}
    	
		protected List<Event> doInBackground(Void... nothings)
		{
			String dateFilter = timetable.makeDateFilter(daysToUpdate);
	    	try
			{
				return timetable.downloadEvents(dateFilter);
			}
	    	catch (Exception e)
			{
	    		this.fatalException = e;
				return null;
			}
		}
		
		protected void onPostExecute(List<Event> newEvents)
		{
			if (newEvents != null)
			{
				this.callback.onResult(newEvents);
			}
			else
			{
				this.callback.onError(this.fatalException);
			}
		}
    }
    
    private static final String weekFormat = "substring(WeekPattern, %1$s, 1) eq '1'";
    private static final String dayFormat = "Day eq %1$s";
    
    /*
     * Maakt een filter om alleen de Activities voor bepaalde dagen te downloaden.
     */
    protected String makeDateFilter(List<Day> daysToUpdate)
    {    	
    	Map<Week, List<Day>> daysInWeek = new HashMap<Week, List<Day>>();
    	for (Day day : daysToUpdate)
    	{
    	    Week week = day.getWeek();
    	
    		if (!daysInWeek.containsKey(week))
    		    daysInWeek.put(week, new ArrayList<Day>());
    		
    		daysInWeek.get(week).add(day);
    	}
    	
    	StringBuilder weeksFilterBuilder = new StringBuilder();
    	
    	Iterator<Week> weekIterator = daysInWeek.keySet().iterator();
    	while (weekIterator.hasNext())
    	{
    		Week week = weekIterator.next();
    		int academicYear;
    		if (week.number >= 36)
    			academicYear = week.year;
    		else
    			academicYear = week.year - 1;
    		Week firstWeek = new Week(academicYear, 36, TimeUtils.CET);
    		int academicWeek = (int)firstWeek.startTime.timeTo(week.startTime).inWeeks();
    		
    		String weekFilter = String.format(weekFormat, academicWeek);
    		
    		StringBuilder daysFilterBuilder = new StringBuilder();    		
    		List<Day> days = daysInWeek.get(week);
    		Iterator<Day> dayIterator = days.iterator();
        	while (dayIterator.hasNext())
        	{
        		Day day = dayIterator.next();
        		WeekDay weekDay = day.getWeekDay();
        		
        		int dayNumber = (int)Math.pow(2, weekDay.ordinal());
        		String dayFilter = String.format(dayFormat, dayNumber);
        		
        		daysFilterBuilder.append(dayFilter);
        		
        		if (dayIterator.hasNext())
        			daysFilterBuilder.append(" or ");
        	}
    		
    		weeksFilterBuilder.append("(");
    		weeksFilterBuilder.append(weekFilter);
    		weeksFilterBuilder.append(" and (");
    		weeksFilterBuilder.append(daysFilterBuilder);
    		weeksFilterBuilder.append("))");
    		
    		if (weekIterator.hasNext())
    			weeksFilterBuilder.append(" or ");
    	}
    	  	
    	return weeksFilterBuilder.toString();
    }
    
    protected abstract List<Event> downloadEvents(String dateFilter) throws Exception;
    
    protected void setEvents(List<Event> events)
    {
    	this.eventList = new ArrayList<Event>(events);
    	this.sort();
    	
    	if (this.eventList.size() > 1)
        {
	        for (int i = 0; i < this.eventList.size() - 2; i++)
	        {
	        	Event e1 = this.eventList.get(i);
	        	Event e2 = this.eventList.get(i + 1);
	        	if (this.eventsAlmostEqual(e1, e2))
	        	{
	        		Event mergedEvent = this.mergeEvents(e1, e2);
	        		this.eventList.remove(i + 1);
	        		this.eventList.remove(i);
	        		this.eventList.add(i, mergedEvent);
	        		i--;
	        	}
	        }
        }
    }
    
    private boolean eventsAlmostEqual(Event e1, Event e2)
    {
    	return e1.startTime.equals(e2.startTime)
    		&& e1.endTime.equals(e2.endTime)
    		&& e1.course.equals(e2.course)
    		&& e1.type.equals(e2.type);
    }
    
    private Event mergeEvents(Event e1, Event e2)
    {
    	Set<String> staffSet = new HashSet<String>();
		staffSet.addAll(e1.staff);
		staffSet.addAll(e2.staff);
		return new Event(e1.startTime,
				e1.endTime,
				e1.course,
				e1.type,
				e1.location + ", " + e2.location,
				staffSet);
    }
    
    public List<Event> getEvents()
    {
    	return Collections.unmodifiableList(this.eventList);
    }
    
    public Map<Day, Time> getUpdateLog()
    {
    	return Collections.unmodifiableMap(this.updateLog);
    }
    
    public Set<Day> getUpdatingDays()
    {
    	return Collections.unmodifiableSet(this.updatesInProgress.keySet());
    }
    
    public Collection<Callback<List<Event>>> getUpdateHandlers()
    {
    	return Collections.unmodifiableCollection(this.updatesInProgress.values());
    }
    
    protected void sort()
    {
    	Collections.sort(this.eventList);
    }
    
    public List<Event> startDuring(Day day, Callback<List<Event>> whenDone)
    {
    	this.updateIfNeeded(whenDone, day);    	
    	return this.startDuring(day);
    }
    
    private List<Event> startDuring(Day day)
    {    	
    	List<Event> events = new ArrayList<Event>();
		for (Event e : this.getEvents())
		{
			if (e.startsDuring(day))
				events.add(e);
		}
		return events;
    }
    
    public String toString()
    {
    	StringBuilder string = new StringBuilder();
    	for (Event e : this.getEvents())
    		string.append(e + "\n");
    	return string.toString();
    }
    
    /*
	 * WeekPattern is een little-endian bitpatroon (als string)
	 * dat de weken waarin iets plaatsvindt aangeeft.
	 * Deze methode zet dit om in een lijst van 'Week's.
	 */
    public static List<Week> calculateWeeksFromPattern(String weekPattern, int academicYear)
	{
    	/*
		 * Een Calendar om de weken mee te berekenen.
		 * Begint bij begin academisch jaar, dus week 0 in jaar 2012 is week 36 van 2012
		 */
		Calendar firstWeek = new Week(academicYear, 36, TimeUtils.CET).toCalendar();
		
		List<Week> weekList = new ArrayList<Week>();
		for (int c = 0; c < weekPattern.length(); c++)
		{
			if (weekPattern.charAt(c) == '1')
			{
				Calendar week = (Calendar)firstWeek.clone();
				week.add(Calendar.WEEK_OF_YEAR, c);
				weekList.add(Week.fromCalendar(week));
			}
		}
		return weekList;
	}
    
    /*
	 * 'Day' is een (integer) bitpatroon om dagen aan te geven
	 * Begint bij maandag = 1 stijgt met macht van 2,
	 * meerdere dagen tegelijk komt eigenlijk niet voor.
	 */
    public static List<WeekDay> calculateDaysFromPattern(int days, int academicYear)
	{
		List<WeekDay> dayList = new ArrayList<WeekDay>();
		for (WeekDay weekDay : WeekDay.values())
		{
			if (0 != ((int)Math.pow(2, weekDay.ordinal()) & days))
				dayList.add(weekDay);
		}
		return dayList;
	}
}
