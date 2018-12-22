import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;

import java.util.Map;
import java.util.Date;
import java.util.PriorityQueue;

public class quakesHelperMethods {

	final private static String TIMESTAMP_FORMAT = "EEE MMM dd hh:mm:ss aa zzz yyyy";

	/**
	 * Get current date and time
	 * 
	 * @return
	 */
	static String getCurrentDataTime() {
		
		DateFormat dateFormat = new SimpleDateFormat(TIMESTAMP_FORMAT);
		Date date = new Date();
		
		return dateFormat.format(date);
	}
	
	
	/**
	 * Convert long to time
	 * 
	 * @param time
	 * @return
	 */
	static String convertLong2Time(long time) {
	    
		Date date = new Date(time);
	    Format format = new SimpleDateFormat(TIMESTAMP_FORMAT);
	    
	    return format.format(date);
	}
	
	/**
	 * Usage:
	 * 
	 * @param className
	 * @param args0
	 */
	static void usage(String className, String args0) {
		
		System.err.println("Usage: java " + className + " --top5 | --<Name of state | State initials>");
		System.out.println("For the following states please use the state code.");
		System.out.println("\tNew Hampshire (NH)"); 
		System.out.println("\tNew Jersey (NJ)"); 
		System.out.println("\tNew Mexico (NM)"); 
		System.out.println("\tNew York (NY)"); 
		System.out.println("\tNorth Carolina (NC)"); 
		System.out.println("\tNorth Dakota (ND)"); 
		System.out.println("\tRhode Island (RI)"); 
		System.out.println("\tSouth Carolina (SC)"); 
		System.out.println("\tSouth Dakota (SD)"); 
		System.out.println("\tWest Virginia (WV)"); 

		System.exit(-1);
	}

	/**
	 * Print the top 5 entries in the priority queue
	 * 
	 * @param pQueue
	 */
	static void pollEarthquakeDataFromPriorityQueue(PriorityQueue<EarthquakeDataNode> pQueue, int count) {

		Integer upperLimit = pQueue.size() > count ? count : pQueue.size();

		for (int i = 0; i < upperLimit; i++) {
			EarthquakeDataNode data = pQueue.poll();
			System.out.println("	" + data.summaryWithLocation + " @ " + convertLong2Time(data.time));
		}
	}

	/**
	 * Print the top earthquakes of a particular state
	 * 
	 * @param earthquakeCountPerState
	 * @param count
	 */
	static void pollEarthquakeDataFromTreeMap(Map<String, Integer> earthquakeCountPerState, int count) {
		
		Integer upperLimit = earthquakeCountPerState.size() > count ? count : earthquakeCountPerState.size();
		
		Integer counter = 0;
		for(String key : earthquakeCountPerState.keySet()) {
			if (counter == upperLimit)
				break;
			System.out.println(key + ": " + earthquakeCountPerState.get(key));
			counter++;
		}
	}

	/**
	 * Fetch the earthquake data by making the API call.
	 * 
	 * @param urlStr
	 * @return
	 */
	public static String getEarthquakesDataFromUSGS(String urlStr) {

		StringBuilder result = new StringBuilder();
		URL url = null;
		
		try {
			url = new URL(urlStr);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		HttpURLConnection conn = null;
		
		try {
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");

			if (conn.getResponseCode() == 200) {
				
				if (conn.getContentType().contains("json")) {
					
					BufferedReader rd = null;
					rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					String line;
					
					while ((line = rd.readLine()) != null) {
						result.append(line);
					}

					rd.close();

					return result.toString();
					
				} else {
					System.err.println("Error: Content type is not of type \"application/json\"");
					System.exit(-1);					
				}
				
			} else {
				System.err.println("Error: Fetching data from USGS.");
				System.exit(-1);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}
