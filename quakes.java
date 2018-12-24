import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Comparator;
import java.util.PriorityQueue;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

/*
 * GeoJSON Summary Format
 * https://earthquake.usgs.gov/earthquakes/feed/v1.0/geojson.php
 */

/**
 * Class to capture necessary earthquake data for further processing.
 * 
 * @author ashekhar
 */
class EarthquakeDataNode {
	Float magnitude;
	String magnitudeType;
	Long time;
	String summaryWithLocation;
}

/**
 * Custom comparator to sort the earthquake data based on the magnitude
 * 
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
 * Custom comparator to sort the earthquake data based on the occurrence in various states
 * 
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
 * Main class
 * 
 * @author ashekhar
 */
public class quakes {

	final private static String USGS_EARTHQUAKE_DATA_API 	= "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_month.geojson";
	
	final private static int TOP_US_STATES_NUMBER_OF_EARTHQUAKES 	= 5;
	final private static int TOP_STRONGEST_EARTHQUAKES_IN_STATE 	= 25;
	final private static int TOP_STRONGEST_EARTHQUAKES_PER_STATE 	= 5;

	private static HashSet<String> NamesOfTerritoriesAndStates 	= new HashSet<String>();
	private static HashSet<String> CodesOfTerritoriesAndStates 	= new HashSet<String>();
	private static Map<String, String> StateNames2Codes 		= new HashMap<String, String>();
	private static Map<String, String> StateCodes2Names 		= new HashMap<String, String>();

	/**
	 * BuildUp States and Territories Data Structures
	 */
	private static void buildUpStatesAndTerritoriesDataStructures() {

		//String stateCode, stateName;
		
		for (int i = 0; i < quakesHelperMethods.StateNamesAndCodes.length; i++) {
			/*
			stateCode = quakesHelperMethods.StateNamesAndCodes[i][0];
			stateName = quakesHelperMethods.StateNamesAndCodes[i][1];
			*/
			// Support both state name and state codes
			NamesOfTerritoriesAndStates.add(quakesHelperMethods.StateNamesAndCodes[i][1].toLowerCase());
			CodesOfTerritoriesAndStates.add(quakesHelperMethods.StateNamesAndCodes[i][0].toLowerCase());
			
			StateNames2Codes.put(quakesHelperMethods.StateNamesAndCodes[i][1].toLowerCase(), quakesHelperMethods.StateNamesAndCodes[i][0]);
			StateCodes2Names.put(quakesHelperMethods.StateNamesAndCodes[i][0].toLowerCase(), quakesHelperMethods.StateNamesAndCodes[i][1]);
		}
		
		if ((NamesOfTerritoriesAndStates.size() != CodesOfTerritoriesAndStates.size())
				|| (StateNames2Codes.size() != StateCodes2Names.size())
				|| (NamesOfTerritoriesAndStates.size() != StateCodes2Names.size())) {
			System.err.println("Error: State data loading.");
			System.exit(-1);
		}
	}

	/**
	 * Verify if the earthquake event occurred within USA or not.
	 * 
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
	 * 
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
	 * 
	 * @param args
	 * @return firstArg
	 */
	private static String validateInputArguments(String[] args) {

		// Get the name of the current program/class
		Class thisClass		= new Object(){}.getClass();
		String className	= thisClass.getEnclosingClass().getSimpleName();
		
		String firstArg = null;
		
		if (args.length == 1) {
			
			// at-least need 3 characters to proceed
			// -- 2 characters
			// State code 2 characters
			if (args[0].length() >= 4) {
			
				if (args[0].substring(0, 2).equals("--")) {
				
					// Remove the initial "--"
					firstArg = args[0].substring(2).toLowerCase();

					if (firstArg.equalsIgnoreCase("top5") || firstArg.equalsIgnoreCase("statestop5")) {
						return firstArg;
					} else if (!isEarthquakeWithinUSA(firstArg)) {
						System.err.println("Error: Invalid state name/state initials.");
						quakesHelperMethods.usage(className, args[0]);
					}
				}
			}
		} else {
			
			if (args.length == 0)
				quakesHelperMethods.usage(className, "");
			else
				quakesHelperMethods.usage(className, Arrays.toString(args));				
		}

		return firstArg;
	}

