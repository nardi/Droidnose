package nl.nardilam.droidnose.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import nl.nardilam.droidnose.Callback;

public class DatanoseQuery
{
	private List<String> urls = null;
    private Exception exception = null;

    public void addQuery(String url)
	{
    	if (this.urls == null)
    		this.urls = new ArrayList<String>();
    	this.urls.add(url);
	}

    public Map<String, JSONArray> query() throws Exception
	{
    	if (this.urls == null)
    		this.urls = new ArrayList<String>();
    	Map<String, JSONArray> results = this.queryAll(this.urls);
    	this.urls.clear();
    	return results;
	}
    
    public JSONArray query(String url) throws Exception
	{
    	Map<String, JSONArray> results = this.queryAll(Collections.singletonList(url));
    	return results.get(url);
	}
    
    public Map<String, JSONArray> queryAll(String... urls) throws Exception
	{
    	Map<String, JSONArray> results = this.queryAll(Arrays.asList(urls));
    	return results;
	}

	public synchronized Map<String, JSONArray> queryAll(Collection<String> urls) throws Exception
	{
        final Map<String, JSONArray> results = new HashMap<String, JSONArray>(urls.size());
        for (final String url : urls)
        {
            DatanoseBatchProcessor.addRequest(url, new Callback<String>()
            {
                public void onResult(String result)
                {
                    try
					{
						results.put(url, parseResult(result));
					}
                    catch (Exception e)
					{
						this.onError(e);
					}
                }
                
				public void onError(Exception e)
				{
					exception = e;
				}
			});
        }
        while (results.size() < urls.size() && exception == null)
        	DatanoseBatchProcessor.handleRequests();
        if (exception != null)
        {
        	Exception e = exception;
        	exception = null;
    		throw e;
        }
        return results;
	}
    
    private JSONArray parseResult(String json) throws IOException, JSONException
	{
        JSONObject response = (JSONObject)new JSONTokener(json).nextValue();

        Object d = response.get("d");

        if (d instanceof JSONArray)
        {
            return (JSONArray)d;
        }
        else if (d instanceof JSONObject)
        {
            JSONObject dObj = (JSONObject)d;
            @SuppressWarnings("unchecked")
			Iterator<String> keyIterator = dObj.keys();
            while (keyIterator.hasNext())
            {
                Object value = dObj.get(keyIterator.next());
                if (value instanceof JSONArray)
                {
                    return (JSONArray)value;
                }
            }
        }
        return new JSONArray(Collections.singletonList(d));
    }
}
