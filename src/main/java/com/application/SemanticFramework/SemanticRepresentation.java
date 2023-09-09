package com.application.SemanticFramework;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.json.JSONObject;

import virtuoso.jena.driver.VirtModel;


import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

public class SemanticRepresentation {

	// Semantic Representation of events information
	public static Model eventsInitialization(String events, String uuid, String year, String month, String day,
			String location, String city, String country) {
		Model model = ModelFactory.createDefaultModel();
		model.setNsPrefix("event", Prefixes.event);
		model.setNsPrefix("time", Prefixes.time);

		model.setNsPrefix("rdf", Prefixes.rdf);
		String event_instance = Prefixes.event + "Event_" + uuid;
		model.add(model.createResource(event_instance), model.createProperty(Prefixes.rdf + "type"),
				model.createResource(Prefixes.event + "Event"));
		model.add(model.createResource(event_instance), model.createProperty(Prefixes.time + "year"), year);

		if (!month.equals("null")) {
			model.add(model.createResource(event_instance), model.createProperty(Prefixes.time + "month"), month);
		}
		if (!day.equals("null")) {
			model.add(model.createResource(event_instance), model.createProperty(Prefixes.time + "day"), day);
		}
		Instant instant = Instant.now();
		DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
				.withZone(ZoneId.systemDefault());
		String formater_output = DATE_TIME_FORMATTER.format(instant);
		LocalDateTime current_time = LocalDateTime.parse(formater_output, DATE_TIME_FORMATTER);

		model.add(model.createResource(event_instance), model.createProperty(Prefixes.time + "inXSDDateTimeStamp"),
				current_time.toString());

		model.add(model.createResource(event_instance), model.createProperty(Prefixes.event + "place"), location);
		if (!city.equals("null")) {
			model.add(model.createResource(event_instance), model.createProperty(Prefixes.event + "city"), city);
		}
		if (!country.equals("null")) {
			model.add(model.createResource(event_instance), model.createProperty(Prefixes.event + "country"), country);
		}
		return model;
	}

