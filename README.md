# Round Robin Load Balancer - Assignment

This project implements a simple Round Robin Weighted Load Balancer in Spring Boot.  
It distributes traffic across multiple backend instances, adjusts weights dynamically based on latency, and performs periodic health checks.


## Prerequisites
- Java 21+
- Maven 3.9+

### 🚀 How to Run
 
# Please follow the below order to bring up the services 

# Step 1: Start the Load Balancer
from cmd line: 
mvn spring-boot:run

# Step 2: start the Backend services on three different ports

from cmd line:
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8043"
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8044"
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8045"

# Step 3 : start the client service to send request to load balancer

from cmd line:
mvn spring-boot:run
