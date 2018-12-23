import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;

import java.nio.file.Path;
import java.nio.file.FileSystems;

import java.util.Map;
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
 * Main class
 *
 */
public class quakes {

	final private static String USA_STATE_DATA 				= "StateNamesAndCodes.csv";
	final private static String USGS_EARTHQUAKE_DATA_API 	= "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_month.geojson";
	
	final private static int TOP_US_STATES_NUMBER_OF_EARTHQUAKES 	= 5;
	final private static int TOP_STRONGEST_EARTHQUAKES_IN_STATE 	= 25;

	private static HashSet<String> NamesOfTerritoriesAndStates 	= new HashSet<String>();
	private static HashSet<String> CodesOfTerritoriesAndStates 	= new HashSet<String>();
	private static Map<String, String> StateNames2Codes 		= new HashMap<String, String>();
	private static Map<String, String> StateCodes2Names 		= new HashMap<String, String>();

	/**
	 * Read the state/territory names and codes to build up the data structure.
	 */
	@Deprecated
	private static void buildUpStatesAndTerritoriesDataStructuresOld() {

		Path path		= FileSystems.getDefault().getPath(".").toAbsolutePath();
		String absPath	= path.toString() + "\\src\\" + USA_STATE_DATA;
		File file		= new File(absPath);
		
		if (!file.exists() || !file.canRead() || file.length() == 0) {
			System.err.println("File: " + file.getAbsolutePath());
			
			if (!file.exists())
				System.err.println(" ... not found.");
			else if (!file.canRead())
				System.err.println(" ... unable to be read.");
			else
				System.err.println(" ... is of size 0 bytes.");
			
			System.exit(-1);
		}

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		String line;
		String stateCode, stateName;
		try {

			while ((line = br.readLine()) != null) {
				stateCode = line.split(",")[0];
				stateName = line.split(",")[1];
				
				// Support both state name and state codes
				NamesOfTerritoriesAndStates.add(stateName);
				CodesOfTerritoriesAndStates.add(stateCode);
				
				StateNames2Codes.put(stateName, stateCode);
				StateCodes2Names.put(stateCode, stateName);
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

	private static void buildUpStatesAndTerritoriesDataStructures() {

		//String stateCode, stateName;
		
		for (int i = 0; i < quakesHelperMethods.StateNamesAndCodes.length; i++) {
			/*
			stateCode = quakesHelperMethods.StateNamesAndCodes[i][0];
			stateName = quakesHelperMethods.StateNamesAndCodes[i][1];
			*/
			// Support both state name and state codes
			NamesOfTerritoriesAndStates.add(quakesHelperMethods.StateNamesAndCodes[i][1]);
			CodesOfTerritoriesAndStates.add(quakesHelperMethods.StateNamesAndCodes[i][0]);
			
			StateNames2Codes.put(quakesHelperMethods.StateNamesAndCodes[i][1], quakesHelperMethods.StateNamesAndCodes[i][0]);
			StateCodes2Names.put(quakesHelperMethods.StateNamesAndCodes[i][0], quakesHelperMethods.StateNamesAndCodes[i][1]);
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

		buildUpStatesAndTerritoriesDataStructures();

		String firstArg = validateInputArguments(args);

		Comparator<EarthquakeDataNode> EarthquakeDataComparator = new EarthquakeDataNodeComparator();

		// Overall top 25 for a given state.
		PriorityQueue<EarthquakeDataNode> sortedStateEarthquakeData = new PriorityQueue<EarthquakeDataNode>(100, EarthquakeDataComparator);

		// Overall top 5 states by number of earthquakes
		Map<String, Integer> earthquakeCountPerState			= new HashMap<String, Integer>();
		EarthquakeMagComparator comp							= new EarthquakeMagComparator(earthquakeCountPerState);
        TreeMap<String,Integer> earthquakeMagnitudeSortedMap	= new TreeMap<String,Integer>(comp);

		// Get earthquake data (API response)
		String earthquakesData = quakesHelperMethods.getEarthquakesDataFromUSGS(USGS_EARTHQUAKE_DATA_API);
		if (!earthquakesData.isEmpty()) {

			JSONObject mainJSONObj = null;
			JSONObject metadataObj = null;
			int getStatus = 0;
			int getTotalDataCount = 0;
			
			try{
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

						// Ignore if mag value is null or if the mag value in
						// title is ?
						if (!magStr.toString().equals("?")) {
							float magFloat = Float.parseFloat(magStr.toString());

							/*
							 * Invalid title: M 4.5 - Federated States of
							 * Micronesia region Proceed ONLY if there is a
							 * state component in the title.
							 */
							if (properties.getString("title").split(", ").length == 2) {

								StringBuilder stateStr = new StringBuilder();
								stateStr.append(properties.getString("title").split(", ")[1]);
								
								// Validate if the data is indeed in the USA
								if (isEarthquakeWithinUSA(stateStr.toString())) {

									StringBuilder stateStrUpdated = new StringBuilder();
									stateStrUpdated = stateStrUpdated.append(convertStateCode2NameIfNeeded(stateStr.toString()));

									// Build earthquake data object
									EarthquakeDataNode eqData	= new EarthquakeDataNode();
									eqData.magnitude			= magFloat;
									eqData.magnitudeType		= properties.getString("magType");
									eqData.time					= properties.getLong("time");
									eqData.summaryWithLocation	= properties.getString("title");

									if (firstArg.equalsIgnoreCase("top5")) {
										
										// Keep track of number of earthquakes in each state.
										if (earthquakeCountPerState.containsKey(stateStrUpdated.toString())) {
											earthquakeCountPerState.put(stateStrUpdated.toString(), earthquakeCountPerState.get(stateStrUpdated.toString()) + 1);
										} else {
											earthquakeCountPerState.put(stateStrUpdated.toString(), 1);
										}
										
									} else {
										
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
					
				} else {
					
					System.out.println("A list of the top 25 strongest earthquakes in " + firstArg + ", highest to lowest. (As of " + quakesHelperMethods.getCurrentDataTime() + ")");
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
