import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;

// https://earthquake.usgs.gov/earthquakes/feed/v1.0/geojson.php
// https://android.jlelse.eu/a-performance-comparison-of-treemap-and-priorityqueue-in-a-certain-use-case-d9c2979276a1

/**
 * @author ashekhar
 * Class to capture necessary earthquake data for further processing.
 */
class EarthquakeDataNode {
	Float magnitude;
	String magnitudeType;
	Long time;
	String summaryWithLocation;
}

/**
 * Custom comparator to sort the earthquake data based on the magnitude
 * @author ashekhar 
 */
class EarthquakeDataNodeComparator implements Comparator<EarthquakeDataNode> {

	// Overriding compare()method of Comparator
	// for descending order of magnitude
	@Override
	public int compare(EarthquakeDataNode e1, EarthquakeDataNode e2) {
		if (e1.magnitude < e2.magnitude)
			return 1;
		else if (e1.magnitude > e2.magnitude)
			return -1;

		return 0;
	}
}

/**
 * Custom comparator to sort the earthquake data based on the occurance in various states
 * @author ashekhar
 * @param <T1>
 * @param <T2>
 */
class EarthquakeMagComparator<T1,T2 extends Comparable<T2>> implements Comparator<T1> {
    Map<T1,T2> base;
    public EarthquakeMagComparator(Map<T1,T2> base) {
        this.base = base;
    }

    @Override
    public int compare(T1 k1, T1 k2) {
        T2 val1 = base.get(k1);
        T2 val2 = base.get(k2);

        return val2.compareTo(val1);
    }
}

/**
 * @author ashekhar
 *
 */
public class quakes {

	final private static String USA_STATE_DATA = "C:\\Users\\ashekhar\\workspace\\Earthquakes\\src\\StateNamesAndCodes.csv";
	final private static String USGS_EARTHQUAKE_DATA_API = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_month.geojson";
	
	final private static int TOP_US_STATES_NUMBER_OF_EARTHQUAKES = 5;
	final private static int TOP_STRONGEST_EARTHQUAKES_IN_STATE = 25;

	private static HashSet<String> NamesOfTerritoriesAndStates = new HashSet<String>();
	private static HashSet<String> CodesOfTerritoriesAndStates = new HashSet<String>();
	private static Map<String, String> StateNames2Codes = new HashMap<String, String>();
	private static Map<String, String> StateCodes2Names = new HashMap<String, String>();

