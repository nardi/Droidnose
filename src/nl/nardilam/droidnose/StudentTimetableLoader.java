package nl.nardilam.droidnose;

import java.io.FileInputStream;
import android.content.Context;
import android.os.AsyncTask;

public class StudentTimetableLoader extends AsyncTask<Void, Void, StudentTimetable>
{
	private TimetableActivity activity;
	private final int studentId;
	private final boolean resetTimetable;
	private Exception fatalException = null;
	
	public StudentTimetableLoader(TimetableActivity activity, int studentId)
	{
		this(activity, studentId, false);
	}
	
	public StudentTimetableLoader(TimetableActivity activity, int studentId, boolean resetTimetable)
	{
		this.setActivity(activity);
		this.studentId = studentId;
		this.resetTimetable = resetTimetable;
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
	
    protected StudentTimetable doInBackground(Void... nothings)
    {
    	String filename = Integer.toString(this.studentId);
    	
    	if (this.resetTimetable)
    	{
			try
			{
				Context context = Utils.getContext();
				context.deleteFile(filename);
			}
			catch (Exception e)
			{
				// Niet erg, geen context = geen bestand = verwijderd bestand
			}
    	}
    	
        /*
		 * Probeer het rooster uit een lokaal bestand te laden
		 */
        try
		{
			StudentTimetable timetable = StudentTimetable.loadFromFile(filename);
			return timetable;
		}
		/*
		 * Als dat niet lukt moet het rooster
		 * (opnieuw) gedownload worden
		 */	
		catch (Exception loadException)
		{
			return this.tryCreateNewStudentTimetable();
		}
    }

    private StudentTimetable tryCreateNewStudentTimetable()
    {
    	try
		{
    		return StudentTimetable.empty(this.studentId);
		}
		catch (Exception fatalException)
		{
			/*
			 * Er ging iets onherstelbaar fout, return null
			 * om dat aan te geven aan de UI-thread
			 */
			this.fatalException = fatalException;
			return null;
		}
    }
    
    protected void onPostExecute(StudentTimetable timetable)
    {
    	if (timetable != null)
    	{
    		this.getActivity().showTimetableView(timetable);
    	}
    	else
    	{
    		/*
    		 * Wordt geen rekening gehouden met ignoreFile
    		 */
			this.getActivity().getNewStudentId(
					"Er is een fout opgetreden bij het ophalen "
				  + "van het rooster voor dit studentnummer:\n\n"
				  + Utils.niceException(fatalException) + "\n\n"
				  + "Controleer aub of het ingevoerde nummer klopt.",
				  Integer.toString(this.studentId));
    	}
    }
}
