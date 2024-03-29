package nl.nardilam.droidnose.gui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import nl.nardilam.droidnose.ChooseDateFragment;
import nl.nardilam.droidnose.Utils;
import nl.nardilam.droidnose.datetime.Day;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

/*
 * Deze view geeft een dag weer als tekst in het formaat
 * "maandag 01 januari".
 */
public class DateTitleView extends TextView
{
	private static final DateFormat dayFormat = new SimpleDateFormat("EEEE d MMMM");
	private static final int padding = Utils.dpToPx(8);
	
	private final Day day;
	
	public DateTitleView(final Context context, final Day day)
	{
		super(context);
		
		this.day = day;
		this.setError(false);
		this.setTextSize(14);
		
		this.setPadding(0, padding, 0, padding);
		this.setGravity(Gravity.CENTER);
		this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, Utils.dpToPx(40)));
		
		this.setClickable(true);
		this.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if (context instanceof FragmentActivity && context instanceof OnDateSetListener && day != null)
				{
					FragmentActivity activity = (FragmentActivity)context;
					DialogFragment newFragment = new ChooseDateFragment(day);
				    newFragment.show(activity.getSupportFragmentManager(), "chooseDate");
				}
			}
		});		
	}
	
	public void setError(boolean error)
	{
		String dayString = "";
		if (day != null)
			dayString = day.startTime.format(dayFormat);
		if (error)
			dayString += " (fout bij laden)";
		this.setText(dayString);
	}
}
