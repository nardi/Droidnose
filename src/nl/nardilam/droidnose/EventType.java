package nl.nardilam.droidnose;

import java.util.*;
import java.io.Serializable;

/*
 * Dit is heel oud, toen wist ik niet dat Java enums had
 */

public class EventType implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String name = null;

    private EventType(String typeName)
    {
        this.name = typeName;
        
        allTypes.add(this);
    }
    
    public boolean equals(Object o)
    {
    	try
		{
			return this.equals((EventType)o);
		}
    	catch (ClassCastException e)
		{
			return false;
		}
    }
	
    public boolean equals(EventType et)
    {
    	return this.name.equals(et.name);
    }
    
    public String toString()
    {
        return name;
    }
    
    public final static List<EventType> allTypes = new ArrayList<EventType>();
    
    // Als we bepaalde soorten colleges willen filteren, kunnen we deze constanten gebruiken
    // Zou je denken, maar tot nu toe (31-3-2013) wordt dit nog niet gebruikt
    public final static EventType Unknown = new EventType("");
    public final static EventType Hoorcollege = new EventType("Hoorcollege");
    public final static EventType Werkcollege = new EventType("Werkcollege");
    public final static EventType Studio_course = new EventType("Studio course");
    public final static EventType Computerpracticum = new EventType("Computerpracticum");
    public final static EventType Tentamen = new EventType("Tentamen");
    public final static EventType Hertentamen = new EventType("Hertentamen");
    public final static EventType Tussentoets = new EventType("Tussentoets");
    public final static EventType Presentatie = new EventType("Presentatie");
    public final static EventType Tutoraat = new EventType("Tutoraat");
    
    public static EventType parse(String typeName)
    {
        for(EventType et : allTypes)
        {
            if (et.name.equals(typeName))
              return et;
        }
        
        return new EventType(typeName);
    }
}
