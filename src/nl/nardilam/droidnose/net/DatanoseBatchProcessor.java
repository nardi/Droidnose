package nl.nardilam.droidnose.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import android.util.Log;

import nl.nardilam.droidnose.Callback;

class RequestCallback implements Callback<String>
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

class DatanoseBatchProcessor
{
	private static final long requestDelay = 1000; //ms
	
	private static Map<String, List<Callback<String>>> requestQueue =
			Collections.synchronizedMap(new HashMap<String, List<Callback<String>>>());
	
	private static String httpGetFromPath(String path)
	{
		return "GET " + path + " HTTP/1.1\r\n"
			 + "Host: content.datanose.nl\r\nAccept: application/json\r\n\r\n";
	}
	
	static String doRequest(String path) throws Exception
	{
		RequestCallback request = new RequestCallback();
		addRequest(path, request);
        handleRequests();
		while (request.result == null && request.exception == null);
		if (request.result != null)
			return request.result;
		else
			throw request.exception;
	}
	
	private static long lastRequestTime = 0;
	static void addRequest(String path, Callback<String> onResult) throws Exception
	{
		if (!requestQueue.containsKey(path))
			requestQueue.put(path, new ArrayList<Callback<String>>());
		requestQueue.get(path).add(onResult);
		lastRequestTime = System.currentTimeMillis();
		Log.v("DatanoseBatchProcessor", "Added request: " + path);
	}
	
	public static synchronized void handleRequests() throws Exception
	{
		long waited;
		while ((waited = System.currentTimeMillis() - lastRequestTime) < requestDelay)
			Thread.sleep(requestDelay - waited);
		if (!requestQueue.isEmpty())
		{
			URL serverAddress = new URL("http://content.datanose.nl/Timetable.svc/$batch");
			HttpURLConnection connection = (HttpURLConnection)serverAddress.openConnection();
			String requestBoundary = UUID.randomUUID().toString();
			
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "multipart/mixed; boundary=" + requestBoundary);
			
			List<String> paths = new ArrayList<String>(requestQueue.size());
			paths.addAll(requestQueue.keySet());
			StringBuilder requestBuilder = new StringBuilder();
			Log.v("DatanoseBatchProcessor", "Sending request for:");
			for (String path : paths)
			{
				Log.v("DatanoseBatchProcessor", path);
				requestBuilder.append("--" + requestBoundary + "\r\n");
				requestBuilder.append("Content-Type: application/http\r\nContent-Transfer-Encoding: binary\r\n\r\n");
				requestBuilder.append(httpGetFromPath(path));
			}
			requestBuilder.append("--" + requestBoundary + "--\r\n");
			
			byte[] request = requestBuilder.toString().getBytes();
			connection.setRequestProperty("Content-Length", Integer.toString(request.length));
			
			connection.connect();
			connection.getOutputStream().write(request);
			
	        BufferedReader responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        String line;
	        StringBuilder bodyBuilder /* lol */ = new StringBuilder();
	        while ((line = responseReader.readLine()) != null)
	        {
	            bodyBuilder.append(line);
	            bodyBuilder.append("\n");
	        }
	        Log.v("DatanoseBatchProcessor", "Recieved response");
	        
			String contentType = connection.getHeaderField("Content-Type");
			String responseBoundary = contentType.substring(contentType.indexOf("boundary=") + "boundary=".length());
			
			connection.disconnect();
			
			String[] responses = bodyBuilder.toString().split("--" + responseBoundary + "(--)*");
			for (int i = 0; i < paths.size(); i++)
			{
				String path = paths.get(i);
				List<Callback<String>> callbackList = requestQueue.get(path);
				try
				{
					String response = responses[i + 1];
					int httpStart = response.indexOf("HTTP/");
					if (httpStart != -1)
					{
						String responseContent = response.substring(httpStart);
						String responseBody = responseContent.substring(responseContent.indexOf("\n\n") + 2).trim();
						// String.isEmpty() bestaat pas sinds API 9...
						if (!responseBody.equals(""))
						{
							for (Callback<String> callback : callbackList)
								callback.onResult(responseBody);
						}
						else
							throw new Exception("No HTTP response body");
					}
					else
						throw new Exception("No HTTP response");
				}
				catch (Exception e)
				{
					for (Callback<String> callback : callbackList)
						callback.onError(e);
				}
				requestQueue.remove(path);
			}
		}
	}
}
