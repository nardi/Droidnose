package nl.nardilam.droidnose.net;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class DatanoseRequest
{
	private static final String serviceURL = "http://content.datanose.nl/Timetable.svc/";
	
    private HttpURLConnection connection = null;
    private URL serverAddress = null;
    private String path = null;
    private String result = null;
    
    public DatanoseRequest(String path)
    {
        this.path = path;
    }
    
    private boolean requestDone = false;
    private void doRequest() throws IOException
    {
        /* if (!requestDone)
        {
            try
            {
                serverAddress = new URL(serviceURL + path);
                connection = (HttpURLConnection)serverAddress.openConnection(); 
                connection.setRequestProperty("Accept", "application/json");
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
        } */
    	
    	if (!requestDone)
        {
            try
            {
                this.result = DatanoseBatchProcessor.doRequest(path);
                requestDone = true;
            }
            catch(Exception e)
            {
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
        return new ByteArrayInputStream(this.result.getBytes());
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
