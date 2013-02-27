package nl.nardilam.droidnose.gui;

import java.util.ArrayList;
import java.util.Collection;
import nl.nardilam.droidnose.Orientation;
import nl.nardilam.droidnose.Utils;
import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HourView extends TimeLayout
{
	private static final int DEFAULT_NUM_HOURS =
			TimeLayout.DEFAULT_ENDHOUR - TimeLayout.DEFAULT_STARTHOUR + 1;
	private static final int MINIMUM_VIEW_HEIGHT = 600; //dp
	
	private final HourView hourView = this;
	
	private HourScrollView scrollView = null;
	private LinearLayout layout = null;
	
	private final Collection<TimeLayout> hourHeightListeners;
	
	public HourScrollView getScrollView()
	{
		return this.scrollView;
	}
	
	public HourView(Context context, int startHour, int endHour)
	{
		super(context, startHour, endHour);
		
		this.scrollView = new HourScrollView(context);
		this.addView(scrollView);
		
		this.layout = new LinearLayout(context);
		this.layout.setOrientation(Orientation.VERTICAL);
		this.scrollView.addView(this.layout);
		
		this.hourHeightListeners = new ArrayList<TimeLayout>();
	}
	
	public void addHourHeightListener(TimeLayout tl)
	{
		this.hourHeightListeners.add(tl);
	}
	
	public void setAvailableHeight(int height)
	{
		this.setHourHeight(height / DEFAULT_NUM_HOURS);
	}
	
	public void setHourHeight(int height)
	{
		super.setHourHeight(height);
		
		for (TimeLayout tl : this.hourHeightListeners)
			tl.setHourHeight(height);
	}
	
	protected void update()
	{
		this.layout.removeAllViews();

		Context context = this.getContext();
		int hourHeight = this.getHourHeight();
		for (int hour = getStartHour(); hour <= getEndHour(); hour++)
		{
			TextView v = new TextView(context);
			v.setHeight(hourHeight);
			v.setPadding(Utils.dpToPx(8), 0, Utils.dpToPx(8), 0);
			v.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
			v.setTextSize(14);
			v.setText(Integer.toString(hour));
			this.layout.addView(v);
		}
	}
	
	/*
	 * Een speciale ScrollView-klasse die de hourHeight van de
	 * hourView en de listeners set zodra deze bekend is.
	 * (Deze is afhankelijk van de grootte van de parentview.)
	 */
	private class HourScrollView extends LinkedScrollView
	{
		public HourScrollView(Context context)
		{
			super(context);
			
			this.setVerticalScrollBarEnabled(false);
		}	
		
		private boolean measured = false;
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
		{
			int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
			int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
			int minHeight = Utils.dpToPx(MINIMUM_VIEW_HEIGHT);
			
			if (!measured)
			{
				if (parentHeight > parentWidth)
					hourView.setAvailableHeight(parentHeight);
				else
					hourView.setAvailableHeight(Math.max(parentWidth, minHeight));
				measured = true;
			}
			
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}
}
