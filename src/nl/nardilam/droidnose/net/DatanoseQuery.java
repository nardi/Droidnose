package nl.nardilam.droidnose.net;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.nardilam.droidnose.Callback;
import nl.nardilam.droidnose.json.JSONParser;
import nl.nardilam.droidnose.json.JSONValue;

public class DatanoseQuery
{
	private final DatanoseQuery datanoseQuery = this;
	private final String url;
	
	public DatanoseQuery(String url)
	{
		this.url = url;
	}
	
	private List<JSONValue> result = null;
	private Exception exception = null;
	public List<JSONValue> query() throws Exception
	{
		this.query(new Callback<List<JSONValue>>()
		{
			public void onResult(List<JSONValue> result)
			{
				datanoseQuery.result = result;
			}

			public void onError(Exception e)
			{
				datanoseQuery.exception = e;
			}
		});
		
		while(this.result == null && this.exception == null);
		
		List<JSONValue> result = this.result;
		Exception exception = this.exception;
		this.result = null;
		this.exception = null;
		
		if (exception != null)
			throw exception;
		else
			return result;
	}
	
	public void query(Callback<List<JSONValue>> callback)
	{
		try
		{
			DatanoseRequest request = new DatanoseRequest(url);
			Map<String, JSONValue> response = JSONParser.parse(request.getBodyStream()).asObject();
			request.finished();
			
			JSONValue d = response.get("d");
			
			if (d.isArray())
			{
				callback.onResult(d.asArray());
			}
			else if (d.isObject())
			{
				Map<String, JSONValue> dObj = d.asObject();
				for (JSONValue value : dObj.values())
				{
					if (value.isArray())
						callback.onResult(value.asArray());
				}
			}
			else
			{
				callback.onResult(Collections.singletonList(d));
			}
		}
		catch (Exception e)
		{
			callback.onError(e);
		}
	}
}
