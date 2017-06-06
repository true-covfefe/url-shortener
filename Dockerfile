FROM openjdk
VOLUME /tmp
ADD target/url-shortener-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
RUN bash -c 'touch /app.jar'
ENTRYPOINT ["java","-Dspring.data.mongodb.uri=mongodb://mongodb/urlshortener", "-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]