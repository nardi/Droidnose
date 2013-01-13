package nl.nardilam.droidnose;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

public class LocationSet implements Serializable
{
	private static final long serialVersionUID = 1L;
	public static final String DEFAULT_LOCATION = "nog onbekende locatie";
	
	public final SortedSet<String> set;
	
	public LocationSet(Collection<String> locations)
	{
		if (locations == null || locations.isEmpty())
			locations = Collections.singleton(DEFAULT_LOCATION);
		this.set = Collections.unmodifiableSortedSet(new TreeSet<String>(locations));
	}
	
	public String toString()
	{
		Iterator<String> iter = this.set.iterator();
		StringBuilder sb = new StringBuilder();
		while (iter.hasNext())
		{
			sb.append(iter.next());
			if (iter.hasNext())
				sb.append(", ");
		}
		return sb.toString();
	}
}
