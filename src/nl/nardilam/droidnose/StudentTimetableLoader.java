package nl.nardilam.droidnose;

import nl.nardilam.droidnose.datetime.Time;
import android.os.AsyncTask;
import android.widget.Toast;

public class StudentTimetableLoader extends AsyncTask<Void, Void, StudentTimetable>
{
	private TimetableActivity activity;
	private final int studentId;
	private final boolean forceRedownload;
	
	private boolean downloadedTimetable = false;
	private boolean saveError = false;
	private Exception fatalException = null;
	
	public StudentTimetableLoader(TimetableActivity activity, int studentId)
	{
		this(activity, studentId, false);
	}
	
	public StudentTimetableLoader(TimetableActivity activity, int studentId, boolean forceRedownload)
	{
		this.setActivity(activity);
		this.studentId = studentId;
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
	
    protected StudentTimetable doInBackground(Void... nothings)
    {
    	/*
    	 * Download het rooster opnieuw als het moet
    	 */
        if (this.forceRedownload)
        	return tryDownloadStudentTimetable();
        
        /*
		 * Probeer anders het rooster uit een lokaal bestand te laden
		 */
        try
		{
			StudentTimetable timetable = StudentTimetable.loadFromFile(Integer.toString(this.studentId));
			/*
			 * Gebruik het lokale bestand als het binnen de laatste 24 uur geupdate is,
			 * download het rooster anders opnieuw
			 */
			if (timetable.lastFullUpdate.timeTo(Time.now()).inHours() < 24)
				return timetable;
			else
				return this.tryDownloadStudentTimetable();
				
		}
		/*
		 * Als dat niet lukt moet het rooster
		 * (opnieuw) gedownload worden
		 */	
		catch (Exception loadException)
		{
			return this.tryDownloadStudentTimetable();
		}
    }
    
    private StudentTimetable tryDownloadStudentTimetable()
    {
    	try
		{
    		Student student = Student.download(this.studentId);
    		StudentTimetable timetable = StudentTimetable.download(student);
    		try
    		{
    			timetable.saveToFile(Integer.toString(this.studentId));
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
			this.fatalException = downloadException;
			return null;
		}
    }

    protected void onPostExecute(StudentTimetable timetable)
    {
    	if (timetable != null)
    	{
    		this.getActivity().showTimetableView(timetable);
    		
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
				this.getActivity().getNewStudentId(
						"Er is een fout opgetreden bij het ophalen "
					  + "van het rooster voor dit studentnummer:\n\n"
					  + fatalException + "\n\n"
					  + "Controleer aub of het ingevoerde nummer klopt.",
					  Integer.toString(this.studentId));
    		}
    	}
    }
}
