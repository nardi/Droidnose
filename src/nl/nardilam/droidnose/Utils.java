package nl.nardilam.droidnose;

import android.content.Context;
import android.util.DisplayMetrics;

public class Utils
{
	private static Context context;
	private static DisplayMetrics displayMetrics;
	
	public static void setContext(Context context)
	{
		if (context != null)
		{
			Utils.context = context;
			Utils.displayMetrics = context.getResources().getDisplayMetrics();
		}
	}
	
	public static Context getContext() throws ContextNotSetException
	{
		if (context != null)
			return context;
		else
			throw new ContextNotSetException();
	}
	
	public static DisplayMetrics getDisplayMetrics()
	{
		return displayMetrics;
	}
	
	public static float getScreenDensity()
	{
		return displayMetrics.density;
	}
	
	public static int dpToPx(float dips)
	{
		return Math.max(Math.round(dips * displayMetrics.density), 1);
	}
	
	public static int pxToDp(float pixels)
	{
		return Math.max(Math.round(pixels / displayMetrics.density), 1);
	}
	
	public static boolean isInPortraitMode()
	{
		return displayMetrics.heightPixels > displayMetrics.widthPixels;
	}
	
	public static String niceException(Throwable t)
	{
		while (t.getCause() != null)
		{
			t = t.getCause();
		}
		StackTraceElement[] st = t.getStackTrace();
		for (int i = 0; i < st.length; i++)
		{
			if (st[i].getClassName().startsWith("nl.nardilam.droidnose"))
			{
				return t.getClass().getName() + " in " + st[i].toString();
			}
		}
		return t.getClass().getName() + " in " + st[0].toString();
	}
	
	public static String unescape(String s)
	{    
	    int length = s.length();
	    StringBuffer sb = new StringBuffer(length);
	    
	    int i = 0;
		char c;
	    while (i < length)
	    {
	        c = s.charAt(i);
	        if (i + 1 < length && s.substring(i, i + 2).equals("\\u"))
	        {
	        	i += 2;
                c = (char)Integer.parseInt(s.substring(i, i + 4), 16);
                i += 4;
	        }
	        else
	        {
	        	i++;
	        }
	        sb.append(c);
	    }
	    return sb.toString();
	}
}
