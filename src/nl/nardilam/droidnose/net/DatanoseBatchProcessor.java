package nl.nardilam.droidnose.net;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import nl.nardilam.droidnose.Callback;

class DatanoseBatchProcessor
{
	private static final long requestDelay = 5000; //ms
	
	private static Map<String, Callback<String>> requestQueue =
			Collections.synchronizedMap(new HashMap<String, Callback<String>>());
	
	private static String httpGetFromPath(String path)
	{
		return "GET /Timetable.svc/" + path + " HTTP/1.1\r\n"
			 + "Host: content.datanose.nl\r\nAccept: application/json\r\n\r\n";
	}
	
	private static long lastRequestTime = 0;
	static void addRequest(String path, Callback<String> onResult)
	{
		requestQueue.put(path, onResult);
		lastRequestTime = System.currentTimeMillis();
		while (System.currentTimeMillis() - lastRequestTime < requestDelay);
		// actually make and do request
	}
	
	private class RequestCallback implements Callback<String>
	{
		String result = null;
		Exception exception = null;
		
		public void onResult(String result)
		{
			this.result = result;
		}

		public void onError(Exception e)
		{
			this.exception = e;
		}	
	}
	
	static String doRequest(String path) throws Exception
	{
		RequestCallback request = new RequestCallback();
		addRequest(path, request);
		while (request.result == null && request.exception == null);
		if (request.result != null)
			return request.result;
		else
			throw request.exception;
	}
}
