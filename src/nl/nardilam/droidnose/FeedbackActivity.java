package nl.nardilam.droidnose;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

public class FeedbackActivity extends Activity
{
	private static final String serviceURL = "http://nardilam.nl/dnfb.php";
	private static final String successResponse = "success";

	private static final String message = "Ben je helemaal tevreden met deze prachtige applicatie"
			+ " en wil je dit heel graag aan iedereen laten weten? Schrijf hieronder dan maar een" 
			+ " berichtje, dan geef ik het wel door.\nOok als je wel iets op te merken hebt aan dit"
			+ " hemels stukje programmeerwerk kan je hier wat achterlaten en krijg je zo snel mogelijk"
			+ " bericht terug zodat je tenminste weet waarom je nu precies fout zit.";
	private static final String italicsString = "waarom";
	
	private final FeedbackActivity activity = this;
	private LinearLayout layout = null;
	private TextView messageText = null;
	private ProgressBar loading = null;
	private Button submit = null;
	private LayoutParams submitParams = null;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		int margin = Utils.dpToPx(0.02f * Utils.getDisplayMetrics().widthPixels);
		
		SpannableStringBuilder prettyMessage = new SpannableStringBuilder(message);
		int italicsFrom = message.indexOf(italicsString), italicsTo = italicsFrom + italicsString.length();
        prettyMessage.setSpan(new StyleSpan(Typeface.ITALIC), italicsFrom, italicsTo, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		this.layout = new LinearLayout(this);
		this.layout.setOrientation(Orientation.VERTICAL);
        this.setContentView(this.layout);
        
        ScrollView sv = new ScrollView(this);
        LayoutParams scrollParams = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
        scrollParams.weight = 1;
        //this.layout.addView(sv, scrollParams);
        
        LinearLayout innerLayout = new LinearLayout(this);
		innerLayout.setOrientation(Orientation.VERTICAL);
		LayoutParams ilParams = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
		ilParams.weight = 1;
		sv.addView(innerLayout, ilParams);
        
        this.messageText = new TextView(this);
        messageText.setText(prettyMessage);
        messageText.setTextSize(14);
        messageText.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);        
        LayoutParams textParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        textParams.weight = 0;
        textParams.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        textParams.setMargins(margin, margin, margin, margin/2);
        this.layout.addView(messageText, textParams);
        
        final EditText sender = new EditText(this);
        sender.setInputType(InputType.TYPE_CLASS_TEXT);
        sender.setHint("Je e-mailadres (als je iets terug wilt horen)");
        LayoutParams senderParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        senderParams.weight = 0;
        senderParams.setMargins(margin, 0, margin, 0);
        this.layout.addView(sender, senderParams);
        
        final EditText detail = new EditText(this);
        detail.setHint("Wat wil je zeggen?");
        detail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        detail.setGravity(Gravity.TOP);
        LayoutParams detailParams = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
        detailParams.weight = 0.5f;
        detailParams.setMargins(margin, 0, margin, 0);
        this.layout.addView(detail, detailParams);
        
        loading = new ProgressBar(activity);
		loading.setIndeterminate(true);
        
        submit = new Button(this);
        submit.setText("Versturen");
        submitParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        submitParams.weight = 0;
        submitParams.setMargins(0, 0, 0, margin);
        submitParams.gravity = Gravity.CENTER;
        
		submit.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				activity.hideOnScreenKeyboard(sender.getWindowToken());
				activity.hideOnScreenKeyboard(detail.getWindowToken());
				layout.removeView(submit);
		        layout.addView(loading, submitParams);
				new FeedbackSender(sender.getText().toString(), detail.getText().toString()).execute();
			}
		});
        
        this.layout.addView(submit, submitParams);
	}
	
	private void hideOnScreenKeyboard(IBinder window)
	{
    	InputMethodManager manager = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);
		manager.hideSoftInputFromWindow(window, 0);
	}
	
	private class FeedbackSender extends AsyncTask<Void, String, String>
	{
		private final String type;
		private final String detail;
		
		public FeedbackSender(String type, String detail)
		{
			this.type = type;
			this.detail = detail;
		}
		
		protected String doInBackground(Void... nothings)
		{
			try
			{
				String fb_type = URLEncoder.encode(this.type, "UTF-8");
				String fb_detail = URLEncoder.encode(this.detail, "UTF-8");
				String query = "fb_type=" + fb_type + "&fb_detail=" + fb_detail;
				
				URL serverAddress = new URL(serviceURL);
				HttpURLConnection connection = (HttpURLConnection)serverAddress.openConnection();
				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				connection.setRequestProperty("Content-Length", Integer.toString(query.length()));
				connection.setDoOutput(true);
				
		        connection.connect();
		        connection.getOutputStream().write(query.getBytes());
		        
		        InputStream is = connection.getInputStream(); 
		        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		        String response = reader.readLine().trim();
		        if (!response.equals(successResponse))
		        	throw new Exception();
		        else
		        	return response;
			} 
			catch (Exception e)
			{
				return null;
			}
		}
		
		protected void onPostExecute(String response)
	    {
	    	if (response != null)
	    	{
    			Toast.makeText(activity.getApplicationContext(), "Je feedback is verzonden!", Toast.LENGTH_LONG).show();
	    		activity.finish();
	    	}
	    	else
	    	{
	    		messageText.setHeight(messageText.getHeight());
	    		messageText.setGravity(Gravity.CENTER);
	    		messageText.setText("Er ging iets mis met het versturen van je feedback.\n"
						   		  + "Misschien moet je het later nog eens proberen?");
	    		layout.removeView(loading);
		        layout.addView(submit, submitParams);
	    	}
	    }
	}
}
