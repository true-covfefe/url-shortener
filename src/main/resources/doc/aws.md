
Created a Linux AMI EC2 instance, logged in via ssh, pulled the project from github, composed via docker-compose and then scaled the url-shortener service 
to 3 nodes. Below is the ```docker-compose ps``` output from the AMI instance. Naturally, this is all very tedious and time-consuming, hence there are tools 
like Swarm, Kubernetes etc..

```
[ec2-user@ip-172-31-21-197 url-shortener]$ docker-compose ps
            Name                          Command               State                   Ports                 
-------------------------------------------------------------------------------------------------------------
urlshortener_lb_1              /sbin/tini -- dockercloud- ...   Up      1936/tcp, 443/tcp, 0.0.0.0:80->80/tcp 
urlshortener_mongodb_1         docker-entrypoint.sh mongod      Up      27017/tcp                             
urlshortener_url-shortener_1   java -Dspring.data.mongodb ...   Up      8080/tcp                              
urlshortener_url-shortener_2   java -Dspring.data.mongodb ...   Up      8080/tcp                              
urlshortener_url-shortener_3   java -Dspring.data.mongodb ...   Up      8080/tcp                              
```

To see it in action, please post a url in body to `http://54.148.141.41`