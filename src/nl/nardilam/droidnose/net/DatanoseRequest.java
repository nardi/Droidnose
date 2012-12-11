package nl.nardilam.droidnose.net;

import java.net.*;
import java.io.*;
import java.util.*;

public abstract class DatanoseRequest
{
	private static final String serviceURL = "http://content.datanose.nl/Timetable.svc/";
	
    private HttpURLConnection connection = null;
    private URL serverAddress = null;
    private String path = null;

    public DatanoseRequest(String path)
    {
        this.path = path;
    }
    
    protected abstract void connectionSetup(HttpURLConnection connection) throws IOException;
    
    private boolean requestDone = false;
    private void doRequest() throws IOException
    {
        if (!requestDone)
        {
            try
            {
                serverAddress = new URL(serviceURL + path);
                connection = (HttpURLConnection)serverAddress.openConnection(); 
                connectionSetup(connection);
                connection.connect();
                requestDone = true;
            }
            catch(Exception e)
            {
                if(connection != null)
                    connection.disconnect();
                IOException ioException = new IOException();
                ioException.initCause(e);
                throw ioException;
            }
        }
    }
    
    public Map<String, List<String>> getHeaders() throws IOException
    {
        if (!requestDone)
            doRequest();
        return connection.getHeaderFields();
    }
    
    public InputStream getBodyStream() throws IOException
    {
        if (!requestDone)
            doRequest();
        return connection.getInputStream();
    }
    
    public String getBody() throws IOException
    {
        InputStream in = getBodyStream();
        BufferedReader rd = new BufferedReader(new InputStreamReader(in));
        String line;
        String body = "";
        
        while ((line = rd.readLine()) != null)
            body += line + "\n";
        return body;
    }
    
    public void finished()
    {
    	if (connection != null)
    		connection.disconnect();
    }
}
