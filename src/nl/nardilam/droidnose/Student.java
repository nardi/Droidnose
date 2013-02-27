package nl.nardilam.droidnose;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

import nl.nardilam.droidnose.datetime.Time;
import nl.nardilam.droidnose.net.DatanoseQuery;

public class Student implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public final int id;
	public final Set<Course> courses;
	public final Map<Course, Group> groups;
	public final Time creationTime;
	
	private Student(int id, Set<Course> courses, Map<Course, Group> groups)
	{
		this.id = id;
		this.courses = courses;
		this.groups = groups;
		this.creationTime = Time.now();
	}
	
	public static Student download(int studentId) throws Exception
	{
		DatanoseQuery dnQuery = new DatanoseQuery();
		String courseUrl = "GetCoursesByStudent?id=" + studentId;
		String groupUrl = "GetGroupsByStudent?id=" + studentId;
		Map<String, JSONArray> results = dnQuery.queryAll(courseUrl, groupUrl);
		
		JSONArray courseResults = results.get(courseUrl);
		
		Set<Course> courses = new HashSet<Course>();
		for (int i = 0; i < courseResults.length(); i++)
		{
			JSONObject course = (JSONObject)courseResults.get(i);
			
			int id = course.getInt("ID");
			String catalogNumber = course.getString("CatalogNumber");
			String name = Utils.unescape(course.getString("Name"));
			int academicYear = course.getInt("AcademicYear");
			
			courses.add(new Course(id, catalogNumber, name, academicYear));
		}
		
		JSONArray groupResults = results.get(groupUrl);
		
		Map<Course, Group> groups = new HashMap<Course, Group>();
		for (int i = 0; i < groupResults.length(); i++)
		{
			JSONObject group = (JSONObject)groupResults.get(i);
			
			int id = group.getInt("ID");
			String identifier = group.getString("Identifier");
			String catalogNumber =  group.getString("CatalogNumber");
			for (Course course : courses)
			{
				if (course.catalogNumber.equals(catalogNumber))
					groups.put(course, new Group(id, course, identifier));
			}
		}
		
		return new Student(studentId, courses, groups);
	}
}
