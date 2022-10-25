# SemanticFramework
The Semantic Framework is a component of the Copernicus Sentinels Collaborative Data Hub Software (DHS) that aims in the detection of earthquake events and Sentinel-1 products from various data providers corresponding to the time before and after the event. The criteria of each search are defined by the user.

## Required technologies
The installation of the SF is achieved using the docker-compose file. The following technologies should be installed in order to successfully run the framework:
-	Docker version (>=2.3.0.3)
-	Docker engine (>=19.03.08)
-	docker-compose (>=1.25.5)

The docker image size is approximately 943 MB for the semantic framework and 154 MB for the KB.
## Framework installation
-	Download “docker-compose.yml” by right-clicking on the page and selecting “Save as…”>“docker-compose.yml”
-	Download “configuration.json” by right-clicking on the page and selecting “Save as…”>“configuration.json”
-	Download “keycloak_configuration.json” by right-clicking on the page and selecting “Save as…” “keycloak_configuration.json”
-	Create a folder named “config” on the same path that “docker-compose.yml” is found.
-	Add “configuration.json” and “keycloak_configuration.json” inside the “config” folder
-	Username and password should be added in case needed (i.e. for scihub) on the configuration file (lines 14-15). Otherwise this data source should be deleted from configuration file (lines 11-18).

After that, navigate to the directory where “docker-compose.yml” has been stored and execute the following command to start SF:

> docker-compose up 

## Testing the installation and first population of the KB
Calling the following URL will pre-populate the KB with location-related information and it will indicate a successful installation of SF:

` http://<IP>:<PORT>/SemanticFramework/api/population `
  
This procedure should run only once, during the installation of the framework. Any user that corresponds to “Semantic Framework Manager” user role can execute this action. The request should be executed using POST type and Content-Type should be set to “application/json”. The request body in this case, can be empty and the expected output is:
```
{ 
  "status": "Successfully added to KB."  
} 
```
  The default <PORT> is 8087 and localhost can be used in the <IP> field for testing the framework locally. The KB is exposed in ports 8890 and 1111. In case needed, all ports can be changed from “docker-compose.yml”. 

## Using the framework
Τhe users with “Search” user role are able to access the following URL in order to execute their queries:
  
` http://<IP>:<PORT>/SemanticFramework/api/retrieve `

The user will be able to execute queries in natural language using the following pattern:
> “eventType located in city, country in year month day with magnitude greater than magnitude”

  It is worth mentioning that month, day and magnitude are optional fields. All keywords are expected in English. Magnitude is expected as a double value with one decimal place and a point (not a comma) as a decimal separator. The earthquake events having magnitude that equals the value requested by the user are also included in the results. Date is expected in yyyy mm dd format. 
  As already mentioned, the request should be executed using POST type and Content-Type should be defined to “application/json”. 
  The request body should be a JSON having the following structure:

```
{
  "text": "earthquake located in Zagreb, Croatia in 2020 with magnitude greater than 5.0" ,
  "page": "1"
} 
```
