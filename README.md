# Earthquakes

Please use tab space = 4

- StateNamesAndCodes.csv: Data file containing names of states and territories along with their codes. (No longer needed - Can be used for reference)
- quakes.java: Main program
- quakesHelperMethods.java: Helper methods
- pom.xml: Dependencies

Source/Reference:
- GeoJSON Summary Format: https://earthquake.usgs.gov/earthquakes/feed/v1.0/geojson.php
- Data source: https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_month.geojson

Suppose your team analyzes the largest earthquakes in the United States.
Your manager has asked you to create a tool that will print the following to the console:
 
1) A list of the top 5 US states by number of earthquakes, highest to lowest
2) A list of the top 25 strongest earthquakes in California, highest to lowest
 
The US Geological survey publishes a dataset perfect for this analysis.
https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_month.geojson
 
Instructions:
- Your program should be runnable using these commands, but may have alternate interfaces.

Part 1) `quakes --top5`

Part 2) `quakes --california`

- It is sufficient to assume that "22km NW of Truckee, California" is an earthquake located in California.
- When displaying an earthquake's data, please include the time (eg format: 2017-07-13T22:09:53+00:00), the location, and magnitude of the quake.

Usage: java quakes --top5 | --<Name of state | State initials>

Note:
For the following states please use the state code.
	New Hampshire (NH)
	New Jersey (NJ)
	New Mexico (NM)
	New York (NY)
	North Carolina (NC)
	North Dakota (ND)
	Rhode Island (RI)
	South Carolina (SC)
	South Dakota (SD)
	West Virginia (WV)
