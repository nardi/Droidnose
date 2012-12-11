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
	
	public static int dipToPx(float dips)
	{
		return Math.max(Math.round(dips * displayMetrics.density), 1);
	}
}
