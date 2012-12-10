package nl.nardilam.droidnose;

import nl.nardilam.droidnose.datetime.Day;
import nl.nardilam.droidnose.datetime.Time;
import nl.nardilam.droidnose.datetime.TimeUtils;
import nl.nardilam.droidnose.gui.TimetableView;
import android.os.Bundle;
import android.os.StrictMode;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TimetableActivity extends Activity
{
	public static final String PREFERENCES_FILE = "DroidnoseSettings";
	public static final String STUDENTID = "StudentId";
	
	// Een alias voor extra duidelijkheid in de binnenklassen
	private final TimetableActivity activity = this;

	public class State
	{
		public String enteredStudentId = null;
		public StudentTimetableLoader loader = null;
		public StudentTimetable timetable = null;
		public Day startDay = null;
	}
	public State currentState = new State();
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		Utils.setContext(this.getApplicationContext());
		
		this.tryRestoreState();
		State currentState = this.currentState;
		
		if (currentState.loader != null)
		{
			currentState.loader.setActivity(this);
		}
		
		if (currentState.timetable != null)
		{
			this.showTimetableView(null, null);
		}
		else if (currentState.loader != null)
		{
			this.showLoadingView();
		}
		else
		{
			this.tryLoadStudentId(this.loadTimetableUsingStudentId);
		}
	}
	
	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);
		
		StudentTimetable currentTimetable = currentState.timetable;
		if (currentTimetable != null
		 && currentTimetable.lastFullUpdate.timeTo(Time.now()).inHours() >= 24)
		{
			this.currentState.loader = new StudentTimetableLoader(this);
			this.currentState.loader.execute(currentTimetable.student.id);
		}
	}
	
	private void tryRestoreState()
	{
		State lastState = (State)this.getLastNonConfigurationInstance();
		if (lastState != null)
			this.currentState = lastState;
	}
	
	private void tryLoadStudentId(Callback<Integer> callback)
	{
		SharedPreferences settings = this.getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
		int studentId = settings.getInt(STUDENTID, -1);
		
		if (studentId != -1)
			callback.onResult(studentId);
		else
			this.getNewStudentId("Er is nog geen studentnummer bekend. Voer deze aub hier in:", callback);
	}
	
	private final Callback<Integer> loadTimetableUsingStudentId = new Callback<Integer>()
	{
		public void onResult(Integer result)
		{
			activity.showLoadingView();
			activity.currentState.timetable = null;
			activity.currentState.loader = new StudentTimetableLoader(activity);
			activity.currentState.loader.execute(result);
		}
	};
	
	public void getNewStudentId(String message, final Callback<Integer> callback)
	{		
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(Orientation.VERTICAL);
		this.setContentView(layout);
        
        final TextView text = new TextView(this);
        text.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        text.setText(message);
        text.setTextSize(16);
        text.setGravity(Gravity.CENTER);
        int padding = Utils.dipToPx(8);
        text.setPadding(padding, padding, padding, padding);
        layout.addView(text);
        
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        if (this.currentState.enteredStudentId != null)
        	input.setText(this.currentState.enteredStudentId);
        layout.addView(input);
        
        Button finished = new Button(this);
        finished.setText("Klaar!");
        View.OnClickListener onFinished = new View.OnClickListener()
        {
			public void onClick(View view)
			{
				try
				{
					String enteredText = input.getText().toString();
					activity.currentState.enteredStudentId = enteredText;
					int studentId = Integer.parseInt(enteredText);
					InputMethodManager manager =
							(InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
					manager.hideSoftInputFromWindow(input.getWindowToken(), 0);
					callback.onResult(studentId);
				}
				catch (NumberFormatException e)
				{
					activity.getNewStudentId("De ingevoerde tekst lijkt geen nummer te zijn. Typfoutje?", callback);
				}
			}
        };
        finished.setOnClickListener(onFinished);
        layout.addView(finished);
	}
	
	public void showLoadingView()
	{
		RelativeLayout layout = new RelativeLayout(this);
		TextView loading = new TextView(this);
		loading.setText("Rooster wordt geladen...");
		loading.setTextSize(18);
		RelativeLayout.LayoutParams params =
				new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		layout.addView(loading, params);
		this.setContentView(layout);
	}
	
	public void showTimetableView(StudentTimetable timetable, Day startDay)
	{
		if (startDay == null)
		{
			if (currentState.startDay != null)
				startDay = currentState.startDay;
			else
				startDay = Day.today(TimeUtils.CET);
		}
		if (timetable == null)
		{
			if (currentState.timetable != null)
				timetable = currentState.timetable;
			else
				return;
		}
		
		TimetableView timetableView = new TimetableView(this, timetable, startDay);
		this.setContentView(timetableView);
		this.setTitle("Persoonlijk rooster");
		this.currentState.timetable = timetable;
		this.currentState.startDay = startDay;
		this.currentState.loader = null;
		Editor settingsEditor = this.getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE).edit();
		settingsEditor.putInt(STUDENTID, timetable.student.id);
		settingsEditor.commit();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == ChooseDateActivity.DATE_REQUEST && resultCode == RESULT_OK)
		{
			Day day = ChooseDateActivity.createDayFromintent(data);
			this.showTimetableView(null, day);
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}

	private MenuItem newStudentId = null;
	private MenuItem manualRefresh = null;
	
	public boolean onCreateOptionsMenu(Menu menu)
	{
		this.newStudentId = menu.add("Verander studentnummer");
		this.manualRefresh = menu.add("Handmatig verversen");
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		State currentState = this.currentState;
	    if (item == this.newStudentId)
	    {
	    	if (currentState.timetable != null)
	    		currentState.enteredStudentId = Integer.toString(currentState.timetable.student.id);
	    	
			this.getNewStudentId("Voer hier het nieuwe studentnummer in:", this.loadTimetableUsingStudentId);
			return true;
	    }
	    else if (item == this.manualRefresh)
	    {
	    	if (currentState.timetable != null)
	    	{
	    		activity.currentState.loader = new StudentTimetableLoader(this, true);
				activity.currentState.loader.execute(currentState.timetable.student.id);
	    		return true;
	    	}
	    	else
	    		return false;
	    }
	    else
	    	return super.onOptionsItemSelected(item);
	}
	
	public Object onRetainNonConfigurationInstance()
	{
		State currentState = this.currentState;
		if (currentState.loader != null)
		{
			currentState.loader.setActivity(null);
		}
		return currentState;
	}
}
