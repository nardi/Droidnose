package nl.nardilam.droidnose.datetime;

import java.util.TimeZone;

public class TimeUtils
{
	/*
	 * Net TimeZone.getTimeZone(), maar dan met exceptions
	 */
	public static final TimeZone GMT = TimeZone.getTimeZone("GMT");
	public static final TimeZone CET = TimeZone.getTimeZone("CET");
	public static TimeZone getTimeZone(String tz)
	{
		TimeZone timeZone = TimeZone.getTimeZone(tz);
		if (timeZone.equals(GMT) && !(tz == "GMT" || tz == "UTC" || tz == "UT"))
			throw new IllegalArgumentException(tz + " is not a valid timezone identfier");
		return timeZone;
	}
}
