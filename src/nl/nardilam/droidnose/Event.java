package nl.nardilam.droidnose;

import java.util.*;
import java.text.*;

import nl.nardilam.droidnose.datetime.Time;
import nl.nardilam.droidnose.datetime.TimePeriod;

public class Event extends TimePeriod
{    
	private static final long serialVersionUID = 1L;
	
	public final Course course;
	public final EventType type;
	public final String location;
	public final Collection<String> staff;
    
    public Event(Time start, Time end, Course course, EventType type, String location, Collection<String> staff)
    {
        super(start, end);
        
        this.course = course;
        this.type = type;
        this.location = location;
        this.staff = Collections.unmodifiableCollection(staff);
    }
    
    private static DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private static DateFormat timeFormat = new SimpleDateFormat("HH:mm");
    public String toString()
    {
        return course + " (" + type + ") in " + location + " op " + startTime.format(dateFormat)
        	 + " van " + startTime.format(timeFormat) + " tot " + endTime.format(timeFormat);
    }
}
