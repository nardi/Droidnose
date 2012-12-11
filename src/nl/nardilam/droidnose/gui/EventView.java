package nl.nardilam.droidnose.gui;

import nl.nardilam.droidnose.Event;
import nl.nardilam.droidnose.Orientation;
import nl.nardilam.droidnose.Utils;
import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class EventView extends TimeLayout
{
	public final Event event;
	
	private RelativeLayout layout = null;
	
	public EventView(Context context, Event event)
	{
		super(context, 0, 0);
		
		this.event = event;
	}
	
	protected void update()
	{
		this.removeAllViews();
		
		Context context = this.getContext();
		int hourHeight = this.getHourHeight();
		int borderSize = 1;
		double numHours = event.duration().inHours();
		
		int height = (int)(hourHeight * numHours);
		LayoutParams params = new LayoutParams(0, height, 0.5f);
		this.setLayoutParams(params);
		
		this.layout = new RelativeLayout(context);
		this.layout.setBackgroundColor(0xFFEAEAEA);
		this.addView(layout);
		
		LinearLayout textLayout = new LinearLayout(context);
		textLayout.setOrientation(Orientation.VERTICAL);
		int padding = Utils.dpToPx(8);
		textLayout.setPadding(padding, padding, padding, padding);
		
		RelativeLayout.LayoutParams textParams =
				new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		textParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		textParams.topMargin = borderSize;
		
		layout.addView(textLayout, textParams);
		
		TextView course = new TextView(context);
		course.setGravity(Gravity.CENTER_HORIZONTAL);
		course.setTextSize(17);
		course.setText(event.course.name);
		textLayout.addView(course);
		
		TextView other = new TextView(context);
		other.setGravity(Gravity.CENTER_HORIZONTAL);
		other.setTextSize(15);
		other.setText(event.type + ", " + event.location);
		textLayout.addView(other);
		
		this.addBorder(borderSize);
	}
	
	private void addBorder(int thickness)
	{
		this.addDivider(thickness, Orientation.HORIZONTAL, RelativeLayout.ALIGN_PARENT_LEFT);
		this.addDivider(thickness, Orientation.VERTICAL, RelativeLayout.ALIGN_PARENT_TOP);
		this.addDivider(thickness, Orientation.VERTICAL, RelativeLayout.ALIGN_PARENT_BOTTOM);
		this.addDivider(thickness, Orientation.HORIZONTAL, RelativeLayout.ALIGN_PARENT_RIGHT);
	}
	
	private void addDivider(int thickness, int orientation, int alignment)
	{
		DividerView divider = new DividerView(this.getContext(), thickness);
		divider.setOrientation(orientation);
		
		RelativeLayout.LayoutParams params =
				new RelativeLayout.LayoutParams(divider.getLayoutParams());
		params.addRule(alignment);
		if (orientation == Orientation.VERTICAL)
			params.leftMargin = params.rightMargin = thickness;
		
		this.layout.addView(divider, params);
	}
}