	// Semantic Representation of results containing both real-world events (i.e. earthquakes) and also Copernicus sentinel metadata
	public static Model resultsMapping(JSONObject result, String uuid, Model model, ArrayList<String> copernicusSources,
			String eventType, ArrayList<String> additionalFields) throws IOException{

		String uuid2 = UUID.randomUUID().toString().replaceAll("-", "");
		String event_instance = Prefixes.event + "Event_" + uuid;
		String eventType_instance = Prefixes.event + StringUtils.capitalize(eventType) + "Event_" + uuid2;
		model.add(model.createResource(event_instance), model.createProperty(Prefixes.event + "hasSubEvent"),
				model.createResource(eventType_instance));
		model.add(model.createResource(eventType_instance), model.createProperty(Prefixes.rdf + "type"),
				model.createResource(Prefixes.event + "Event"));
		if (result.getJSONObject("event").has("eventId")) {
			model.add(model.createResource(eventType_instance), model.createProperty(Prefixes.event + "hasId"),
					result.getJSONObject("event").getString("eventId"));
		}
		if (result.getJSONObject("event").has("magnitude")) {
			model.add(model.createResource(eventType_instance), model.createProperty(Prefixes.event + "magnitude"),
					result.getJSONObject("event").getString("magnitude"));
		}
		if (result.getJSONObject("event").has("depth")) {
			model.add(model.createResource(eventType_instance), model.createProperty(Prefixes.event + "depth"),
					result.getJSONObject("event").getString("depth"));
		}
		for (int i = 0; i < additionalFields.size(); i++) {
			model.add(model.createResource(eventType_instance),
					model.createProperty(Prefixes.event + additionalFields.get(i)),
					result.getJSONObject("event").getString(additionalFields.get(i)));
		}
		model.add(model.createResource(eventType_instance), model.createProperty(Prefixes.event + "latitude"),
				result.getJSONObject("event").getString("latitude"));
		model.add(model.createResource(eventType_instance), model.createProperty(Prefixes.event + "longitude"),
				result.getJSONObject("event").getString("longitude"));
		model.add(model.createResource(eventType_instance), model.createProperty(Prefixes.time + "inXSDDateTimeStamp"),
				result.getJSONObject("event").getString("timestamp").substring(0,
						result.getJSONObject("event").getString("timestamp").length() - 4) + "Z");

		for (int j = 0; j < copernicusSources.size(); j++) {
			
			for (int i = 0; i < result.getJSONArray(copernicusSources.get(j)).length(); i++) {

				uuid2 = UUID.randomUUID().toString().replaceAll("-", "");
                String product_instance = Prefixes.event + "Product_" + uuid2;
                System.out.println("This is needed: " + result.getJSONArray(copernicusSources.get(j)).getJSONObject(i));
                String uuidOrbit = result.getJSONArray(copernicusSources.get(j)).getJSONObject(i).getString("id").toString();//.replace("https://catalogue.onda-dias.eu/dias-catalogue/Products(", "").replace(")", "").replace(" ", "");// parse this to get the id and give to the function below to retrieve orbit and pass
                System.out.println("The uuid: " + uuidOrbit);
                System.out.println("The URL of the product: " + uuidOrbit);
                Integer flagException = 0;
				
				try{
	        		List<String> infoNames = Arrays.asList("relativeorbitnumber", "orbitdirection");
	        		List<String> results = getProductInfo(uuidOrbit, infoNames);
	        		System.out.println("The orbit number: " + results.get(0));
					System.out.println("The pass direction: " + results.get(2));
					System.out.println("The url: " + results.get(1));
					
					model.add(model.createResource(product_instance), model.createProperty(Prefixes.event + "productURL"),
							result.getJSONArray(copernicusSources.get(j)).getJSONObject(i).getString("productURL"));
					model.add(model.createResource(product_instance), model.createProperty(Prefixes.event + "source"),
							copernicusSources.get(j));
					model.add(model.createResource(product_instance), model.createProperty(Prefixes.event + "hasOrbit"),
							results.get(0).toString());
					model.add(model.createResource(product_instance), model.createProperty(Prefixes.event + "hasPassDirection"),
							results.get(2).toString());
				} catch (Exception  e) {
					flagException = 1;
				    System.out.println("I could not find information about this product: " + uuidOrbit + ". I will look in Onda Dias.");
				}

				if (flagException == 1){
					try{
	        			List<String> infoNames = Arrays.asList("relativeorbitnumber", "orbitdirection");
	        			List<String> results = getProductInfo2nd(uuidOrbit, infoNames);
	        			System.out.println("The orbit number: " + results.get(0));
						System.out.println("The pass direction: " + results.get(2));
						System.out.println("The url: " + results.get(1));

						model.add(model.createResource(product_instance), model.createProperty(Prefixes.event + "productURL"),
								result.getJSONArray(copernicusSources.get(j)).getJSONObject(i).getString("productURL"));
						model.add(model.createResource(product_instance), model.createProperty(Prefixes.event + "source"),
								copernicusSources.get(j));
						model.add(model.createResource(product_instance), model.createProperty(Prefixes.event + "hasOrbit"),
								results.get(0).toString());
						model.add(model.createResource(product_instance), model.createProperty(Prefixes.event + "hasPassDirection"),
								results.get(2).toString());
					} catch (Exception  e) {
						System.out.println("Orbit and Pass Direction do not exist from this data source");
					}
				}

				model.add(model.createResource(eventType_instance), model.createProperty(Prefixes.event + "product"),
						model.createResource(product_instance));
				model.add(model.createResource(product_instance), model.createProperty(Prefixes.rdf + "type"),
						model.createResource(Prefixes.event + "Product"));
				model.add(model.createResource(product_instance),
						model.createProperty(Prefixes.time + "inXSDDateTimeStamp"),
						result.getJSONArray(copernicusSources.get(j)).getJSONObject(i).getString("date"));
				model.add(model.createResource(product_instance), model.createProperty(Prefixes.event + "place"),
						result.getJSONArray(copernicusSources.get(j)).getJSONObject(i).getString("location"));
				model.add(model.createResource(product_instance), model.createProperty(Prefixes.event + "hasId"),
						result.getJSONArray(copernicusSources.get(j)).getJSONObject(i).getString("id"));
				
			}
		}

		return model;
	}

	public static void storeModel(Model model, String kb_address, String repository) {

		VirtModel virtualModel = VirtModel.openDatabaseModel(repository, kb_address, "dba", "mysecret");

		// Add model
		virtualModel.add(model);
		virtualModel.commit();
		virtualModel.close();
	}