	/**
	 * Read the state/territory names and codes to build up the data structure.
	 */
	private static void readStateData() {

		File file = new File(USA_STATE_DATA);

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		String line;
		String code, name;
		try {

			while ((line = br.readLine()) != null) {
				code = line.split(",")[0];
				name = line.split(",")[1];
				
				// Support both state name and state codes
				NamesOfTerritoriesAndStates.add(name);
				CodesOfTerritoriesAndStates.add(code);
				StateNames2Codes.put(name, code);
				StateCodes2Names.put(code, name);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		if ((NamesOfTerritoriesAndStates.size() != CodesOfTerritoriesAndStates.size())
				|| (StateNames2Codes.size() != StateCodes2Names.size())
				|| (NamesOfTerritoriesAndStates.size() != StateCodes2Names.size())) {
			System.err.println("Error: State data loading.");
			System.exit(-1);
		}
	}

	/**
	 * Verify if the earthquake event occured within USA or not.
	 * @param stateStr
	 * @return boolean
	 */
	public static Boolean isEarthquakeWithinUSA(String stateStr) {

		if (NamesOfTerritoriesAndStates.contains(stateStr) || CodesOfTerritoriesAndStates.contains(stateStr))
			return true;
		else
			return false;
	}

	/**
	 * Convert the state/territory code to its corresponding name
	 * @param stateStr
	 * @return stateStr
	 */
	private static String convertStateCode2NameIfNeeded(String stateStr) {
		if (CodesOfTerritoriesAndStates.contains(stateStr))
			return StateCodes2Names.get(stateStr);
		else
			return stateStr;
	}

	/**
	 * Validate input arguments.
	 * @param args
	 * @return firstArg
	 */
	private static String validateInputArguments(String[] args) {

		Class thisClass = new Object(){}.getClass();
		String className = thisClass.getEnclosingClass().getSimpleName();
		
		String firstArg = null;
		
		if (args.length == 1) {
			
			// atleast need 3 characters to proceed
			if (args[0].length() > 3) {
			
				if (args[0].substring(0, 2).equals("--")) {
				
					// Remove the initial "--"
					firstArg = args[0].substring(2);

					if (firstArg.equalsIgnoreCase("top5")) {
						return firstArg;
					} else if (!isEarthquakeWithinUSA(firstArg)) {
						System.err.println("Error: Invalid state name/state initials.");
						quakesHelperMethods.usage(className, args[0]);
					}
				}			
			} else {
				quakesHelperMethods.usage(className, args[0]);				
			}
		}

		return firstArg;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		readStateData();

		String firstArg = validateInputArguments(args);

		Comparator<EarthquakeDataNode> EarthquakeDataComparator = new EarthquakeDataNodeComparator();

		// Overall top 25 for a given state.
		PriorityQueue<EarthquakeDataNode> sortedStateEarthquakeData = new PriorityQueue<EarthquakeDataNode>(100, EarthquakeDataComparator);

		// Overall top 5 states by number of earthquakes
		Map<String, Integer> earthquakeCountPerState = new HashMap<String, Integer>();
		EarthquakeMagComparator comp = new EarthquakeMagComparator(earthquakeCountPerState);
        TreeMap<String,Integer> earthquakeMagnitudeSortedMap = new TreeMap<String,Integer>(comp);

		// Get earthquake data (API response)
		String earthquakesData = quakesHelperMethods.getEarthquakesData(USGS_EARTHQUAKE_DATA_API);
		if (!earthquakesData.isEmpty()) {

			// Convert the string to a JSON object
			JSONObject mainJSONObj = new JSONObject(earthquakesData);

			// Parse
			JSONObject metadataObj = mainJSONObj.getJSONObject("metadata");
			int getStatus = metadataObj.getInt("status");
			int getDataCount = metadataObj.getInt("count");

			// Verify the API status code
			if (getStatus == 200) {

				JSONArray readingsJSONArray = mainJSONObj.getJSONArray("features");

				// Loop through the earthquake data
				for (int i = 0; i < getDataCount; i++) {

					JSONObject reading = readingsJSONArray.getJSONObject(i);
					JSONObject properties = reading.getJSONObject("properties");

					// Focus only on earthquake data
					if (properties.getString("type").equals("earthquake")) {

						/*
						 * title: M ? - 6km W of Cobb, CA mag: null
						 * 
						 * Data did not seem to be clean. Hence opted to fetch
						 * the magnitude from the title string.
						 * 
						 * title: M 0.5 - 9km NE of Aguanga, CA Parse the title
						 * to get the magnitude value
						 */
						String magStr = properties.getString("title").split(" - ")[0].split(" ")[1];
						float magFloat;

						// Ignore if mag value is null or if the mag value in
						// title is ?
						if (!magStr.equals("?")) {
							magFloat = Float.parseFloat(magStr);

							/*
							 * Invalid title: M 4.5 - Federated States of
							 * Micronesia region Proceed ONLY if there is a
							 * state component in the title.
							 */
							if (properties.getString("title").split(", ").length == 2) {
								String stateStr = properties.getString("title").split(", ")[1];

								// Validate if the data is indeed in the USA
								if (isEarthquakeWithinUSA(stateStr)) {

									stateStr = convertStateCode2NameIfNeeded(stateStr);

									// Build earthquake data object
									EarthquakeDataNode eqData = new EarthquakeDataNode();
									eqData.magnitude = magFloat;
									eqData.magnitudeType = properties.getString("magType");
									eqData.time = properties.getLong("time");
									eqData.summaryWithLocation = properties.getString("title");

									if (firstArg.equalsIgnoreCase("top5")) {
										// Keep track of number of earthquakes in each state.
										if (earthquakeCountPerState.containsKey(stateStr)) {
											earthquakeCountPerState.put(stateStr, earthquakeCountPerState.get(stateStr) + 1);
										} else {
											earthquakeCountPerState.put(stateStr, 1);
										}
									} else {
										firstArg = convertStateCode2NameIfNeeded(firstArg);
										if (firstArg.equalsIgnoreCase(stateStr))
											sortedStateEarthquakeData.add(eqData);
									}
								}
							}
						}
					}
				}

				DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd hh:mm:ss aa zzz yyyy");
				Date date = new Date();
				
				if (firstArg.equalsIgnoreCase("top5")) {
					earthquakeMagnitudeSortedMap.putAll(earthquakeCountPerState);
					System.out.println("A list of the top 5 US states by number of earthquakes, highest to lowest. (As of " + dateFormat.format(date) + ")");
					quakesHelperMethods.pollEarthquakeDataFromTreeMap(earthquakeMagnitudeSortedMap, TOP_US_STATES_NUMBER_OF_EARTHQUAKES);
				} else {
					System.out.println("A list of the top 25 strongest earthquakes in " + firstArg + ", highest to lowest. (As of " + dateFormat.format(date) + ")");
					System.out.println("Earthquakes reported for state: " + firstArg + " (Total number of recordings: " + sortedStateEarthquakeData.size() + ")");
					quakesHelperMethods.pollEarthquakeDataFromPriorityQueue(sortedStateEarthquakeData, TOP_STRONGEST_EARTHQUAKES_IN_STATE);
				}

			} else {
				System.err.println("Error: Unable to fetch earthquake data.");
				System.exit(-1);
			}
		}
	}
}
