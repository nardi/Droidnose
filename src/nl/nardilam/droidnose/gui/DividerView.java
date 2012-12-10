package nl.nardilam.droidnose.gui;

import nl.nardilam.droidnose.Orientation;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class DividerView extends View
{
	private final int thickness;
	private final int marginSize;
	
	public DividerView(Context context, int thickness)
	{
		this(context, thickness, 0);
	}
	
	public DividerView(Context context, int thickness, int marginSize)
	{
		this(context, thickness, marginSize, Color.BLACK & 0xAAFFFFFF);
	}
	
	public DividerView(Context context, int thickness, int marginSize, int color)
	{
		super(context);
		
		this.thickness = thickness; 
		this.marginSize = marginSize; 
		this.setOrientation(Orientation.HORIZONTAL);
		this.setBackgroundColor(color);
	}
	
	public void setOrientation(int orientation)
	{
		LinearLayout.LayoutParams params = null;
		if (orientation == Orientation.HORIZONTAL)
		{
			params = new LinearLayout.LayoutParams(thickness, LayoutParams.MATCH_PARENT);
			params.topMargin = params.bottomMargin = marginSize;
		}
		else if (orientation == Orientation.VERTICAL)
		{
			params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, thickness);
			params.leftMargin = params.rightMargin = marginSize;
		}
		if (params != null)
		{
			params.weight = 0;
			params.gravity = Gravity.CENTER;
			this.setLayoutParams(params);
		}
	}
}
