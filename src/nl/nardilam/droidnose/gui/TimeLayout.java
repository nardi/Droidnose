package nl.nardilam.droidnose.gui;

import nl.nardilam.droidnose.Orientation;
import android.content.Context;
import android.view.ViewGroup;
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
	private int hourHeight = 0;
	public final int startHour;
	public final int endHour;
	
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
	
	public void setWidth(int width)
	{
		ViewGroup.LayoutParams params = this.getLayoutParams();
		if (params == null)
			params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.width = width;
		this.setLayoutParams(params);
	}
	
	protected void setHeight(int height)
	{
		ViewGroup.LayoutParams params = this.getLayoutParams();
		if (params == null)
			params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.height = height;
		this.setLayoutParams(params);
	}
	
	protected void update()
	{
	}
}
