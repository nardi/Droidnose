package nl.nardilam.droidnose;

import java.io.Serializable;

public class Course implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public final int id;
	public final String catalogNumber;
	public final String name;
	public final int academicYear;
	
	public Course(int id, String catalogNumber, String name, int academicYear)
	{
		this.id = id;
		this.catalogNumber = catalogNumber;
		this.name = name;
		this.academicYear = academicYear;
	}
	
	public String toString()
	{
		return this.name;
	}
}
