# mancala-app by Dima Vilda

Project was written on java 8, contract was defined in src/main/java/com/vilda/mancala/mancalaapp/openapi/mancala-client.yaml 
using openapi tool, to see contract UI, just compile the project and go to tagret/contractUI/index.html and run this page in your browser.

To support database versioning, liquidbase was added. 
Project is covered by unit and mockMvc tests. The persistence layer was introduced by h2 in-memory db. 
To see changeset records and mancala game info, go to http://localhost:8080/h2 and write mancalaVilda in the Password: section. 
All app properties you can find in src/main/resources/application.yml file.



## Main commands to run the app
in console mvn clean, install -> run MancalaAppApplication.class or write mvn spring-boot:run in console
or
java -jar path/to/mancala-app-0.0.1-SNAPSHOT.jar

## Additional help in this application support by:
***Mail:*** dimavilda@gmail.com