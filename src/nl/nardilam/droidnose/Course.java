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
	
	public boolean equals(Object o)
    {
    	try
		{
			return this.equals((Course)o);
		}
    	catch (ClassCastException e)
		{
			return false;
		}
    }
	
    public boolean equals(Course c)
    {
    	return this.id == c.id
    		&& this.catalogNumber.equals(c.catalogNumber)
    		&& this.name.equals(c.name)
    		&& this.academicYear == c.academicYear;
    }
	
	public String toString()
	{
		return this.name;
	}
}
