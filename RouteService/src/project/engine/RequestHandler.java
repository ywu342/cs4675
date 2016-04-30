package project.engine;

import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Scanner;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import project.utility.DBConnector;
import project.utility.PythonEngine;
import project.utility.ResultCache;
import project.utility.RouteBoxer;
import project.utility.RouteBoxer.LatLng;
import project.utility.RouteBoxer.LatLngBounds;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Base path was based on /Recommender/*
 */
@Path("Route")
public class RequestHandler {
	float NUM_BOXES=10;
	
	@GET
	@Path("/getRoute")
	@Produces("text/plain")
	public String getRoute(
			@DefaultValue("") @QueryParam("start_latlng") String start_latlng,
			@DefaultValue("") @QueryParam("end_latlng") String end_latlng,
			@DefaultValue("") @QueryParam("origin_address") String origin_address,
			@DefaultValue("") @QueryParam("destination_address") String destination_address,
			@DefaultValue("365") @QueryParam("miles") double miles)  {
		ObjectMapper mapper = new ObjectMapper();
		JsonFactory factory = mapper.getFactory(); // since 2.1 use mapper.getFactory() instead
		RouteBoxer rb = new RouteBoxer();
		ResultCache.initial();
		boolean updateFlag = true;
		try{
			String result = "";
			
			// check direct matching
			result = ResultCache.getPreviousResult(origin_address, destination_address);
			if(!result.equals("NO RESULT") && !result.equals("ERROR")){
				return result;
			}
			String direction_url = "https://maps.googleapis.com/maps/api/directions/json";
			String charset = "UTF-8";  // Or in Java 7 and later, use the constant: java.nio.charset.StandardCharsets.UTF_8.name()

			String direction_query = String.format("origin=%s&destination=%s",
					URLEncoder.encode(origin_address, charset),
					URLEncoder.encode(destination_address, charset));

			URLConnection connection = new URL(direction_url + "?" + direction_query).openConnection();
			connection.setRequestProperty("Accept-Charset", charset);
			InputStream response = connection.getInputStream();

			Scanner scanner = new Scanner(response);
			String responseBody = scanner.useDelimiter("\\A").next();
			
			// parse response
			JsonParser jp = factory.createParser(responseBody);
			JsonNode DirectionObj = mapper.readTree(jp);
			
			// check fuzzy matching
			LatLng startCenter = rb.new LatLng();
			startCenter.lat = DirectionObj.get("routes").elements().next().get("legs").elements().next().get("start_location").get("lat").asDouble();
			startCenter.lng = DirectionObj.get("routes").elements().next().get("legs").elements().next().get("start_location").get("lng").asDouble();
			
			LatLng endCenter = rb.new LatLng();
			endCenter.lat = DirectionObj.get("routes").elements().next().get("legs").elements().next().get("end_location").get("lat").asDouble();
			endCenter.lng = DirectionObj.get("routes").elements().next().get("legs").elements().next().get("end_location").get("lng").asDouble();
			
			result = ResultCache.getFuzzyCenterPreviousResult(startCenter, endCenter);
			if(!result.equals("NO RESULT") && !result.equals("ERROR")){
				return result;
			}
			
			int distanceMeter =  DirectionObj.get("routes").elements().next().get("legs").elements().next().get("distance").get("value").asInt()/1000;

			ArrayList<LatLng> paths = new ArrayList<LatLng>();
			ArrayList<LatLngBounds> boxes = null;
			
			// From version 1, version 2 will use overview_polyline
//			Iterator<JsonNode> stepIterator = steps.iterator();
//			while(stepIterator.hasNext()){
//				JsonNode temp = stepIterator.next();
//				String tempPolyline = temp.get("polyline").get("points").asText();
//				paths.addAll(rb.decodePath(tempPolyline));			
//			}
			
			paths.addAll(rb.decodePath(DirectionObj.get("routes").elements().next().get("overview_polyline").get("points").asText()));
			boxes = (ArrayList<LatLngBounds>) rb.box(paths, distanceMeter/NUM_BOXES);
			
			ArrayNode stationLatLng = mapper.createArrayNode();
			
			// Matching the result into the database 
			LatLngBounds startBox = boxes.get(0);
			LatLngBounds endBox = boxes.get(boxes.size()-1);
			String place_url = "https://maps.googleapis.com/maps/api/place/radarsearch/json";
			for(LatLngBounds box : boxes){
				// Check the distance from the first box, be sure within certain range
				if(box.getCenter().distanceFrom(startBox.getCenter())*0.621371/1000.0 > miles*0.8){
					continue;
				}		
				String place_query = String.format("location="+(box.getNorthEast().lat+box.getSouthWest().lat)/2+","+(box.getNorthEast().lng+box.getSouthWest().lng)/2+"&radius=500&type=gas_station&key=AIzaSyDiIGEtWebcQxHSWIKzyS6J1H4HFfpomtY");
				connection = new URL(place_url + "?" + place_query).openConnection();
				connection.setRequestProperty("Accept-Charset", charset);
				response = connection.getInputStream();
				
				// parse response
				scanner = new Scanner(response);
				responseBody = scanner.useDelimiter("\\A").next();
				jp = factory.createParser(responseBody);
				JsonNode PlaceObj = mapper.readTree(jp);
				Iterator<JsonNode> stationLocations = PlaceObj.get("results").elements();
				while(stationLocations.hasNext()){
					JsonNode station = stationLocations.next();
					JsonNode location = station.get("geometry").get("location");
					stationLatLng.add(location);
				}	
			}
			
			try( PrintWriter outFile = new PrintWriter( "./candidateStation.json" )  ){
				outFile.println( mapper.writeValueAsString(stationLatLng) );
				if(outFile!=null)
					outFile.close();
			}
			
			String[] args = {System.getProperty("com.sun.aas.instanceRoot")+"/config/candidateStation.json", origin_address, destination_address,"1"};
			result = PythonEngine.runPython(args, "algorithm.py");
			if(result.equals("ERROR")){
				return "ERROR WHILE PROCESSING ALGORITHM";
			}
			// Update the cache for result
			if(updateFlag){
				ResultCache.updatePreviousResultTable(origin_address, destination_address, result);
				ResultCache.updateFuzzyPreviousResultTable(startBox, endBox, result);
			}
			return result;
			
			
		}catch(Exception e){
			return e.getMessage();
		}

	}

}
