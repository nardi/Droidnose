package nl.nardilam.droidnose.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JSONParser
{
	public static JSONValue parse(InputStream in) throws IOException, JSONException
	{
		return parse(new InputStreamReader(in));
	}
	
	public static JSONValue parse(Reader reader) throws IOException, JSONException
	{
		return parse(new AwesomeReader(reader));
	}
	
	public static JSONValue parse(AwesomeReader reader) throws IOException, JSONException
	{
		return readValue(reader);
	}

	private static JSONValue readValue(AwesomeReader reader) throws IOException, JSONException
	{
		/*
		 * Try to find a control character, then read the value using a different method,
		 * or try to parse the read data if there's an 'ending' character (, ] }).
		 */
		StringBuffer otherData = new StringBuffer();
		try
		{
			char readChar = reader.readUntil(otherData, '"', '\'', '[', '{', ',', ']', '}');
			switch(readChar)
			{
	        	case '"':
	        	case '\'':
	        		return new JSONValue(readString(reader));
	        	case '[':
	        		return new JSONValue(readArray(reader));
	        	case '{':
	        		return new JSONValue(readObject(reader));
	        	case ',':
	        	case ']':
	        	case '}':
	        		return tryParseMiscValue(otherData.toString());
	        }
		}
		/*
		 * If EOF is reached before any of the chars are found, just try to parse the whole thing.
		 */
		catch (MissingCharException e)
		{
			return tryParseMiscValue(otherData.toString());
		}
		
		/*
		 * The program should never reach this, but if it does, same thing.
		 */
		return tryParseMiscValue(otherData.toString());
	}
	
	/*
	 * Tries to parse the given string as Boolean, Number, or finally String
	 */
	private static JSONValue tryParseMiscValue(String json) throws JSONException
	{
		String value = json.toString().trim();
		if (value.equals(""))
			throw new JSONException("No valid JSON value found");
		
		if (value.toLowerCase().equals("true"))
		{
			return new JSONValue(true);
		}
		else if(value.toLowerCase().equals("false"))
		{
			return new JSONValue(false);
		}
		try
		{
			return new JSONValue(Double.parseDouble(value));
		}
		catch (NumberFormatException e)
		{
			return new JSONValue(value);
		}
	}
	
	private static String readString(AwesomeReader reader) throws IOException, JSONException
	{
		char stringDelimiter;
		try
		{
			 stringDelimiter = reader.readPast('"', '\'');
		}
		catch (MissingCharException e)
		{
			throw new JSONException("No valid string found");
		}
		
		StringBuffer string = new StringBuffer();
		try
		{
			reader.readPast(string, stringDelimiter);
		}
		catch (MissingCharException e)
		{
			throw new JSONException("EOF reached before string end (\")");
		}
		
		return string.toString();
	}
	
	private static List<JSONValue> readArray(AwesomeReader reader) throws IOException, JSONException
	{
		try
		{
			reader.readPast('[');
		}
		catch (MissingCharException e)
		{
			throw new JSONException("No valid array found");
		}
		
		List<JSONValue> array = new ArrayList<JSONValue>();
		char readChar;
		do
		{
			tryReadArrayValue(reader, array);
			try
			{
				readChar = reader.readPast(',', ']');
			}
			catch (MissingCharException e)
			{
				throw new JSONException("EOF reached before array end (])");
			}
		} while (readChar != ']');
		
		return array;
	}
	
	private static void tryReadArrayValue(AwesomeReader reader, List<JSONValue> array) throws IOException
	{
		try
		{
    		array.add(readValue(reader));
		}
    	catch (JSONException e) { } // Something went wrong? Skip value
	}
	
	private static Map<String, JSONValue> readObject(AwesomeReader reader) throws IOException, JSONException
	{
		try
		{
			reader.readPast('{');
		}
		catch (MissingCharException e)
		{
			throw new JSONException("No valid object found");
		}
		
		Map<String, JSONValue> object = new HashMap<String, JSONValue>();
		char readChar;
		do
		{
			tryReadObjectValue(reader, object);
			try
			{
				readChar = reader.readPast(',', '}');
			}
			catch (MissingCharException e)
			{
				throw new JSONException("EOF reached before object end (})");
			}
		} while (readChar != '}');
		
		return object;
	}
	
	private static void tryReadObjectValue(AwesomeReader reader, Map<String, JSONValue> object) throws IOException, JSONException
	{
		try
		{
			String key = readValue(reader).toString();
			reader.readPast(':');
			JSONValue value = readValue(reader);
			object.put(key, value);
		}
		catch (Exception e) // Something went wrong? Skip value
    	{
			try
			{
				reader.readUntil(',', '}');
			}
			catch (MissingCharException e1)
			{
				throw new JSONException("EOF reached before object end (})");
			}
    	}
	}
}
