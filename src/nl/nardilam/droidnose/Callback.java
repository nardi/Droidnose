package nl.nardilam.droidnose;

/*
 * Een simpele listener-interface om iets als callback-functies te maken
 */
public interface Callback<ResultType>
{
	public void onResult(ResultType result);
}
