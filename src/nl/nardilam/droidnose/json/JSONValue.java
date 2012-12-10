package nl.nardilam.droidnose.json;

import java.util.List;
import java.util.Map;

/*
 * All possible JSON value types
 */

enum JSONType
{
	String,  // -> String
    Number,  // -> double
    Boolean, // -> boolean
    Array,   // -> List<JSONValue>
    Object   // -> Map<String, JSONValue>
}

@SuppressWarnings("unchecked")
public class JSONValue
{
	private final JSONType type;
	private final Object data;
	
	/*
     * Construction methods
     */
	
	private JSONValue(JSONType type, Object data)
	{
		this.type = type;
		this.data = data;
	}
	
	public JSONValue(String data)
	{
		this(JSONType.String, data);
	}
	
	public JSONValue(double data)
	{
		this(JSONType.Number, data);
	}
	
	public JSONValue(boolean data)
	{
		this(JSONType.Boolean, data);
	}
	
	public JSONValue(List<JSONValue> data)
	{
		this(JSONType.Array, data);
	}
	
	public JSONValue(Map<String, JSONValue> data)
	{
		this(JSONType.Object, data);
	}
	
	/*
     * Methods for checking and retrieving the data in the right format)
     */
	
	public boolean isString()
	{
		return type == JSONType.String;
	}
	
	public String asString()
	{
		return (String)data;
	}
	
	public boolean isNumber()
	{
		return type == JSONType.Number;
	}
	
	public double asNumber()
	{
		return (Double)data;
	}
	
	public boolean isBoolean()
	{
		return type == JSONType.Boolean;
	}
	
	public boolean asBoolean()
	{
		return (Boolean)data;
	}
	
	public boolean isArray()
	{
		return type == JSONType.Array;
	}
	
	public List<JSONValue> asArray()
	{
		return (List<JSONValue>)data;
	}
	
	public boolean isObject()
	{
		return type == JSONType.Object;
	}
	
	public Map<String, JSONValue> asObject()
	{
		return (Map<String, JSONValue>)data;
	}
	
	public String toString()
	{
		return data.toString();
	}
}

