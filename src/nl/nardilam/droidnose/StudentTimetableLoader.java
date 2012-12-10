package nl.nardilam.droidnose;

import java.io.IOException;

import nl.nardilam.droidnose.datetime.Day;
import nl.nardilam.droidnose.datetime.Time;
import nl.nardilam.droidnose.datetime.TimeUtils;
import nl.nardilam.droidnose.gui.TimetableView;
import nl.nardilam.droidnose.json.JSONException;
import android.os.AsyncTask;
import android.widget.TextView;
import android.widget.Toast;

public class StudentTimetableLoader extends AsyncTask<Integer, Void, StudentTimetable>
{
	private final StudentTimetableLoader timetableLoader = this;
	private TimetableActivity activity;
	private final boolean forceRedownload;
	private boolean downloadedTimetable = false;
	private boolean saveError = false;
	
	public StudentTimetableLoader(TimetableActivity activity)
	{
		this(activity, false);
	}
	
	public StudentTimetableLoader(TimetableActivity activity, boolean forceRedownload)
	{
		this.setActivity(activity);
		this.forceRedownload = forceRedownload;
	}
	
	public void setActivity(TimetableActivity activity)
	{
		this.activity = activity;
	}
	
	public TimetableActivity getActivity()
	{
		while (activity == null);
		return this.activity;
	}
	
	protected void onPreExecute()
	{
	}
	
    protected StudentTimetable doInBackground(Integer... ids)
    {
    	int studentId = ids[0];
        
    	/*
    	 * Download het rooster opnieuw als het moet
    	 */
        if (this.forceRedownload)
        	return tryDownloadStudentTimetable(studentId);
        
        /*
		 * Probeer anders het rooster uit een lokaal bestand te laden
		 */
        try
		{
			StudentTimetable timetable = StudentTimetable.loadFromFile(Integer.toString(studentId));
			/*
			 * Gebruik het lokale bestand als het binnen de laatste 24 uur geupdate is,
			 * download het rooster anders opnieuw
			 */
			if (timetable.lastFullUpdate.timeTo(Time.now()).inHours() < 24)
				return timetable;
			else
				return this.tryDownloadStudentTimetable(studentId);
				
		}
		/*
		 * Als dat niet lukt moet het rooster
		 * (opnieuw) gedownload worden
		 */	
		catch (Exception loadException)
		{
			return this.tryDownloadStudentTimetable(studentId);
		}
    }
    
    private StudentTimetable tryDownloadStudentTimetable(int studentId)
    {
    	try
		{
    		Student student = Student.download(studentId);
    		StudentTimetable timetable = StudentTimetable.download(student);
    		try
    		{
    			timetable.saveToFile(Integer.toString(studentId));
    		}
    		catch (Exception saveException)
    		{
    			/*
    			 * Kon iets niet opgeslagen worden?
    			 * Jammer, maar geen reden om de applicatie te stoppen.
    			 */
    			this.saveError = true;
    		}
    		this.downloadedTimetable = true;
    		return timetable;
		}
		catch (Exception downloadException)
		{
			/*
			 * Er ging iets onherstelbaar fout, return null
			 * om dat aan te geven aan de UI-thread
			 */
			return null;
		}
    }

    protected void onPostExecute(StudentTimetable timetable)
    {
    	if (timetable != null)
    	{
    		this.getActivity().showTimetableView(timetable, null);
    		
    		String toastText = "";
    		
    		if (this.downloadedTimetable)
    			toastText += "Rooster met succes gedownload";
    		if (this.saveError)
    			toastText += ", maar fout bij lokaal opslaan rooster";
    		
    		if (!toastText.equals(""))
    			Toast.makeText(this.getActivity(), toastText, Toast.LENGTH_SHORT).show();
    	}
    	else
    	{
    		if (forceRedownload)
    		{
    			Toast.makeText(this.getActivity(), "Fout bij downloaden rooster", Toast.LENGTH_SHORT).show();
    		}
    		else
    		{
	    		Callback<Integer> callback = new Callback<Integer>()
				{
					public void onResult(Integer result)
					{
						new StudentTimetableLoader(timetableLoader.getActivity()).execute(result);
					}
				};
				this.getActivity().getNewStudentId(
						"Er is een fout opgetreden bij het ophalen "
					  + "van het rooster voor dit studentnummer.\n"
					  + "Controleer aub of het ingevoerde nummer klopt.", callback);
    		}
    	}
    }
}
