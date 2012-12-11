package nl.nardilam.droidnose;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import android.content.Context;

import nl.nardilam.droidnose.datetime.Day;
import nl.nardilam.droidnose.datetime.Duration;
import nl.nardilam.droidnose.datetime.Time;
import nl.nardilam.droidnose.datetime.Week;
import nl.nardilam.droidnose.datetime.WeekDay;
import nl.nardilam.droidnose.json.JSONException;
import nl.nardilam.droidnose.json.JSONValue;
import nl.nardilam.droidnose.net.DatanoseQuery;

public class StudentTimetable extends Timetable
{    
	private static final long serialVersionUID = 1L;
	
	public final Student student;
    
    private StudentTimetable(Student student, List<Event> events)
    {
        super(events);
        this.student = student;
    }
    
    public void saveToFile(String filename) throws ContextNotSetException, IOException
	{
    	Context context = Utils.getContext();
    	FileOutputStream file = context.openFileOutput(filename, Context.MODE_PRIVATE);
        ObjectOutputStream out = new ObjectOutputStream(file);
        out.writeObject(this); 
        out.close();
	}
	
	public static StudentTimetable loadFromFile(String filename) throws ContextNotSetException, IOException, ClassNotFoundException
	{
		Context context = Utils.getContext();
    	FileInputStream file = context.openFileInput(filename);
        ObjectInputStream in = new ObjectInputStream(file);
        StudentTimetable timetable = (StudentTimetable)in.readObject();
        in.close();
        timetable.sort();
        return timetable;
	}
    
    public String toString()
    {
    	return "Rooster voor student " + this.student.id + ":\n"
    			+ super.toString();
    }
    
    public static StudentTimetable download(Student student) throws Exception
    {
    	StudentTimetable t = StudentTimetable.initialize(student);
    	t.update(null);
    	return t;
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
    
    protected List<Event> downloadEvents(String filter) throws IOException, JSONException
    {
    	List<Event> events = new ArrayList<Event>();
    	
    	for (Course course : this.student.courses)
    	{
    		String groupId = this.student.groups.get(course).identifier;
    		String groupFilter = String.format(groupFormat, groupId);
    		String queryUrl = "GetActivitiesByCourse?id=" + course.id
    						+ "&$filter=(" + groupFilter + ")";
    		if (filter != null && !filter.equals(""))
    			queryUrl += " and (" + filter + ")";
    		queryUrl = queryUrl.replaceAll(" ", "%20");
    		DatanoseQuery activitiesByCourse = new DatanoseQuery(queryUrl);
    		List<JSONValue> results = activitiesByCourse.query();

    		for (JSONValue value : results)
    		{
				Map<String, JSONValue> activity = value.asObject();
				
				/*
				 * Locaties ophalen kost erg veel requests en tijd, kan
				 * waarschijnlijk sneller als batchrequest gedaan worden
				 */
				String location = "nog onbekende locatie";
				DatanoseQuery locationsByActivity = new DatanoseQuery("GetLocationsByActivity?id="
						+ (int)activity.get("ID").asNumber());
				List<JSONValue> locations = locationsByActivity.query();
				if (!locations.isEmpty())
					location = locations.get(0).asObject().get("Name").asString();
    			
				EventType type = EventType.parse(activity.get("ActivityType").asString());
				
				String weekPattern = activity.get("WeekPattern").toString();
				List<Week> weekList = Timetable.calculateWeeksFromPattern(weekPattern, course.academicYear);
				
				int days = (int)activity.get("Day").asNumber();
				List<WeekDay> dayList = Timetable.calculateDaysFromPattern(days, course.academicYear);
								
				for (Week week : weekList)
				{
					for (WeekDay weekDay : dayList)
					{
						Day day = week.getDay(weekDay);
						int startTime = (int)activity.get("StartTime").asNumber();
						Time start = day.startTime.add(Duration.hours(startTime));
						
						Double duration = activity.get("Duration").asNumber();
						Time end = start.add(Duration.hours(duration));
						
						events.add(new Event(start, end, course, type, location, new ArrayList<String>()));
					}
				}
    		}
    	}
    	
    	return events;
    }
}
