package com.application.SemanticFramework;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class DataTranslator {

	public static JSONArray translateEvents(String results) {
		
		/*Acceptable data schema for events:
		[ 
		  {
		    "eventId": "22703251",
		    "depth": "19.7",
		    "latitude": "38.1076",
		    "magnitude": "5.2",
		    "timestamp": "2019-07-19T11:13:17.666000",
		    "longitude": "23.5393"
		  }
		]*/

		String[] parts = results.split("\n");
		String[] subparts;
		JSONObject translatedEvent;
		JSONArray translatedEvents = new JSONArray();

		for (int i = 1; i < parts.length; i++) {

			if (parts[i].contains("|")) {
				subparts = parts[i].split("\\|");

				translatedEvent = new JSONObject();
				translatedEvent.put("eventId", subparts[0]);
				translatedEvent.put("timestamp", subparts[1]);
				translatedEvent.put("latitude", subparts[2]);
				translatedEvent.put("longitude", subparts[3]);
				translatedEvent.put("depth", subparts[4]);
				translatedEvent.put("magnitude", subparts[10]);
				translatedEvents.put(translatedEvent);
			}
		}
		return translatedEvents;
	}

	public static JSONArray translateCopernicusMetadata(String results, String source) {

		/*Acceptable data schema for copernicus metadata:
		 * [
			  {
			    "date": "2019-07-30T15:29:18.175Z",
			    "location": "MULTIPOLYGON (((38.069313 21.988863, 40.469334 22.408005, 40.154675 24.031868, 37.724159 23.615938, 38.069313 21.988863)))",
			    "id": "f828a4fb-a93b-4cee-9185-e8dad26aa6eb",
			    "productURL": "https://scihub.copernicus.eu/dhus/odata/v1/Products('f828a4fb-a93b-4cee-9185-e8dad26aa6eb')/"
			  }
		   ]
		 */

		JsonObject jobject = new Gson().fromJson(results, JsonObject.class);

		JSONArray translatedProducts = new JSONArray();

		JSONObject product = new JSONObject();
		if (source.equals("dhus")) {
			if (jobject.getAsJsonObject("feed").has("entry")) {
				JsonArray entry = jobject.getAsJsonObject("feed").getAsJsonArray("entry");

				for (int i = 0; i < entry.size(); i++) {

					product = new JSONObject();

					product.put("id", entry.get(i).getAsJsonObject().get("id").getAsString());
					product.put("productURL", "https://scihub.copernicus.eu/dhus/odata/v1/Products('"
							+ entry.get(i).getAsJsonObject().get("id").getAsString() + "')/");
					for (int j = 0; j < entry.get(i).getAsJsonObject().getAsJsonArray("date").size(); j++) {
						if (entry.get(i).getAsJsonObject().getAsJsonArray("date").get(j).getAsJsonObject().get("name")
								.getAsString().equals("beginposition")) {
							product.put("date", entry.get(i).getAsJsonObject().getAsJsonArray("date").get(j)
									.getAsJsonObject().get("content").getAsString());
						}
					}
					for (int j = 0; j < entry.get(i).getAsJsonObject().getAsJsonArray("str").size(); j++) {
						if (entry.get(i).getAsJsonObject().getAsJsonArray("str").get(j).getAsJsonObject().get("name")
								.getAsString().equals("footprint")) {
							product.put("location", entry.get(i).getAsJsonObject().getAsJsonArray("str").get(j)
									.getAsJsonObject().get("content").getAsString());
						}
					}
					translatedProducts.put(product);
				}
			}
		} else {
			if (jobject.has("value")) {
				JsonArray entry = jobject.getAsJsonArray("value");

				for (int i = 0; i < entry.size(); i++) {

					product = new JSONObject();
					product.put("id", entry.get(i).getAsJsonObject().get("id").getAsString());
					product.put("productURL", "https://catalogue.onda-dias.eu/dias-catalogue/Products("
							+ entry.get(i).getAsJsonObject().get("id").getAsString() + ")");
					product.put("date", entry.get(i).getAsJsonObject().get("beginPosition").getAsString());
					product.put("location", entry.get(i).getAsJsonObject().get("footprint").getAsString());
					translatedProducts.put(product);
				}
			}
		}

		return translatedProducts;
	}

}
