package nl.nardilam.droidnose.net;

import java.net.HttpURLConnection;

public class XMLDatanoseRequest extends DatanoseRequest
{
    public XMLDatanoseRequest(String path)
    {
        super(path);
    }
    
    protected void connectionSetup(HttpURLConnection connection)
    {
    	connection.setRequestProperty("Accept", "application/atom+xml,application/xml");
    }
}
