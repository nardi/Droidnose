package nl.nardilam.droidnose.net;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import nl.nardilam.droidnose.json.JSONException;
import nl.nardilam.droidnose.json.JSONParser;
import nl.nardilam.droidnose.json.JSONValue;

public class DatanoseQuery
{
	private final String url;
	
	public DatanoseQuery(String url)
	{
		this.url = url;
	}
	
	public List<JSONValue> query() throws IOException, JSONException
	{
		DatanoseRequest req = new JSONDatanoseRequest(url);
		Map<String, JSONValue> resp = JSONParser.parse(req.getBodyStream()).asObject();
		req.finished();
		JSONValue d = resp.get("d");
		if (d.isArray())
			return d.asArray();
		else if (d.isObject())
		{
			Map<String, JSONValue> dObj = d.asObject();
			for (JSONValue value : dObj.values())
			{
				if (value.isArray())
					return value.asArray();
			}
		}
		return Collections.singletonList(d);
	}
}
