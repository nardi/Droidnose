package nl.nardilam.droidnose;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class ContextActivity extends FragmentActivity
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
