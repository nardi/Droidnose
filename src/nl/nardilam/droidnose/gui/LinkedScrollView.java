package nl.nardilam.droidnose.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import android.content.Context;
import android.widget.ScrollView;

public class LinkedScrollView extends ScrollView
{
	private final LinkedScrollView linkedScrollView = this;
	private final Set<LinkedScrollView> links;
	private final Collection<LinkedScrollView> scrollOrigin;
	
	public LinkedScrollView(Context context)
	{
		super(context);
		this.links = new HashSet<LinkedScrollView>();
		this.scrollOrigin = new ArrayList<LinkedScrollView>();
	}
	
	public void linkTo(final LinkedScrollView lsv)
	{
		if (lsv != null)
		{
			links.add(lsv);
			lsv.links.add(linkedScrollView);
			this.post(new Runnable()
			{
				public void run()
				{
					linkedScrollView.scrollTo(lsv.getScrollX(), lsv.getScrollY());
				}
			});			
		}
	}
	
	public void unlink()
	{
		for (LinkedScrollView lsv : this.links)
			this.unlink(lsv);
	}
	
	public void unlink(LinkedScrollView lsv)
	{
		links.remove(lsv);
		lsv.links.remove(this);
	}
	
	public void scrollBy(int x, int y, LinkedScrollView origin)
	{
		this.scrollOrigin.add(origin);
		super.scrollBy(x, y);
	}
	
	public void scrollTo(int x, int y, LinkedScrollView origin)
	{
		this.scrollOrigin.add(origin);
		super.scrollTo(x, y);
	}
	
	protected void onScrollChanged(int x, int y, int oldx, int oldy)
	{
		for (LinkedScrollView lsv : this.links)
		{
			if (!this.scrollOrigin.contains(lsv))
				lsv.scrollTo(x, y, this);
		}
		this.scrollOrigin.clear();
	}
}