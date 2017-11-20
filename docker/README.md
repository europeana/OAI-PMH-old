#Using the Dockerized OAI-PMH test environment

You can use docker to quickly setup a test environment. The docker environment consists a Tomcat server

We assume you have Docker already installed on your computer. You need docker-compose version 1.8.0 or later to 
be able to run it ([installation instructions here](https://github.com/docker/compose/releases)).

Remember to configure the /oaicat/src/main/webapp/WEB-INF/oaicat.properties 

##Starting docker
- Go to the docker folder and execute the command: `docker-compose up`
- After startup the Tomcat server is available at http://localhost:8082
- OAI server is available at http://localhost:8082/oaicat/

##Usage:
 - If you press <kbd>Ctrl</kbd>+<kbd>C</kbd> in the terminal then docker will stop, preserving your current containers. You can restart by
   executing docker-compose up again. If you want to do a clean start you can throw your old containers away first with
   this command: `docker rm docker_oai-pmh-server_1
 - For debugging use Java/Tomcat port = 8002


##Favorite Docker commands:

**Start all API containers**: docker-compose up

**View all running containers**:
docker ps

**Restart Tomcat API application**:
docker restart oai-pmh-tomcat-server

**Start all API containers in detached mode**:
docker-compose up -d

**Build all images**:
docker-compose build

**Shutdown and remove containers**:
docker-compose down


