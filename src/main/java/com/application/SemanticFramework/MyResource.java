
package com.application.SemanticFramework;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;

import java.net.*;
import java.io.*;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

@Path("/{task}")
public class MyResource {

	public static String repository;
	public static String geoRepository;

	@POST
	@Consumes(javax.ws.rs.core.MediaType.APPLICATION_JSON)
	@Produces("text/plain")

	public Response retrieval(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
			@PathParam("task") String task, String object) throws IOException{

		boolean has_search_role = false;
		boolean has_sfmanager_role = false;

		Logger logger = Logger.getLogger("MyLog");

		// Log file initialization
		FileHandler fh = null;
		try {
			File file = new File("/logs/logFile.log");
			file.setWritable(true);
			file.setReadable(true);
			file.setExecutable(true);

			fh = new FileHandler("/logs/logFile.log", true);

			logger.addHandler(fh);

			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);

		} catch (IOException e) {
			logger.severe("Exception:" + e);
			e.printStackTrace();
		}

		// Reading keycloak configuration
		JSONParser parser = new JSONParser();
		org.json.simple.JSONObject keycloak_json = new org.json.simple.JSONObject();
		try {
			keycloak_json = (org.json.simple.JSONObject) parser
					.parse(new FileReader("/config/keycloak_configuration.json"));
		} catch (FileNotFoundException e1) {
			logger.severe("Exception:" + e1);
			e1.printStackTrace();
		} catch (IOException e1) {
			logger.severe("Exception:" + e1);
			e1.printStackTrace();
		} catch (ParseException e1) {
			logger.severe("Exception:" + e1);
			e1.printStackTrace();
		}

		String kc_address = keycloak_json.get("URL").toString();
		String realm = keycloak_json.get("realm").toString();
		String client_name = keycloak_json.get("client_name").toString();
		String adminUsername = keycloak_json.get("adminUsername").toString();
		String adminPassword = keycloak_json.get("adminPassword").toString();

		if (authorization == null) {

			String resp = "Basic authentication is required.";
			JsonObject error = new JsonObject();
			error.addProperty("error", resp);
			logger.info("[Response code]: 200, [Response]: " + error + "\n");
			fh.close();
			return Response.status(200).entity(error.toString()).build();
		}
		// Check user authentication
		if (authorization.length() > 0) {

			byte[] decoded = Base64.getDecoder().decode(authorization.replace("Basic ", ""));
			String decodedStr = new String(decoded, StandardCharsets.UTF_8);

			if (decodedStr.length() < 3) {
				String response = "Invalid user credentials.";
				JsonObject error = new JsonObject();
				error.addProperty("error", response);
				logger.info("[Response code]: 200, [Response]: " + error + "\n");
				fh.close();
				return Response.status(200).entity(error.toString()).build();
			}
			String[] parts = decodedStr.split(":");
			String username = parts[0];
			String password = parts[1];

			String userid = "null";
			String token = "";
			OkHttpClient client1 = new OkHttpClient().newBuilder().build();
			MediaType mediaType1 = MediaType.parse("application/x-www-form-urlencoded");
			RequestBody rbody1 = RequestBody.create(mediaType1,
					"grant_type=password&client_id=" + client_name + "&client_secret="
							+ keycloak_json.get("client_secret").toString() + "&password=" + password + "&username="
							+ username + "");
			Request request1 = new Request.Builder()
					.url(kc_address + "realms/" + realm + "/protocol/openid-connect/token").method("POST", rbody1)
					.addHeader("Content-Type", "application/x-www-form-urlencoded").build();

			okhttp3.Response response1;
			try {
				response1 = client1.newCall(request1).execute();
				String respbody1 = response1.body().string();

				JSONObject jresponse1 = new JSONObject(respbody1);

				System.out.println(jresponse1);
				logger.info("[Response]: " + jresponse1 + "\n");
				if (jresponse1.has("error_description")) {

					String resp = jresponse1.get("error_description") + ".";
					JsonObject error = new JsonObject();
					error.addProperty("error", resp);
					logger.info("[Response code]: 200, [Response]: " + error + "\n");
					fh.close();
					return Response.status(200).entity(error.toString()).build();

				}
				OkHttpClient client = new OkHttpClient().newBuilder().build();
				MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
				RequestBody rbody = RequestBody.create(mediaType,
						"grant_type=password&client_id=" + client_name + "&client_secret="
								+ keycloak_json.get("client_secret").toString() + "&password=" + adminPassword
								+ "&username=" + adminUsername);
				Request request = new Request.Builder()
						.url(kc_address + "realms/" + realm + "/protocol/openid-connect/token").method("POST", rbody)
						.addHeader("Content-Type", "application/x-www-form-urlencoded").build();
				okhttp3.Response keycloakresponse;

				keycloakresponse = client.newCall(request).execute();
				String respbody = keycloakresponse.body().string();

				System.out.println(respbody);
				logger.info("[Response]: " + respbody + "\n");

				JSONObject jresponse = new JSONObject(respbody);

				token = jresponse.getString("access_token");

				Unirest.setTimeouts(0, 0);
				HttpResponse<String> response2 = Unirest.get(kc_address + "admin/realms/" + realm + "/users")
						.header("Authorization", "Bearer " + token).asString();
				JsonArray entry = new Gson().fromJson(response2.getBody(), JsonArray.class);

				boolean found = false;
				for (int i = 0; i < entry.size(); i++) {

					if (username.equals(entry.get(i).getAsJsonObject().get("username").getAsString())) {

						found = true;
						userid = entry.get(i).getAsJsonObject().get("id").getAsString();
					}
				}
				if (!found) {

					String resp = "No user found for username '" + username + "'. ";
					JsonObject error = new JsonObject();
					error.addProperty("error", resp);
					logger.info("[Response code]: 200, [Response]: " + error + "\n");
					fh.close();
					return Response.status(200).entity(error.toString()).build();

				}
				Unirest.setTimeouts(0, 0);
				HttpResponse<String> response3 = Unirest
						.get(kc_address + "admin/realms/" + realm + "/users/" + userid + "/role-mappings/clients/"
								+ keycloak_json.get("client_container_id").toString())
						.header("Authorization", "Bearer " + token).asString();
				logger.info("[Response]: " + response3 + "\n");

				JsonArray role = new Gson().fromJson(response3.getBody(), JsonArray.class);

				for (int i = 0; i < role.size(); i++) {

					if (role.get(i).getAsJsonObject().get("name").getAsString().equals("Search")) {

						has_search_role = true;
					}
					if (role.get(i).getAsJsonObject().get("name").getAsString().equals("Semantic Framework Manager")) {

						has_sfmanager_role = true;
					}

				}

			} catch (IOException e2) {
				logger.severe("Exception:" + e2);
				e2.printStackTrace();
			} catch (UnirestException e) {
				logger.severe("Exception:" + e);
				e.printStackTrace();
			}

		} else {
			String resp = "Basic authentication is required.";
			JsonObject error = new JsonObject();
			error.addProperty("error", resp);
			logger.info("[Response code]: 200, [Response]: " + error + "\n");
			fh.close();
			return Response.status(200).entity(error.toString()).build();
		}

		repository = System.getenv("KB_ADDRESS_REPO") + "SentinelRepo";
		geoRepository = System.getenv("KB_ADDRESS_REPO") + "GeoRepo";

		String kb_address_retrieve = System.getenv("KB_ADDRESS_RETRIEVE");

