package nl.nardilam.droidnose;

import android.os.AsyncTask;

/*
 * Dit ding lijkt een beetje overbodig, kan vervangen worden door een AsyncCall lijkt me
 */

public class TimetableSaver extends AsyncTask<Timetable, Void, Boolean>
{
	protected Boolean doInBackground(Timetable... arg0)
	{
		try
		{
			for (Timetable t : arg0)
				t.saveToFile();
		}
		catch (Exception e)
		{
			return false;
		}
		
		return true;
	}
	
	public static void save(Timetable... timetables)
	{
		new TimetableSaver().execute(timetables);
	}
}
