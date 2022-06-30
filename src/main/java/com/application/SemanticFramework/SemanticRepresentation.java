package com.application.SemanticFramework;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.json.JSONObject;

import virtuoso.jena.driver.VirtModel;

public class SemanticRepresentation {

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

	public static Model resultsMapping(JSONObject result, String uuid, Model model, ArrayList<String> copernicusSources,
			String eventType, ArrayList<String> additionalFields) {

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
				model.add(model.createResource(product_instance), model.createProperty(Prefixes.event + "productURL"),
						result.getJSONArray(copernicusSources.get(j)).getJSONObject(i).getString("productURL"));
				model.add(model.createResource(product_instance), model.createProperty(Prefixes.event + "source"),
						copernicusSources.get(j));

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
}
