import java.net.URL;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.MalformedURLException;

import java.util.Map;
import java.util.Calendar;
import java.util.PriorityQueue;

public class quakesHelperMethods {

	/**
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

		Calendar c = Calendar.getInstance();

		for (int i = 0; i < upperLimit; i++) {
			EarthquakeDataNode data = pQueue.poll();
			c.setTimeInMillis(data.time);
			System.out.println("	" + data.summaryWithLocation + " @ " + c.getTime());
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
	public static String getEarthquakesData(String urlStr) {

		//"https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_month.geojson";

		StringBuilder result = new StringBuilder();
		URL url = null;
		try {
			url = new URL(urlStr);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}

		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) url.openConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			conn.setRequestMethod("GET");
		} catch (ProtocolException e) {

			e.printStackTrace();
		}
		BufferedReader rd = null;
		try {
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		String line;
		try {
			while ((line = rd.readLine()) != null) {
				result.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			rd.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result.toString();
	}
}
