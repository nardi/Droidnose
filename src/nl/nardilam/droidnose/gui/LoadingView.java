package nl.nardilam.droidnose.gui;

import nl.nardilam.droidnose.Utils;
import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LoadingView extends RelativeLayout
{
	public LoadingView(Context context)
	{
		super(context);
		
		LinearLayout layout = new LinearLayout(context);

        ProgressBar pBar = new ProgressBar(context);
        pBar.setIndeterminate(true);
        layout.addView(pBar);
        
		TextView text = new TextView(context);
		text.setText("Rooster wordt geladen...");
		text.setTextSize(18);
		text.setGravity(Gravity.CENTER_VERTICAL);
		text.setPadding(Utils.dpToPx(4), 0, 0, 0);
		layout.addView(text, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		
		RelativeLayout.LayoutParams params =
				new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		this.addView(layout, params);
	}
}
