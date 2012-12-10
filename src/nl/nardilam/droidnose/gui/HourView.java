package nl.nardilam.droidnose.gui;

import nl.nardilam.droidnose.Utils;
import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;

public class HourView extends TimeLayout
{
	private static final int DEFAULT_NUM_HOURS = 9;
	
	public HourView(Context context, int startHour, int endHour)
	{
		super(context, startHour, endHour);
	}
	
	public void setAvailableHeight(int height)
	{
		this.setHourHeight(height / DEFAULT_NUM_HOURS);
	}
	
	protected void update()
	{
		this.removeAllViews();

		Context context = this.getContext();
		int hourHeight = this.getHourHeight();
		for (int hour = startHour; hour <= endHour; hour++)
		{
			TextView v = new TextView(context);
			v.setHeight(hourHeight);
			v.setPadding(Utils.dipToPx(8), 0, Utils.dipToPx(8), 0);
			v.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
			v.setTextSize(14);
			v.setText(Integer.toString(hour));
			this.addView(v);
		}
	}
}
