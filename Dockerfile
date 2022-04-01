FROM java:8

# Install maven
RUN apt-get -y update && apt-get install -y maven

WORKDIR /code

# Prepare by downloading dependencies
ADD pom.xml /code/pom.xml
ADD server.xml /code/server.xml

# Prepare by downloading dependencies
ADD /libs/virt_jena3.jar /code/virt_jena3.jar
ADD /libs/virtjdbc4.jar /code/virtjdbc4.jar

ENV KB_ADDRESS default_env_value

#ADD configuration.json /code/config/configuration.json
ADD cities_countries_clear.json /code/cities_countries_clear.json

# Adding source, compile and package into a fat jar
ADD src /code/src
RUN ["mvn", "package"]

EXPOSE 8087
CMD ["mvn", "tomcat7:run"]