    private static List<String> getProductInfo(String productUUID, List<String> infoNames) throws IOException {
		String path = "/config/configuration.json";
		BufferedReader bufferedReader;
		JsonElement conf = null;

		String username = "";
		String password = "";
		String baseURLNEW = "";
		try {
		    bufferedReader = new BufferedReader(new FileReader(path));
		    Gson gson = new Gson();
		    conf = gson.fromJson(bufferedReader, JsonElement.class);
		    for (int i = 0; i <
				conf.getAsJsonObject().get("sources").getAsJsonArray().size(); i++) {

    			 if
				 (conf.getAsJsonObject().get("sources").getAsJsonArray().get(i).getAsJsonObject().has("type"))
				{
					 if(conf.getAsJsonObject().get("sources").getAsJsonArray().get(i).getAsJsonObject().get("type").toString().replace("\"", "").equals("orbitPass"))
       					{
   						 	 JsonObject obj = new JsonObject();
    					 	 obj = conf.getAsJsonObject().get("sources").getAsJsonArray().get(i).getAsJsonObject();
    					 	 username = obj.get("username").toString();
    					 	 password = obj.get("password").toString();
    					 	 baseURLNEW = obj.get("dataSource").toString();
        				}
        		}
        	}		 

		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		}
					 
    
    URL url = new URL(baseURLNEW.replace("[productUUID]", productUUID.toString()).replace("\"", ""));
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");
    con.setRequestProperty("Content-Type", "application/json");

    // Add authentication credentials to request
    String authHelper = username.replace("\"","") + ":" + password.replace("\"","");
    String encodedCredentials = Base64.getEncoder().encodeToString((authHelper).getBytes());
    
    con.setRequestProperty("Authorization", "Basic " + encodedCredentials);

    int responseCode = con.getResponseCode();
    if (responseCode == HttpURLConnection.HTTP_OK) {
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        List<String> results = new ArrayList<String>();
        for (String infoName : infoNames) {
            String infoPatternString = "\"name\":\"" + infoName + "\",\"content\":\"([^\"]+)\"";
            Pattern infoPattern = Pattern.compile(infoPatternString);
            Matcher infoMatcher = infoPattern.matcher(response.toString());
            if (infoMatcher.find()) {
                String infoValue = infoMatcher.group(1);
                results.add(infoValue);
                results.add(url.toString());
            }
        }
        return results;
    } else {
        System.out.println("Error: " + responseCode);
        return null;
    }
}

    private static List<String> getProductInfo2nd(String productUUID, List<String> infoNames) throws IOException {
		String path = "/config/configuration.json";
		BufferedReader bufferedReader;
		JsonElement conf = null;

		String username = "";
		String password = "";
		String baseURLNEW = "";
		try {
		    bufferedReader = new BufferedReader(new FileReader(path));
		    Gson gson = new Gson();
		    conf = gson.fromJson(bufferedReader, JsonElement.class);
		    for (int i = 0; i <
				conf.getAsJsonObject().get("sources").getAsJsonArray().size(); i++) {

    			 if
				 (conf.getAsJsonObject().get("sources").getAsJsonArray().get(i).getAsJsonObject().has("type"))
				{
					 if(conf.getAsJsonObject().get("sources").getAsJsonArray().get(i).getAsJsonObject().get("type").toString().replace("\"", "").equals("orbitPass"))
       					{
   						 	 JsonObject obj = new JsonObject();
    					 	 obj = conf.getAsJsonObject().get("sources").getAsJsonArray().get(i).getAsJsonObject();
    					 	 username = obj.get("username").toString();
    					 	 password = obj.get("password").toString();
    					 	 baseURLNEW = obj.get("dataSource").toString();
        				}
        		}
        	}		 

		} catch (FileNotFoundException e) {
		    e.printStackTrace();
		}
					 
    
    URL url = new URL(baseURLNEW.replace("[productUUID]", productUUID.toString()).replace("\"", "").replace("colhub2", "colhub").replace("colhub3", "colhub"));
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");
    con.setRequestProperty("Content-Type", "application/json");

    // Add authentication credentials to request
    String authHelper = username.replace("\"","") + ":" + password.replace("\"","");
    String encodedCredentials = Base64.getEncoder().encodeToString((authHelper).getBytes());
    
    con.setRequestProperty("Authorization", "Basic " + encodedCredentials);

    int responseCode = con.getResponseCode();
    if (responseCode == HttpURLConnection.HTTP_OK) {
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        List<String> results = new ArrayList<String>();
        for (String infoName : infoNames) {
            String infoPatternString = "\"name\":\"" + infoName + "\",\"content\":\"([^\"]+)\"";
            Pattern infoPattern = Pattern.compile(infoPatternString);
            Matcher infoMatcher = infoPattern.matcher(response.toString());
            if (infoMatcher.find()) {
                String infoValue = infoMatcher.group(1);
                results.add(infoValue);
                results.add(url.toString());
            }
        }
        return results;
    } else {
        System.out.println("Error: " + responseCode);
        return null;
    }
	}



}