	/**
	 * Main program
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		buildUpStatesAndTerritoriesDataStructures();

		String firstArg = validateInputArguments(args);

		Comparator<EarthquakeDataNode> EarthquakeDataComparator = new EarthquakeDataNodeComparator();

		/*
		 * Overall top 25 for a given state.
		 * --<State/Terriroty code | State/Terriroty name>
		 */
		PriorityQueue<EarthquakeDataNode> sortedStateEarthquakeData = new PriorityQueue<EarthquakeDataNode>(TOP_STRONGEST_EARTHQUAKES_IN_STATE, EarthquakeDataComparator);

		/*
		 * Overall top 5 states by number of earthquakes
		 * --top5
		 */
		Map<String, Integer> earthquakeCountPerState			= new HashMap<String, Integer>();
		EarthquakeMagComparator comp							= new EarthquakeMagComparator(earthquakeCountPerState);
        TreeMap<String,Integer> earthquakeMagnitudeSortedMap	= new TreeMap<String,Integer>(comp);

		/*
		 * Overall top 5 per state.
		 * --statestop5
		 * 
		 * For each state, the value is a priority queue. The priority queue is created and 
		 * assigned to the map during run time only for those states there is data.
		 */
		Map<String, Object> earthquakeDataPerState = new HashMap<>();
		//PriorityQueue<EarthquakeDataNode> sortedEarthquakeDataPerState = new PriorityQueue<EarthquakeDataNode>(TOP_STRONGEST_EARTHQUAKES_PER_STATE, EarthquakeDataComparator);

