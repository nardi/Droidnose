package nl.nardilam.droidnose;

import nl.nardilam.droidnose.datetime.Day;
import nl.nardilam.droidnose.datetime.TimeUtils;
import nl.nardilam.droidnose.gui.LoadingView;
import nl.nardilam.droidnose.gui.TimetableView;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;

public class TimetableActivity extends ContextActivity
{
	public static final String PREFERENCES_FILE = "DroidnoseSettings";
	public static final String STUDENTID = "StudentId";
	
	// Een alias voor extra duidelijkheid in de binnenklassen
	private final TimetableActivity activity = this;

	/*
	 * Dit object wordt gebruikt om de huidige staat van de Activity
	 * in op te slaan in geval van een vernietiging ervan door wat dan ook.
	 * 
	 * loader houdt bij of er een rooster geladen wordt en het bijbehorende object,
	 * timetable bevat het huidige rooster en startDay de dag die tenminste
	 * op het scherm moet komen.
	 */
	public class State
	{
		public boolean isLoading = false;
		public StudentTimetableLoader loader = null;
		public StudentTimetable timetable = null;
		public Day startDay = null;
	}
	public State currentState = new State();
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
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
			this.tryLoadStudentId();
		}
	}
	
	@SuppressWarnings("deprecation")
	private void tryRestoreState()
	{
		State lastState = (State)this.getLastNonConfigurationInstance();
		if (lastState != null)
			this.currentState = lastState;
	}
	
	private void tryLoadStudentId()
	{
		SharedPreferences settings = this.getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
		int studentId = settings.getInt(STUDENTID, -1);
		
		if (studentId != -1)
			this.loadTimetableUsingStudentId(studentId);
		else
			this.getNewStudentId("Er is nog geen studentnummer bekend. Deze kan je hier invullen, als je wilt:");
	}
	
	private void loadTimetableUsingStudentId(int studentId)
	{
		this.showLoadingView();
		this.currentState.timetable = null;
		this.currentState.loader = new StudentTimetableLoader(activity, studentId);
		this.currentState.loader.execute();
	}
	
	public void getNewStudentId(String message)
	{
		this.getNewStudentId(message, null);
	}
	
	public void getNewStudentId(String message, String defaultInput)
	{
		Intent intent = StudentIdActivity.createIntent(this, message, defaultInput);
		this.startActivityForResult(intent, ActivityRequests.STUDENT_ID_REQUEST);
	}
	
	public void showLoadingView()
	{
		this.currentState.isLoading = true;
		this.setTitle("Droidnose");
		this.setContentView(new LoadingView(this, "Rooster wordt geladen..."));
	}
	
	public void showTimetableView()
	{
		this.showTimetableView(null, null);
	}
	
	public void showTimetableView(StudentTimetable timetable)
	{
		this.showTimetableView(timetable, null);
	}
	
	public void showTimetableView(Day startDay)
	{
		this.showTimetableView(null, startDay);
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
		
		this.currentState.isLoading = false;
		this.currentState.timetable = timetable;
		this.currentState.startDay = startDay;
		this.currentState.loader = null;
		
		Editor settingsEditor = this.getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE).edit();
		settingsEditor.putInt(STUDENTID, timetable.getStudent().id);
		settingsEditor.commit();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == ActivityRequests.DATE_REQUEST && resultCode == Activity.RESULT_OK)
		{
			Day day = ChooseDateActivity.createDayFromIntent(data);
			this.showTimetableView(day);
		}
		
		if (requestCode == ActivityRequests.STUDENT_ID_REQUEST)
		{
			if (resultCode == Activity.RESULT_OK)
			{
				int studentId = StudentIdActivity.getStudentIdFromIntent(data);
				this.loadTimetableUsingStudentId(studentId);
			}
			else if (resultCode == Activity.RESULT_CANCELED && currentState.timetable == null)
			{
				this.finish();
			}
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	private MenuItem[] menuItems = new MenuItem[2];
	
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuItem newStudentId = menu.add("Verander studentnummer");
		newStudentId.setOnMenuItemClickListener(new OnMenuItemClickListener()
		{
			public boolean onMenuItemClick(MenuItem item)
			{
				String defaultInput = null;
				if (activity.currentState.timetable != null)
		    		defaultInput = Integer.toString(activity.currentState.timetable.getStudent().id);
		    	
				activity.getNewStudentId("Voer hier het nieuwe studentnummer in:", defaultInput);
				return true;
			}
		});
		this.menuItems[0] = newStudentId;
		
		MenuItem manualRefresh = menu.add("Handmatig verversen");
		manualRefresh.setOnMenuItemClickListener(new OnMenuItemClickListener()
		{
			public boolean onMenuItemClick(MenuItem item)
			{
				State state = activity.currentState;
				if (state.timetable != null)
		    	{
		    		state.loader = new StudentTimetableLoader(activity, state.timetable.getStudent().id, true);
					state.loader.execute();
		    		return true;
		    	}
		    	else
		    		return false;
			}
		});
		this.menuItems[1] = manualRefresh;
        
        /* MenuItem feedback = menu.add("Feedback");
		feedback.setOnMenuItemClickListener(new OnMenuItemClickListener()
		{
			public boolean onMenuItemClick(MenuItem item)
			{
                // launch feedbackactivity
                return false;
			}
		});	
		this.menuItems[2] = feedback; */
		
		return true;
	}
	
	public boolean onPrepareOptionsMenu(Menu menu)
	{		
		for (MenuItem m : this.menuItems)
			m.setVisible(!this.currentState.isLoading);
		
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
	
	protected void onDestroy()
	{        
	    super.onDestroy();
	    
	    if (currentState.timetable != null)
	    {
	    	for (Callback<Timetable.DayEvents> c : currentState.timetable.getUpdateHandlers())
	    	{
	    		if (c instanceof ContextCallback<?, ?>)
	    		{
	    			@SuppressWarnings("unchecked")
					ContextCallback<Timetable.DayEvents, Object> cc = (ContextCallback<Timetable.DayEvents, Object>)c;
	    			Object context = cc.getContext();
	    			if (context instanceof View || context instanceof Activity)
	    			{
	    				cc.setContext(null);
	    			}
	    		}
	    	}
	    	
	    	TimetableSaver.save(currentState.timetable);
	    }
	}
}
