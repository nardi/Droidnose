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
}
