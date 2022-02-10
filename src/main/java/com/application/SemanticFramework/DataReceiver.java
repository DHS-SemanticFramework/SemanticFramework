package com.application.SemanticFramework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DataReceiver {

	public static String eventReceiver(String cityLat, String cityLong, String year, String month, String day, String magnitude, String source, String username, String password) throws IOException {
		  
		
		  System.out.println(year+"-"+  month+"-"+  day +"-"+ magnitude);
		  cityLat = "37.97945";
		  cityLong= "23.71622";
		  String date, enddate;
		  if(month.equals("null")) {
			date = year+"-01-01";
			enddate = year+"-12-31";
		  }
		  else if (day.equals("null")) {
			date = year+"-"+month+"-01";
			String lastday = getLastDateOfMonth(date.replaceAll("-", "/"));
			enddate = year+"-"+month+"-"+lastday; 
		  }
		  else {
			date = year+"-"+month+"-"+day;
			enddate = year+"-"+month+"-"+day;
		  }
		
		  if(Double.parseDouble(magnitude)<5.0) {
			  magnitude="5.0";
		  }
		
		  String urlToRead = "https://webservices.ingv.it/fdsnws/event/1/query?starttime="+date+"T00:00:00&endtime="+enddate+"T23:59:59&minmagnitude="+magnitude+"&format=text&lat="+cityLat+"&lon="+cityLong+"&maxradiuskm=50";
		  urlToRead =source.replace("[SelectedDate]", date).replace("[EndDate]", enddate).replace("[magnitude]", magnitude).replace("[CityLat]", cityLat).replace("[CityLong]", cityLong);
		  System.out.println(urlToRead);
		  StringBuilder result = new StringBuilder();
	      URL url = new URL(urlToRead);
	      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	      conn.setRequestMethod("GET");
	      BufferedReader reader = new BufferedReader(
	                  new InputStreamReader(conn.getInputStream())); 
	          for (String line; (line = reader.readLine()) != null; ) {
	              result.append(line+"\n");
	          }
	     
	      return result.toString();
		
	}
	public static String productsReceiver(String eventDate, String eventLat, String eventLong, String source, String username, String password) throws IOException, UnirestException {
		
		  /* Configuration file: 
		   •	sensing date of the data source(s) in the form of yyyy-mm-dd ?
		  */
		  String minus = dateCalculation(eventDate.substring(0, 10), "minus");
		  String plus = dateCalculation(eventDate.substring(0, 10), "plus");
		  ArrayList <String> dataSources = new ArrayList <String> ();
		  
		  System.out.println(source);
		  source=source.replace("[eventLat]", eventLat).replace("[eventLong]", eventLong).replace("[eventStartTime]", minus).replace("[eventEndTime]", plus);
		  System.out.println(source);
		  dataSources.add(source);
		  String result="";
		  String finalresult ="";

		  if(!source.contains("onda-dias")) {
			  result = "";
	      
			  URL url = new URL(source);
			  HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			  conn.setRequestMethod("GET");
			  conn.setRequestProperty("Content-Type", "application/json");
	  
	        
			  String auth = username + ":" + password;
			  byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
	      
			  String authHeaderValue = "Basic " + new String(encodedAuth);
			  conn.setRequestProperty("Authorization", authHeaderValue);

			  BufferedReader reader = new BufferedReader(
	                  new InputStreamReader(conn.getInputStream()));
	          for (String line; (line = reader.readLine()) != null; ) {
	             
	             result+=line; 
	          }
	          
	          finalresult+=DataTranslator.translateCopernicusMetadata(result.toString(), "dhus");
	    
		  }
		  else {
			  result = "";
			  Unirest.setTimeouts(0, 0);
			  HttpResponse<String> respon = Unirest.get(source)
				.header("Cookie", "SRVNAME=N01")
			    .asString();
			  
			  result+=respon.getBody();
			  finalresult+=DataTranslator.translateCopernicusMetadata(result.toString(), "onda-dias");
		  
		  }
		  
		return finalresult.toString();
			  
		
	}
	public static String dateCalculation(String date, String operation) {
		
		Instant inst = Instant.parse(date+"T00:00:00.00Z"); 

		if (operation.equals("minus")) {
			// subtract 12 Days to Instant 
			Instant value = inst.minus(Period.ofDays(12)); 
			// print result 
			System.out.println("Instant after subtracting Days: " + value); 
			return value.toString().substring(0,10);
		}
		if (operation.equals("plus")) {
			// add 12 Days to Instant 
			Instant value = inst.plus(Period.ofDays(12)); 
			// print result 
			System.out.println("Instant after adding Days: " + value); 
			return value.toString().substring(0,10);
		}
		return "null";
	}
	
	 public static String getLastDateOfMonth(String date){
		 
		 LocalDate convertedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		 convertedDate = convertedDate.withDayOfMonth(
		                                 convertedDate.getMonth().length(convertedDate.isLeapYear()));
	     return String.valueOf(convertedDate.getDayOfMonth());
	    }
}
