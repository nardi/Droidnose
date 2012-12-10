package nl.nardilam.droidnose;

import java.io.Serializable;

public class Group implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public final int id;
	public final Course course;
	public final String identifier;
	
	public Group(int id, Course course, String identifier)
	{
		this.id = id;
		this.course = course;
		this.identifier = identifier;
	}
}
