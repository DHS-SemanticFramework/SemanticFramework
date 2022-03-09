
package com.application.SemanticFramework;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.codehaus.jettison.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.exceptions.UnirestException;


/** Example resource class hosted at the URI path "/myresource"
 */

/** Example resource class hosted at the URI path "/myresource"
 */

@Path("/{task}")
public class MyResource {
    
    /** Method processing HTTP GET requests, producing "text/plain" MIME media
     * type.
     * @return String that will be send back as a response of type "text/plain".
     * @throws org.json.JSONException 
     * @throws UnirestException 
     */
	
	
	//Input: earthquake located in Rome, Italy in 2019 -03- -02- with magnitude greater than -6.0-.
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces("text/plain")
	
	public Response retrieval(@PathParam("task") String task, String object) throws JSONException, IOException, ParseException, org.json.JSONException, UnirestException {
		
		if(task.equals("retrieve")) {
		String kb_address = System.getenv("KB_ADDRESS"); //docker
		String response;
		Gson gson = new Gson();
		
		JsonElement json = gson.fromJson(object, JsonElement.class);
		JsonObject jobject = json.getAsJsonObject();
		String input= jobject.get("text").getAsString();
		String city="null", country="null", year="null", month="null", day="null", magnitude="5.0";
		String latitude="null", longitude="null";
		
		if(input.startsWith("earthquake located in ")) {
			String parsed=input;
			parsed=parsed.replace("earthquake located in ", "");
			List<String> keywordList = Arrays.asList(parsed.split(" "));
		
			if(keywordList.size()>0 && !keywordList.get(0).isEmpty()) {
				if(keywordList.contains("with") && (!keywordList.contains("magnitude") || !keywordList.contains("greater") ||!keywordList.contains("than") || !isDouble(keywordList.get(keywordList.size()-1)) 
						|| !hasOneDecimalPoint(keywordList.get(keywordList.size()-1)))) {
					response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
							+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
							+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
							+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
							+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
							+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
					JsonObject error = new JsonObject ();
					error.addProperty("error", response);	
					return Response.status(200).entity(error.toString()).build();
				}
				if(keywordList.contains("magnitude") && (!keywordList.contains("with") || !keywordList.contains("greater") ||!keywordList.contains("than")|| !isDouble(keywordList.get(keywordList.size()-1)) 
						|| !hasOneDecimalPoint(keywordList.get(keywordList.size()-1)))) {
					response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
							+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
							+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
							+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
							+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
							+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
					JsonObject error = new JsonObject ();
					error.addProperty("error", response);	
					return Response.status(200).entity(error.toString()).build();
				}
				if(keywordList.contains("greater") && (!keywordList.contains("magnitude") || !keywordList.contains("with") ||!keywordList.contains("than")|| !isDouble(keywordList.get(keywordList.size()-1)) 
						|| !hasOneDecimalPoint(keywordList.get(keywordList.size()-1)))) {
					response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
							+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
							+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
							+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
							+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
							+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
					JsonObject error = new JsonObject ();
					error.addProperty("error", response);	
					return Response.status(200).entity(error.toString()).build();
				}
				if(keywordList.contains("than") && (!keywordList.contains("magnitude") || !keywordList.contains("greater") ||!keywordList.contains("with")|| !isDouble(keywordList.get(keywordList.size()-1)) 
						|| !hasOneDecimalPoint(keywordList.get(keywordList.size()-1)))) {
					response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
							+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
							+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
							+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
							+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
							+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
					JsonObject error = new JsonObject ();
					error.addProperty("error", response);	
					return Response.status(200).entity(error.toString()).build();
				}
			
			if(keywordList.get(0).contains(",")) {
				if(keywordList.get(0).toString().charAt(keywordList.get(0).toString().length()-1)!=',' ) {
					response = "Space expected after comma. Input should follow the following pattern: "
							+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
							+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
							+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
							+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
							+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
					JsonObject error = new JsonObject ();
					error.addProperty("error", response);	
					return Response.status(200).entity(error.toString()).build();
				}
				city = keywordList.get(0).replaceAll(",", "");
				System.out.println(keywordList.size());
				if(keywordList.size()<2) {
					response = "Country expected after city. Input should follow the following pattern: "
							+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
							+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
							+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
							+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
							+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
					JsonObject error = new JsonObject ();
					error.addProperty("error", response);	
					return Response.status(200).entity(error.toString()).build();
				}
				
				if(!keywordList.get(1).equals("in")) {
					country=keywordList.get(1);
					if(keywordList.size()<3) {
						response = "Expected 'in' keyword after city, country. Input should follow the following pattern: "
								+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
								+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
								+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
								+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
								+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
						JsonObject error = new JsonObject ();
						error.addProperty("error", response);	
						return Response.status(200).entity(error.toString()).build();
					}
					if(keywordList.get(2).equals("in")) {
						if(keywordList.size()<4) {
							response = "Expected 4-digit integer for 'year'. Input should follow the following pattern: "
									+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
									+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
									+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
									+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
									+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
							JsonObject error = new JsonObject ();
							error.addProperty("error", response);	
							return Response.status(200).entity(error.toString()).build();
						}
						if(isInteger(keywordList.get(3)) && keywordList.get(3).toString().length()==4) {
							
							year=keywordList.get(3);
							if(keywordList.size()==5) {
								if(keywordList.get(4).equals("with")) { 
									
									response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
											+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
											+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
											+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
											+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
											+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
									JsonObject error = new JsonObject ();
									error.addProperty("error", response);	
									return Response.status(200).entity(error.toString()).build();
								}
								if(!isInteger(keywordList.get(4)) || keywordList.get(4).toString().length()!=2) { 
								
								response = "Expected 2-digit integer for 'month'. Input should follow the following pattern: "
										+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
										+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
										+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
										+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
										+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
								JsonObject error = new JsonObject ();
								error.addProperty("error", response);	
								return Response.status(200).entity(error.toString()).build();
							}
							
							}
							if(keywordList.size()==6) {
								if(keywordList.get(5).equals("with")) { 
									
									response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
											+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
											+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
											+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
											+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
											+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
									JsonObject error = new JsonObject ();
									error.addProperty("error", response);	
									return Response.status(200).entity(error.toString()).build();
								}
								if(!isInteger(keywordList.get(5)) || keywordList.get(5).toString().length()!=2) { 
									
									response = "Expected 2-digit integer for 'day'. Input should follow the following pattern: "
											+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
											+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
											+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
											+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
											+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
									JsonObject error = new JsonObject ();
									error.addProperty("error", response);	
									return Response.status(200).entity(error.toString()).build();
									
								}
							}
							
							if(keywordList.contains("with") && (!keywordList.contains("magnitude") || !keywordList.contains("greater") ||!keywordList.contains("than") || !isDouble(keywordList.get(keywordList.size()-1)) 
									|| !hasOneDecimalPoint(keywordList.get(keywordList.size()-1)))) {
								response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
										+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
										+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
										+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
										+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
										+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
								JsonObject error = new JsonObject ();
								error.addProperty("error", response);	
								return Response.status(200).entity(error.toString()).build();
							}
							if(keywordList.contains("magnitude") && (!keywordList.contains("with") || !keywordList.contains("greater") ||!keywordList.contains("than")|| !isDouble(keywordList.get(keywordList.size()-1)) 
									|| !hasOneDecimalPoint(keywordList.get(keywordList.size()-1)))) {
								response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
										+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
										+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
										+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
										+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
										+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
								JsonObject error = new JsonObject ();
								error.addProperty("error", response);	
								return Response.status(200).entity(error.toString()).build();
							}
							if(keywordList.contains("greater") && (!keywordList.contains("magnitude") || !keywordList.contains("with") ||!keywordList.contains("than")|| !isDouble(keywordList.get(keywordList.size()-1)) 
									|| !hasOneDecimalPoint(keywordList.get(keywordList.size()-1)))) {
								response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
										+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
										+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
										+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
										+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
										+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
								JsonObject error = new JsonObject ();
								error.addProperty("error", response);	
								return Response.status(200).entity(error.toString()).build();
							}
							if(keywordList.contains("than") && (!keywordList.contains("magnitude") || !keywordList.contains("greater") ||!keywordList.contains("with")|| !isDouble(keywordList.get(keywordList.size()-1)) 
									|| !hasOneDecimalPoint(keywordList.get(keywordList.size()-1)))) {
								response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
										+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
										+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
										+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
										+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
										+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
								JsonObject error = new JsonObject ();
								error.addProperty("error", response);	
								return Response.status(200).entity(error.toString()).build();
							} 
							
							if(keywordList.size()>6) { 
								
							if(isInteger(keywordList.get(4)) && keywordList.get(4).toString().length()==2) {
								
								month = keywordList.get(4);
								if(!keywordList.get(5).equals("with")) {
								if(!isInteger(keywordList.get(5)) || keywordList.get(5).toString().length()!=2) { 
									
									response = "Expected 2-digit integer for 'day'. Input should follow the following pattern: "
											+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
											+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
											+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
											+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
											+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
									JsonObject error = new JsonObject ();
									error.addProperty("error", response);	
									return Response.status(200).entity(error.toString()).build();
								}
								}
								else {
									if(keywordList.size()>=10) { 
										if(keywordList.get(6).equals("magnitude") && keywordList.get(7).equals("greater") && keywordList.get(8).equals("than")) {
											if(!isDouble(keywordList.get(9)) || !hasOneDecimalPoint(keywordList.get(9))) {
												response = "Expected double value with one decimal point as 'magnitude'. Input should follow the following pattern: "
														+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
														+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
														+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
														+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
														+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
												JsonObject error = new JsonObject ();
												error.addProperty("error", response);	
												return Response.status(200).entity(error.toString()).build();
											}
											
											magnitude=keywordList.get(9).toString(); 
											if(Double.parseDouble(magnitude)<5.0) {
												response = "The value of the magnitude must be at least 5.0.";
												JsonObject error = new JsonObject ();
												error.addProperty("error", response);	
												return Response.status(200).entity(error.toString()).build();
											}
										}
										else {
											response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
													+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
													+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
													+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
													+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
													+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
											JsonObject error = new JsonObject ();
											error.addProperty("error", response);	
											return Response.status(200).entity(error.toString()).build();
										}
									}
								}
								if(isInteger(keywordList.get(5)) && keywordList.get(5).toString().length()==2) {
									day = keywordList.get(5);
									if(keywordList.size()>=11) { 
										if(keywordList.get(6).equals("with") && keywordList.get(7).equals("magnitude") && keywordList.get(8).equals("greater")
											&& keywordList.get(9).equals("than")) {
											if(!isDouble(keywordList.get(10)) || !hasOneDecimalPoint(keywordList.get(10))) {
												response = "Expected double value with one decimal point as 'magnitude'. Input should follow the following pattern: "
														+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
														+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
														+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
														+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
														+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
												JsonObject error = new JsonObject ();
												error.addProperty("error", response);	
												return Response.status(200).entity(error.toString()).build();
											}
											
											magnitude=keywordList.get(10).toString(); 
											if(Double.parseDouble(magnitude)<5.0) {
												response = "The value of the magnitude must be at least 5.0.";
												JsonObject error = new JsonObject ();
												error.addProperty("error", response);	
												return Response.status(200).entity(error.toString()).build();
											}
										}
										else {
											response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
													+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
													+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
													+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
													+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
													+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
											JsonObject error = new JsonObject ();
											error.addProperty("error", response);	
											return Response.status(200).entity(error.toString()).build();
										}
									}
									else {
										response = "Expected 'with magnitude greater than <value>'. Input should follow the following pattern: "
												+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
												+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
												+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
												+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
												+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
										JsonObject error = new JsonObject ();
										error.addProperty("error", response);	
										return Response.status(200).entity(error.toString()).build();
									}
								}
								}
								else if(keywordList.size()==9) {
								
								if(keywordList.get(4).equals("with") && keywordList.get(5).equals("magnitude") && keywordList.get(6).equals("greater")
									&& keywordList.get(7).equals("than")) {
									if(!isDouble(keywordList.get(8)) || !hasOneDecimalPoint(keywordList.get(8))) {
										response = "Expected double value with one decimal point as 'magnitude'. Input should follow the following pattern: "
												+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
												+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
												+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
												+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
												+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
										JsonObject error = new JsonObject ();
										error.addProperty("error", response);	
										return Response.status(200).entity(error.toString()).build();
									}
								magnitude=keywordList.get(8).toString(); 
								if(Double.parseDouble(magnitude)<5.0) {
									response = "The value of the magnitude must be at least 5.0.";
									JsonObject error = new JsonObject ();
									error.addProperty("error", response);	
									return Response.status(200).entity(error.toString()).build();
								}
							    System.out.println("magnitude"+magnitude);
								}
							} 
								else if(keywordList.size()>=10) {
									
									if(keywordList.get(5).equals("with") && keywordList.get(6).equals("magnitude") && keywordList.get(7).equals("greater")
										&& keywordList.get(8).equals("than")) {
										if(!isDouble(keywordList.get(9)) || !hasOneDecimalPoint(keywordList.get(9))) {
											response = "Expected double value with one decimal point as 'magnitude'. Input should follow the following pattern: "
													+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
													+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
													+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
													+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
													+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
											JsonObject error = new JsonObject ();
											error.addProperty("error", response);	
											return Response.status(200).entity(error.toString()).build();
										}
									magnitude=keywordList.get(9).toString(); 
									if(Double.parseDouble(magnitude)<5.0) {
										response = "The value of the magnitude must be at least 5.0.";
										JsonObject error = new JsonObject ();
										error.addProperty("error", response);	
										return Response.status(200).entity(error.toString()).build();
									}
									}
								}
								
								
							}
							else if(keywordList.size()>=9) { 
								if(keywordList.get(4).equals("with") && keywordList.get(5).equals("magnitude") && keywordList.get(6).equals("greater")
									&& keywordList.get(7).equals("than")) {
									if(!isDouble(keywordList.get(8)) || !hasOneDecimalPoint(keywordList.get(8))) {
										response = "Expected double value with one decimal point as 'magnitude'. Input should follow the following pattern: "
												+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
												+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
												+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
												+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
												+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
										JsonObject error = new JsonObject ();
										error.addProperty("error", response);	
										return Response.status(200).entity(error.toString()).build();
									}
									magnitude=keywordList.get(8).toString(); 
									if(Double.parseDouble(magnitude)<5.0) {
										response = "The value of the magnitude must be at least 5.0.";
										JsonObject error = new JsonObject ();
										error.addProperty("error", response);	
										return Response.status(200).entity(error.toString()).build();
									}
								}
							}
							
						}
						else {
							response = "Expected 4-digit integer for 'year'. Input should follow the following pattern: "
									+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
									+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
									+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
									+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
									+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
							JsonObject error = new JsonObject ();
							error.addProperty("error", response);	
							return Response.status(200).entity(error.toString()).build();
						}
					}
					else {
						response = "Expected 'in' keyword after city, country. Input should follow the following pattern: "
								+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
								+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
								+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
								+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
								+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
						JsonObject error = new JsonObject ();
						error.addProperty("error", response);	
						return Response.status(200).entity(error.toString()).build();
					}
				}
				else {
					response = "Country expected after city. Input should follow the following pattern: "
							+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
							+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
							+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
							+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
							+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
					JsonObject error = new JsonObject ();
					error.addProperty("error", response);	
					return Response.status(200).entity(error.toString()).build();
				}
			}
			else {
				if(!keywordList.get(0).contains("POINT")) {
					response = "Comma (,) expected after city. Input should follow the following pattern: "
							+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
							+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
							+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
							+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
							+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
					JsonObject error = new JsonObject ();
					error.addProperty("error", response);	
					return Response.status(200).entity(error.toString()).build();
				}
				else {
					
					if (keywordList.get(0).contains("POINT")) {
						System.out.println(keywordList.get(1)+" "+keywordList.get(2)+" "+keywordList.get(3)+" ");
						 
						if(!isDouble(keywordList.get(1).replace("(", "")) || !isDouble(keywordList.get(2).replace(")", ""))) {
							response = "Double numbers expected as latitude longitude after POINT. Input should follow the following pattern: "
									+"“earthquake located in POINT (<latitude> <longitude>) in <year> <month> <day> with magnitude greater than <magnitude>” . "
									+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
									+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
									+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
									+ "mentioned above is the following: “earthquake located in POINT (45.0 77.90424715741715) in 2019 03 24 with magnitude greater than 6.0”";
							JsonObject error = new JsonObject ();
							error.addProperty("error", response);	
							return Response.status(200).entity(error.toString()).build();
						}
						else {
							latitude = keywordList.get(1).replace("(", "");
							longitude = keywordList.get(2).replace(")", "");
							if(!keywordList.get(3).equals("in")) {
							response = "Expected 'in' keyword after POINT (latitude longitude). Input should follow the following pattern: "
									+"“earthquake located in POINT (<latitude> <longitude>) in <year> <month> <day> with magnitude greater than <magnitude>” . "
									+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
									+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
									+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
									+ "mentioned above is the following: “earthquake located in POINT (45.0 77.90424715741715) in 2019 03 24 with magnitude greater than 6.0”";
							JsonObject error = new JsonObject ();
							error.addProperty("error", response);	
							return Response.status(200).entity(error.toString()).build();
							}
							else {
						
							if(isInteger(keywordList.get(4)) && keywordList.get(4).toString().length()==4) {
								
								year=keywordList.get(4);
								
									if(keywordList.size()>5) {
									if(isInteger(keywordList.get(5)) || keywordList.get(5).toString().length()==2) { 
										
										month = keywordList.get(5);
										if(keywordList.size()>6) {
										if(isInteger(keywordList.get(6)) && keywordList.get(6).toString().length()==2) { 
											
											
											day = keywordList.get(6);
											if(keywordList.size()>11) {
											 if (keywordList.get(7).equals("with")) {
												
													if(keywordList.get(8).equals("magnitude") && keywordList.get(9).equals("greater") && keywordList.get(10).equals("than") && isDouble(keywordList.get(keywordList.size()-1)) 
															&& hasOneDecimalPoint(keywordList.get(keywordList.size()-1))) {
														
														magnitude = keywordList.get(keywordList.size()-1);
														if(Double.parseDouble(magnitude)<5.0) {
															response = "The value of the magnitude must be at least 5.0.";
															JsonObject error = new JsonObject ();
															error.addProperty("error", response);	
															return Response.status(200).entity(error.toString()).build();
														}
													}	
												}
											}
										}
										else if((isInteger(keywordList.get(6)) && keywordList.get(6).toString().length()!=2) ||
												(!isInteger(keywordList.get(6)) && keywordList.get(6).toString().length()==2)) {
											
											response = "Expected 2-digit integer for 'day'. Input should follow the following pattern: "
													+"“earthquake located in POINT (<latitude> <longitude>) in <year> <month> <day> with magnitude greater than <magnitude>” . "
													+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
													+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
													+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
													+ "mentioned above is the following: “earthquake located in POINT (45.0 77.90424715741715) in 2019 03 24 with magnitude greater than 6.0”";
											JsonObject error = new JsonObject ();
											error.addProperty("error", response);	
											return Response.status(200).entity(error.toString()).build();
										}
										else if (keywordList.get(6).equals("with")) {
											if(keywordList.size()>10) {
												if(keywordList.get(7).equals("magnitude") && keywordList.get(8).equals("greater") && keywordList.get(9).equals("than") && isDouble(keywordList.get(keywordList.size()-1)) 
														&& hasOneDecimalPoint(keywordList.get(keywordList.size()-1))) {
													
													magnitude = keywordList.get(keywordList.size()-1);
													if(Double.parseDouble(magnitude)<5.0) {
														response = "The value of the magnitude must be at least 5.0.";
														JsonObject error = new JsonObject ();
														error.addProperty("error", response);	
														return Response.status(200).entity(error.toString()).build();
													}
												}
											}
										}
										}
									}
									else if(keywordList.get(5).equals("with")) {
										if(keywordList.size()>9) {
											if(keywordList.get(6).equals("magnitude") && keywordList.get(7).equals("greater") && keywordList.get(8).equals("than") && isDouble(keywordList.get(keywordList.size()-1)) 
													&& hasOneDecimalPoint(keywordList.get(keywordList.size()-1))) {
												
												magnitude = keywordList.get(keywordList.size()-1);
												if(Double.parseDouble(magnitude)<5.0) {
													response = "The value of the magnitude must be at least 5.0.";
													JsonObject error = new JsonObject ();
													error.addProperty("error", response);	
													return Response.status(200).entity(error.toString()).build();
												}
											}
										}
									}
									else if((isInteger(keywordList.get(5)) && keywordList.get(5).toString().length()!=2) ||
											(!isInteger(keywordList.get(5)) && keywordList.get(5).toString().length()==2)) {
										
										response = "Expected 2-digit integer for 'month'. Input should follow the following pattern: "
												+"“earthquake located in POINT (<latitude> <longitude>) in <year> <month> <day> with magnitude greater than <magnitude>” . "
												+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
												+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
												+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
												+ "mentioned above is the following: “earthquake located in POINT (45.0 77.90424715741715) in 2019 03 24 with magnitude greater than 6.0”";
										JsonObject error = new JsonObject ();
										error.addProperty("error", response);	
										return Response.status(200).entity(error.toString()).build();
									}
									}
						
									
							}
							else {
								response = "Expected 4-digit integer for 'year'. Input should follow the following pattern: "
										+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
										+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
										+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
										+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
										+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
								JsonObject error = new JsonObject ();
								error.addProperty("error", response);	
								return Response.status(200).entity(error.toString()).build();
							
							}
							}
						}
					}
				}
			}
			}
			else {
				response = "City expected after 'earthquake located in' phrase. Input should follow the following pattern: "
						+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
						+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
						+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
						+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
						+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
				JsonObject error = new JsonObject ();
				error.addProperty("error", response);	
				return Response.status(200).entity(error.toString()).build();
			}
			
		}
		else {
			
			if(!input.startsWith("earthquake")) {
				
				response = "Input should start with 'earthquake' keyword. Input should follow the following pattern: "
						+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
						+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
						+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
						+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
						+ "mentioned above is the following: “earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0”";
				JsonObject error = new JsonObject ();
				error.addProperty("error", response);	
				return Response.status(200).entity(error.toString()).build();
			}
			else {
			
				if(!input.startsWith("earthquake located in")) {
					
					response = "Input should start with 'earthquake located in' phrase. Input should follow the following pattern: "
							+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
							+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
							+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
							+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
							+ "mentioned above is the following: earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0";
					JsonObject error = new JsonObject ();
					error.addProperty("error", response);	
					return Response.status(200).entity(error.toString()).build();
				}
				else if(!input.startsWith("earthquake located in ")) {
					
					response = "Space expected after 'earthquake located in' phrase. Input should follow the following pattern: "
							+"“earthquake located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . "
							+ "It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. "
							+ "Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. "
							+ "Date is expected in yyyy mm dd format. So for instance, an input example that contains all the parameters "
							+ "mentioned above is the following: earthquake located in Rome, Italy in 2019 03 24 with magnitude greater than 6.0";
					JsonObject error = new JsonObject ();
					error.addProperty("error", response);	
					return Response.status(200).entity(error.toString()).build();
				}
			}
		} 
		
		if(latitude.equals("null") && longitude.equals("null")) {
			// Calling location detection
			// latitude =
			// longitude =
		}
		String localDir = System.getProperty("user.dir");

		String path ="C:/Users/mariarousi/eclipse-workspace/SemanticFramework/configuration.json";
		//String path ="/code/config/configuration.json"; //docker
	    BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
	    JsonElement conf = gson.fromJson(bufferedReader, JsonElement.class);
	   
	    ArrayList<String> copernicusSources = new ArrayList <String>();
	    ArrayList<String> copernicusUsername = new ArrayList <String>();
	    ArrayList<String> copernicusPassword = new ArrayList <String>();
	    
	    String eventSource = "", eventUsername="", eventPassword="";
	    
	    for (int i=0; i<conf.getAsJsonObject().get("sources").getAsJsonArray().size(); i++) {
	    	if(conf.getAsJsonObject().get("sources").getAsJsonArray().get(i).getAsJsonObject().has("type")) {
	    		JsonObject obj = new JsonObject ();
	    		obj = conf.getAsJsonObject().get("sources").getAsJsonArray().get(i).getAsJsonObject();
	    		if(obj.get("type").getAsString().equals("copernicus")) {
	    			if(obj.get("dataSource").getAsString().contains("[DHuSAddress]") && obj.has("DHuSAddress") ) {
	    				copernicusSources.add(obj.get("dataSource").getAsString().replace("[DHuSAddress]", obj.get("DHuSAddress").getAsString()));
	    			}
	    			else {
	    				copernicusSources.add(obj.get("dataSource").getAsString());
	    			}
	    			copernicusUsername.add(obj.get("username").getAsString());
	    			copernicusPassword.add(obj.get("password").getAsString());
	    		}
	    	}
	    	
	    	if(conf.getAsJsonObject().get("sources").getAsJsonArray().get(i).getAsJsonObject().has("eventType")) {
	    		JsonObject obj = new JsonObject ();
	    		obj = conf.getAsJsonObject().get("sources").getAsJsonArray().get(i).getAsJsonObject();
	    		if(input.startsWith(obj.get("eventType").getAsString())) {
		    		eventSource=obj.get("dataSource").getAsString();
		    		eventUsername=obj.get("username").getAsString();
		    		eventPassword=obj.get("password").getAsString();
	    		}
	    		else {
	    			response = "Input should start with '"+obj.get("eventType").getAsString()+" located in' phrase. Input should follow the following pattern: "
							+"“"+obj.get("eventType").getAsString()+" located in <city>, <country> in <year> <month> <day> with magnitude greater than <magnitude>” . ";
	    			JsonObject error = new JsonObject ();
					error.addProperty("error", response);	
					return Response.status(200).entity(error.toString()).build();
			
	    		}
	    	} 	
	    }
		String uuid = UUID.randomUUID().toString().replaceAll("-", "");

		String location="POINT ("+latitude+" "+longitude+")";
		String events = DataReceiver.eventReceiver(latitude, longitude, year, month, day, magnitude, eventSource, eventUsername, eventPassword);
		
		JSONArray translatedEvents = DataTranslator.translateEvents(events);
		
		JSONObject result = new JSONObject();
		Model model = ModelFactory.createDefaultModel();
		String source="";
		for (int i=0; i<translatedEvents.length(); i++) {
			if(i==0) {
				model= SemanticRepresentation.eventsInitialization(eventSource, uuid, year, month, day, location, city, country);
			}
			result = new JSONObject();
			result.put("event", translatedEvents.get(i));
			for (int j=0; j<copernicusSources.size(); j++) {
				if(copernicusSources.get(j).contains("scihub")) {
					
					
					source = copernicusSources.get(j);
				}
				else if (copernicusSources.get(j).contains("onda-dias")) {
					
					source = copernicusSources.get(j);
				}
				
				result.put(source, DataReceiver.productsReceiver(translatedEvents.getJSONObject(i).getString("timestamp"), translatedEvents.getJSONObject(i).getString("latitude"), 
						translatedEvents.getJSONObject(i).getString("longitude"), copernicusSources.get(j), copernicusUsername.get(j), 
						copernicusPassword.get(j)));
			}
			model=SemanticRepresentation.resultsMapping(result, uuid, model, copernicusSources);
			
		}
		SemanticRepresentation.storeModel(model, kb_address);
		
		JSONArray results;
		results=SemanticRetrieval.retrieve(uuid,copernicusSources);

        return Response.status(200).entity(results.toString()).build();
		}
		else if(task.equals("population")) {
			
		}
		JsonObject error = new JsonObject ();
		error.addProperty("error", "Unexpected parameter {task}. Was expecting one of 'population' or 'retrieve'.");	
		return Response.status(200).entity(error.toString()).build();

		
    	
    }
		public static boolean isInteger(String s) {
		    try { 
		        Integer.parseInt(s); 
		    } catch(NumberFormatException e) { 
		        return false; 
		    } catch(NullPointerException e) {
		        return false;
		    }
		    // only got here if we didn't return false
		    return true;
		}
		private static boolean isDouble(String string)
		{
		    try
		    {
		        Double.parseDouble(string);
		    }
		    catch (NumberFormatException e)
		    {
		        return false;
		    }
		    return true;
		}
		
		public static boolean hasOneDecimalPoint(String floatNumber) {
			
			String floatAsString= String.valueOf(floatNumber);
			
			if(floatAsString.contains(".") && floatAsString.length()==3) {
				int indexOfDecimal = floatAsString.indexOf(".");
				if(floatAsString.substring(indexOfDecimal).length() == 2) {
					return true;
				}
			}
			return false;
		}
}
