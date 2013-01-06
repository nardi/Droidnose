package nl.nardilam.droidnose;

import android.app.Activity; 
import android.os.Bundle;

public class ContextActivity extends Activity
{
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		try
        {
        	Utils.getContext();
        }
        catch (ContextNotSetException e)
        {
        	Utils.setContext(this.getApplicationContext());
        }
	}
}
