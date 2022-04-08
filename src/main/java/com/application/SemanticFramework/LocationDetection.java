package com.application.SemanticFramework;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class LocationDetection {
	public static Model preprocessing(Logger logger) {

		try {
			String path = "/code/cities_countries_clear.json";

			File file = new File(path);
			FileInputStream fis;

			fis = new FileInputStream(file);

			byte[] data = new byte[(int) file.length()];
			fis.read(data);
			fis.close();

			String str = new String(data, "UTF-8");
			Gson gson = new Gson();

			JsonElement json = gson.fromJson(str, JsonElement.class);
			JsonArray object = json.getAsJsonArray();

			Model model = ModelFactory.createDefaultModel();
			model.setNsPrefix("geo", Prefixes.geo);
			model.setNsPrefix("rdfs", Prefixes.rdfs);
			model.setNsPrefix("rdf", Prefixes.rdf);

			for (int i = 0; i < object.size(); i++) {
				String city_instance = Prefixes.geo + "City_" + i;
				model.add(model.createResource(city_instance), model.createProperty(Prefixes.rdf + "type"),
						model.createResource(Prefixes.geo + "City"));
				model.add(model.createResource(city_instance), model.createProperty(Prefixes.rdfs + "label"),
						object.get(i).getAsJsonObject().get("name").getAsString());

				String country_instance = Prefixes.geo + "Country_" + i;
				model.add(model.createResource(city_instance), model.createProperty(Prefixes.geo + "belongsIn"),
						model.createResource(country_instance));
				model.add(model.createResource(country_instance), model.createProperty(Prefixes.rdf + "type"),
						model.createResource(Prefixes.geo + "Country"));
				model.add(model.createResource(country_instance), model.createProperty(Prefixes.rdfs + "label"),
						object.get(i).getAsJsonObject().get("country").getAsString());

				String location_instance = Prefixes.geo + "Location_" + i;
				model.add(model.createResource(city_instance), model.createProperty(Prefixes.geo + "isLocatedIn"),
						model.createResource(location_instance));
				model.add(model.createResource(location_instance), model.createProperty(Prefixes.rdf + "type"),
						model.createResource(Prefixes.geo + "Location"));
				model.add(model.createResource(location_instance), model.createProperty(Prefixes.geo + "hasLatitude"),
						object.get(i).getAsJsonObject().get("lat").getAsString());
				model.add(model.createResource(location_instance), model.createProperty(Prefixes.geo + "hasLongitude"),
						object.get(i).getAsJsonObject().get("lng").getAsString());
			}
			return model;
		} catch (IOException e) {

			logger.severe("Exception:" + e);
			e.printStackTrace();
		}
		return null;
	}

	public static String retrieveLatLong(String city, String country, String service, Logger logger) {

		String latitude = "null";
		String longitude = "null";

		String query = "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n"
				+ "prefix geo:<http://www.semanticweb.org/location-ontology#>\r\n"
				+ "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#>\r\n"
				+ "prefix xsd:<http://www.w3.org/2001/XMLSchema#>\r\n" + "" + "\r\n" + "select ?latitude ?longitude "
				+ "FROM <" + MyResource.geoRepository + "> \r\n" + "where {" + "?city rdf:type geo:City.\r\n"
				+ "?city rdfs:label ?city_lbl.\r\n" +

				"?city geo:belongsIn ?country.\r\n" + "?country rdf:type geo:Country.\r\n"
				+ "?country rdfs:label ?country_lbl.\r\n" +

				"?city geo:isLocatedIn ?location.\r\n" + "?location rdf:type geo:Location.\r\n"
				+ "?location geo:hasLatitude ?latitude.\r\n" + "?location geo:hasLongitude ?longitude.\r\n"
				+ " FILTER (xsd:string(?city_lbl)=xsd:string(\"" + city + "\"))\r\n"
				+ " FILTER (xsd:string(?country_lbl)=xsd:string(\"" + country + "\"))\r\n" + "} LIMIT 1\r\n";

		QueryExecution qe = QueryExecutionFactory.sparqlService(service, query);
		try {
			ResultSet results = qe.execSelect();
			for (; results.hasNext();) {

				QuerySolution soln = results.nextSolution();

				if (soln.contains("latitude") && soln.contains("longitude")) {

					latitude = soln.get("latitude").toString();
					longitude = soln.get("longitude").toString();
				}
			}
		} catch (Exception e) {
			logger.severe("Query error:" + e);
			System.out.println("Query error:" + e);
		} finally {
			qe.close();
		}
		return latitude + ":" + longitude;
	}

}
