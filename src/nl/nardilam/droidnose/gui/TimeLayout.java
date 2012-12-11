package nl.nardilam.droidnose.gui;

import nl.nardilam.droidnose.Orientation;
import android.content.Context;
import android.widget.LinearLayout;

/*
 * Een TimeLayout is een LinearLayout gebaseerd op een (verticale) tijdsschaal.
 * Deze bevat daarom methoden om om te gaan met een hoogte per uur (hourHeight).
 * Omdat deze hourHeight mogelijk afhankelijk is van de beschikbare ruimte,
 * wordt in de setter hiervoor een update-methode aangeroepen, die de child-
 * views kan vervangen of aanpassen.
 */
public class TimeLayout extends LinearLayout
{
	public static final int DEFAULT_STARTHOUR = 9;
	public static final int DEFAULT_ENDHOUR = 17;
	
	private int hourHeight = 0;
	private int startHour;
	private int endHour;
	
	public TimeLayout(Context context)
	{
		this(context, DEFAULT_STARTHOUR, DEFAULT_ENDHOUR);
	}
	
	public TimeLayout(Context context, int startHour, int endHour)
	{
		super(context);
		
		this.startHour = startHour;
		this.endHour = endHour;
		this.setOrientation(Orientation.VERTICAL);
	}
	
	public void setHourHeight(int height)
	{
		this.hourHeight = height;
		this.update();
	}
	
	public int getHourHeight()
	{
		return this.hourHeight;
	}
	
	public void setHourRange(int startHour, int endHour)
	{
		this.startHour = startHour;
		this.endHour = endHour;
		this.update();
	}
	
	public int getStartHour()
	{
		return this.startHour;
	}
	
	public int getEndHour()
	{
		return this.endHour;
	}
	
	protected void update()
	{
	}
}
