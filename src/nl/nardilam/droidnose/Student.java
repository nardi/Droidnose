package nl.nardilam.droidnose;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nl.nardilam.droidnose.datetime.Time;
import nl.nardilam.droidnose.json.JSONException;
import nl.nardilam.droidnose.json.JSONValue;
import nl.nardilam.droidnose.net.DatanoseQuery;

public class Student implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public final int id;
	public final List<Course> courses;
	public final Map<Course, Group> groups;
	public final Time creationTime;
	
	private Student(int id, List<Course> courses, Map<Course, Group> groups)
	{
		this.id = id;
		this.courses = courses;
		this.groups = groups;
		this.creationTime = Time.now();
	}
	
	public static Student download(int studentId) throws IOException, JSONException
	{
		DatanoseQuery coursesByStudent = new DatanoseQuery("GetCoursesByStudent?id=" + studentId);
		List<JSONValue> courseResults = coursesByStudent.query();
		
		List<Course> courses = new ArrayList<Course>();
		for (JSONValue value : courseResults)
		{
			Map<String, JSONValue> course = value.asObject();
			
			int id = (int)course.get("ID").asNumber();
			String catalogNumber = course.get("CatalogNumber").asString();
			String name = course.get("Name").asString();
			int academicYear = (int)course.get("AcademicYear").asNumber();
			
			courses.add(new Course(id, catalogNumber, name, academicYear));
		}
		
		DatanoseQuery groupsByStudent = new DatanoseQuery("GetGroupsByStudent?id=" + studentId);
		List<JSONValue> groupResults = groupsByStudent.query();
		
		Map<Course, Group> groups = new HashMap<Course, Group>();
		for (JSONValue value : groupResults)
		{
			Map<String, JSONValue> group = value.asObject();
			
			int id = (int)group.get("ID").asNumber();
			String identifier = group.get("Identifier").asString();
			String catalogNumber =  group.get("CatalogNumber").asString();
			Course course = null;
			for (Course c : courses)
			{
				if (c.catalogNumber.equals(catalogNumber))
					course = c;
			}
			
			if (course != null)
				groups.put(course, new Group(id, course, identifier));
		}
		
		return new Student(studentId, courses, groups);
	}
}
