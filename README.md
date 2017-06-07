# Url-Shortener

This service is used for shortening urls and when provided with the returned code, redirecting to the initial url. 
Spring Boot framework was used for developing the service, MongoDB was used the persistence layer, but using another 
database JPA-compliant would prove to be quite easy. Guava and Apache-Commons are other dependencies utilized.

##### How it works

A MongoDB must be running either locally or remotely and the service checks for its existence during startup in order 
to cache previously entered values into memory. There are 2 ways to specify the mongodb instance

* `spring.data.mongodb.uri` parameter in the `application.properties` 
file should be changed to the address of the mongodb instance. A mongodb instance running in a docker container was 
used for the following application property. `spring.data.mongodb.uri=mongodb://192.168.99.100:32775/urlshortener`

* Another method for running the application is after running `mvn clean install` in the root folder of the project, the 
following command can be executed:

`java -Dspring.data.mongodb.uri=mongodb://192.168.99.100:32769/urlshortener -jar target/url-shortener-0.0.1-SNAPSHOT.jar`


##### `POST`

A Post request is used for posting the url into the service. The url is entered into body of the request as plain-text.
The service, when prompted with a post request, first validates the posted url, and if the validation is successful, 
generates a hash value and returns a response url that can be used to fetch back the original url.

**Example**
If the application is running locally, A Post request to `http://localhost:8080` with raw plain-text data such as 
`http://bbc.co.uk` returns text response `http://localhost:8080/url/458d4efa`. Postman was 
used during testing manually, curl should also be fine.

##### `GET`

The returned response `http://localhost:8080/url/458d4efa` in the previous example can be entered 
into a browser's address bar and entered, the request will be directed to `http://bbc.co.uk`.

### Architectural Choices

* Initially, developed a post endpoint that accepted any url after the service's url, without the need for a 
post body such as: `http://localhost:8080/http://bbc.co.uk`. The service also worked fine this way but 
problem was that while writing unit tests, the `DispatcherServlet` was deleting one of the slahes in the entered 
url. So `http://bbc.co.uk` was becoming `http:/bbc.co.uk`. Working around this issue would mean throwing away 
validation, so switched to using body instead.
* MD5 was used initially for hashing the url. UUID would also have worked fine but seeing the length of the returned 
hashes, switched to Murmur32 instead.


### Possible Improvements

* Instead of using embedded Cache (Guava), move the logic to outside of the container and utilize Memcache or variant.
* Use a load balancer in front like HaProxy for distributing the load between worker nodes when scaled.
* Use multi-node MongoDB instead of single node
* Containerize LB, Microservice and MongoDB layers and host them on cloud (AWS, Google Cloud or variant depending on 
their ease of use for Docker Swarm support)
* Overall, this project is a good candidate for serverless architecture as well as scaling dramatically.
