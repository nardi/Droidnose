package nl.nardilam.droidnose.datetime;

public class Duration
{
	protected static final long msPerSecond = 1000;
	protected static final long msPerMinute = 60 * msPerSecond;
	protected static final long msPerHour = 60 * msPerMinute;
	protected static final long msPerDay = 24 * msPerHour;
	protected static final long msPerWeek = 7 * msPerDay;
	
	protected final long milliSeconds;
	
	protected Duration(long milliSeconds)
	{
		this.milliSeconds = milliSeconds;
	}
	
	public long inMilliSeconds()
	{
		return this.milliSeconds;
	}
	
	public double inSeconds()
	{
		return (double)this.milliSeconds / msPerSecond;
	}
	
	public double inMinutes()
	{
		return (double)this.milliSeconds / msPerMinute;
	}
	
	public double inHours()
	{
		return (double)this.milliSeconds / msPerHour;
	}
	
	public double inDays()
	{
		return (double)this.milliSeconds / msPerDay;
	}
	
	public double inWeeks()
	{
		return (double)this.milliSeconds / msPerWeek;
	}
	
	public Duration add(Duration d)
	{
		return new Duration(this.milliSeconds + d.milliSeconds);
	}
	
	public Duration subtract(Duration d)
	{
		return new Duration(this.milliSeconds - d.milliSeconds);
	}
	
	public String toString()
	{
		String string = "";
		long hours = milliSeconds / msPerHour;
		if (hours != 0)
			string += hours + ((hours == 1) ? " hour" : " hours");
		long restMs = milliSeconds % msPerHour;
		if (restMs != 0)
		{
			long minutes = restMs / msPerMinute;
			restMs = milliSeconds % msPerMinute;
			double seconds = (double)restMs / msPerSecond;
			if (seconds != 0)
			{
				if (minutes != 0)
					string += ", " + minutes + ((minutes == 1) ? " minute," : " minutes,");
				string += " and " + seconds + ((seconds == 1) ? " second" : " seconds");
			}
			else if (minutes != 0)
				string += " and " + minutes + ((minutes == 1) ? " minute" : " minutes");
		}
		return string;
	}
	
	public static Duration weeks(double weeks)
	{
		return new Duration((long)(weeks * msPerWeek));
	}
	
	public static Duration days(double days)
	{
		return new Duration((long)(days * msPerDay));
	}
	
	public static Duration hours(double hours)
	{
		return new Duration((long)(hours * msPerHour));
	}
	
	public static Duration minutes(double minutes)
	{
		return new Duration((long)(minutes * msPerMinute));
	}
	
	public static Duration seconds(double seconds)
	{
		return new Duration((long)(seconds * msPerSecond));
	}
}
