package nl.nardilam.droidnose;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;
import android.content.Context;
import android.os.AsyncTask;

import nl.nardilam.droidnose.datetime.Day;
import nl.nardilam.droidnose.datetime.Duration;
import nl.nardilam.droidnose.datetime.Time;
import nl.nardilam.droidnose.datetime.TimeUtils;
import nl.nardilam.droidnose.datetime.Week;
import nl.nardilam.droidnose.datetime.WeekDay;
import nl.nardilam.droidnose.net.DatanoseQuery;

public class StudentTimetable extends Timetable
{    
	private static final long serialVersionUID = 1L;
	
	private final StudentTimetable timetable = this;
	
	private Student student;
	public Student getStudent()
	{
		return this.student;
	}
    
    private StudentTimetable(Student student, List<Event> events)
    {
        super(events);
        this.student = student;
    }
    
    /*
	 * update wordt geoverride, om eerst te controleren of de studentinformatie
	 * niet geupdatet moet worden
	 */
    public void update(final Callback<EventCollection> whenDone,  final List<Day> daysToUpdate)
    {
    	if (!this.student.creationTime.add(Duration.hours(24)).isAfter(Time.now()))
    	{    			
    		this.updateStudentInfo(new Callback<Student>()
			{
				public void onResult(Student result)
				{
					timetable.update(whenDone, daysToUpdate);
				}

				public void onError(Exception e)
				{
					whenDone.onError(e);
				}
			});
    	}
    	else
    	{
    		super.update(whenDone, daysToUpdate);
    	}
    }
    
    /*
	 * Studentinformatie updaten gebeurt nu vanaf hier, kan beter lijkt me
	 */
    
    public void updateStudentInfo(Callback<Student> whenDone)
    {
    	new StudentDownloader(whenDone).execute();
    }
    
    private class StudentDownloader extends AsyncTask<Void, Void, Student>
    {
    	private final Callback<Student> callback;
    	private Exception fatalException = null;
    	
    	public StudentDownloader(Callback<Student> callback)
    	{
    		this.callback = callback;
    	}
    	
		protected Student doInBackground(Void... nothings)
		{
	    	try
			{
				return Student.download(timetable.student.id);
			}
	    	catch (Throwable t)
			{
				return null;
			}
	    	/* catch (Exception e)
			{
	    		this.fatalException = e;
				return null;
			} */
		}
		
		protected void onPostExecute(Student student)
		{
			if (student != null)
			{
				timetable.student = student;
				this.callback.onResult(student);
			}
			else
			{
				this.callback.onError(this.fatalException);
			}
		}
    }
    
    public void saveToFile() throws ContextNotSetException, IOException
    {
    	this.saveToFile(Integer.toString(this.getStudent().id));
    }
	
	public static StudentTimetable loadFromFile(String filename) throws ContextNotSetException, IOException, ClassNotFoundException
	{
		Context context = Utils.getContext();
    	FileInputStream file = context.openFileInput(filename);
        ObjectInputStream in = new ObjectInputStream(file);
        StudentTimetable timetable = (StudentTimetable)in.readObject();
        in.close();
        // Nog even een sort voor de zekerheid
        timetable.sort();
        return timetable;
	}
    
    public String toString()
    {
    	return "Rooster voor student " + this.student.id + ":\n"
    			+ super.toString();
    }
    
    public static StudentTimetable empty(int studentId) throws Exception
    {
    	return StudentTimetable.initialize(Student.download(studentId));
    }
    
    public static StudentTimetable initialize(Student student)
    {
    	return new StudentTimetable(student, new ArrayList<Event>());
    }
    
    private static final String groupFormat =
		"Groups eq '%1$s'"
	  + " or substringof('%1$s,', Groups) eq true"
	  + " or substringof(',%1$s', Groups) eq true"
	  + " or Groups eq ''";
    
    protected List<EventCollection> downloadEvents(Collection<Day> days) throws Exception
    {
    	List<EventCollection> eventCollections = new ArrayList<EventCollection>();
    	DatanoseQuery dnQuery = new DatanoseQuery();
    	
    	Set<Week> weeks = new HashSet<Week>();
    	for (Day day : days)
   			weeks.add(day.getWeek());
    	
    	Map<Week, String> weekUrls = new HashMap<Week, String>();
    	for (Week week : weeks)
    	{
    		int academicYear = week.number >= 36 ? week.year : week.year - 1;
        	Week firstWeek = new Week(academicYear, 36, TimeUtils.CET);
    		int academicWeek = (int)firstWeek.startTime.timeTo(week.startTime).inWeeks();
    		String queryUrl = "http://content.datanose.nl/Timetable.svc/GetActivitiesByStudent?id="
    						 + this.student.id + "&week=" + academicWeek;
    		dnQuery.addQuery(queryUrl);
    		weekUrls.put(week, queryUrl);
    	}
    	
		Map<String, JSONArray> resultsPerWeek = dnQuery.query();
		for (Week week : weeks)
		{
			List<Event> weekEvents = new ArrayList<Event>();
			JSONArray results = resultsPerWeek.get(weekUrls.get(week));
			for (int i = 0; i < results.length(); i++)
			{
				JSONObject activity = (JSONObject)results.get(i);
				dnQuery.addQuery("GetLocationsByActivity?id=" + activity.getInt("ID"));
			}
			
			Map<String, JSONArray> locationResults = dnQuery.query();
			
			for (int i = 0; i < results.length(); i++)
			{
				JSONObject activity = (JSONObject)results.get(i);
				
				// Moet nog een goede manier vinden om dit aan een vak te linken
				int academicYear = week.number >= 36 ? week.year : week.year - 1;
				Course course = new Course(0, "", "", academicYear);
				String cn = activity.getString("Name");
				for (Course c : this.student.courses)
				{
					if (cn.startsWith(c.catalogNumber))
						course = c;
				}
				
				JSONArray locations = locationResults.get("GetLocationsByActivity?id=" + activity.getInt("ID"));
				List<String> locationNames = new ArrayList<String>();
				for (int l = 0; l < locations.length(); l++)
				{
					JSONObject location = (JSONObject)locations.get(l);
					locationNames.add(location.getString("Name"));
				}
				
				EventType type = EventType.parse(activity.getString("ActivityType"));				
				
				int dayNum = activity.getInt("Day");
				List<WeekDay> dayList = Timetable.calculateDaysFromPattern(dayNum, course.academicYear);
				
				for (WeekDay weekDay : dayList)
				{
					Day day = week.getDay(weekDay);
					
					int startTime = activity.getInt("StartTime");
					Time start = day.startTime.add(Duration.hours(startTime));
					
					Double duration = activity.getDouble("Duration");
					Time end = start.add(Duration.hours(duration));
					
					weekEvents.add(new Event(start, end, course, type, locationNames, new ArrayList<String>()));
				}
			}
			eventCollections.add(new EventCollection(week, weekEvents));
		}
    	
    	return eventCollections;
    }
}
