package nl.nardilam.droidnose.datetime;

import java.io.Serializable;

public class TimePeriod implements Serializable, Comparable<TimePeriod>
{	
	private static final long serialVersionUID = 1L;
	
	public final Time startTime;
	public final Time endTime;
    
	/*
     * Construction methods
     */
	
    public TimePeriod(Time start, Time end)
    {
        this.startTime = start;
        this.endTime = end;
    }
    
    /*
     * Conversion methods
     */
    
    public TimePeriod add(Duration d)
    {
    	return new TimePeriod(this.startTime, this.endTime.add(d));
    }
    
    public TimePeriod subtract(Duration d)
    {
    	return new TimePeriod(this.startTime, this.endTime.subtract(d));
    }
    
    public TimePeriod move(Duration d)
    {
    	return new TimePeriod(this.startTime.add(d), this.endTime.add(d));
    }
    
    public Duration duration()
    {
    	return this.startTime.timeTo(this.endTime);
    }
    
    /*
     * Comparison methods
     */
    
    public boolean equals(Object o)
    {
    	try
		{
			return this.equals((TimePeriod)o);
		}
    	catch (ClassCastException e)
		{
			return false;
		}
    }
	
    public boolean equals(TimePeriod tp)
    {
    	return this.startTime.equals(tp.startTime)
    		&& this.endTime.equals(tp.endTime);
    }
    
    public int hashCode()
    {
    	int result = (int)serialVersionUID;
    	result = 31 * result + this.startTime.hashCode();
    	result = 31 * result + this.endTime.hashCode();
    	return result;
    }
    
    public boolean startsDuring(TimePeriod tp)
    {
    	return startTime.isDuring(tp);
    }
    
    public boolean endsDuring(TimePeriod tp)
    {
    	return endTime.isDuring(tp) || endTime.equals(tp.endTime);
    }
    
    public boolean isDuring(TimePeriod tp)
    {
    	return this.startsDuring(tp) && this.endsDuring(tp);
    }
    
    public int compareTo(TimePeriod tp)
	{
		int startTimeComp = this.startTime.compareTo(tp.startTime);
		if (startTimeComp != 0)
			return startTimeComp;
		else
			return this.endTime.compareTo(tp.endTime);
	}
    
    /*
     * Display methods
     */
    
    public String toString()
    {
    	return startTime + " to " + endTime;
    }
}
