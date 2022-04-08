package com.application.SemanticFramework;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.json.JSONArray;
import org.json.JSONObject;

public class SemanticRetrieval {

	public static JSONArray retrieve(String uuid, ArrayList<String> copernicusSources, String service, Logger logger) {

		JSONArray events = new JSONArray();
		JSONObject event = new JSONObject();

		String query;

		query = "prefix event:<http://purl.org/NET/c4dm/event.owl#>\r\n"
				+ "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "prefix time:<http://www.w3.org/2006/time#>\r\n"
				+ "prefix xsd:<http://www.w3.org/2001/XMLSchema#>\r\n" + "\r\n" + "select * " + "FROM <"
				+ MyResource.repository + "> \r\n" + "where {" + "?event rdf:type event:Event.\r\n"
				+ "OPTIONAL { ?event event:city ?city.}\r\n" + "OPTIONAL {?event event:country ?country.}\r\n"
				+ "?event time:year ?year.\r\n" + "OPTIONAL {?event time:month ?month.}\r\n"
				+ "OPTIONAL {?event time:day ?day.}\r\n" + "OPTIONAL {?event event:place ?place.}\r\n"
				+ "?event event:hasSubEvent ?e_event.\r\n" + "?e_event event:hasId ?id.\r\n"
				+ "?e_event event:magnitude ?magnitude.\r\n" + "?e_event event:depth ?depth.\r\n"
				+ "?e_event event:latitude ?latitude.\r\n" + "?e_event event:longitude ?longitude.\r\n"
				+ "?e_event time:inXSDDateTimeStamp ?e_timestamp.\r\n" + "\r\n"
				+ " FILTER (?event=<http://purl.org/NET/c4dm/event.owl#Event_" + uuid + ">)\r\n" +

				"} \r\n";

		QueryExecution qe = QueryExecutionFactory.sparqlService(service, query);
		try {
			ResultSet results = qe.execSelect();
			for (; results.hasNext();) {
				event = new JSONObject();
				QuerySolution soln = results.nextSolution();

				if (soln.contains("city")) {

					event.put("city", soln.get("city"));
				}
				if (soln.contains("country")) {

					event.put("country", soln.get("country"));
				}
				event.put("year", soln.get("year"));
				if (soln.contains("month")) {

					event.put("month", soln.get("month"));
				}
				if (soln.contains("day")) {

					event.put("day", soln.get("day"));
				}
				if (soln.contains("place")) {

					event.put("location_coordinates", soln.get("place"));
				}
				JSONObject m_value = new JSONObject();
				m_value.put("value", soln.get("magnitude"));
				event.put("magnitude", m_value);
				JSONObject d_value = new JSONObject();
				d_value.put("value", soln.get("depth"));
				event.put("depth", d_value);
				JSONObject lat_value = new JSONObject();
				lat_value.put("value", soln.get("latitude"));
				JSONObject values = new JSONObject();
				values.put("latitude", lat_value);
				JSONObject long_value = new JSONObject();
				long_value.put("value", soln.get("longitude"));

				values.put("longitude", long_value);
				event.put("epicentral_location", values);

				event.put("timestamp", soln.get("e_timestamp"));
				String e_event = soln.get("e_event").toString();
				JSONArray images_before = new JSONArray();
				JSONArray images_after = new JSONArray();
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
							+ "?product event:productURL ?p_url.\r\n" + " FILTER (?e_event=<" + e_event + ">)\r\n"
							+ "FILTER (xsd:string(?p_src)=\"" + copernicusSources.get(i)
							+ "\"^^<http://www.w3.org/2001/XMLSchema#string>)" +

							"FILTER (xsd:dateTime(?e_timestamp)<=xsd:dateTime(?p_timestamp))\r\n" + "} \r\n"
							+ "ORDER BY ASC (?p_timestamp)\r\n" + "LIMIT 1\r\n" + "";

					QueryExecution qe2 = QueryExecutionFactory.sparqlService(service, query);
					try {
						ResultSet results2 = qe2.execSelect();
						for (; results2.hasNext();) {
							QuerySolution soln2 = results2.nextSolution();

							images_after.put(soln2.get("p_url"));
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
							+ "?product event:productURL ?p_url.\r\n" + " FILTER (?e_event=<" + e_event + ">)\r\n"
							+ "FILTER (xsd:dateTime(?p_timestamp)<=xsd:dateTime(?e_timestamp))\r\n"
							+ "FILTER (xsd:string(?p_src)=\"" + copernicusSources.get(i)
							+ "\"^^<http://www.w3.org/2001/XMLSchema#string>)" + "} \r\n"
							+ "ORDER BY DESC (?p_timestamp)\r\n" + "LIMIT 1\r\n" + "" + "";

					QueryExecution qe3 = QueryExecutionFactory.sparqlService(service, query);
					try {
						ResultSet results3 = qe3.execSelect();
						for (; results3.hasNext();) {

							QuerySolution soln3 = results3.nextSolution();

							images_before.put(soln3.get("p_url"));
						}
					} catch (Exception e) {
						logger.info("Query error:" + e);

						System.out.println("Query error:" + e);
					} finally {
						qe3.close();
					}
				}
				event.put("image_before", images_before);
				event.put("image_after", images_after);
				events.put(event);
				event = new JSONObject();
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