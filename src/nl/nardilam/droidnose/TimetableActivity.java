package nl.nardilam.droidnose;

import nl.nardilam.droidnose.datetime.Day;
import nl.nardilam.droidnose.datetime.Time;
import nl.nardilam.droidnose.datetime.TimeUtils;
import nl.nardilam.droidnose.gui.LoadingView;
import nl.nardilam.droidnose.gui.StudentIdView;
import nl.nardilam.droidnose.gui.TimetableView;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;

public class TimetableActivity extends Activity
{
	public static final String PREFERENCES_FILE = "DroidnoseSettings";
	public static final String STUDENTID = "StudentId";
	
	// Een alias voor extra duidelijkheid in de binnenklassen
	private final TimetableActivity activity = this;

	/*
	 * Dit object wordt gebruikt om de huidige staat van de Activity
	 * in op te slaan in geval van een vernietiging ervan door wat dan ook.
	 * 
	 * enteredStudentId houdt de ingevulde tekst in het studentnummervakje bij,
	 * loader houdt bij of er een rooster geladen wordt en het bijbehorende object,
	 * timetable bevat het huidige rooster en startDay de dag die tenminste
	 * op het scherm moet komen.
	 */
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
	
	/*
	 * Deze methode wordt bij hervatten van de applicatie aangeroepen
	 * om indien nodig het rooster opnieuw te downloaden 
	 */
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
		this.setContentView(new StudentIdView(this, message, callback));
	}
	
	public void showLoadingView()
	{
		this.setContentView(new LoadingView(this));
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
	
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuItem newStudentId = menu.add("Verander studentnummer");
		newStudentId.setOnMenuItemClickListener(new OnMenuItemClickListener()
		{
			public boolean onMenuItemClick(MenuItem item)
			{
				if (currentState.timetable != null)
		    		currentState.enteredStudentId = Integer.toString(currentState.timetable.student.id);
		    	
				activity.getNewStudentId("Voer hier het nieuwe studentnummer in:",
						activity.loadTimetableUsingStudentId);
				return true;
			}
		});
		
		MenuItem manualRefresh = menu.add("Handmatig verversen");
		manualRefresh.setOnMenuItemClickListener(new OnMenuItemClickListener()
		{
			public boolean onMenuItemClick(MenuItem item)
			{
				if (currentState.timetable != null)
		    	{
		    		activity.currentState.loader = new StudentTimetableLoader(activity, true);
					activity.currentState.loader.execute(currentState.timetable.student.id);
		    		return true;
		    	}
		    	else
		    		return false;
			}
		});
		
		return true;
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
