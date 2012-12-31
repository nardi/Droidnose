package nl.nardilam.droidnose;

public abstract class ContextCallback<ResultType, ContextType> implements Callback<ResultType>
{
	private ContextType context;
	
	public final void setContext(ContextType context)
	{
		this.context = context;
	}

	@Override
	public final void onResult(ResultType result)
	{
		if (this.context != null)
			this.onResult(result, this.context);
	}

	protected abstract void onResult(ResultType result, ContextType context);
}
