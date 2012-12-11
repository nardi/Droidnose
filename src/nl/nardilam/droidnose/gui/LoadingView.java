package nl.nardilam.droidnose.gui;

import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class LoadingView extends RelativeLayout
{
	public LoadingView(Context context)
	{
		super(context);

		TextView loading = new TextView(context);
		loading.setText("Rooster wordt geladen...");
		loading.setTextSize(18);
		RelativeLayout.LayoutParams params =
				new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		this.addView(loading, params);
	}
}
