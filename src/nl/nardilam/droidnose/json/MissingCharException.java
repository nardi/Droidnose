package nl.nardilam.droidnose.json;

public class MissingCharException extends Exception
{
	private static final long serialVersionUID = 1L;
	
	public MissingCharException()
	{
		super();
	}
	
	public MissingCharException(String string)
	{
		super(string);
	}
}
