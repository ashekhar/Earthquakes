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
		
		System.err.println("Usage: java " + className + " --top5 | --statestop5 | --<Name of state | State initials>\n");
		System.out.println("--top5: A list of the top 5 US states by number of earthquakes, highest to lowest");
		System.out.println("--statestop5: A list of the top 25 strongest earthquakes in each state of occurence, highest to lowest");
		System.out.println("--<Name of state | State initials>: A list of the top 25 strongest earthquakes in a specific state, highest to lowest");
		System.out.println("\tEx: --california | --California | --CA | --ca\n");
		System.out.println("For the following states/territories, please use the state code.");
		System.out.println("States ...");
		System.out.println("\tDistrict of Columbia (DC)");
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
		System.out.println("Territories ...");
		System.out.println("\tAmerican Samoa (AS)");
		System.out.println("\tNorthern Mariana Islands (MP)");
		System.out.println("\tPuerto Rico (PR)");
		System.out.println("\tU.S. Virgin Islands (VI)");
		
		System.exit(-1);
	}

	/**
	 * To uppercase 1st character of a word
	 * 
	 * @param givenString
	 * @return
	 */
	public static String capitalizeFully(String givenString) {
		
		if (givenString.isEmpty())
			return givenString;
		
	    String[] arr = givenString.split(" ");
	    StringBuilder strBuilder = new StringBuilder();

	    for (int i = 0; i < arr.length; i++) {
	    	strBuilder.append(Character.toUpperCase(arr[i].charAt(0)))
	            .append(arr[i].substring(1))
	            .append(" ");
	    }          
	    return strBuilder.toString().trim();
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

		System.out.println("Fetching earthquake data from USGS (https://earthquake.usgs.gov) ...");
		
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

			// HTTP Status
			if (conn.getResponseCode() == 200) {
				
				// Content Type is JSON
				if (conn.getContentType().contains("json")) {
					
					BufferedReader rd = null;
					rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
					String line;
					
					int lineCounter = 0;
					// Display 1 "." for every 100 lines
					while ((line = rd.readLine()) != null) {
						result.append(line);
						lineCounter++;
						if (lineCounter % 100 == 0)
							System.out.print(".");
					}

					rd.close();

					System.out.println("\n");
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
	
	// Decided to add this at the bottom of the class due to length of the data.
	final static String[][] StateNamesAndCodes = new String[][] {
		{"AL", "Alabama"},
		{"AK", "Alaska"},
		{"AZ", "Arizona"},
		{"AR", "Arkansas"},
		{"CA", "California"},
		{"CO", "Colorado"},
		{"CT", "Connecticut"},
		{"DE", "Delaware"},
		{"DC", "District of Columbia"},
		{"FL", "Florida"},
		{"GA", "Georgia"},
		{"HI", "Hawaii"},
		{"ID", "Idaho"},
		{"IL", "Illinois"},
		{"IN", "Indiana"},
		{"IA", "Iowa"},
		{"KS", "Kansas"},
		{"KY", "Kentucky"},
		{"LA", "Louisiana"},
		{"ME", "Maine"},
		{"MD", "Maryland"},
		{"MA", "Massachusetts"},
		{"MI", "Michigan"},
		{"MN", "Minnesota"},
		{"MS", "Mississippi"},
		{"MO", "Missouri"},
		{"MT", "Montana"},
		{"NE", "Nebraska"},
		{"NV", "Nevada"},
		{"NH", "New Hampshire"},
		{"NJ", "New Jersey"},
		{"NM", "New Mexico"},
		{"NY", "New York"},
		{"NC", "North Carolina"},
		{"ND", "North Dakota"},
		{"OH", "Ohio"},
		{"OK", "Oklahoma"},
		{"OR", "Oregon"},
		{"PA", "Pennsylvania"},
		{"RI", "Rhode Island"},
		{"SC", "South Carolina"},
		{"SD", "South Dakota"},
		{"TN", "Tennessee"},
		{"TX", "Texas"},
		{"UT", "Utah"},
		{"VT", "Vermont"},
		{"VA", "Virginia"},
		{"WA", "Washington"},
		{"WV", "West Virginia"},
		{"WI", "Wisconsin"},
		{"WY", "Wyoming"},
		{"AS", "American Samoa"},
		{"GU", "Guam"},
		{"MP", "Northern Mariana Islands"},
		{"PR", "Puerto Rico"},
		{"VI", "U.S. Virgin Islands"}		
//		{"VI", "United States Virgin Islands"}		
	};
}
