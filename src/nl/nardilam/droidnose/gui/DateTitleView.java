package nl.nardilam.droidnose.gui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import nl.nardilam.droidnose.ChooseDateActivity;
import nl.nardilam.droidnose.Utils;
import nl.nardilam.droidnose.datetime.Day;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
	
	public DateTitleView(final Context context, final Day day)
	{
		super(context);
		
		if (day != null)
		{
			String dayString = day.startTime.format(dayFormat);
			this.setText(dayString);
		}
		else
		{
			this.setText("");
		}
		this.setTextSize(14);
		
		this.setPadding(0, padding, 0, padding);
		this.setGravity(Gravity.CENTER);
		this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, Utils.dpToPx(40)));
		
		this.setClickable(true);
		this.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if (context instanceof Activity && day != null)
				{
					Activity activity = (Activity)context;
					Intent intent = ChooseDateActivity.createIntentFromDay(day, new Intent(activity, ChooseDateActivity.class));
					activity.startActivityForResult(intent, ChooseDateActivity.DATE_REQUEST);
				}
			}
		});		
	}
}