		// Get earthquake data (API response)
		String earthquakesData = quakesHelperMethods.getEarthquakesDataFromUSGS(USGS_EARTHQUAKE_DATA_API);
		if (!earthquakesData.isEmpty()) {

			JSONObject mainJSONObj = null;
			JSONObject metadataObj = null;
			int getStatus = 0;
			int getTotalDataCount = 0;
			
			try {
				// Convert the string to a JSON object & parse
				mainJSONObj 		= new JSONObject(earthquakesData);
				metadataObj			= mainJSONObj.getJSONObject("metadata");
				getStatus			= metadataObj.getInt("status");
				getTotalDataCount	= metadataObj.getInt("count");

			} catch (JSONException e) {
					e.printStackTrace();
			}

			// Verify the API status code
			if (getStatus == 200) {

				JSONArray readingsJSONArray = null;
				try {
					readingsJSONArray = mainJSONObj.getJSONArray("features");					
				} catch (JSONException e) {
					e.printStackTrace();
				}

				// Loop through the earthquake data
				JSONObject reading		= null;
				JSONObject properties	= null;
				
				for (int i = 0; i < getTotalDataCount; i++) {

					reading		= readingsJSONArray.getJSONObject(i);
					properties	= reading.getJSONObject("properties");

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

						StringBuilder magStr = new StringBuilder();
						magStr.append(properties.getString("title").split(" - ")[0].split(" ")[1]);

						// Ignore if mag value is null or if the mag value in title is ?
						if (!magStr.toString().equals("?")) {
							float magFloat = Float.parseFloat(magStr.toString());

							/*
							 * Invalid title: M 4.5 - Federated States of Micronesia region 
							 * Proceed ONLY if there is a state component in the title.
							 */
							if (properties.getString("title").split(", ").length == 2) {

								StringBuilder stateStr = new StringBuilder();
								stateStr.append(properties.getString("title").split(", ")[1].toLowerCase());
								
								// Validate if the data is indeed in the USA
								if (isEarthquakeWithinUSA(stateStr.toString())) {

									StringBuilder stateStrUpdated = new StringBuilder();
									stateStrUpdated = stateStrUpdated.append(convertStateCode2NameIfNeeded(stateStr.toString()).toLowerCase());

									// Build earthquake data object
									EarthquakeDataNode eqData	= new EarthquakeDataNode();
									eqData.magnitude			= magFloat;
									eqData.magnitudeType		= properties.getString("magType");
									eqData.time					= properties.getLong("time");
									eqData.summaryWithLocation	= properties.getString("title");

									if (firstArg.equalsIgnoreCase("top5")) {
										
										/*
										 * Overall top 5 earthquakes in USA
										 * Keep track of number of earthquakes in each state.
										 */
										if (earthquakeCountPerState.containsKey(stateStrUpdated.toString())) {
											earthquakeCountPerState.put(stateStrUpdated.toString(), earthquakeCountPerState.get(stateStrUpdated.toString()) + 1);
										} else {
											earthquakeCountPerState.put(stateStrUpdated.toString(), 1);
										}
										
									} else if (firstArg.equalsIgnoreCase("statestop5")) {
										
										/*
										 * Overall top 5 earthquakes in USA
										 * Create the priority queue for a given state
										 */
										if (!earthquakeDataPerState.containsKey(stateStr.toString())) {
											earthquakeDataPerState.put(stateStr.toString(), new PriorityQueue<EarthquakeDataNode>(TOP_STRONGEST_EARTHQUAKES_PER_STATE, EarthquakeDataComparator));
										}

										// Populate the priority queue
										PriorityQueue<EarthquakeDataNode> tempPQueue;
										tempPQueue = (PriorityQueue<EarthquakeDataNode>) earthquakeDataPerState.get(stateStr.toString());
										tempPQueue.add(eqData);										
										
									} else {
										
										// Collecting data for a particular state
										firstArg = convertStateCode2NameIfNeeded(firstArg);
										if (firstArg.equalsIgnoreCase(stateStrUpdated.toString()))
											sortedStateEarthquakeData.add(eqData);
									}
								}
							}
						}
					}
				}

				if (firstArg.equalsIgnoreCase("top5")) {
					
					//Do this ("putAll") ONLY if the argument is "top5"
					earthquakeMagnitudeSortedMap.putAll(earthquakeCountPerState);
					
					System.out.println("A list of the top 5 US states by number of earthquakes, highest to lowest. (As of " + quakesHelperMethods.getCurrentDataTime() + ")");
					quakesHelperMethods.pollEarthquakeDataFromTreeMap(earthquakeMagnitudeSortedMap, TOP_US_STATES_NUMBER_OF_EARTHQUAKES);
				
				} else if (firstArg.equalsIgnoreCase("statestop5")) {

					System.out.println("A list of the top 5 earthquakes in each state, highest to lowest. (As of " + quakesHelperMethods.getCurrentDataTime() + ")");
					Set<String> keys = earthquakeDataPerState.keySet();
					PriorityQueue<EarthquakeDataNode> sortedEarthquakeDataPerState = null;
					
					for (String state : keys) {
						sortedEarthquakeDataPerState = (PriorityQueue<EarthquakeDataNode>) earthquakeDataPerState.get(state);
						System.out.println("State: " + quakesHelperMethods.capitalizeFully(state) + " (Number of earthquake(s) reported : "	+ sortedEarthquakeDataPerState.size() + ")");
						quakesHelperMethods.pollEarthquakeDataFromPriorityQueue(sortedEarthquakeDataPerState, TOP_STRONGEST_EARTHQUAKES_PER_STATE);
					}
					
				} else {
					
					System.out.println("A list of the top 25 strongest earthquakes in " + quakesHelperMethods.capitalizeFully(firstArg) + ", highest to lowest. (As of " + quakesHelperMethods.getCurrentDataTime() + ")");
					System.out.println("Earthquakes reported for state: " + quakesHelperMethods.capitalizeFully(firstArg) + " (Number of earthquake(s) reported: " + sortedStateEarthquakeData.size() + ")");
					quakesHelperMethods.pollEarthquakeDataFromPriorityQueue(sortedStateEarthquakeData, TOP_STRONGEST_EARTHQUAKES_IN_STATE);
				}

			} else {
				System.err.println("Error: Unable to fetch earthquake data.");
				System.exit(-1);
			}
		}
	}
}
