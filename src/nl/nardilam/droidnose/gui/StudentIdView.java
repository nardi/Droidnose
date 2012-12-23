package nl.nardilam.droidnose.gui;

import nl.nardilam.droidnose.Callback;
import nl.nardilam.droidnose.Orientation;
import nl.nardilam.droidnose.TimetableActivity;
import nl.nardilam.droidnose.Utils;
import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/* public class StudentIdView extends LinearLayout
{
	public StudentIdView(final TimetableActivity context, String message, final Callback<Integer> callback)
	{
		super(context);
		
this.setOrientation(Orientation.VERTICAL);
        
        final TextView text = new TextView(context);
        text.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        text.setText(message);
        text.setTextSize(16);
        text.setGravity(Gravity.CENTER);
        int padding = Utils.dpToPx(8);
        text.setPadding(padding, padding, padding, padding);
        this.addView(text);
        
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        if (context.currentState.enteredStudentId != null)
        	input.setText(context.currentState.enteredStudentId);

        input.addTextChangedListener(new TextWatcher()
		{
			public void afterTextChanged(Editable s)
			{
				context.currentState.enteredStudentId = s.toString();
			}
			
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
			}
			
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}
		});
        
        this.addView(input);
        
        Button finished = new Button(context);
        finished.setText("Klaar!");
        
        View.OnClickListener onFinished = new View.OnClickListener()
        {
			public void onClick(View view)
			{
				try
				{
					String enteredText = input.getText().toString();
					int studentId = Integer.parseInt(enteredText);

					InputMethodManager manager =
							(InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
					manager.hideSoftInputFromWindow(input.getWindowToken(), 0);
					
					callback.onResult(studentId);
				}
				catch (NumberFormatException e)
				{
					text.setText("De ingevoerde tekst lijkt geen nummer te zijn. Typfoutje?");
				}
			}
        };
        
        finished.setOnClickListener(onFinished);
        this.addView(finished);
	}
} */
