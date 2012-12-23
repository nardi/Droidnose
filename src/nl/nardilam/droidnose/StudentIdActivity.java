package nl.nardilam.droidnose;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.text.InputType;

public class StudentIdActivity extends Activity
{
	public static final String MESSAGE = "nl.nardilam.droidnose.Message";
	public static final String DEFAULT_INPUT = "nl.nardilam.droidnose.DefaultInput";
	public static final String STUDENT_ID = "nl.nardilam.droidnose.StudentId";
	
	private static final String DEFAULT_MESSAGE = "Voer hier het studentnummer in:";
	
	private final StudentIdActivity activity = this;
	
	private LinearLayout layout = null;
	
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    
        this.layout = new LinearLayout(this);
        this.layout.setOrientation(Orientation.VERTICAL);
        this.setContentView(this.layout);
        
        int padding = Utils.dpToPx(8);
        
        final TextView text = new TextView(this);
        text.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        text.setText(this.getMessage());
        text.setTextSize(16);
        text.setGravity(Gravity.CENTER);
        text.setPadding(padding, 2 * padding, padding, padding);
        this.layout.addView(text);
        
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        input.setText(this.getDefaultInput());
        input.setPadding(padding, 0, padding, 0);
        this.layout.addView(input);
        
        Button finished = new Button(this);
        finished.setText("Klaar!");
        finished.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        
        View.OnClickListener onFinished = new View.OnClickListener()
        {
			public void onClick(View view)
			{
				try
				{
					String enteredText = input.getText().toString();
					int studentId = Integer.parseInt(enteredText);
					
					/*
					 * Dit verbergt het schermtoetsenbord, indien nodig.
					 */
					InputMethodManager manager =
							(InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
					manager.hideSoftInputFromWindow(input.getWindowToken(), 0);
					
					activity.setResult(Activity.RESULT_OK, createIntentFromStudentId(studentId));
					activity.finish();
				}
				catch (NumberFormatException e)
				{
					text.setText("De ingevoerde tekst lijkt geen nummer te zijn. Typfoutje?");
				}
			}
        };
        
        finished.setOnClickListener(onFinished);
        this.layout.addView(finished);
    }
    
    public static Intent createIntent(Context context, String message, String defaultInput)
    {
    	Intent intent = new Intent(context, StudentIdActivity.class);
    	if (message != null)
    		intent.putExtra(MESSAGE, message);
    	if (defaultInput != null)
    		intent.putExtra(DEFAULT_INPUT, defaultInput);
    	return intent;
    }
    
    private static Intent createIntentFromStudentId(int studentId)
    {
    	Intent intent = new Intent();
    	intent.putExtra(STUDENT_ID, studentId);
    	return intent;
    }
    
    public static int getStudentIdFromIntent(Intent intent)
    {
    	return intent.getIntExtra(STUDENT_ID, -1);
    }
    
    public String getMessage()
    {
    	String message = this.getIntent().getStringExtra(MESSAGE);
        if (message == null)
        	message = DEFAULT_MESSAGE;
        return message;
    }
    
    public String getDefaultInput()
    {
    	String defaultInput = this.getIntent().getStringExtra(DEFAULT_INPUT);
        if (defaultInput == null)
        	defaultInput = "";
        return defaultInput;
    }
}