		if (task.equals("retrieve")) {
			if (!has_search_role) {
				String resp = "The user has no assigned 'Search' role.";
				JsonObject error = new JsonObject();
				error.addProperty("error", resp);
				logger.info("[Response code]: 401, [Response]: " + error + "\n");
				fh.close();
				return Response.status(401).entity(error.toString()).build();

			}

			String kb_address = System.getenv("KB_ADDRESS");
			String response;
			Gson gson = new Gson();

			JsonElement json = gson.fromJson(object, JsonElement.class);
			JsonObject jobject = json.getAsJsonObject();


			//Some processing to get the input from Valadis
			String inputText = "";
			try{
				JsonObject jobjectNLP = jobject.get("nlp").getAsJsonObject();
				System.out.println("NLPjson:" + jobject.get("nlp"));
				inputText = jobjectNLP.get("event").toString().replace("\"", "") + " located in ";
				
				if (jobjectNLP.get("point").toString().replace("\"", "").equals("false")){

				if (!jobjectNLP.get("city").toString().replace("\"", "").equals("null")){
					if(!jobjectNLP.get("country").toString().replace("\"", "").equals("null")){
					inputText = inputText + jobjectNLP.get("city").toString().replace("\"", "") + ", " + jobjectNLP.get("country").toString().replace("\"", "");
				}
				}
				if (jobjectNLP.get("country").toString().replace("\"", "").equals("null")) {
					if (!jobjectNLP.get("city").toString().replace("\"", "").equals("null")){
					inputText = inputText + jobjectNLP.get("city").toString().replace("\"", "");
				}
				} 
				if (!jobjectNLP.get("country").toString().replace("\"", "").equals("null")) {
					if(jobjectNLP.get("city").toString().replace("\"", "").equals("null")){
					inputText = inputText + jobjectNLP.get("country").toString().replace("\"", "");
					}
				}

				}
				else{
					inputText = inputText + "POINT (" + jobjectNLP.get("latitude").toString().replace("\"", "")
					+ " " +  jobjectNLP.get("lognitude").toString().replace("\"", "") + ")";
				}

				if (!jobjectNLP.get("year").toString().replace("\"", "").equals("null")) {
					inputText = inputText + " in " + jobjectNLP.get("year").toString().replace("\"", "");
				}
				if (!jobjectNLP.get("month").toString().replace("\"", "").equals("null")) {
					if (jobjectNLP.get("month").toString().replace("\"", "").length() > 1){
					inputText = inputText + " " + jobjectNLP.get("month").toString().replace("\"", "");
					}
					else{
						inputText = inputText + " 0" + jobjectNLP.get("month").toString().replace("\"", "");
					}
				}
				if (!jobjectNLP.get("day").toString().replace("\"", "").equals("null")) {
					if (jobjectNLP.get("day").toString().replace("\"", "").length() > 1){
					inputText = inputText + " " + jobjectNLP.get("day").toString().replace("\"", "");
					}
					else{
						inputText = inputText + " 0" + jobjectNLP.get("day").toString().replace("\"", "");
					}
				}
				if (!jobjectNLP.get("magnitude").toString().replace("\"", "").equals("null")) {
					if (jobjectNLP.get("comparative").toString().replace("\"", "").equals(">=") || jobjectNLP.get("comparative").toString().replace("\"", "").equals(">")){
						inputText = inputText + " with magnitude greater than " + jobjectNLP.get("magnitude").toString().replace("\"", "");
					}
					else if (jobjectNLP.get("comparative").toString().replace("\"", "").equals("=")){
						inputText = inputText + " with magnitude " + jobjectNLP.get("magnitude").toString().replace("\"", "");
					}
					else if (jobjectNLP.get("comparative").toString().replace("\"", "").equals("<=") || jobjectNLP.get("comparative").toString().replace("\"", "").equals("<")){
						inputText = inputText + " with magnitude less than " + jobjectNLP.get("magnitude").toString().replace("\"", "");
					}
				}
				System.out.println("Current text: " + inputText);
			}
			catch (Exception e) {
				System.out.println("Some error sry Alex");
			}
			//
			//String input = jobject.get("text").getAsString();
			String input = inputText;
			String pagenumber = jobject.get("page").getAsString();

			String city = "null", country = "null", year = "null", month = "null", day = "null", magnitude = "5.0";
			String latitude = "null", longitude = "null";

			String path = "/config/configuration.json";
			BufferedReader bufferedReader;
			JsonElement conf = null;
			try {
				bufferedReader = new BufferedReader(new FileReader(path));
				conf = gson.fromJson(bufferedReader, JsonElement.class);
				System.out.println("The conf: " + conf);
			} catch (FileNotFoundException e) {

				logger.severe("Exception:" + e);
				e.printStackTrace();
			}
			ArrayList<String> copernicusSources = new ArrayList<String>();
			ArrayList<String> copernicusAddresses = new ArrayList<String>();
			ArrayList<String> copernicusUsername = new ArrayList<String>();
			ArrayList<String> copernicusPassword = new ArrayList<String>();

			ArrayList<String> additionalFields = new ArrayList<String>();
			String eventSource = "", eventUsername = "", eventPassword = "", associatedId = "";
			String eventType = input.split(" ")[0];
			String resultsperpage = conf.getAsJsonObject().get("resultsPerPage").getAsString();
			// Matching event type with the corresponding Copernicus data sources and
			// products
			for (int i = 0; i < conf.getAsJsonObject().get("sources").getAsJsonArray().size(); i++) {

				if (conf.getAsJsonObject().get("sources").getAsJsonArray().get(i).getAsJsonObject().has("eventType")) {
					JsonObject obj = new JsonObject();
					obj = conf.getAsJsonObject().get("sources").getAsJsonArray().get(i).getAsJsonObject();
					System.out.println("Object json: " + obj);
					if (input.startsWith(obj.get("eventType").getAsString())) {
						eventSource = obj.get("dataSource").getAsString();
						eventUsername = obj.get("username").getAsString();
						eventPassword = obj.get("password").getAsString();
						associatedId = obj.get("associatedId").getAsString();

						if (obj.has("additionalFields")) {
							JsonArray additional = obj.get("additionalFields").getAsJsonArray();

							// Creating an empty ArrayList of type Object
							additionalFields = new ArrayList<String>();

							// Checking whether the JSON array has some value or not
							if (additional != null) {

								// Iterating JSON array
								for (int j = 0; j < additional.size(); j++) {

									// Adding each element of JSON array into ArrayList
									additionalFields.add(additional.get(j).getAsString());
								}
							}
						}
					}
				}
			}

			if (associatedId.length() == 0) {
				response = "Invalid eventType. EventType should match the configuration file.";
				JsonObject error = new JsonObject();
				error.addProperty("error", response);
				logger.info("[Response code]: 200, [Response]: " + error + "\n");
				fh.close();
				return Response.status(200).entity(error.toString()).build();
			} else {
				for (int i = 0; i < conf.getAsJsonObject().get("sources").getAsJsonArray().size(); i++) {
					if (conf.getAsJsonObject().get("sources").getAsJsonArray().get(i).getAsJsonObject().has("type")) {
						JsonObject obj = new JsonObject();
						obj = conf.getAsJsonObject().get("sources").getAsJsonArray().get(i).getAsJsonObject();

						if (obj.get("type").getAsString().equals("copernicus")
								&& obj.get("associatedId").getAsString().equals(associatedId)) {
							if (obj.get("dataSource").getAsString().contains("[DHuSAddress]")
									&& obj.has("DHuSAddress")) {
								copernicusSources.add(obj.get("dataSource").getAsString().replace("[DHuSAddress]",
										obj.get("DHuSAddress").getAsString()));
								copernicusAddresses.add(obj.get("DHuSAddress").getAsString());
							} else {
								copernicusSources.add(obj.get("dataSource").getAsString());
								copernicusAddresses.add("null");
							}
							copernicusUsername.add(obj.get("username").getAsString());
							copernicusPassword.add(obj.get("password").getAsString());
						}
					}
				}
			}

			String uuid = UUID.randomUUID().toString().replaceAll("-", "");
			//New errors by NLP
			JsonObject jobjectNLPerror = jobject.get("nlp").getAsJsonObject();
			if (jobjectNLPerror.get("magnitude").toString().replace("\"", "").equals("9999912")) {
					response = "The query contains more than one magnitude references";
					JsonObject error = new JsonObject();
					error.addProperty("error", response);
					logger.info("[Response code]: 200, [Response]: " + error + "\n");
					fh.close();
					return Response.status(200).entity(error.toString()).build();
				}
			else if (jobjectNLPerror.get("magnitude").toString().replace("\"", "").equals("9999913")) {
					response = "Unexpected error related to magnitude input";
					JsonObject error = new JsonObject();
					error.addProperty("error", response);
					logger.info("[Response code]: 200, [Response]: " + error + "\n");
					fh.close();
					return Response.status(200).entity(error.toString()).build();
				}
			else if (jobjectNLPerror.get("magnitude").toString().replace("\"", "").equals("9999914")) {
					response = "Magnitude format error: decimal separator should be a point (e.g. 6.2)";
					JsonObject error = new JsonObject();
					error.addProperty("error", response);
					logger.info("[Response code]: 200, [Response]: " + error + "\n");
					fh.close();
					return Response.status(200).entity(error.toString()).build();
				}

			if (jobjectNLPerror.get("event").toString().replace("\"", "").equals("9999921")) {
					response = "The query does not refer to an earthquake event";
					JsonObject error = new JsonObject();
					error.addProperty("error", response);
					logger.info("[Response code]: 200, [Response]: " + error + "\n");
					fh.close();
					return Response.status(200).entity(error.toString()).build();
				}
			else if (jobjectNLPerror.get("event").toString().replace("\"", "").equals("9999922")) {
					response = "Unexpected error related to earthquake event relatedness";
					JsonObject error = new JsonObject();
					error.addProperty("error", response);
					logger.info("[Response code]: 200, [Response]: " + error + "\n");
					fh.close();
					return Response.status(200).entity(error.toString()).build();
				}

			if (jobjectNLPerror.get("comparative").toString().replace("\"", "").equals("9999951")) {
					response = "The query contains more than one comparative adjective (>, <, =, >=, <=)";
					JsonObject error = new JsonObject();
					error.addProperty("error", response);
					logger.info("[Response code]: 200, [Response]: " + error + "\n");
					fh.close();
					return Response.status(200).entity(error.toString()).build();
				}
			else if (jobjectNLPerror.get("comparative").toString().replace("\"", "").equals("9999953")) {
					response = "The query contains more than symbol comparative adjective";
					JsonObject error = new JsonObject();
					error.addProperty("error", response);
					logger.info("[Response code]: 200, [Response]: " + error + "\n");
					fh.close();
					return Response.status(200).entity(error.toString()).build();
				}
			else if (jobjectNLPerror.get("comparative").toString().replace("\"", "").equals("9999954")) {
					response = "Unexpected error related to the comparative adjective input";
					JsonObject error = new JsonObject();
					error.addProperty("error", response);
					logger.info("[Response code]: 200, [Response]: " + error + "\n");
					fh.close();
					return Response.status(200).entity(error.toString()).build();
				}


			if (jobjectNLPerror.get("year").toString().replace("\"", "").equals("9999931")) {
					response = "The query contains a date in a wrong format	(year)";
					JsonObject error = new JsonObject();
					error.addProperty("error", response);
					logger.info("[Response code]: 200, [Response]: " + error + "\n");
					fh.close();
					return Response.status(200).entity(error.toString()).build();
				}
			else if (jobjectNLPerror.get("year").toString().replace("\"", "").equals("9999932")) {
					response = "No date reference in the query: year is mandatory (e.g. 2018)";
					JsonObject error = new JsonObject();
					error.addProperty("error", response);
					logger.info("[Response code]: 200, [Response]: " + error + "\n");
					fh.close();
					return Response.status(200).entity(error.toString()).build();
				}
			else if (jobjectNLPerror.get("year").toString().replace("\"", "").equals("9999933")) {
					response = "The query contains more than one date references";
					JsonObject error = new JsonObject();
					error.addProperty("error", response);
					logger.info("[Response code]: 200, [Response]: " + error + "\n");
					fh.close();
					return Response.status(200).entity(error.toString()).build();
				}
			else if (jobjectNLPerror.get("year").toString().replace("\"", "").equals("9999934")) {
					response = "Unexpected error related to date reference input";
					JsonObject error = new JsonObject();
					error.addProperty("error", response);
					logger.info("[Response code]: 200, [Response]: " + error + "\n");
					fh.close();
					return Response.status(200).entity(error.toString()).build();
				}


			if (jobjectNLPerror.get("city").toString().replace("\"", "").equals("9999941") && jobjectNLPerror.get("country").toString().replace("\"", "").equals("9999941")) {
					response = "No event location reference in the query: country is mandatory (e.g. Italy)";
					JsonObject error = new JsonObject();
					error.addProperty("error", response);
					logger.info("[Response code]: 200, [Response]: " + error + "\n");
					fh.close();
					return Response.status(200).entity(error.toString()).build();
				}
			else if (jobjectNLPerror.get("city").toString().replace("\"", "").equals("9999942") && jobjectNLPerror.get("country").toString().replace("\"", "").equals("9999942")) {
					response = "The query contains more than one city or country";
					JsonObject error = new JsonObject();
					error.addProperty("error", response);
					logger.info("[Response code]: 200, [Response]: " + error + "\n");
					fh.close();
					return Response.status(200).entity(error.toString()).build();
				}
			else if (jobjectNLPerror.get("city").toString().replace("\"", "").equals("9999943") && jobjectNLPerror.get("country").toString().replace("\"", "").equals("9999943")) {
					response = "The query contains more than one country";
					JsonObject error = new JsonObject();
					error.addProperty("error", response);
					logger.info("[Response code]: 200, [Response]: " + error + "\n");
					fh.close();
					return Response.status(200).entity(error.toString()).build();
				}
			else if (jobjectNLPerror.get("city").toString().replace("\"", "").equals("9999944") && jobjectNLPerror.get("country").toString().replace("\"", "").equals("9999944")) {
					response = "Unexpected error related to country or city references";
					JsonObject error = new JsonObject();
					error.addProperty("error", response);
					logger.info("[Response code]: 200, [Response]: " + error + "\n");
					fh.close();
					return Response.status(200).entity(error.toString()).build();
				}

			// Keyword understanding and error handling
			if (input.startsWith("earthquake located in ")) {
				String parsed = input;
				parsed = parsed.replace("earthquake located in ", "");
				List<String> keywordList = Arrays.asList(parsed.split(" "));

				if (keywordList.size() > 0 && !keywordList.get(0).isEmpty()) {
					if (keywordList.contains("with") && (!keywordList.contains("magnitude")
							|| !keywordList.contains("greater") || !keywordList.contains("than")
							|| !isDouble(keywordList.get(keywordList.size() - 1))
							|| !hasOneDecimalPoint(keywordList.get(keywordList.size() - 1)))) {
						response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
								+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
								+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
								+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
								+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
								+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
						JsonObject error = new JsonObject();
						error.addProperty("error", response);
						logger.info("[Response code]: 200, [Response]: " + error + "\n");
						fh.close();
						return Response.status(200).entity(error.toString()).build();
					}
					if (keywordList.contains("magnitude") && (!keywordList.contains("with")
							|| !keywordList.contains("greater") || !keywordList.contains("than")
							|| !isDouble(keywordList.get(keywordList.size() - 1))
							|| !hasOneDecimalPoint(keywordList.get(keywordList.size() - 1)))) {
						response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
								+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
								+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
								+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
								+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
								+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
						JsonObject error = new JsonObject();
						error.addProperty("error", response);
						logger.info("[Response code]: 200, [Response]: " + error + "\n");
						fh.close();
						return Response.status(200).entity(error.toString()).build();
					}
					if (keywordList.contains("greater") && (!keywordList.contains("magnitude")
							|| !keywordList.contains("with") || !keywordList.contains("than")
							|| !isDouble(keywordList.get(keywordList.size() - 1))
							|| !hasOneDecimalPoint(keywordList.get(keywordList.size() - 1)))) {
						response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
								+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
								+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
								+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
								+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
								+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
						JsonObject error = new JsonObject();
						error.addProperty("error", response);
						logger.info("[Response code]: 200, [Response]: " + error + "\n");
						fh.close();
						return Response.status(200).entity(error.toString()).build();
					}
					if (keywordList.contains("than") && (!keywordList.contains("magnitude")
							|| !keywordList.contains("greater") || !keywordList.contains("with")
							|| !isDouble(keywordList.get(keywordList.size() - 1))
							|| !hasOneDecimalPoint(keywordList.get(keywordList.size() - 1)))) {
						response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
								+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
								+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
								+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
								+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
								+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
						JsonObject error = new JsonObject();
						error.addProperty("error", response);
						logger.info("[Response code]: 200, [Response]: " + error + "\n");
						fh.close();
						return Response.status(200).entity(error.toString()).build();
					}

					if (keywordList.get(0).contains(",")) {
						if (keywordList.get(0).toString().charAt(keywordList.get(0).toString().length() - 1) != ',') {
							response = "Space expected after comma. Input should follow the following pattern: "
									+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
									+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
									+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
									+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
									+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
							JsonObject error = new JsonObject();
							error.addProperty("error", response);
							logger.info("[Response code]: 200, [Response]: " + error + "\n");
							fh.close();
							return Response.status(200).entity(error.toString()).build();
						}

						city = keywordList.get(0).replaceAll(",", "");

						if (keywordList.size() < 2) {
							response = "Country expected after city. Input should follow the following pattern: "
									+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
									+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
									+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
									+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
									+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
							JsonObject error = new JsonObject();
							error.addProperty("error", response);
							logger.info("[Response code]: 200, [Response]: " + error + "\n");
							fh.close();
							return Response.status(200).entity(error.toString()).build();
						}

						if (!keywordList.get(1).equals("in")) {
							int counter_country = 0;
							for (int i = 1; i < keywordList.size(); i++) {
								if (keywordList.get(i).equals("in")) {
									country = "";
								}
							}
							for (int i = 1; i < keywordList.size(); i++) {
								if (keywordList.get(i).equals("in")) {

									counter_country = i - 2;
									country = country.substring(1);
									break;
								}

								country = country + " " + keywordList.get(i);
							}

							if (keywordList.size() < counter_country + 3) {

								response = "Expected 'in' keyword after city, country. Input should follow the following pattern: "
										+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
										+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
										+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
										+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
										+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
								JsonObject error = new JsonObject();
								error.addProperty("error", response);
								logger.info("[Response code]: 200, [Response]: " + error + "\n");
								fh.close();
								return Response.status(200).entity(error.toString()).build();
							}
							if (keywordList.get(counter_country + 2).equals("in")) {
								if (keywordList.size() < counter_country + 4) {
									response = "Expected 4-digit integer for 'year'. Input should follow the following pattern: "
											+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
											+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
											+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
											+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
											+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
									JsonObject error = new JsonObject();
									error.addProperty("error", response);
									logger.info("[Response code]: 200, [Response]: " + error + "\n");
									fh.close();
									return Response.status(200).entity(error.toString()).build();
								}
								if (isInteger(keywordList.get(counter_country + 3))
										&& keywordList.get(counter_country + 3).toString().length() == 4) {

									year = keywordList.get(counter_country + 3);
									if (keywordList.size() == counter_country + 5) {
										if (keywordList.get(counter_country + 4).equals("with")) {

											response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
													+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
													+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
													+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
													+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
													+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
											JsonObject error = new JsonObject();
											error.addProperty("error", response);
											logger.info("[Response code]: 200, [Response]: " + error + "\n");
											fh.close();
											return Response.status(200).entity(error.toString()).build();
										}
										if (!isInteger(keywordList.get(counter_country + 4))
												|| keywordList.get(counter_country + 4).toString().length() != 2) {

											response = "Expected 2-digit integer for 'month'. Input should follow the following pattern: "
													+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
													+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
													+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
													+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
													+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
											JsonObject error = new JsonObject();
											error.addProperty("error", response);
											logger.info("[Response code]: 200, [Response]: " + error + "\n");
											fh.close();
											return Response.status(200).entity(error.toString()).build();
										}

									}
									if (keywordList.size() >= counter_country + 5) {
										if (!keywordList.get(counter_country + 4).equals("with")) {

											if (!isInteger(keywordList.get(counter_country + 4))
													|| keywordList.get(counter_country + 4).toString().length() != 2) {

												response = "Expected 2-digit integer for 'month'. Input should follow the following pattern: "
														+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
														+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
														+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
														+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
														+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
												JsonObject error = new JsonObject();
												error.addProperty("error", response);
												logger.info("[Response code]: 200, [Response]: " + error + "\n");
												fh.close();
												return Response.status(200).entity(error.toString()).build();
											}
										}
									}
									if (keywordList.size() == counter_country + 6) {
										if (keywordList.get(counter_country + 5).equals("with")) {

											response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
													+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
													+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
													+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
													+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
													+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
											JsonObject error = new JsonObject();
											error.addProperty("error", response);
											logger.info("[Response code]: 200, [Response]: " + error + "\n");
											fh.close();
											return Response.status(200).entity(error.toString()).build();
										}
										if (!isInteger(keywordList.get(counter_country + 5))
												|| keywordList.get(counter_country + 5).toString().length() != 2) {

											response = "Expected 2-digit integer for 'day'. Input should follow the following pattern: "
													+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
													+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
													+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
													+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
													+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
											JsonObject error = new JsonObject();
											error.addProperty("error", response);
											logger.info("[Response code]: 200, [Response]: " + error + "\n");
											fh.close();
											return Response.status(200).entity(error.toString()).build();

										}
									}

									if (keywordList.contains("with") && (!keywordList.contains("magnitude")
											|| !keywordList.contains("greater") || !keywordList.contains("than")
											|| !isDouble(keywordList.get(keywordList.size() - 1))
											|| !hasOneDecimalPoint(keywordList.get(keywordList.size() - 1)))) {
										response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
												+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
												+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
												+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
												+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
												+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
										JsonObject error = new JsonObject();
										error.addProperty("error", response);
										logger.info("[Response code]: 200, [Response]: " + error + "\n");
										fh.close();
										return Response.status(200).entity(error.toString()).build();
									}
									if (keywordList.contains("magnitude") && (!keywordList.contains("with")
											|| !keywordList.contains("greater") || !keywordList.contains("than")
											|| !isDouble(keywordList.get(keywordList.size() - 1))
											|| !hasOneDecimalPoint(keywordList.get(keywordList.size() - 1)))) {
										response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
												+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
												+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
												+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
												+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
												+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
										JsonObject error = new JsonObject();
										error.addProperty("error", response);
										logger.info("[Response code]: 200, [Response]: " + error + "\n");
										fh.close();
										return Response.status(200).entity(error.toString()).build();
									}
									if (keywordList.contains("greater") && (!keywordList.contains("magnitude")
											|| !keywordList.contains("with") || !keywordList.contains("than")
											|| !isDouble(keywordList.get(keywordList.size() - 1))
											|| !hasOneDecimalPoint(keywordList.get(keywordList.size() - 1)))) {
										response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
												+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
												+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
												+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
												+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
												+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
										JsonObject error = new JsonObject();
										error.addProperty("error", response);
										logger.info("[Response code]: 200, [Response]: " + error + "\n");
										fh.close();
										return Response.status(200).entity(error.toString()).build();
									}
									if (keywordList.contains("than") && (!keywordList.contains("magnitude")
											|| !keywordList.contains("greater") || !keywordList.contains("with")
											|| !isDouble(keywordList.get(keywordList.size() - 1))
											|| !hasOneDecimalPoint(keywordList.get(keywordList.size() - 1)))) {
										response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
												+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
												+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
												+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
												+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
												+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
										JsonObject error = new JsonObject();
										error.addProperty("error", response);
										logger.info("[Response code]: 200, [Response]: " + error + "\n");
										fh.close();
										return Response.status(200).entity(error.toString()).build();
									}

									if (keywordList.size() >= counter_country + 5) {

										if (isInteger(keywordList.get(counter_country + 4))
												&& keywordList.get(counter_country + 4).toString().length() == 2) {

											month = keywordList.get(counter_country + 4);
											if (Integer.parseInt(month) < 1 || Integer.parseInt(month) > 12) {
												response = "Month expected after year as an integer value between 1 and 12.";
												JsonObject error = new JsonObject();
												error.addProperty("error", response);
												logger.info("[Response code]: 200, [Response]: " + error + "\n");
												fh.close();
												return Response.status(200).entity(error.toString()).build();
											}
											if (keywordList.size() > counter_country + 5) {
												if (!keywordList.get(counter_country + 5).equals("with")) {
													if (!isInteger(keywordList.get(counter_country + 5)) || keywordList
															.get(counter_country + 5).toString().length() != 2) {

														response = "Expected 2-digit integer for 'day'. Input should follow the following pattern: "
																+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
																+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
																+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
																+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
																+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
														JsonObject error = new JsonObject();
														error.addProperty("error", response);
														logger.info(
																"[Response code]: 200, [Response]: " + error + "\n");
														fh.close();
														return Response.status(200).entity(error.toString()).build();
													}
												} else {
													if (keywordList.size() >= counter_country + 10) {
														if (keywordList.get(counter_country + 6).equals("magnitude")
																&& keywordList.get(counter_country + 7)
																		.equals("greater")
																&& keywordList.get(counter_country + 8)
																		.equals("than")) {
															if (!isDouble(keywordList.get(counter_country + 9))
																	|| !hasOneDecimalPoint(
																			keywordList.get(counter_country + 9))) {
																response = "Expected double value with one decimal point as 'magnitude'. Input should follow the following pattern: "
																		+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
																		+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
																		+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
																		+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
																		+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
																JsonObject error = new JsonObject();
																error.addProperty("error", response);
																logger.info("[Response code]: 200, [Response]: " + error
																		+ "\n");
																fh.close();
																return Response.status(200).entity(error.toString())
																		.build();
															}

															magnitude = keywordList.get(counter_country + 9).toString();
															if (Double.parseDouble(magnitude) < 5.0) {
																response = "The value of the magnitude must be at least 5.0.";
																JsonObject error = new JsonObject();
																error.addProperty("error", response);
																logger.info("[Response code]: 200, [Response]: " + error
																		+ "\n");
																fh.close();
																return Response.status(200).entity(error.toString())
																		.build();
															}
														} else {
															response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
																	+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
																	+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
																	+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
																	+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
																	+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
															JsonObject error = new JsonObject();
															error.addProperty("error", response);
															logger.info("[Response code]: 200, [Response]: " + error
																	+ "\n");
															fh.close();
															return Response.status(200).entity(error.toString())
																	.build();
														}
													}
												}
												if (isInteger(keywordList.get(counter_country + 5)) && keywordList
														.get(counter_country + 5).toString().length() == 2) {
													day = keywordList.get(counter_country + 5);
													if (keywordList.size() >= counter_country + 11) {
														if (keywordList.get(counter_country + 6).equals("with")
																&& keywordList.get(counter_country + 7)
																		.equals("magnitude")
																&& keywordList.get(counter_country + 8)
																		.equals("greater")
																&& keywordList.get(counter_country + 9)
																		.equals("than")) {
															if (!isDouble(keywordList.get(counter_country + 10))
																	|| !hasOneDecimalPoint(
																			keywordList.get(counter_country + 10))) {
																response = "Expected double value with one decimal point as 'magnitude'. Input should follow the following pattern: "
																		+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
																		+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
																		+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
																		+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
																		+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
																JsonObject error = new JsonObject();
																error.addProperty("error", response);
																logger.info("[Response code]: 200, [Response]: " + error
																		+ "\n");
																fh.close();
																return Response.status(200).entity(error.toString())
																		.build();
															}

															magnitude = keywordList.get(counter_country + 10)
																	.toString();
															if (Double.parseDouble(magnitude) < 5.0) {
																response = "The value of the magnitude must be at least 5.0.";
																JsonObject error = new JsonObject();
																error.addProperty("error", response);
																logger.info("[Response code]: 200, [Response]: " + error
																		+ "\n");
																fh.close();
																return Response.status(200).entity(error.toString())
																		.build();
															}
														} else {
															response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
																	+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
																	+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
																	+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
																	+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
																	+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
															JsonObject error = new JsonObject();
															error.addProperty("error", response);
															logger.info("[Response code]: 200, [Response]: " + error
																	+ "\n");
															fh.close();
															return Response.status(200).entity(error.toString())
																	.build();
														}
													} else {
														if (keywordList.size() > counter_country + 6) {
															response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
																	+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
																	+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
																	+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
																	+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
																	+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
															JsonObject error = new JsonObject();
															error.addProperty("error", response);
															logger.info("[Response code]: 200, [Response]: " + error
																	+ "\n");
															fh.close();
															return Response.status(200).entity(error.toString())
																	.build();
														}
													}
												}
											}
										}

										else if (keywordList.size() == counter_country + 9) {

											if (keywordList.get(counter_country + 4).equals("with")
													&& keywordList.get(counter_country + 5).equals("magnitude")
													&& keywordList.get(counter_country + 6).equals("greater")
													&& keywordList.get(counter_country + 7).equals("than")) {
												if (!isDouble(keywordList.get(counter_country + 8))
														|| !hasOneDecimalPoint(keywordList.get(counter_country + 8))) {
													response = "Expected double value with one decimal point as 'magnitude'. Input should follow the following pattern: "
															+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
															+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
															+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
															+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
															+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
													JsonObject error = new JsonObject();
													error.addProperty("error", response);
													logger.info("[Response code]: 200, [Response]: " + error + "\n");
													fh.close();
													return Response.status(200).entity(error.toString()).build();
												}
												magnitude = keywordList.get(counter_country + 8).toString();
												if (Double.parseDouble(magnitude) < 5.0) {
													response = "The value of the magnitude must be at least 5.0.";
													JsonObject error = new JsonObject();
													error.addProperty("error", response);
													logger.info("[Response code]: 200, [Response]: " + error + "\n");
													fh.close();
													return Response.status(200).entity(error.toString()).build();
												}

											}
										} else if (keywordList.size() >= counter_country + 10) {

											if (keywordList.get(counter_country + 5).equals("with")
													&& keywordList.get(counter_country + 6).equals("magnitude")
													&& keywordList.get(counter_country + 7).equals("greater")
													&& keywordList.get(counter_country + 8).equals("than")) {
												if (!isDouble(keywordList.get(counter_country + 9))
														|| !hasOneDecimalPoint(keywordList.get(counter_country + 9))) {
													response = "Expected double value with one decimal point as 'magnitude'. Input should follow the following pattern: "
															+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
															+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
															+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
															+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
															+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
													JsonObject error = new JsonObject();
													error.addProperty("error", response);
													logger.info("[Response code]: 200, [Response]: " + error + "\n");
													fh.close();
													return Response.status(200).entity(error.toString()).build();
												}
												magnitude = keywordList.get(counter_country + 9).toString();
												if (Double.parseDouble(magnitude) < 5.0) {
													response = "The value of the magnitude must be at least 5.0.";
													JsonObject error = new JsonObject();
													error.addProperty("error", response);
													logger.info("[Response code]: 200, [Response]: " + error + "\n");
													fh.close();
													return Response.status(200).entity(error.toString()).build();
												}
											}
										} else if (keywordList.size() > counter_country + 4) {
											if (!keywordList.get(counter_country + 4).equals("with")) {
												if (!isInteger(keywordList.get(counter_country + 4)) || keywordList
														.get(counter_country + 4).toString().length() != 2) {

													response = "Expected 2-digit integer for 'month'. Input should follow the following pattern: "
															+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
															+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
															+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
															+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
															+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
													JsonObject error = new JsonObject();
													error.addProperty("error", response);
													logger.info("[Response code]: 200, [Response]: " + error + "\n");
													fh.close();
													return Response.status(200).entity(error.toString()).build();
												}
											}

										}

									} else if (keywordList.size() >= counter_country + 9) {
										if (keywordList.get(counter_country + 4).equals("with")
												&& keywordList.get(counter_country + 5).equals("magnitude")
												&& keywordList.get(counter_country + 6).equals("greater")
												&& keywordList.get(counter_country + 7).equals("than")) {
											if (!isDouble(keywordList.get(counter_country + 8))
													|| !hasOneDecimalPoint(keywordList.get(counter_country + 8))) {
												response = "Expected double value with one decimal point as 'magnitude'. Input should follow the following pattern: "
														+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
														+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
														+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
														+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
														+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
												JsonObject error = new JsonObject();
												error.addProperty("error", response);
												logger.info("[Response code]: 200, [Response]: " + error + "\n");
												fh.close();
												return Response.status(200).entity(error.toString()).build();
											}
											magnitude = keywordList.get(counter_country + 8).toString();
											if (Double.parseDouble(magnitude) < 5.0) {
												response = "The value of the magnitude must be at least 5.0.";
												JsonObject error = new JsonObject();
												error.addProperty("error", response);
												logger.info("[Response code]: 200, [Response]: " + error + "\n");
												fh.close();
												return Response.status(200).entity(error.toString()).build();
											}
										}
									} else if (keywordList.size() > counter_country + 4) {

										if (isInteger(keywordList.get(counter_country + 4))
												&& keywordList.get(counter_country + 4).toString().length() == 2) {

											month = keywordList.get(counter_country + 4);
											if (Integer.parseInt(month) < 1 || Integer.parseInt(month) > 12) {
												response = "Month expected after year as an integer value between 1 and 12.";
												JsonObject error = new JsonObject();
												error.addProperty("error", response);
												logger.info("[Response code]: 200, [Response]: " + error + "\n");
												fh.close();
												return Response.status(200).entity(error.toString()).build();
											}
										}
									}
								} else {
									response = "Expected 4-digit integer for 'year'. Input should follow the following pattern: "
											+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
											+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
											+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
											+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
											+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
									JsonObject error = new JsonObject();
									error.addProperty("error", response);
									logger.info("[Response code]: 200, [Response]: " + error + "\n");
									fh.close();
									return Response.status(200).entity(error.toString()).build();
								}
							} else {
								response = "Expected 'in' keyword after city, country. Input should follow the following pattern: "
										+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
										+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
										+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
										+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
										+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
								JsonObject error = new JsonObject();
								error.addProperty("error", response);
								logger.info("[Response code]: 200, [Response]: " + error + "\n");
								fh.close();
								return Response.status(200).entity(error.toString()).build();
							}
						} else {
							response = "Country expected after city. Input should follow the following pattern: "
									+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
									+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
									+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
									+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
									+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
							JsonObject error = new JsonObject();
							error.addProperty("error", response);
							logger.info("[Response code]: 200, [Response]: " + error + "\n");
							fh.close();
							return Response.status(200).entity(error.toString()).build();
						}
					} else {
						if (!keywordList.get(0).contains("POINT")) {
							int counter = 0;
							for (int i = 0; i < keywordList.size(); i++) {
								if (keywordList.get(i).contains(",")) {
									city = "";
								}
							}
							if (city.equals("null")) {
								response = "Comma (,) expected after city. Input should follow the following pattern: "
										+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
										+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
										+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
										+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
										+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
								JsonObject error = new JsonObject();
								error.addProperty("error", response);
								logger.info("[Response code]: 200, [Response]: " + error + "\n");
								fh.close();
								return Response.status(200).entity(error.toString()).build();
							}

							for (int i = 0; i < keywordList.size(); i++) {
								if (keywordList.get(i).contains(",")) {
									city = city + " " + keywordList.get(i).replaceAll(",", "");
									counter = i;
									city = city.substring(1);
									break;
								}

								city = city + " " + keywordList.get(i);
							}

							if (keywordList.size() < counter + 2) {
								response = "Country expected after city. Input should follow the following pattern: "
										+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
										+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
										+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
										+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
										+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
								JsonObject error = new JsonObject();
								error.addProperty("error", response);
								logger.info("[Response code]: 200, [Response]: " + error + "\n");
								fh.close();
								return Response.status(200).entity(error.toString()).build();
							}

							if (!keywordList.get(counter + 1).equals("in")) {
								int counter_country = 0;
								for (int i = counter + 1; i < keywordList.size(); i++) {
									if (keywordList.get(i).equals("in")) {
										country = "";
									}
								}
								for (int i = counter + 1; i < keywordList.size(); i++) {
									if (keywordList.get(i).equals("in")) {

										counter_country = i - 2 - counter;
										country = country.substring(1);
										break;
									}

									country = country + " " + keywordList.get(i);
								}

								if (keywordList.size() < counter_country + counter + 3) {
									response = "Expected 'in' keyword after city, country. Input should follow the following pattern: "
											+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
											+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
											+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
											+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
											+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
									JsonObject error = new JsonObject();
									error.addProperty("error", response);
									logger.info("[Response code]: 200, [Response]: " + error + "\n");
									fh.close();
									return Response.status(200).entity(error.toString()).build();
								}
								if (keywordList.get(counter_country + counter + 2).equals("in")) {
									if (keywordList.size() < counter_country + counter + 4) {
										response = "Expected 4-digit integer for 'year'. Input should follow the following pattern: "
												+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
												+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
												+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
												+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
												+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
										JsonObject error = new JsonObject();
										error.addProperty("error", response);
										logger.info("[Response code]: 200, [Response]: " + error + "\n");
										fh.close();
										return Response.status(200).entity(error.toString()).build();
									}
									if (isInteger(keywordList.get(counter_country + counter + 3)) && keywordList
											.get(counter_country + counter + 3).toString().length() == 4) {

										year = keywordList.get(counter_country + counter + 3);
										if (keywordList.size() == counter_country + counter + 5) {
											if (keywordList.get(counter_country + counter + 4).equals("with")) {

												response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
														+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
														+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
														+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
														+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
														+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
												JsonObject error = new JsonObject();
												error.addProperty("error", response);
												logger.info("[Response code]: 200, [Response]: " + error + "\n");
												fh.close();
												return Response.status(200).entity(error.toString()).build();
											}
											if (!isInteger(keywordList.get(counter_country + counter + 4))
													|| keywordList.get(counter_country + counter + 4).toString()
															.length() != 2) {

												response = "Expected 2-digit integer for 'month'. Input should follow the following pattern: "
														+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
														+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
														+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
														+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
														+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
												JsonObject error = new JsonObject();
												error.addProperty("error", response);
												logger.info("[Response code]: 200, [Response]: " + error + "\n");
												fh.close();
												return Response.status(200).entity(error.toString()).build();
											}

										}
										if (keywordList.size() == counter_country + counter + 6) {
											if (keywordList.get(counter_country + counter + 5).equals("with")) {

												response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
														+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
														+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
														+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
														+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
														+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
												JsonObject error = new JsonObject();
												error.addProperty("error", response);
												logger.info("[Response code]: 200, [Response]: " + error + "\n");
												fh.close();
												return Response.status(200).entity(error.toString()).build();
											}
											if (!isInteger(keywordList.get(counter_country + counter + 5))
													|| keywordList.get(counter_country + counter + 5).toString()
															.length() != 2) {

												response = "Expected 2-digit integer for 'day'. Input should follow the following pattern: "
														+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
														+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
														+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
														+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
														+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
												JsonObject error = new JsonObject();
												error.addProperty("error", response);
												logger.info("[Response code]: 200, [Response]: " + error + "\n");
												fh.close();
												return Response.status(200).entity(error.toString()).build();

											}
										}

										if (keywordList.contains("with") && (!keywordList.contains("magnitude")
												|| !keywordList.contains("greater") || !keywordList.contains("than")
												|| !isDouble(keywordList.get(keywordList.size() - 1))
												|| !hasOneDecimalPoint(keywordList.get(keywordList.size() - 1)))) {
											response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
													+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
													+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
													+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
													+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
													+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
											JsonObject error = new JsonObject();
											error.addProperty("error", response);
											logger.info("[Response code]: 200, [Response]: " + error + "\n");
											fh.close();
											return Response.status(200).entity(error.toString()).build();
										}
										if (keywordList.contains("magnitude") && (!keywordList.contains("with")
												|| !keywordList.contains("greater") || !keywordList.contains("than")
												|| !isDouble(keywordList.get(keywordList.size() - 1))
												|| !hasOneDecimalPoint(keywordList.get(keywordList.size() - 1)))) {
											response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
													+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
													+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
													+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
													+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
													+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
											JsonObject error = new JsonObject();
											error.addProperty("error", response);
											logger.info("[Response code]: 200, [Response]: " + error + "\n");
											fh.close();
											return Response.status(200).entity(error.toString()).build();
										}
										if (keywordList.contains("greater") && (!keywordList.contains("magnitude")
												|| !keywordList.contains("with") || !keywordList.contains("than")
												|| !isDouble(keywordList.get(keywordList.size() - 1))
												|| !hasOneDecimalPoint(keywordList.get(keywordList.size() - 1)))) {
											response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
													+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
													+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
													+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
													+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
													+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
											JsonObject error = new JsonObject();
											error.addProperty("error", response);
											logger.info("[Response code]: 200, [Response]: " + error + "\n");
											fh.close();
											return Response.status(200).entity(error.toString()).build();
										}
										if (keywordList.contains("than") && (!keywordList.contains("magnitude")
												|| !keywordList.contains("greater") || !keywordList.contains("with")
												|| !isDouble(keywordList.get(keywordList.size() - 1))
												|| !hasOneDecimalPoint(keywordList.get(keywordList.size() - 1)))) {
											response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
													+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
													+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
													+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
													+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
													+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
											JsonObject error = new JsonObject();
											error.addProperty("error", response);
											logger.info("[Response code]: 200, [Response]: " + error + "\n");
											fh.close();
											return Response.status(200).entity(error.toString()).build();
										}

										if (keywordList.size() >= counter_country + counter + 5) {

											if (isInteger(keywordList.get(counter_country + counter + 4)) && keywordList
													.get(counter_country + counter + 4).toString().length() == 2) {

												month = keywordList.get(counter_country + counter + 4);
												if (Integer.parseInt(month) < 1 || Integer.parseInt(month) > 12) {
													response = "Month expected after year as an integer value between 1 and 12.";
													JsonObject error = new JsonObject();
													error.addProperty("error", response);
													logger.info("[Response code]: 200, [Response]: " + error + "\n");
													fh.close();
													return Response.status(200).entity(error.toString()).build();
												}
												if (keywordList.size() > counter_country + counter + 5) {
													if (!keywordList.get(counter_country + counter + 5)
															.equals("with")) {
														if (!isInteger(keywordList.get(counter_country + counter + 5))
																|| keywordList.get(counter_country + counter + 5)
																		.toString().length() != 2) {

															response = "Expected 2-digit integer for 'day'. Input should follow the following pattern: "
																	+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
																	+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
																	+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
																	+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
																	+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
															JsonObject error = new JsonObject();
															error.addProperty("error", response);
															logger.info("[Response code]: 200, [Response]: " + error
																	+ "\n");
															fh.close();
															return Response.status(200).entity(error.toString())
																	.build();
														}
													} else {
														if (keywordList.size() >= counter_country + counter + 10) {
															if (keywordList.get(counter_country + counter + 6)
																	.equals("magnitude")
																	&& keywordList.get(counter_country + counter + 7)
																			.equals("greater")
																	&& keywordList.get(counter_country + counter + 8)
																			.equals("than")) {
																if (!isDouble(
																		keywordList.get(counter_country + counter + 9))
																		|| !hasOneDecimalPoint(keywordList
																				.get(counter_country + counter + 9))) {
																	response = "Expected double value with one decimal point as 'magnitude'. Input should follow the following pattern: "
																			+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
																			+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
																			+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
																			+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
																			+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
																	JsonObject error = new JsonObject();
																	error.addProperty("error", response);
																	logger.info("[Response code]: 200, [Response]: "
																			+ error + "\n");
																	fh.close();
																	return Response.status(200).entity(error.toString())
																			.build();
																}

																magnitude = keywordList
																		.get(counter_country + counter + 9).toString();
																if (Double.parseDouble(magnitude) < 5.0) {
																	response = "The value of the magnitude must be at least 5.0.";
																	JsonObject error = new JsonObject();
																	error.addProperty("error", response);
																	logger.info("[Response code]: 200, [Response]: "
																			+ error + "\n");
																	fh.close();
																	return Response.status(200).entity(error.toString())
																			.build();
																}
															} else {
																response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
																		+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
																		+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
																		+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
																		+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
																		+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
																JsonObject error = new JsonObject();
																error.addProperty("error", response);
																logger.info("[Response code]: 200, [Response]: " + error
																		+ "\n");
																fh.close();
																return Response.status(200).entity(error.toString())
																		.build();
															}
														}
													}
													if (isInteger(keywordList.get(counter_country + counter + 5))
															&& keywordList.get(counter_country + counter + 5).toString()
																	.length() == 2) {
														day = keywordList.get(counter_country + counter + 5);
														if (keywordList.size() >= counter_country + counter + 11) {
															if (keywordList.get(counter_country + counter + 6)
																	.equals("with")
																	&& keywordList.get(counter_country + counter + 7)
																			.equals("magnitude")
																	&& keywordList.get(counter_country + counter + 8)
																			.equals("greater")
																	&& keywordList.get(counter_country + counter + 9)
																			.equals("than")) {
																if (!isDouble(
																		keywordList.get(counter_country + counter + 10))
																		|| !hasOneDecimalPoint(keywordList
																				.get(counter_country + counter + 10))) {
																	response = "Expected double value with one decimal point as 'magnitude'. Input should follow the following pattern: "
																			+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
																			+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
																			+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
																			+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
																			+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
																	JsonObject error = new JsonObject();
																	error.addProperty("error", response);
																	logger.info("[Response code]: 200, [Response]: "
																			+ error + "\n");
																	fh.close();
																	return Response.status(200).entity(error.toString())
																			.build();
																}

																magnitude = keywordList
																		.get(counter_country + counter + 10).toString();
																if (Double.parseDouble(magnitude) < 5.0) {
																	response = "The value of the magnitude must be at least 5.0.";
																	JsonObject error = new JsonObject();
																	error.addProperty("error", response);
																	logger.info("[Response code]: 200, [Response]: "
																			+ error + "\n");
																	fh.close();
																	return Response.status(200).entity(error.toString())
																			.build();
																}
															} else {
																response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
																		+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
																		+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
																		+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
																		+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
																		+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
																JsonObject error = new JsonObject();
																error.addProperty("error", response);
																logger.info("[Response code]: 200, [Response]: " + error
																		+ "\n");
																fh.close();
																return Response.status(200).entity(error.toString())
																		.build();
															}
														} else {
															if (keywordList.size() > counter_country + counter + 6) {
																response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
																		+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
																		+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
																		+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
																		+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
																		+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
																JsonObject error = new JsonObject();
																error.addProperty("error", response);
																logger.info("[Response code]: 200, [Response]: " + error
																		+ "\n");
																fh.close();
																return Response.status(200).entity(error.toString())
																		.build();
															}
														}
													}
												}
											} else if (!keywordList.get(counter_country + counter + 4).equals("with")) {
												if (!isInteger(keywordList.get(counter_country + counter + 4))
														|| keywordList.get(counter_country + counter + 4).toString()
																.length() != 2) {
													response = "Expected 2-digit integer for 'month'. Input should follow the following pattern: "
															+ "“earthquake located in POINT (<latitude> <longitude>) in <year> <month> <day> with magnitude greater than <magnitude>” . "
															+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
															+ "Latitude, longitude and magnitude are expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
															+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
															+ "mentioned above is the following: “earthquake located in POINT (45.0 77.90424715741715) in 2019 03 24 with magnitude greater than 6.0”";
													JsonObject error = new JsonObject();
													error.addProperty("error", response);
													logger.info("[Response code]: 200, [Response]: " + error + "\n");
													fh.close();
													return Response.status(200).entity(error.toString()).build();
												}
											} else if (keywordList.size() == counter_country + counter + 9) {

												if (keywordList.get(counter_country + counter + 4).equals("with")
														&& keywordList.get(counter_country + counter + 5)
																.equals("magnitude")
														&& keywordList.get(counter_country + counter + 6)
																.equals("greater")
														&& keywordList.get(counter_country + counter + 7)
																.equals("than")) {
													if (!isDouble(keywordList.get(counter_country + counter + 8))
															|| !hasOneDecimalPoint(
																	keywordList.get(counter_country + counter + 8))) {
														response = "Expected double value with one decimal point as 'magnitude'. Input should follow the following pattern: "
																+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
																+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
																+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
																+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
																+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
														JsonObject error = new JsonObject();
														error.addProperty("error", response);
														logger.info(
																"[Response code]: 200, [Response]: " + error + "\n");
														fh.close();
														return Response.status(200).entity(error.toString()).build();
													}
													magnitude = keywordList.get(counter_country + counter + 8)
															.toString();
													if (Double.parseDouble(magnitude) < 5.0) {
														response = "The value of the magnitude must be at least 5.0.";
														JsonObject error = new JsonObject();
														error.addProperty("error", response);
														logger.info(
																"[Response code]: 200, [Response]: " + error + "\n");
														fh.close();
														return Response.status(200).entity(error.toString()).build();
													}

												}
											} else if (keywordList.size() >= counter_country + counter + 10) {

												if (keywordList.get(counter_country + counter + 5).equals("with")
														&& keywordList.get(counter_country + counter + 6)
																.equals("magnitude")
														&& keywordList.get(counter_country + counter + 7)
																.equals("greater")
														&& keywordList.get(counter_country + counter + 8)
																.equals("than")) {
													if (!isDouble(keywordList.get(counter_country + counter + 9))
															|| !hasOneDecimalPoint(
																	keywordList.get(counter_country + counter + 9))) {
														response = "Expected double value with one decimal point as 'magnitude'. Input should follow the following pattern: "
																+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
																+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
																+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
																+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
																+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
														JsonObject error = new JsonObject();
														error.addProperty("error", response);
														logger.info(
																"[Response code]: 200, [Response]: " + error + "\n");
														fh.close();
														return Response.status(200).entity(error.toString()).build();
													}
													magnitude = keywordList.get(counter_country + counter + 9)
															.toString();
													if (Double.parseDouble(magnitude) < 5.0) {
														response = "The value of the magnitude must be at least 5.0.";
														JsonObject error = new JsonObject();
														error.addProperty("error", response);
														logger.info(
																"[Response code]: 200, [Response]: " + error + "\n");
														fh.close();
														return Response.status(200).entity(error.toString()).build();
													}
												}
											}

										} else if (keywordList.size() >= counter_country + counter + 9) {
											if (keywordList.get(counter_country + counter + 4).equals("with")
													&& keywordList.get(counter_country + counter + 5)
															.equals("magnitude")
													&& keywordList.get(counter_country + counter + 6).equals("greater")
													&& keywordList.get(counter_country + counter + 7).equals("than")) {
												if (!isDouble(keywordList.get(counter_country + counter + 8))
														|| !hasOneDecimalPoint(
																keywordList.get(counter_country + counter + 8))) {
													response = "Expected double value with one decimal point as 'magnitude'. Input should follow the following pattern: "
															+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
															+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
															+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
															+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
															+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
													JsonObject error = new JsonObject();
													error.addProperty("error", response);
													logger.info("[Response code]: 200, [Response]: " + error + "\n");
													fh.close();
													return Response.status(200).entity(error.toString()).build();
												}
												magnitude = keywordList.get(counter_country + counter + 8).toString();
												if (Double.parseDouble(magnitude) < 5.0) {
													response = "The value of the magnitude must be at least 5.0.";
													JsonObject error = new JsonObject();
													error.addProperty("error", response);
													logger.info("[Response code]: 200, [Response]: " + error + "\n");
													fh.close();
													return Response.status(200).entity(error.toString()).build();
												}
											}
										}

									} else {
										response = "Expected 4-digit integer for 'year'. Input should follow the following pattern: "
												+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
												+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
												+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
												+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
												+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
										JsonObject error = new JsonObject();
										error.addProperty("error", response);
										logger.info("[Response code]: 200, [Response]: " + error + "\n");
										fh.close();
										return Response.status(200).entity(error.toString()).build();
									}
								} else {
									response = "Expected 'in' keyword after city, country. Input should follow the following pattern: "
											+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
											+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
											+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
											+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
											+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
									JsonObject error = new JsonObject();
									error.addProperty("error", response);
									logger.info("[Response code]: 200, [Response]: " + error + "\n");
									fh.close();
									return Response.status(200).entity(error.toString()).build();
								}
							} else {
								response = "Country expected after city. Input should follow the following pattern: "
										+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
										+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
										+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
										+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
										+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
								JsonObject error = new JsonObject();
								error.addProperty("error", response);
								logger.info("[Response code]: 200, [Response]: " + error + "\n");
								fh.close();
								return Response.status(200).entity(error.toString()).build();
							}

						} else {

							if (keywordList.get(0).contains("POINT")) {

								if (!input.contains("POINT ")) {
									response = "Space expected after POINT. Input should follow the following pattern: "
											+ "“earthquake located in POINT (<latitude> <longitude>) in <year> <month> <day> with magnitude greater than <magnitude>” . "
											+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
											+ "Latitude, longitude and magnitude are expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
											+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
											+ "mentioned above is the following: “earthquake located in POINT (45.0 77.90424715741715) in 2019 03 24 with magnitude greater than 6.0”";
									JsonObject error = new JsonObject();
									error.addProperty("error", response);
									logger.info("[Response code]: 200, [Response]: " + error + "\n");
									fh.close();
									return Response.status(200).entity(error.toString()).build();
								}
								if (!isDouble(keywordList.get(1).replace("(", ""))
										|| !isDouble(keywordList.get(2).replace(")", ""))) {
									response = "Double numbers expected as latitude longitude after POINT. Input should follow the following pattern: "
											+ "“earthquake located in POINT (<latitude> <longitude>) in <year> <month> <day> with magnitude greater than <magnitude>” . "
											+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
											+ "Latitude, longitude and magnitude are expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
											+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
											+ "mentioned above is the following: “earthquake located in POINT (45.0 77.90424715741715) in 2019 03 24 with magnitude greater than 6.0”";
									JsonObject error = new JsonObject();
									error.addProperty("error", response);
									logger.info("[Response code]: 200, [Response]: " + error + "\n");
									fh.close();
									return Response.status(200).entity(error.toString()).build();
								} else {
									if (!keywordList.get(1).startsWith("(")) {
										response = "Ρarenthesis '(' expected after POINT. Input should follow the following pattern: "
												+ "“earthquake located in POINT (<latitude> <longitude>) in <year> <month> <day> with magnitude greater than <magnitude>” . "
												+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
												+ "Latitude, longitude and magnitude are expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
												+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
												+ "mentioned above is the following: “earthquake located in POINT (45.0 77.90424715741715) in 2019 03 24 with magnitude greater than 6.0”";
										JsonObject error = new JsonObject();
										error.addProperty("error", response);
										logger.info("[Response code]: 200, [Response]: " + error + "\n");
										fh.close();
										return Response.status(200).entity(error.toString()).build();
									}
									if (!keywordList.get(2).endsWith(")")) {
										response = "Ρarenthesis ')' expected after longitude. Input should follow the following pattern: "
												+ "“earthquake located in POINT (<latitude> <longitude>) in <year> <month> <day> with magnitude greater than <magnitude>” . "
												+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
												+ "Latitude, longitude and magnitude are expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
												+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
												+ "mentioned above is the following: “earthquake located in POINT (45.0 77.90424715741715) in 2019 03 24 with magnitude greater than 6.0”";
										JsonObject error = new JsonObject();
										error.addProperty("error", response);
										logger.info("[Response code]: 200, [Response]: " + error + "\n");
										fh.close();
										return Response.status(200).entity(error.toString()).build();
									}
									latitude = keywordList.get(1).replace("(", "");
									longitude = keywordList.get(2).replace(")", "");
									if (!keywordList.get(3).equals("in")) {
										response = "Expected 'in' keyword after POINT (latitude longitude). Input should follow the following pattern: "
												+ "“earthquake located in POINT (<latitude> <longitude>) in <year> <month> <day> with magnitude greater than <magnitude>” . "
												+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
												+ "Latitude, longitude and magnitude are expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
												+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
												+ "mentioned above is the following: “earthquake located in POINT (45.0 77.90424715741715) in 2019 03 24 with magnitude greater than 6.0”";
										JsonObject error = new JsonObject();
										error.addProperty("error", response);
										logger.info("[Response code]: 200, [Response]: " + error + "\n");
										fh.close();
										return Response.status(200).entity(error.toString()).build();
									} else {

										if (isInteger(keywordList.get(4))
												&& keywordList.get(4).toString().length() == 4) {

											year = keywordList.get(4);

											if (keywordList.size() > 5) {
												if (isInteger(keywordList.get(5))
														&& keywordList.get(5).toString().length() == 2) {

													month = keywordList.get(5);
													if (Integer.parseInt(month) < 1 || Integer.parseInt(month) > 12) {
														response = "Month expected after year as an integer value between 1 and 12.";
														JsonObject error = new JsonObject();
														error.addProperty("error", response);
														logger.info(
																"[Response code]: 200, [Response]: " + error + "\n");
														fh.close();
														return Response.status(200).entity(error.toString()).build();
													}
													if (keywordList.size() > 6) {
														if (isInteger(keywordList.get(6))
																&& keywordList.get(6).toString().length() == 2) {

															day = keywordList.get(6);
															if (keywordList.size() > 11) {
																if (keywordList.get(7).equals("with")) {

																	if (keywordList.get(8).equals("magnitude")
																			&& keywordList.get(9).equals("greater")
																			&& keywordList.get(10).equals("than")
																			&& isDouble(keywordList
																					.get(keywordList.size() - 1))
																			&& hasOneDecimalPoint(keywordList
																					.get(keywordList.size() - 1))) {

																		magnitude = keywordList
																				.get(keywordList.size() - 1);
																		if (Double.parseDouble(magnitude) < 5.0) {
																			response = "The value of the magnitude must be at least 5.0.";
																			JsonObject error = new JsonObject();
																			error.addProperty("error", response);
																			logger.info(
																					"[Response code]: 200, [Response]: "
																							+ error + "\n");
																			fh.close();
																			return Response.status(200)
																					.entity(error.toString()).build();
																		}
																	}
																}
															}
														}

														else if (keywordList.get(6).equals("with")) {
															if (keywordList.size() > 10) {
																if (keywordList.get(7).equals("magnitude")
																		&& keywordList.get(8).equals("greater")
																		&& keywordList.get(9).equals("than")
																		&& isDouble(
																				keywordList.get(keywordList.size() - 1))
																		&& hasOneDecimalPoint(keywordList
																				.get(keywordList.size() - 1))) {

																	magnitude = keywordList.get(keywordList.size() - 1);
																	if (Double.parseDouble(magnitude) < 5.0) {
																		response = "The value of the magnitude must be at least 5.0.";
																		JsonObject error = new JsonObject();
																		error.addProperty("error", response);
																		logger.info("[Response code]: 200, [Response]: "
																				+ error + "\n");
																		fh.close();
																		return Response.status(200)
																				.entity(error.toString()).build();
																	}
																}
															}
														} else if ((!isInteger(keywordList.get(6))
																|| keywordList.get(6).toString().length() != 2)) {

															response = "Expected 2-digit integer for 'day'. Input should follow the following pattern: "
																	+ "“earthquake located in POINT (<latitude> <longitude>) in <year> <month> <day> with magnitude greater than <magnitude>” . "
																	+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
																	+ "Latitude, longitude and magnitude are expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
																	+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
																	+ "mentioned above is the following: “earthquake located in POINT (45.0 77.90424715741715) in 2019 03 24 with magnitude greater than 6.0”";
															JsonObject error = new JsonObject();
															error.addProperty("error", response);
															logger.info("[Response code]: 200, [Response]: " + error
																	+ "\n");
															fh.close();
															return Response.status(200).entity(error.toString())
																	.build();
														}
													}
												} else if (keywordList.get(5).equals("with")) {
													if (keywordList.size() > 9) {
														if (keywordList.get(6).equals("magnitude")
																&& keywordList.get(7).equals("greater")
																&& keywordList.get(8).equals("than")
																&& isDouble(keywordList.get(keywordList.size() - 1))
																&& hasOneDecimalPoint(
																		keywordList.get(keywordList.size() - 1))) {

															magnitude = keywordList.get(keywordList.size() - 1);
															if (Double.parseDouble(magnitude) < 5.0) {
																response = "The value of the magnitude must be at least 5.0.";
																JsonObject error = new JsonObject();
																error.addProperty("error", response);
																logger.info("[Response code]: 200, [Response]: " + error
																		+ "\n");
																fh.close();
																return Response.status(200).entity(error.toString())
																		.build();
															}
														}
													}
												} else if ((!isInteger(keywordList.get(5))
														|| keywordList.get(5).toString().length() != 2)) {

													response = "Expected 2-digit integer for 'month'. Input should follow the following pattern: "
															+ "“earthquake located in POINT (<latitude> <longitude>) in <year> <month> <day> with magnitude greater than <magnitude>” . "
															+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
															+ "Latitude, longitude and magnitude are expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
															+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
															+ "mentioned above is the following: “earthquake located in POINT (45.0 77.90424715741715) in 2019 03 24 with magnitude greater than 6.0”";
													JsonObject error = new JsonObject();
													error.addProperty("error", response);
													logger.info("[Response code]: 200, [Response]: " + error + "\n");
													fh.close();
													return Response.status(200).entity(error.toString()).build();
												}
											}

										} else {
											response = "Expected 4-digit integer for 'year'. Input should follow the following pattern: "
													+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
													+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
													+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
													+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
													+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
											JsonObject error = new JsonObject();
											error.addProperty("error", response);
											logger.info("[Response code]: 200, [Response]: " + error + "\n");
											fh.close();
											return Response.status(200).entity(error.toString()).build();

										}
									}
								}
							}

						}
					}
				} else {
					response = "City expected after 'earthquake located in' phrase. Input should follow the following pattern: "
							+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
							+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
							+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
							+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
							+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
					JsonObject error = new JsonObject();
					error.addProperty("error", response);
					logger.info("[Response code]: 200, [Response]: " + error + "\n");
					fh.close();
					return Response.status(200).entity(error.toString()).build();
				}

			} else {

				if (!input.startsWith("earthquake")) {

					if (!input.startsWith(eventType + " located in ")) {
						response = "Input should start with '<event> located in' phrase. Input should follow the following pattern: "
								+ "“<event> located in <city>, <country> in <year> <month> <day>” . "
								+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
								+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
								+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
								+ "mentioned above is the following: earthquake located in Rome, Italy in 2019 03 24";
						JsonObject error = new JsonObject();
						error.addProperty("error", response);
						logger.info("[Response code]: 200, [Response]: " + error + "\n");
						fh.close();
						return Response.status(200).entity(error.toString()).build();
					}

					String parsed = input;
					parsed = parsed.replace(eventType + " located in ", "");
					List<String> keywordList = Arrays.asList(parsed.split(" "));

					if (keywordList.size() > 6) {

						if (!keywordList.toString().contains("POINT")) {
							response = "Input size larger than expected. Input should follow the following pattern: "
									+ "“<event> located in <city>, <country> in <year> <month> <day>” . "
									+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
									+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
									+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24”";
							JsonObject error = new JsonObject();
							error.addProperty("error", response);
							logger.info("[Response code]: 200, [Response]: " + error + "\n");
							fh.close();
							return Response.status(200).entity(error.toString()).build();
						} else if (keywordList.size() > 7) {
							response = "Input size larger than expected. Input should follow the following pattern: "
									+ "“<event> located in <city>, <country> in <year> <month> <day>” . "
									+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
									+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
									+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24”";
							JsonObject error = new JsonObject();
							error.addProperty("error", response);
							logger.info("[Response code]: 200, [Response]: " + error + "\n");
							fh.close();
							return Response.status(200).entity(error.toString()).build();
						}
					}
					if (keywordList.size() > 0 && !keywordList.get(0).isEmpty()) {

						if (keywordList.get(0).contains(",")) {
							if (keywordList.get(0).toString()
									.charAt(keywordList.get(0).toString().length() - 1) != ',') {
								response = "Space expected after comma. Input should follow the following pattern: "
										+ "“<event> located in <city>, <country> in <year> <month> <day>” . "
										+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
										+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
										+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24”";
								JsonObject error = new JsonObject();
								error.addProperty("error", response);
								logger.info("[Response code]: 200, [Response]: " + error + "\n");
								fh.close();
								return Response.status(200).entity(error.toString()).build();
							}

							city = keywordList.get(0).replaceAll(",", "");

							if (keywordList.size() < 2) {
								response = "Country expected after city. Input should follow the following pattern: "
										+ "“<event> located in <city>, <country> in <year> <month> <day>” . "
										+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
										+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
										+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24”";
								JsonObject error = new JsonObject();
								error.addProperty("error", response);
								logger.info("[Response code]: 200, [Response]: " + error + "\n");
								fh.close();
								return Response.status(200).entity(error.toString()).build();
							}

							if (!keywordList.get(1).equals("in")) {
								int counter_country = 0;
								for (int i = 1; i < keywordList.size(); i++) {
									if (keywordList.get(i).equals("in")) {
										country = "";
									}
								}
								for (int i = 1; i < keywordList.size(); i++) {
									if (keywordList.get(i).equals("in")) {

										counter_country = i - 2;
										country = country.substring(1);
										break;
									}

									country = country + " " + keywordList.get(i);
								}

								if (keywordList.size() < counter_country + 3) {

									response = "Expected 'in' keyword after city, country. Input should follow the following pattern: "
											+ "“<event> located in <city>, <country> in <year> <month> <day>” . "
											+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
											+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
											+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24”";
									JsonObject error = new JsonObject();
									error.addProperty("error", response);
									logger.info("[Response code]: 200, [Response]: " + error + "\n");
									fh.close();
									return Response.status(200).entity(error.toString()).build();
								}
								if (keywordList.get(counter_country + 2).equals("in")) {
									if (keywordList.size() < counter_country + 4) {
										response = "Expected 4-digit integer for 'year'. Input should follow the following pattern: "
												+ "“<event> located in <city>, <country> in <year> <month> <day>” . "
												+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
												+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
												+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24”";
										JsonObject error = new JsonObject();
										error.addProperty("error", response);
										logger.info("[Response code]: 200, [Response]: " + error + "\n");
										fh.close();
										return Response.status(200).entity(error.toString()).build();
									}
									if (isInteger(keywordList.get(counter_country + 3))
											&& keywordList.get(counter_country + 3).toString().length() == 4) {

										year = keywordList.get(counter_country + 3);
										if (keywordList.size() >= counter_country + 5) {

											if (!isInteger(keywordList.get(counter_country + 4))
													|| keywordList.get(counter_country + 4).toString().length() != 2) {

												response = "Expected 2-digit integer for 'month'. Input should follow the following pattern: "
														+ "“<event> located in <city>, <country> in <year> <month> <day>” . "
														+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
														+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
														+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24”";
												JsonObject error = new JsonObject();
												error.addProperty("error", response);
												logger.info("[Response code]: 200, [Response]: " + error + "\n");
												fh.close();
												return Response.status(200).entity(error.toString()).build();
											}

										}

										if (keywordList.size() >= counter_country + 6) {

											if (!isInteger(keywordList.get(counter_country + 5))
													|| keywordList.get(counter_country + 5).toString().length() != 2) {

												response = "Expected 2-digit integer for 'day'. Input should follow the following pattern: "
														+ "“<event> located in <city>, <country> in <year> <month> <day>” . "
														+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
														+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
														+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24”";
												JsonObject error = new JsonObject();
												error.addProperty("error", response);
												logger.info("[Response code]: 200, [Response]: " + error + "\n");
												fh.close();
												return Response.status(200).entity(error.toString()).build();

											}
										}

										if (keywordList.size() >= counter_country + 5) {

											if (isInteger(keywordList.get(counter_country + 4))
													&& keywordList.get(counter_country + 4).toString().length() == 2) {

												month = keywordList.get(counter_country + 4);
												if (Integer.parseInt(month) < 1 || Integer.parseInt(month) > 12) {
													response = "Month expected after year as an integer value between 1 and 12.";
													JsonObject error = new JsonObject();
													error.addProperty("error", response);
													logger.info("[Response code]: 200, [Response]: " + error + "\n");
													fh.close();
													return Response.status(200).entity(error.toString()).build();
												}
												if (keywordList.size() > counter_country + 5) {
													if (!keywordList.get(counter_country + 5).equals("with")) {
														if (!isInteger(keywordList.get(counter_country + 5))
																|| keywordList.get(counter_country + 5).toString()
																		.length() != 2) {

															response = "Expected 2-digit integer for 'day'. Input should follow the following pattern: "
																	+ "“<event> located in <city>, <country> in <year> <month> <day>” . "
																	+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
																	+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
																	+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24”";
															JsonObject error = new JsonObject();
															error.addProperty("error", response);
															logger.info("[Response code]: 200, [Response]: " + error
																	+ "\n");
															fh.close();
															return Response.status(200).entity(error.toString())
																	.build();
														}
													}
													if (isInteger(keywordList.get(counter_country + 5)) && keywordList
															.get(counter_country + 5).toString().length() == 2) {
														day = keywordList.get(counter_country + 5);

													}
												}
											}

											else if (keywordList.size() > counter_country + 4) {
												if (!keywordList.get(counter_country + 4).equals("with")) {
													if (!isInteger(keywordList.get(counter_country + 4)) || keywordList
															.get(counter_country + 4).toString().length() != 2) {

														response = "Expected 2-digit integer for 'month'. Input should follow the following pattern: "
																+ "“<event> located in <city>, <country> in <year> <month> <day>” . "
																+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
																+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
																+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24”";
														JsonObject error = new JsonObject();
														error.addProperty("error", response);
														logger.info(
																"[Response code]: 200, [Response]: " + error + "\n");
														fh.close();
														return Response.status(200).entity(error.toString()).build();
													}
												}

											}

										} else if (keywordList.size() > counter_country + 4) {

											if (isInteger(keywordList.get(counter_country + 4))
													&& keywordList.get(counter_country + 4).toString().length() == 2) {

												month = keywordList.get(counter_country + 4);
												if (Integer.parseInt(month) < 1 || Integer.parseInt(month) > 12) {
													response = "Month expected after year as an integer value between 1 and 12.";
													JsonObject error = new JsonObject();
													error.addProperty("error", response);
													logger.info("[Response code]: 200, [Response]: " + error + "\n");
													fh.close();
													return Response.status(200).entity(error.toString()).build();
												}
											}
										}
									} else {
										response = "Expected 4-digit integer for 'year'. Input should follow the following pattern: "
												+ "“<event> located in <city>, <country> in <year> <month> <day>” . "
												+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
												+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
												+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24”";
										JsonObject error = new JsonObject();
										error.addProperty("error", response);
										logger.info("[Response code]: 200, [Response]: " + error + "\n");
										fh.close();
										return Response.status(200).entity(error.toString()).build();
									}
								} else {
									response = "Expected 'in' keyword after city, country. Input should follow the following pattern: "
											+ "“<event> located in <city>, <country> in <year> <month> <day>” . "
											+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
											+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
											+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24”";
									JsonObject error = new JsonObject();
									error.addProperty("error", response);
									logger.info("[Response code]: 200, [Response]: " + error + "\n");
									fh.close();
									return Response.status(200).entity(error.toString()).build();
								}
							} else {
								response = "Country expected after city. Input should follow the following pattern: "
										+ "“<event> located in <city>, <country> in <year> <month> <day>” . "
										+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
										+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
										+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24”";
								JsonObject error = new JsonObject();
								error.addProperty("error", response);
								logger.info("[Response code]: 200, [Response]: " + error + "\n");
								fh.close();
								return Response.status(200).entity(error.toString()).build();
							}
						} else {
							if (!keywordList.get(0).contains("POINT")) {
								int counter = 0;
								for (int i = 0; i < keywordList.size(); i++) {
									if (keywordList.get(i).contains(",")) {
										city = "";
									}
								}
								if (city.equals("null")) {
									response = "Comma (,) expected after city. Input should follow the following pattern: "
											+ "“<event> located in <city>, <country> in <year> <month> <day>” . "
											+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
											+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
											+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24”";
									JsonObject error = new JsonObject();
									error.addProperty("error", response);
									logger.info("[Response code]: 200, [Response]: " + error + "\n");
									fh.close();
									return Response.status(200).entity(error.toString()).build();
								}

								for (int i = 0; i < keywordList.size(); i++) {
									if (keywordList.get(i).contains(",")) {
										city = city + " " + keywordList.get(i).replaceAll(",", "");
										counter = i;
										city = city.substring(1);
										break;
									}

									city = city + " " + keywordList.get(i);
								}

								if (keywordList.size() < counter + 2) {
									response = "Country expected after city. Input should follow the following pattern: "
											+ "“<event> located in <city>, <country> in <year> <month> <day>” . "
											+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
											+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
											+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24”";
									JsonObject error = new JsonObject();
									error.addProperty("error", response);
									logger.info("[Response code]: 200, [Response]: " + error + "\n");
									fh.close();
									return Response.status(200).entity(error.toString()).build();
								}

								if (!keywordList.get(counter + 1).equals("in")) {
									int counter_country = 0;
									for (int i = counter + 1; i < keywordList.size(); i++) {
										if (keywordList.get(i).equals("in")) {
											country = "";
										}
									}
									for (int i = counter + 1; i < keywordList.size(); i++) {
										if (keywordList.get(i).equals("in")) {

											counter_country = i - 2 - counter;
											country = country.substring(1);
											break;
										}

										country = country + " " + keywordList.get(i);
									}

									if (keywordList.size() < counter_country + counter + 3) {
										response = "Expected 'in' keyword after city, country. Input should follow the following pattern: "
												+ "“<event> located in <city>, <country> in <year> <month> <day>” . "
												+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
												+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
												+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24”";
										JsonObject error = new JsonObject();
										error.addProperty("error", response);
										logger.info("[Response code]: 200, [Response]: " + error + "\n");
										fh.close();
										return Response.status(200).entity(error.toString()).build();
									}
									if (keywordList.get(counter_country + counter + 2).equals("in")) {
										if (keywordList.size() < counter_country + counter + 4) {
											response = "Expected 4-digit integer for 'year'. Input should follow the following pattern: "
													+ "“<event> located in <city>, <country> in <year> <month> <day>” . "
													+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
													+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
													+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24”";
											JsonObject error = new JsonObject();
											error.addProperty("error", response);
											logger.info("[Response code]: 200, [Response]: " + error + "\n");
											fh.close();
											return Response.status(200).entity(error.toString()).build();
										}
										if (isInteger(keywordList.get(counter_country + counter + 3)) && keywordList
												.get(counter_country + counter + 3).toString().length() == 4) {

											year = keywordList.get(counter_country + counter + 3);
											if (keywordList.size() >= counter_country + counter + 5) {

												if (!isInteger(keywordList.get(counter_country + counter + 4))
														|| keywordList.get(counter_country + counter + 4).toString()
																.length() != 2) {

													response = "Expected 2-digit integer for 'month'. Input should follow the following pattern: "
															+ "“<event> located in <city>, <country> in <year> <month> <day>” . "
															+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
															+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
															+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24”";
													JsonObject error = new JsonObject();
													error.addProperty("error", response);
													logger.info("[Response code]: 200, [Response]: " + error + "\n");
													fh.close();
													return Response.status(200).entity(error.toString()).build();
												}

											}
											if (keywordList.size() >= counter_country + counter + 6) {

												if (!isInteger(keywordList.get(counter_country + counter + 5))
														|| keywordList.get(counter_country + counter + 5).toString()
																.length() != 2) {

													response = "Expected 2-digit integer for 'day'. Input should follow the following pattern: "
															+ "“<event> located in <city>, <country> in <year> <month> <day>” . "
															+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
															+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
															+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24”";
													JsonObject error = new JsonObject();
													error.addProperty("error", response);
													logger.info("[Response code]: 200, [Response]: " + error + "\n");
													fh.close();
													return Response.status(200).entity(error.toString()).build();

												}
											}

											if (keywordList.size() >= counter_country + counter + 5) {

												if (isInteger(keywordList.get(counter_country + counter + 4))
														&& keywordList.get(counter_country + counter + 4).toString()
																.length() == 2) {

													month = keywordList.get(counter_country + counter + 4);
													if (Integer.parseInt(month) < 1 || Integer.parseInt(month) > 12) {
														response = "Month expected after year as an integer value between 1 and 12.";
														JsonObject error = new JsonObject();
														error.addProperty("error", response);
														logger.info(
																"[Response code]: 200, [Response]: " + error + "\n");
														fh.close();
														return Response.status(200).entity(error.toString()).build();
													}
													if (keywordList.size() > counter_country + counter + 5) {
														if (!keywordList.get(counter_country + counter + 5)
																.equals("with")) {
															if (!isInteger(
																	keywordList.get(counter_country + counter + 5))
																	|| keywordList.get(counter_country + counter + 5)
																			.toString().length() != 2) {

																response = "Expected 2-digit integer for 'day'. Input should follow the following pattern: "
																		+ "“<event> located in <city>, <country> in <year> <month> <day>” . "
																		+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
																		+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
																		+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24”";
																JsonObject error = new JsonObject();
																error.addProperty("error", response);
																logger.info("[Response code]: 200, [Response]: " + error
																		+ "\n");
																fh.close();
																return Response.status(200).entity(error.toString())
																		.build();
															}
														}
														if (isInteger(keywordList.get(counter_country + counter + 5))
																&& keywordList.get(counter_country + counter + 5)
																		.toString().length() == 2) {
															day = keywordList.get(counter_country + counter + 5);

														}
													}
												} else if (!keywordList.get(counter_country + counter + 4)
														.equals("with")) {
													if (!isInteger(keywordList.get(counter_country + counter + 4))
															|| keywordList.get(counter_country + counter + 4).toString()
																	.length() != 2) {
														response = "Expected 2-digit integer for 'month'. Input should follow the following pattern: "
																+ "“<event> located in POINT (<latitude> <longitude>) in <year> <month> <day>” . "
																+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
																+ "Latitude and longitude are expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
																+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
																+ "mentioned above is the following: “earthquake located in POINT (45.0 77.90424715741715) in 2019 03 24”";
														JsonObject error = new JsonObject();
														error.addProperty("error", response);
														logger.info(
																"[Response code]: 200, [Response]: " + error + "\n");
														fh.close();
														return Response.status(200).entity(error.toString()).build();
													}
												}
											}
										} else {
											response = "Expected 4-digit integer for 'year'. Input should follow the following pattern: "
													+ "“<event> located in <city>, <country> in <year> <month> <day>” . "
													+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
													+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
													+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24”";
											JsonObject error = new JsonObject();
											error.addProperty("error", response);
											logger.info("[Response code]: 200, [Response]: " + error + "\n");
											fh.close();
											return Response.status(200).entity(error.toString()).build();
										}
									} else {
										response = "Expected 'in' keyword after city, country. Input should follow the following pattern: "
												+ "“<event> located in <city>, <country> in <year> <month> <day>” . "
												+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
												+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
												+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24”";
										JsonObject error = new JsonObject();
										error.addProperty("error", response);
										logger.info("[Response code]: 200, [Response]: " + error + "\n");
										fh.close();
										return Response.status(200).entity(error.toString()).build();
									}
								} else {
									response = "Country expected after city. Input should follow the following pattern: "
											+ "“<event> located in <city>, <country> in <year> <month> <day>” . "
											+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
											+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
											+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24”";
									JsonObject error = new JsonObject();
									error.addProperty("error", response);
									logger.info("[Response code]: 200, [Response]: " + error + "\n");
									fh.close();
									return Response.status(200).entity(error.toString()).build();
								}

							} else {

								if (keywordList.get(0).contains("POINT")) {

									if (!input.contains("POINT ")) {
										response = "Space expected after POINT. Input should follow the following pattern: "
												+ "“<event> located in POINT (<latitude> <longitude>) in <year> <month> <day>” . "
												+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
												+ "Latitude and longitude are expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
												+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
												+ "mentioned above is the following: “earthquake located in POINT (45.0 77.90424715741715) in 2019 03 24”";

										JsonObject error = new JsonObject();
										error.addProperty("error", response);
										logger.info("[Response code]: 200, [Response]: " + error + "\n");
										fh.close();
										return Response.status(200).entity(error.toString()).build();
									}
									if (!isDouble(keywordList.get(1).replace("(", ""))
											|| !isDouble(keywordList.get(2).replace(")", ""))) {
										response = "Double numbers expected as latitude longitude after POINT. Input should follow the following pattern: "
												+ "“<event> located in POINT (<latitude> <longitude>) in <year> <month> <day>” . "
												+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
												+ "Latitude and longitude are expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
												+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
												+ "mentioned above is the following: “earthquake located in POINT (45.0 77.90424715741715) in 2019 03 24”";
										JsonObject error = new JsonObject();
										error.addProperty("error", response);
										logger.info("[Response code]: 200, [Response]: " + error + "\n");
										fh.close();
										return Response.status(200).entity(error.toString()).build();
									} else {
										if (!keywordList.get(1).startsWith("(")) {
											response = "Ρarenthesis '(' expected after POINT. Input should follow the following pattern: "
													+ "“<event> located in POINT (<latitude> <longitude>) in <year> <month> <day>” . "
													+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
													+ "Latitude and longitude are expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
													+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
													+ "mentioned above is the following: “earthquake located in POINT (45.0 77.90424715741715) in 2019 03 24”";
											JsonObject error = new JsonObject();
											error.addProperty("error", response);
											logger.info("[Response code]: 200, [Response]: " + error + "\n");
											fh.close();
											return Response.status(200).entity(error.toString()).build();
										}
										if (!keywordList.get(2).endsWith(")")) {
											response = "Ρarenthesis ')' expected after longitude. Input should follow the following pattern: "
													+ "“<event> located in POINT (<latitude> <longitude>) in <year> <month> <day>” . "
													+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
													+ "Latitude and longitude are expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
													+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
													+ "mentioned above is the following: “earthquake located in POINT (45.0 77.90424715741715) in 2019 03 24”";
											JsonObject error = new JsonObject();
											error.addProperty("error", response);
											logger.info("[Response code]: 200, [Response]: " + error + "\n");
											fh.close();
											return Response.status(200).entity(error.toString()).build();
										}
										latitude = keywordList.get(1).replace("(", "");
										longitude = keywordList.get(2).replace(")", "");
										if (!keywordList.get(3).equals("in")) {
											response = "Expected 'in' keyword after POINT (latitude longitude). Input should follow the following pattern: "
													+ "“<event> located in POINT (<latitude> <longitude>) in <year> <month> <day>” . "
													+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
													+ "Latitude and longitude are expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
													+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
													+ "mentioned above is the following: “earthquake located in POINT (45.0 77.90424715741715) in 2019 03 24”";
											JsonObject error = new JsonObject();
											error.addProperty("error", response);
											logger.info("[Response code]: 200, [Response]: " + error + "\n");
											fh.close();
											return Response.status(200).entity(error.toString()).build();
										} else {

											if (isInteger(keywordList.get(4))
													&& keywordList.get(4).toString().length() == 4) {

												year = keywordList.get(4);

												if (keywordList.size() > 5) {
													if (isInteger(keywordList.get(5))
															&& keywordList.get(5).toString().length() == 2) {

														month = keywordList.get(5);
														if (Integer.parseInt(month) < 1
																|| Integer.parseInt(month) > 12) {
															response = "Month expected after year as an integer value between 1 and 12.";
															JsonObject error = new JsonObject();
															error.addProperty("error", response);
															logger.info("[Response code]: 200, [Response]: " + error
																	+ "\n");
															fh.close();
															return Response.status(200).entity(error.toString())
																	.build();
														}
														if (keywordList.size() > 6) {
															if (isInteger(keywordList.get(6))
																	&& keywordList.get(6).toString().length() == 2) {

																day = keywordList.get(6);

															}

															else if ((!isInteger(keywordList.get(6))
																	|| keywordList.get(6).toString().length() != 2)) {

																response = "Expected 2-digit integer for 'day'. Input should follow the following pattern: "
																		+ "“<event> located in POINT (<latitude> <longitude>) in <year> <month> <day>” . "
																		+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
																		+ "Latitude and longitude are expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
																		+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
																		+ "mentioned above is the following: “earthquake located in POINT (45.0 77.90424715741715) in 2019 03 24”";
																JsonObject error = new JsonObject();
																error.addProperty("error", response);
																logger.info("[Response code]: 200, [Response]: " + error
																		+ "\n");
																fh.close();
																return Response.status(200).entity(error.toString())
																		.build();
															}
														}
													} else if ((!isInteger(keywordList.get(5))
															|| keywordList.get(5).toString().length() != 2)) {

														response = "Expected 2-digit integer for 'month'. Input should follow the following pattern: "
																+ "“<event> located in POINT (<latitude> <longitude>) in <year> <month> <day>” . "
																+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
																+ "Latitude and longitude are expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
																+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
																+ "mentioned above is the following: “earthquake located in POINT (45.0 77.90424715741715) in 2019 03 24”";
														JsonObject error = new JsonObject();
														error.addProperty("error", response);
														logger.info(
																"[Response code]: 200, [Response]: " + error + "\n");
														fh.close();
														return Response.status(200).entity(error.toString()).build();
													}
												}

											} else {
												response = "Expected 4-digit integer for 'year'. Input should follow the following pattern: "
														+ "“<event> located in <city>, <country> in <year> <month> <day>” . "
														+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
														+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
														+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24”";

												JsonObject error = new JsonObject();
												error.addProperty("error", response);
												logger.info("[Response code]: 200, [Response]: " + error + "\n");
												fh.close();
												return Response.status(200).entity(error.toString()).build();

											}
										}
									}
								}

							}
						}
					} else {
						response = "City expected after 'earthquake located in' phrase. Input should follow the following pattern: "
								+ "“<event> located in <city>, <country> in <year> <month> <day>” . "
								+ "It is worth mentioning that month and day are optional fields. All keywords are expected in English. "
								+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
								+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24”";

						JsonObject error = new JsonObject();
						error.addProperty("error", response);
						logger.info("[Response code]: 200, [Response]: " + error + "\n");
						fh.close();
						return Response.status(200).entity(error.toString()).build();
					}

				} else {

					if (!input.startsWith("earthquake located in")) {

						response = "Input should start with 'earthquake located in' phrase. Input should follow the following pattern: "
								+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
								+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
								+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
								+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
								+ "mentioned above is the following: earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0";
						JsonObject error = new JsonObject();
						error.addProperty("error", response);
						logger.info("[Response code]: 200, [Response]: " + error + "\n");
						fh.close();
						return Response.status(200).entity(error.toString()).build();
					} else if (!input.startsWith("earthquake located in ")) {

						response = "Space expected after 'earthquake located in' phrase. Input should follow the following pattern: "
								+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
								+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
								+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
								+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
								+ "mentioned above is the following: earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0";
						JsonObject error = new JsonObject();
						error.addProperty("error", response);
						logger.info("[Response code]: 200, [Response]: " + error + "\n");
						fh.close();
						return Response.status(200).entity(error.toString()).build();
					}
				}
			}

			if (latitude.equals("null") && longitude.equals("null")) {

				if (!city.equals("null") && !country.equals("null")) {
					// Calling location detection
					String latLong = LocationDetection.retrieveLatLong(city, country, kb_address_retrieve, logger);

					String[] locparts = latLong.split(":");
					latitude = locparts[0];
					longitude = locparts[1];
					if (latitude.equals("null") || longitude.equals("null")) {
						response = "Location not found. Please make sure to add a valid city and country name.";
						JsonObject error = new JsonObject();
						error.addProperty("error", response);
						logger.info("[Response code]: 200, [Response]: " + error + "\n");
						fh.close();
						return Response.status(200).entity(error.toString()).build();
					}
				} else {
					response = "Location not found. Please make sure to add a valid city and country name. Input should follow the following pattern: \r\n"
							+ "“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . \r\n"
							+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. \r\n"
							+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator.\r\n"
							+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters \r\n"
							+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
					JsonObject error = new JsonObject();
					error.addProperty("error", response);
					logger.info("[Response code]: 200, [Response]: " + error + "\n");
					fh.close();
					return Response.status(200).entity(error.toString()).build();
				}
			}

			String location = "POINT (" + latitude + " " + longitude + ")";

			System.out.println("[latitude]:" + latitude + " [longitude]" + longitude + " [year]:" + year + " [month]:"
					+ month + " [day]:" + day + " [magnitude]:" + magnitude + " [city]:" + city + " [country]:"
					+ country);
			logger.info("[latitude]:" + latitude + " [longitude]" + longitude + " [year]:" + year + " [month]:" + month
					+ " [day]:" + day + " [magnitude]:" + magnitude + " [city]:" + city + " [country]:" + country);
			JSONObject jobj = new JSONObject();
			jobj.put("cityLat", latitude);
			jobj.put("cityLong", longitude);
			jobj.put("year", year);
			jobj.put("month", month);
			jobj.put("day", day);

			jobj.put("source", eventSource);
			jobj.put("username", eventUsername);
			jobj.put("password", eventPassword);

			jobj.put("pageNumber", pagenumber);
			jobj.put("resultsPerPage", resultsperpage);
			System.out.println("Hey look at this event: " + jobj);
			Unirest.setTimeouts(0, 0);
			HttpResponse<String> eresponse;
			JSONArray translatedEvents = null;
			String dr_address = System.getenv("DR_ADDRESS");
			try {
				// Calling Data Receiver for non-earthquake events (generic use case)
				if (!input.startsWith("earthquake")) {
					eresponse = Unirest.post(dr_address + "event-" + eventType)
							.header("Content-Type", "application/json").body(jobj).asString();
					System.out.println("[Data Receiver request status]" + eresponse.getStatus());
					System.out.println("[Data Receiver request response]" + eresponse.getBody());
					translatedEvents = new JSONArray(eresponse.getBody().toString());
				} else { // Calling Data Receiver for earthquake events
					jobj.put("magnitude", magnitude);
					eresponse = Unirest.post(dr_address + "earthquake").header("Content-Type", "application/json")
							.body(jobj).asString();
					System.out.println("[Data Receiver request status]" + eresponse.getStatus());
					System.out.println("[Data Receiver request response]" + eresponse.getBody());
					translatedEvents = new JSONArray(eresponse.getBody().toString());
					
				}
			} catch (UnirestException e) {
				e.printStackTrace();
			}

			JSONObject result = new JSONObject();
			Model model = ModelFactory.createDefaultModel();
			String source = "";
			for (int i = 0; i < translatedEvents.length(); i++) {
				if (i == 0) {
					model = SemanticRepresentation.eventsInitialization(eventSource, uuid, year, month, day, location,
							city, country);
				}
				result = new JSONObject();
				result.put("event", translatedEvents.get(i));
				for (int j = 0; j < copernicusSources.size(); j++) {

					source = copernicusSources.get(j);
					Unirest.setTimeouts(0, 0);
					HttpResponse<String> presponse;
					JSONArray translatedProducts = null;
					JSONObject pobj = new JSONObject();
					pobj.put("eventDate", translatedEvents.getJSONObject(i).getString("timestamp"));
					pobj.put("eventLat", translatedEvents.getJSONObject(i).getString("latitude"));
					pobj.put("eventLong", translatedEvents.getJSONObject(i).getString("longitude"));
					//pobj.put("source", copernicusSources.get(j).replace("&format=json", "&fl=name,beginposition,endposition,orbitnumber,orbitdirection,polarisationmode,producttype&orderby=beginposition%20desc&format=json"));
					pobj.put("source", copernicusSources.get(j));
					
					pobj.put("username", copernicusUsername.get(j));
					pobj.put("password", copernicusPassword.get(j));
					pobj.put("address", copernicusAddresses.get(j));
					System.out.println("Hey look at this product: " + pobj);
					System.out.println("Product without orbit number: " + copernicusSources.get(j).toString());
					System.out.println("Product with orbit number: " + copernicusSources.get(j).toString().replace("&format=json", "&fl=name,beginposition,endposition,orbitnumber,orbitdirection,polarisationmode,producttype&orderby=beginposition%20desc&format=json"));
					
					try {
						// Calling Data Receiver for sentinel products
						presponse = Unirest.post(dr_address + "product").header("Content-Type", "application/json")
								.body(pobj).asString();
						
						System.out.println("[Data Receiver request status]" + presponse.getStatus());
						System.out.println("[Data Receiver request response]" + presponse.getBody());
						translatedProducts = new JSONArray(presponse.getBody().toString());

					} catch (UnirestException e) {

						e.printStackTrace();
					}
					result.put(source, translatedProducts);
					System.out.println("The translated product: " + translatedProducts);
				}

				model = SemanticRepresentation.resultsMapping(result, uuid, model, copernicusSources, eventType,
						additionalFields);

				// System.out.println("The results: " + result);
				// System.out.println("The uuid: " + uuid);
				// System.out.println("The model: " + model);
				// System.out.println("The copernicusSources: " + copernicusSources);
				// System.out.println("The eventType: " + eventType);
				// System.out.println("The additionalFields: " + additionalFields);
			}
			SemanticRepresentation.storeModel(model, kb_address, repository);

			JsonArray results = new JsonArray();
			results = SemanticRetrieval.retrieve(uuid, copernicusSources, kb_address_retrieve, logger,
					additionalFields);
			logger.info("[Response code]: 200, [Response]: " + results.toString() + "\n");
			fh.close();
			CleaningKB.delete(uuid, kb_address, repository);
			return Response.status(200).entity(String.valueOf(results)).build();
		} else if (task.equals("population")) {
			// Only users with Semantic Framework Manager role are able to fire population
			if (!has_sfmanager_role) {
				String resp = "The user has no assigned 'Semantic Framework Manager' role.";
				JsonObject error = new JsonObject();
				error.addProperty("error", resp);
				logger.info("[Response code]: 401, [Response]: " + error + "\n");
				fh.close();
				return Response.status(401).entity(error.toString()).build();
			}
			// Semantic representation and storage of location-related information (this
			// procedure should be done only once on the initial setup of the framework)
			String kb_address = System.getenv("KB_ADDRESS");
			System.out.println("Hi Alex 0");
			Model geoModel = ModelFactory.createDefaultModel();
			System.out.println("Hi Alex 1");
			geoModel = LocationDetection.preprocessing(logger);
			System.out.println("Hi Alex 2");
			System.out.println("The geo model: " + geoModel);
			System.out.println("The geo repository: " + geoRepository);
			SemanticRepresentation.storeModel(geoModel, kb_address, geoRepository);
			JsonObject status = new JsonObject();
			status.addProperty("status", "Successfully added to KB.");
			logger.info("[Response code]: 200, [Response]: " + status.toString() + "\n");
			fh.close();
			return Response.status(200).entity(status.toString()).build();

		}

		JsonObject error = new JsonObject();
		error.addProperty("error", "Unexpected parameter {task}. Was expecting one of 'population' or 'retrieve'.");
		logger.info("[Response code]: 200, [Response]: " + error.toString() + "\n");
		fh.close();
		return Response.status(200).entity(error.toString()).build();

	}



	public static boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return false;
		} catch (NullPointerException e) {
			return false;
		}
		// only got here if we didn't return false
		return true;
	}

	private static boolean isDouble(String string) {
		try {
			Double.parseDouble(string);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public static boolean hasOneDecimalPoint(String floatNumber) {

		String floatAsString = String.valueOf(floatNumber);

		if (floatAsString.contains(".") && floatAsString.length() == 3) {
			int indexOfDecimal = floatAsString.indexOf(".");
			if (floatAsString.substring(indexOfDecimal).length() == 2) {
				return true;
			}
		}
		return false;
	}
}
