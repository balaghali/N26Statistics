# REST API to calculate statistics of transactions from the last 60 seconds

Build a REST API to calculate real time statistics from the last 60 seconds.
There will be two APIs, one of them is called every time a transaction is made. It is also the sole input of this RESTful API.
The other one returns the statistic based of the transactions of the last 60 seconds.
 
POST /transactions

Every Time a new transaction happened, this endpoint will be invoked.

JSON Body:
{
"amount": 12.3,
"timestamp": 1478192204000
}

Where:
* amount - transaction amount - in double
* timestamp - transaction time in epoch in millis in UTC time zone (this is not current timestamp) - in long

Returns: 
Empty body with either 201 or 204.
* 201 - in case of success
* 204 - if transaction is older than 60 seconds

GET /statistics

This is the endpoint that returns the statistic based on the transactions which happened in the last 60 seconds.

Returns:
JSON Body
{
"sum": 1000,
"avg": 100,
"max": 200,
"min": 50,
"count": 10
}

Where:
* sum is a double specifying the total sum of transaction value in the last 60 seconds
* avg is a double specifying the average amount of transaction value in the last 60 seconds
* max is a double specifying single highest transaction value in the last 60 seconds
* min is a double specifying single lowest transaction value in the last 60 seconds
* count is a long specifying the total number of transactions happened in the last 60 seconds

-----------------------------------------------------------------------------------------------------------------------------------------------------------------------

# Instructions to run

= Vert.x Maven Starter

== Prerequisites

* Apache Maven
* JDK 8+

== Getting started

Clone/Checkout this project onto fileSystem:

git clone https://github.com/tejaghali/N26Statistics.git

== Running the project

Once you have retrieved the project, you can check that everything works with:

[source]
----
mvn test exec:java
----

The command compiles the project and runs the tests, then  it launches the application, so you can check by yourself. Open your browser to http://localhost:8080/ You should see a _Welcome_to_statistics message.

Use Postman (Chrome extension / Desktop App),
	call endpoint 'localhost:8080/transactions' to POST and provide input JSON Body 'amount' and 'transaction' as mentioned in the body'.
Sample post body ,   
	{
		"amount": 11.18,
		"timestamp": {{current_timestamp}}
	} 
Define the following script under pre-requisite scripts to get the current timestamp
	var current_timestamp = new Date().getTime();
	postman.setEnvironmentVariable("current_timestamp", current_timestamp); 

Then call the endpoint 'localhost:8080/statistics' to GET statistics.

== Anatomy of the project

The project contains:

* a `pom.xml` file
* a _main_ verticle file (/VertxStatistics/src/main/java/com/stats/restverticle/RestVerticleApplicationStarter.java)
* and unit tests 
* a stand alone utility class provided to run from IDE (/VertxStatistics/src/main/java/com/stats/restservice/application/RestVerticleStandAlone.java)

== Building the project

To build the project, just use:

----
mvn clean package
----

It generates a _fat-jar_ in the `target` directory.

== Deploying the fat jar 
To run the fat jar use following command and access the localhost:8080/
java -jar jarName-0.1.snapshot-fat.jar 

swagger json
----
swagger.json file is included to document the rest api's in Swagger UI 
This swagger template is generated from open source tools 