package nl.nardilam.droidnose.datetime;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class Week extends TimePeriod
{
	private static final long serialVersionUID = 1L;
	
	public final int year;
	public final int number;
	public final TimeZone timeZone;
	
	private final Calendar calendar;
	
	/*
	 * Om dingen wat te versimpelen maken we weken locale-onafhankelijk.
	 */
	private static void neutralizeCalendar(Calendar calendar)
	{
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		calendar.setMinimalDaysInFirstWeek(4);
	}
	
	/*
	 * Beetje vreemde constructie omdat super in de eerste expressie van de constructor aangeroepen moet worden.
	 * Kan nog verbeterd worden.
	 */
	
	private static Calendar makeCalendar(int year, int number, TimeZone tz)
	{
		Calendar calendar = Calendar.getInstance(tz);
		calendar.clear();
		neutralizeCalendar(calendar);
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.WEEK_OF_YEAR, number);
		return calendar;
	}
	
	private static Time calculateStartTime(int year, int number, TimeZone tz)
	{
		Calendar start = makeCalendar(year, number, tz);
		return new Time(start.getTime(), tz);
	}
	
	private static Time calculateEndTime(int year, int number, TimeZone tz)
	{
		Calendar end = makeCalendar(year, number, tz);
		end.add(Calendar.WEEK_OF_YEAR, 1);
		return new Time(end.getTime(), tz);
	}
	
	public Week(int year, int number, TimeZone tz)
	{
		super(calculateStartTime(year, number, tz),
			  calculateEndTime(year, number, tz));
		
		this.year = year;
		this.number = number;
		this.timeZone = tz;
		
		this.calendar = makeCalendar(year, number, tz);
	}
	
	public Week(int year, int number, String tz) throws IllegalArgumentException
	{
		this(year, number, TimeUtils.getTimeZone(tz));
	}
	
	public Calendar toCalendar()
	{
		return this.calendar;
	}
	
	public Week toTimeZone(TimeZone tz)
	{
		return new Week(year, number, tz);
	}
	
	public Week toTimeZone(String tz) throws IllegalArgumentException
	{
		return new Week(year, number, TimeUtils.getTimeZone(tz));
	}
	
	public Day getDay(WeekDay day)
	{
		Calendar dayCalendar = (Calendar)this.calendar.clone();
		dayCalendar.add(Calendar.DAY_OF_WEEK, day.ordinal());
		return Day.fromCalendar(dayCalendar);
	}
	
	public String toString()
	{
		return number + "-" + year;
	}
	
	public static Week fromCalendar(Calendar calendar)
	{
		Calendar c = (Calendar)calendar.clone();
		neutralizeCalendar(c);
		return new Week(c.get(Calendar.YEAR), c.get(Calendar.WEEK_OF_YEAR), c.getTimeZone());
	}
	
	public static Week parse(String week, DateFormat format) throws ParseException
	{
		Date date = format.parse(week);
		Calendar calendar = Calendar.getInstance(format.getTimeZone());
		neutralizeCalendar(calendar);
		calendar.setTime(date);
		return fromCalendar(calendar);
	}
	
	public static Week thisWeek()
	{
		return fromCalendar(Calendar.getInstance());
	}
}

