package nl.nardilam.droidnose;

import java.util.Calendar;
import nl.nardilam.droidnose.datetime.Day;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.DialogInterface.OnClickListener;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.CalendarView;
import android.widget.DatePicker;

public class ChooseDateFragment extends DialogFragment
{
	private final Day startDay;
	private OnDateSetListener listener = null;

	public ChooseDateFragment(Day startDay)
	{
		this.startDay = startDay;
		this.setRetainInstance(true);
	}
	
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		
		if (activity instanceof OnDateSetListener)
			this.listener = (OnDateSetListener)activity;
	}
	
	public void onDestroyView()
	{
		if (getDialog() != null)
			getDialog().setOnDismissListener(null);
		super.onDestroyView();
	}
	
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		DatePickerDialog d = new DatePickerDialog(getActivity(), listener, startDay.year, startDay.month - 1, startDay.day);
		d.setTitle("Kies een datum");
		d.setButton(DatePickerDialog.BUTTON_POSITIVE, "Klaar!", d);
		d.setButton(Dialog.BUTTON_NEGATIVE, "Laat maar", (OnClickListener)null);
		
		if (Build.VERSION.SDK_INT >= 11)
		{
			DatePicker datePicker = d.getDatePicker();
			if (Utils.isInPortraitMode())
			{
				datePicker.setCalendarViewShown(true);
				datePicker.setSpinnersShown(false);
	
				if (Build.VERSION.SDK_INT >= 12)
				{
					CalendarView cv = datePicker.getCalendarView();
					cv.setFirstDayOfWeek(Calendar.MONDAY);
					cv.setShowWeekNumber(true);
				}
			}
			else
			{
				datePicker.setCalendarViewShown(false);
				datePicker.setSpinnersShown(true);
			}
		}
		
		return d;
	}
}