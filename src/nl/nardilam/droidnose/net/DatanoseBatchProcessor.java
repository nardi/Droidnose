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
	private static final long requestDelay = 5000; //ms
	
	private static Map<String, Callback<String>> requestQueue =
			Collections.synchronizedMap(new HashMap<String, Callback<String>>());
	
	private static String httpGetFromPath(String path)
	{
		return "GET /Timetable.svc/" + path + " HTTP/1.1\r\n"
			 + "Host: content.datanose.nl\r\nAccept: application/json\r\n\r\n";
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
	
	private static long lastRequestTime = 0;
	static void addRequest(String path, Callback<String> onResult) throws Exception
	{
		requestQueue.put(path, onResult);
		lastRequestTime = System.currentTimeMillis();
		while (System.currentTimeMillis() - lastRequestTime < requestDelay);
		handleRequests();
	}
	
	private static synchronized void handleRequests() throws Exception
	{
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
			for (String path : paths)
			{
				requestBuilder.append("--" + requestBoundary + "\r\n");
				requestBuilder.append("Content-Type: application/http \r\nContent-Transfer-Encoding:binary\r\n\r\n");
				requestBuilder.append(httpGetFromPath(path));
			}
			requestBuilder.append("--" + requestBoundary + "--\r\n");
			
			byte[] request = requestBuilder.toString().getBytes();
			connection.setRequestProperty("Content-Length", Integer.toString(request.length));
			
			connection.connect();
			connection.getOutputStream().write(request);
			
	        BufferedReader responseReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        String line;
	        String body = "";
	        while ((line = responseReader.readLine()) != null)
	            body += line + "\n";
	        
			String contentType = connection.getHeaderField("Content-Type");
			String responseBoundary = contentType.substring(contentType.indexOf("boundary=") + "boundary=".length());
			
			connection.disconnect();
			
			String[] responses = body.split("--" + responseBoundary + "(--)*");
			for (int i = 0; i < paths.size(); i++)
			{
				Callback<String> callback = requestQueue.get(paths.get(i));
				try
				{
					String response = responses[i];
					int httpStart = response.indexOf("HTTP/");
					if (httpStart != -1)
					{
						String responseContent = response.substring(httpStart);
						String responseBody = responseContent.substring(response.indexOf("\n\n") + 2).trim();
						if (responseBody != "")
						{
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
					callback.onError(e);
				}
			}
		}
	}
}
