package com.application.SemanticFramework;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

import java.io.StringReader;
import com.google.gson.stream.JsonReader;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class SemanticRetrieval {

	// Retrieve the information that comply with the given criteria 
	public static JsonArray retrieve(String uuid, ArrayList<String> copernicusSources, String service, Logger logger,
			ArrayList<String> additionalFields) {

		JsonArray events = new JsonArray();
		JsonObject event = new JsonObject();

		String additionals = "";
		for (int i = 0; i < additionalFields.size(); i++) {
			additionals += "?e_event event:" + additionalFields.get(i) + " ?" + additionalFields.get(i) + ".\r\n";

		}

		String query;

		query = "prefix event:<http://purl.org/NET/c4dm/event.owl#>\r\n"
				+ "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "prefix time:<http://www.w3.org/2006/time#>\r\n"
				+ "prefix xsd:<http://www.w3.org/2001/XMLSchema#>\r\n" + "\r\n" + "select * " + "FROM <"
				+ MyResource.repository + "> \r\n" + "where {" + "?event rdf:type event:Event.\r\n"
				+ "OPTIONAL { ?event event:city ?city.}\r\n" + "OPTIONAL {?event event:country ?country.}\r\n"
				+ "?event time:year ?year.\r\n" + "OPTIONAL {?event time:month ?month.}\r\n"
				+ "OPTIONAL {?event time:day ?day.}\r\n" + "OPTIONAL {?event event:place ?place.}\r\n"
				+ "?event event:hasSubEvent ?e_event.\r\n" + "OPTIONAL{?e_event event:hasId ?id.}\r\n"
				+ "OPTIONAL {?e_event event:magnitude ?magnitude.}\r\n" + "OPTIONAL {?e_event event:depth ?depth.}\r\n"
				+ additionals + "?e_event event:latitude ?latitude.\r\n" + "?e_event event:longitude ?longitude.\r\n"
				+ "?e_event time:inXSDDateTimeStamp ?e_timestamp.\r\n" + "\r\n"
				+ " FILTER (?event=<http://purl.org/NET/c4dm/event.owl#Event_" + uuid + ">)\r\n" +

				"} \r\n";

		QueryExecution qe = QueryExecutionFactory.sparqlService(service, query);
		try {
			ResultSet results = qe.execSelect();
			for (; results.hasNext();) {
				event = new JsonObject();
				QuerySolution soln = results.nextSolution();

				event.addProperty("year", soln.get("year").toString());
				if (soln.contains("month")) {

					event.addProperty("month", soln.get("month").toString());
				}
				if (soln.contains("day")) {

					event.addProperty("day", soln.get("day").toString());
				}
				if (soln.contains("city")) {

					event.addProperty("city", soln.get("city").toString());
				}
				if (soln.contains("country")) {

					event.addProperty("country", soln.get("country").toString());
				}

				if (soln.contains("place")) {

					event.addProperty("location_coordinates", soln.get("place").toString());
				}

				JsonObject lat_value = new JsonObject();
				lat_value.addProperty("value", soln.get("latitude").toString());
				JsonObject values = new JsonObject();
				values.add("latitude", lat_value);
				JsonObject long_value = new JsonObject();
				long_value.addProperty("value", soln.get("longitude").toString());
				values.add("longitude", long_value);
				String timestamp = soln.get("e_timestamp").toString();
				String e_event = soln.get("e_event").toString();
				JsonArray images_before = new JsonArray();
				JsonArray images_after = new JsonArray();

				JsonArray orbit_after = new JsonArray();
				JsonArray pass_after = new JsonArray();
				JsonArray location_after = new JsonArray();

				JsonArray location_before = new JsonArray();
				JsonArray orbit_before = new JsonArray();
				JsonArray pass_before = new JsonArray();

				JsonArray timestamp_after = new JsonArray();
				JsonArray timestamp_before = new JsonArray();
				for (int i = 0; i < copernicusSources.size(); i++) {
					query = "prefix event:<http://purl.org/NET/c4dm/event.owl#>\r\n"
							+ "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
							+ "prefix time:<http://www.w3.org/2006/time#>\r\n"
							+ "prefix xsd:<http://www.w3.org/2001/XMLSchema#>\r\n" + "\r\n" + "select * " + "FROM <"
							+ MyResource.repository + "> \r\n" +

							"where {\r\n" + "?e_event time:inXSDDateTimeStamp ?e_timestamp.\r\n"
							+ "?e_event event:product ?product.\r\n" + "?product rdf:type event:Product. \r\n"
							+ "?product event:source ?p_src.\r\n" + "?product time:inXSDDateTimeStamp ?p_timestamp.\r\n"
							+ "?product event:place ?p_location.\r\n" + "?product event:hasId ?p_id.\r\n"
							+ "optional{?product event:hasOrbit ?orbit.}\r\n" + "optional{?product event:hasPassDirection ?pass.}\r\n"
							+ "optional{?product event:productURL ?p_url.}\r\n" + " FILTER (?e_event=<" + e_event + ">)\r\n"
							+ "FILTER (xsd:string(?p_src)=\"" + copernicusSources.get(i)
							+ "\"^^<http://www.w3.org/2001/XMLSchema#string>)" +

							"FILTER (xsd:dateTime(?e_timestamp)<=xsd:dateTime(?p_timestamp))\r\n" + "} \r\n"
							+ "ORDER BY ASC (?p_timestamp)\r\n";

							//+ "ORDER BY ASC (?p_timestamp)\r\n" + "LIMIT 1\r\n" + "";
					
					QueryExecution qe2 = QueryExecutionFactory.sparqlService(service, query);
					try {
						ResultSet results2 = qe2.execSelect();
						for (; results2.hasNext();) {
							QuerySolution soln2 = results2.nextSolution();

							images_after.add(soln2.get("p_url").toString());
							timestamp_after.add(soln2.get("p_timestamp").toString());
							orbit_after.add(soln2.get("orbit").toString());
							pass_after.add(soln2.get("pass").toString());
							location_after.add(soln2.get("p_location").toString());
							System.out.println("The orbit after data: " + orbit_after);
							System.out.println("The pass after data: " + pass_after);
							System.out.println("The location after data: " + location_after);
						}
					} catch (Exception e) {
						logger.info("Query error:" + e);

						System.out.println("Query error:" + e);
					} finally {
						qe2.close();
					}

					query = "prefix event:<http://purl.org/NET/c4dm/event.owl#>\r\n"
							+ "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
							+ "prefix time:<http://www.w3.org/2006/time#>\r\n"
							+ "prefix xsd:<http://www.w3.org/2001/XMLSchema#>\r\n" + "\r\n" +

							"select * " + "FROM <" + MyResource.repository + "> \r\n" +

							"where {\r\n" + "?e_event time:inXSDDateTimeStamp ?e_timestamp.\r\n"
							+ "?e_event event:product ?product.\r\n" + "?product rdf:type event:Product. \r\n"
							+ "?product event:source ?p_src.\r\n" + "?product time:inXSDDateTimeStamp ?p_timestamp.\r\n"
							+ "?product event:place ?p_location.\r\n" + "?product event:hasId ?p_id.\r\n"
							+ "optional{?product event:productURL ?p_url.}\r\n" + " FILTER (?e_event=<" + e_event + ">)\r\n"
							+ "optional{?product event:hasOrbit ?orbit.}\r\n" + "optional{?product event:hasPassDirection ?pass.}\r\n"
							
							+ "FILTER (xsd:dateTime(?p_timestamp)<=xsd:dateTime(?e_timestamp))\r\n"
							+ "FILTER (xsd:string(?p_src)=\"" + copernicusSources.get(i)
							+ "\"^^<http://www.w3.org/2001/XMLSchema#string>)" + "} \r\n"
							+ "ORDER BY ASC (?p_timestamp)\r\n";

							//+ "ORDER BY DESC (?p_timestamp)\r\n" + "LIMIT 1\r\n" + "" + "";
				    
					QueryExecution qe3 = QueryExecutionFactory.sparqlService(service, query);
					try {
						ResultSet results3 = qe3.execSelect();
						for (; results3.hasNext();) {

							QuerySolution soln3 = results3.nextSolution();

							images_before.add(soln3.get("p_url").toString());
							timestamp_before.add(soln3.get("p_timestamp").toString());
							orbit_before.add(soln3.get("orbit").toString());
							pass_before.add(soln3.get("pass").toString());
							location_before.add(soln3.get("p_location").toString());
							System.out.println("The orbit before data: " + orbit_before);
							System.out.println("The pass before data: " + pass_before);
							System.out.println("The location before data: " + location_before);
						}
					} catch (Exception e) {
						logger.info("Query error:" + e);

						System.out.println("Query error:" + e);
					} finally {
						qe3.close();
					}
				}
				JsonArray pairs_time = new JsonArray();
				try {
						
						for (int time=0; time <images_after.size(); time++) {

							if (orbit_after.get(time).toString().replace("\"", "").equals(orbit_before.get(time).toString().replace("\"", "")) && 
								pass_after.get(time).toString().replace("\"", "").equals(pass_before.get(time).toString().replace("\"", ""))){
								JsonParser jsonParser = new JsonParser();

								JsonObject before = new JsonObject();
								JsonObject after = new JsonObject();
								int helperTime = time + 1;
								String keybefore = "images_before" + helperTime;
								String keyafter = "image_after" + helperTime;
								before.addProperty("link", images_before.get(time).toString().replace("\"", ""));
								after.addProperty("link", images_after.get(time).toString().replace("\"", ""));
								before.addProperty("sensing_date", timestamp_before.get(time).toString().replace("\"", ""));
								after.addProperty("sensing_date", timestamp_after.get(time).toString().replace("\"", ""));
								before.addProperty("location", location_before.get(time).toString().replace("\"", ""));
								after.addProperty("location", location_after.get(time).toString().replace("\"", ""));
								before.addProperty("orbit_number", orbit_before.get(time).toString().replace("\"", ""));
								after.addProperty("orbit_number", orbit_after.get(time).toString().replace("\"", ""));
								before.addProperty("pass_direction", pass_before.get(time).toString().replace("\"", ""));
								after.addProperty("pass_direction", pass_after.get(time).toString().replace("\"", ""));

								event.add(keybefore, before);
								event.add(keyafter, after);

								pairs_time.add("before: " + images_before.get(time).toString().replace("\"", "") + " after:" + images_after.get(time).toString().replace("\"", ""));
							}
						}
					} catch (Exception e) {
						logger.info("Query error:" + e);
						System.out.println("Query error:" + e);
				}
				if (soln.contains("magnitude")) {
					JsonObject m_value = new JsonObject();
					m_value.addProperty("value", soln.get("magnitude").toString());
					event.add("magnitude", m_value);
				}
				if (soln.contains("depth")) {
					JsonObject d_value = new JsonObject();
					d_value.addProperty("value", soln.get("depth").toString());
					event.add("depth", d_value);
				}
				for (int i = 0; i < additionalFields.size(); i++) {
					if (soln.contains(additionalFields.get(i))) {
						event.addProperty(additionalFields.get(i), soln.get(additionalFields.get(i)).toString());
					}
				}
				event.add("epicentral_location", values);
				event.addProperty("timestamp", timestamp);

				events.add(event);
				event = new JsonObject();
			}
		} catch (Exception e) {
			logger.info("Query error:" + e);

			System.out.println("Query error:" + e);
		} finally {
			qe.close();
		}
		return events;
	}
}