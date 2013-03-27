package nl.nardilam.droidnose;

import android.os.AsyncTask;

public class AsyncCall<ArgumentType, ResultType>
{
	private final Function<ArgumentType, ResultType> function;
	private final Callback<ResultType> callback;
	
	public AsyncCall(Function<ArgumentType, ResultType> function, Callback<ResultType> callback)
	{
		this.function = function;
		this.callback = callback;
	}
	
	public void call(ArgumentType arg)
	{
		new AsyncCaller(arg).execute();
	}
	
	private class AsyncCaller extends AsyncTask<Void, Void, ResultType>
	{
		private final ArgumentType arg;
		public AsyncCaller(ArgumentType arg)
		{
			this.arg = arg;
		}
		
		private Exception exception = null;
		protected ResultType doInBackground(Void... nothings)
		{
			try
			{
				ResultType result = function.call(arg);
				return result;
			}
			catch (Exception e)
			{
				this.exception = e;
				return null;
			}
		}
		
		protected void onPostExecute(ResultType result)
	    {
	    	if (this.exception != null)
	    	{
	    		callback.onError(this.exception);
	    	}
	    	else
	    	{
	    		callback.onResult(result);
	    	}
	    }
	}
}
