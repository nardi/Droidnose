package nl.nardilam.droidnose;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import android.content.Context;
import android.os.AsyncTask;

import nl.nardilam.droidnose.datetime.Day;
import nl.nardilam.droidnose.datetime.Duration;
import nl.nardilam.droidnose.datetime.Time;
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
    
    public void update(final Callback<DayEvents> whenDone,  final List<Day> daysToUpdate)
    {
    	/*
    	 * Eerst controleren of de studentinformatie
    	 * niet geupdatet moet worden
    	 */
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
	    	catch (Exception e)
			{
	    		this.fatalException = e;
				return null;
			}
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
        /*
         * Nog even een sort voor de zekerheid
         */
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
    
    protected List<Event> downloadEvents(String dateFilter) throws Exception
    {
    	List<Event> events = new ArrayList<Event>();
    	Map<Course, String> urlList = new HashMap<Course, String>(this.student.courses.size());
    	DatanoseQuery dnQuery = new DatanoseQuery();
    	
    	for (Course course : this.student.courses)
		{
    		String queryUrl = "GetActivitiesByCourse?id=" + course.id + "&$filter=";
    		boolean inGroup = this.student.groups.containsKey(course);
    		boolean hasFilter = dateFilter != null && !dateFilter.trim().equals("");
    		if (inGroup)
    		{
	    		String groupId = this.student.groups.get(course).identifier;
	    		String groupFilter = String.format(groupFormat, groupId);
	    		queryUrl += "(" + groupFilter + ")";
    		}
    		if (hasFilter)
    		{
    			if (inGroup)
    				queryUrl += " and ";
				queryUrl += "(" + dateFilter + ")";
    		}
    		queryUrl = queryUrl.replaceAll(" ", "%20");
    		
    		urlList.put(course, queryUrl);
    	}
    	
    	Map<String, JSONArray> resultsPerCourse = dnQuery.queryAll(urlList.values());
    	for (Course course : this.student.courses)
		{
			JSONArray results = resultsPerCourse.get(urlList.get(course));
			for (int i = 0; i < results.length(); i++)
			{
				JSONObject activity = (JSONObject)results.get(i);
				dnQuery.addQuery("GetLocationsByActivity?id=" + activity.getInt("ID"));
			}
			
			Map<String, JSONArray> locationResults = dnQuery.query();
			
			for (int i = 0; i < results.length(); i++)
			{
				JSONObject activity = (JSONObject)results.get(i);
				
				JSONArray locations = locationResults.get("GetLocationsByActivity?id=" + activity.getInt("ID"));
				List<String> locationNames = new ArrayList<String>();
				for (int l = 0; l < locations.length(); l++)
				{
					JSONObject location = (JSONObject)locations.get(l);
					locationNames.add(location.getString("Name"));
				}
    			
				EventType type = EventType.parse(activity.getString("ActivityType"));

				String weekPattern = activity.get("WeekPattern").toString();
				List<Week> weekList = Timetable.calculateWeeksFromPattern(weekPattern, course.academicYear);

				int days = activity.getInt("Day");
				List<WeekDay> dayList = Timetable.calculateDaysFromPattern(days, course.academicYear);

				for (Week week : weekList)
				{
					for (WeekDay weekDay : dayList)
					{
						Day day = week.getDay(weekDay);
						int startTime = activity.getInt("StartTime");
						Time start = day.startTime.add(Duration.hours(startTime));
						
						Double duration = activity.getDouble("Duration");
						Time end = start.add(Duration.hours(duration));
						
						events.add(new Event(start, end, course, type, locationNames, new ArrayList<String>()));
					}
				}
    		}
    	}
    	
    	return events;
    }
}
