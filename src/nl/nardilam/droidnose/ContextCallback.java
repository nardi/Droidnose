package nl.nardilam.droidnose;

public abstract class ContextCallback<ResultType, ContextType> implements Callback<ResultType>
{
	private ContextType context;
	
	public final void setContext(ContextType context)
	{
		this.context = context;
	}
	
	public final ContextType getContext()
	{
		return this.context;
	}

	@Override
	public final void onResult(ResultType result)
	{
		if (this.context != null)
			this.onResult(result, this.context);
	}

	protected abstract void onResult(ResultType result, ContextType context);
	
	@Override
	public final void onError(Exception e)
	{
		if (this.context != null)
			this.onError(e, this.context);
	}

	protected abstract void onError(Exception e, ContextType context);
}
