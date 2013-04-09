package nl.nardilam.droidnose.datetime;

import java.util.Date;
import java.util.TimeZone;
import java.util.Calendar;
import java.text.DateFormat;
import java.text.ParseException;
import java.io.Serializable;

public class Time implements Serializable, Comparable<Time>
{
	private static final long serialVersionUID = 1L;

	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
    
    protected final Date internalDate;
    public final TimeZone timeZone;

    /*
     * Construction methods
     */
    
    public Time(Date date, TimeZone tz)
    {
        this.internalDate = date;
        this.timeZone = tz;
    }
    
    public Time(Date date, String tz)
    {
    	this(date, TimeZone.getTimeZone(tz));
    }
    
    public Time(Date date)
    {
        this(date, UTC);
    }
    
    public static Time parse(String dateTime, DateFormat format) throws ParseException
    {
        return new Time(format.parse(dateTime), format.getTimeZone());
    }
    
    public static Time now()
    {
        return new Time(new Date(), TimeZone.getDefault());
    }
    
    /*
     * Conversion methods
     */
    
    public Calendar toCalendar()
    {
    	Calendar calendar = Calendar.getInstance(timeZone);
    	calendar.setTime(internalDate);
    	return calendar;
    }
    
    public Time inTimeZone(TimeZone tz)
    {
        return new Time(this.internalDate, tz);
    }
    
    public Time inTimeZone(String tz)
    {
        return this.inTimeZone(TimeZone.getTimeZone(tz));
    }
    
    public Day getDay()
    {
    	return Day.fromCalendar(this.toCalendar());
    }
    
    public Week getWeek()
	{
		return Week.fromCalendar(this.toCalendar());
	}
    
    public Time add(Duration d)
	{
		return new Time(new Date(this.internalDate.getTime() + d.inMilliSeconds()), this.timeZone);
	}
	
	public Time subtract(Duration d)
	{
		return new Time(new Date(this.internalDate.getTime() - d.inMilliSeconds()), this.timeZone);
	}
    
    /*
     * Comparison methods
     */
    
	public boolean equals(Object o)
    {
    	try
		{
			return this.equals((Time)o);
		}
    	catch (ClassCastException e)
		{
			return false;
		}
    }
	
    public boolean equals(Time t)
    {
    	return this.internalDate.equals(t.internalDate);
    }
    
    public int hashCode()
    {
    	int result = (int)serialVersionUID;
    	result = 31 * result + this.internalDate.hashCode();
    	return result;
    }
    
    public boolean isBefore(Time t)
    {
    	return this.internalDate.before(t.internalDate);
    }
    
    public boolean isAfter(Time t)
    {
    	return this.internalDate.after(t.internalDate);
    }
    
    public boolean isDuring(TimePeriod tp)
    {
    	return (this.isAfter(tp.startTime) && this.isBefore(tp.endTime))
    		|| (this.equals(tp.startTime));
    }
    
    public Duration timeTo(Time t)
    {
    	return new Duration(t.internalDate.getTime() - this.internalDate.getTime());
    }
	
	public int compareTo(Time t)
	{
		return this.internalDate.compareTo(t.internalDate);
	}
    
    /*
     * Display methods
     */
    
    public String format(DateFormat fmt)
    {
    	DateFormat format = (DateFormat)fmt.clone();
    	format.setTimeZone(this.timeZone);
        return format.format(this.internalDate);
    }
    
    public String toString()
    {
        DateFormat df = DateFormat.getInstance();
        df.setTimeZone(timeZone);
        return df.format(internalDate);
    }
}
