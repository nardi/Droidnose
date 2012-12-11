package nl.nardilam.droidnose.datetime;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Day extends TimePeriod
{
	private static final long serialVersionUID = 1L;
	
	public final int year;
	public final int month;
	public final int day;
	public final TimeZone timeZone;
	
	/*
	 * Beetje vreemde constructie omdat super in de eerste expressie van de constructor aangeroepen moet worden.
	 * Kan nog verbeterd worden.
	 */
	
	private Calendar calendar;
	
	private static Calendar makeCalendar(int year, int month, int day, TimeZone tz)
	{
		Calendar calendar = Calendar.getInstance(tz);
		calendar.clear();
		calendar.set(year, month - 1, day, 0, 0, 0);
		return calendar;
	}
	
	private static Time calculateStartTime(int year, int month, int day, TimeZone tz)
	{
		Calendar start = makeCalendar(year, month, day, tz);
		return new Time(start.getTime(), tz);
	}
	
	private static Time calculateEndTime(int year, int month, int day, TimeZone tz)
	{
		Calendar end = makeCalendar(year, month, day, tz);
		end.add(Calendar.DAY_OF_MONTH, 1);
		return new Time(end.getTime(), tz);
	}
	
	public Day(int year, int month, int day, TimeZone tz)
	{
		super(calculateStartTime(year, month, day, tz),
			  calculateEndTime(year, month, day, tz));
		
		this.year = year;
		this.month = month;
		this.day = day;
		this.timeZone = tz;
		
		this.calendar = makeCalendar(year, month, day, tz);
	}
	
	public Day(int year, int month, int day, String tz) throws IllegalArgumentException
	{
		this(year, month, day, TimeUtils.getTimeZone(tz));
	}
	
	public Calendar toCalendar()
	{
		return calendar;
	}
	
	public Day toTimeZone(TimeZone tz)
	{
		return new Day(year, month, day, tz);
	}
	
	public Day toTimeZone(String tz) throws IllegalArgumentException
	{
		return new Day(year, month, day, TimeUtils.getTimeZone(tz));
	}
	
	public Day add(int days)
    {
		Calendar c = (Calendar)this.calendar.clone();
		c.add(Calendar.DAY_OF_YEAR, days);
		return Day.fromCalendar(c);
    }
	
	public Week getWeek()
	{
		return Week.fromCalendar(this.toCalendar());
	}
	public String toString()
	{
		return day + "-" + month + "-" + year;
	}
	
	public static Day fromCalendar(Calendar c)
	{
		return new Day(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH), c.getTimeZone());
	}
	
	public static Day parse(String day, DateFormat format) throws ParseException
	{
		Date date = format.parse(day);
		Calendar calendar = Calendar.getInstance(format.getTimeZone());
		calendar.setTime(date);
		return fromCalendar(calendar);
	}
	
	public static Day today()
	{
		return fromCalendar(Calendar.getInstance());
	}
	
	public static Day today(TimeZone tz)
	{
		return fromCalendar(Calendar.getInstance(tz));
	}
}
