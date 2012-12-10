package nl.nardilam.droidnose.net;

import java.net.HttpURLConnection;

public class JSONDatanoseRequest extends DatanoseRequest
{
    public JSONDatanoseRequest(String path)
    {
        super(path);
    }
    
    protected void connectionSetup(HttpURLConnection connection)
    {
    	connection.setRequestProperty("Accept", "application/json");
    }
}

