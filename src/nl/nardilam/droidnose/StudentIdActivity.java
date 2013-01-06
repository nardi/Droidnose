package nl.nardilam.droidnose;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.text.InputType;

public class StudentIdActivity extends ContextActivity
{
	public static final String MESSAGE = "nl.nardilam.droidnose.Message";
	public static final String DEFAULT_INPUT = "nl.nardilam.droidnose.DefaultInput";
	public static final String STUDENT_ID = "nl.nardilam.droidnose.StudentId";
	
	private static final String DEFAULT_MESSAGE = "Voer hier het studentnummer in:";
	
	private final StudentIdActivity activity = this;
	
	private RelativeLayout layout = null;
	
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    
        this.layout = new RelativeLayout(this);
        this.setContentView(this.layout);
        
        int margin = Utils.dpToPx(0.02f * Utils.getDisplayMetrics().widthPixels);
        
        final TextView text = new TextView(this);
        text.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        text.setText(this.getMessage());
        text.setTextSize(16);
        text.setGravity(Gravity.CENTER);
        //text.setPadding(padding, 2 * padding, padding, padding);
        text.setId(1);
        RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        textParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        textParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        textParams.setMargins(margin, margin, margin, margin);
        this.layout.addView(text, textParams);
        
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        input.setText(this.getDefaultInput());
        //input.setPadding(padding, 0, padding, 0);
        input.setId(2);
        RelativeLayout.LayoutParams inputParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        inputParams.addRule(RelativeLayout.BELOW, text.getId());
        inputParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        inputParams.setMargins(margin, 0, margin, 0);
        this.layout.addView(input, inputParams);
        
        Button finished = new Button(this);
        finished.setText("Klaar!");
        RelativeLayout.LayoutParams finishedParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        finishedParams.addRule(RelativeLayout.BELOW, input.getId());
        finishedParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        
        View.OnClickListener onFinished = new View.OnClickListener()
        {
			public void onClick(View view)
			{
				try
				{
					String enteredText = input.getText().toString();
					int studentId = Integer.parseInt(enteredText);
					
					activity.hideOnScreenKeyboard(input.getWindowToken());
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
        this.layout.addView(finished, finishedParams);
    }
    
    private void hideOnScreenKeyboard(IBinder window)
	{
    	InputMethodManager manager = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		manager.hideSoftInputFromWindow(window, 0);
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
