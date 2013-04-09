package nl.nardilam.droidnose;

public abstract class Function<ArgumentType, ResultType>
{	
	public abstract ResultType call(ArgumentType arg) throws Exception;
}
