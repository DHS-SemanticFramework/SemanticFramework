package com.application.SemanticFramework;

import org.apache.jena.rdf.model.Model;

import virtuoso.jena.driver.VirtModel;
import virtuoso.jena.driver.VirtuosoUpdateFactory;
import virtuoso.jena.driver.VirtuosoUpdateRequest;

public class CleaningKB {

	public static void delete(String uuid, String kb_address, String repository) {

	String query = "prefix event:<http://purl.org/NET/c4dm/event.owl#>\r\n" + 
			"prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\r\n" + 
			"prefix time:<http://www.w3.org/2006/time#>\r\n" + 
			"prefix xsd:<http://www.w3.org/2001/XMLSchema#>\r\n" + 
			"prefix xsd:<http://www.w3.org/2001/XMLSchema#>\r\n" + 
			"\r\n" + 
			"delete \r\n" + 
			"{ GRAPH <"+ MyResource.repository + "> {\r\n" + 
			"    ?event ?p ?o.\r\n" + 
			"    ?event event:hasSubEvent ?e_event.\r\n" + 
			" 	?e_event ?e_p ?e_o.\r\n" + 
			"	?e_event event:product ?product.\r\n" + 
			" 	?product ?p_p ?p_o.\r\n" + 
			"    }\r\n" + 
			"}\r\n" + 
			"where {\r\n" + 
			"    ?event rdf:type event:Event.\r\n" + 
			"    ?event ?p ?o.\r\n" + 
			"    ?event event:hasSubEvent ?e_event.\r\n" + 
			" 	?e_event ?e_p ?e_o.\r\n" + 
			"	?e_event event:product ?product.\r\n" + 
			" 	?product rdf:type event:Product.\r\n" + 
			" 	?product ?p_p ?p_o.\r\n" + 
			"  	FILTER (?event=<http://purl.org/NET/c4dm/event.owl#Event_"+uuid+">)\r\n" + 
			"    \r\n" + 
			"}";
		
		  Model m = VirtModel.openDatabaseModel(repository, kb_address, "dba", "mysecret"); 
		  VirtuosoUpdateRequest vur = VirtuosoUpdateFactory.create(query, m);
		  vur.exec();  
	}
	
}
