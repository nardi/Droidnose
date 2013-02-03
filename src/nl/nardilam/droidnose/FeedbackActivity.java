package nl.nardilam.droidnose;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class FeedbackActivity extends Activity
{
	private static final String serviceURL = "http://nardilam.nl/dnfb.php";

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		Button submit = new Button(this);
		submit.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				try
				{
					String fb_type = URLEncoder.encode("", "UTF-8");
					String fb_detail = URLEncoder.encode("", "UTF-8");
					String query = "fb_type=" + fb_type + "&fb_detail=" + fb_detail;
					URL serverAddress = new URL(serviceURL);
					HttpURLConnection connection = (HttpURLConnection)serverAddress.openConnection();
					connection.setRequestMethod("POST");
					connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
					connection.setRequestProperty("Content-Length", Integer.toString(query.length()));
					connection.setDoOutput(true);
					OutputStream os = connection.getOutputStream();
					OutputStreamWriter writer = new OutputStreamWriter(os);
					writer.write(query);
			        connection.connect();
			        InputStream is = connection.getInputStream(); 
			        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			        String response = reader.readLine().trim();
				} 
				catch (Exception e)
				{
					//show error
				}
			}
		});
	}
}
