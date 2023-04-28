# Install Avighna-Docker in your host machine

- Require docker for this artefact. To install docker in ubuntu follow the below link
  https://phoenixnap.com/kb/install-docker-on-ubuntu-20-04
# Build & Run Avighna Docker image
- Unzip the avighna-docker-resource.zip

```shell script
unzip avighna-docker-resource.zip
```

- Change directory to avighna-docker-resource

```shell script
cd avighna-docker-resource
```

- Build Avighna docker image

```shell script
docker build . -t avighna-image
```

- Run the Avighna docker container in the interactive mode to use it as a ubuntu terminal.

```shell script
docker run --name avighna-container --interactive --tty -p 9001:8080 -p 8081:8081 -p 9000:9000 -p 9411:9411 avighna-image
```

**Note:**
- In the above command, we are exposing the Docker port 8080 and assign it to host port 9001. If there is a server running in docker with port 8080, to access that server in the host machine use the link http://localhost:9001/. Similarly, we mapped docker's 8081 port to host's 8081 port.

- Once you run the above command, you enter into the ubuntu terminal with all the resources required to run Avighna on the evaluation projects. The present working directory is **/root/avighna/**

# Run Avighna with the evaluation projects
**Note:**
- To know the options available for Avighna use the below command.

```shell script
java -jar avighna-cmd-interface-1.0.0.jar

#Below is the output of the above command

    ___        _       __
   /   |_   __(_)___ _/ /_  ____  ____ _
  / /| | | / / / __ `/ __ \/ __ \/ __ `/
 / ___ | |/ / / /_/ / / / / / / / /_/ /
/_/  |_|___/_/\__, /_/ /_/_/ /_/\__,_/
             /____/     Cmd-Interface



usage: avighna-cmd-interface
 -aa,--app-arg <arg>              Program arguments for the given
                                  application. If multiple arguments then
                                  separate it by :
 -aaj,--avighna-agent-jar <arg>   Path of the avighna agent jar file
 -aj,--app-jar <arg>              Path of the application jar file to
                                  generate dynamic information
 -dar,--dacapo-app-arg <arg>      Arguments to the DACAPO application. The
                                  value format is <dacapo
                                  application>:<data size>. These provided
                                  arguments will be appended after the
                                  given application jar file to the final
                                  command
 -dtf,--dont-track-fake-edge      Dont track fake edges while generating
                                  dynamic traces
 -lrf,--list-request-file <arg>   Path of the YAML file that contains the
                                  list of requests. Request must be full
                                  curl command
 -od,--out-root-dir <arg>         Root directory to store the output
 -rap,--root-app-packages <arg>   Root application package(s) for
                                  generating the dynamic cg. Multiple
                                  packages are seperated by :
 -sdf,--save-dot-files            Save the generated dynamic trace as dot
                                  files
 -sif,--save-img-files            Save the generated dynamic trace as
                                  image files
 -sra,--single-run-app            This argument indicates that the given
                                  app is a single run and not the web
                                  application which runs infinitely until
                                  explicitly terminated.
```

- For the below commands to run Avighna on different evaluation projects, we have used the options -sif to save the DOT graph as image file to visualize. This option takes more time depending on the size of the DOT graph. If you do not wish to visualize the DOT graph and want to run the command quickly, then remove -sif option.

# Running Avighna on Spring applications:
## fredbet application
- **Link:** https://github.com/fred4jupiter/fredbet
- **Tag:** tag/release_2.0.0
- **Root Application Package:** de.fred4jupiter.fredbet
- **Present Working Directory:** /root/avighna/ (The below commands uses relative path, make sure you are in the correct present working directory)
- Run Avighna on fredbet.

```shell script
java -jar avighna-cmd-interface-1.0.0.jar -aaj avighna-agent-1.0.0.jar -aj Spring-Projects/fredbet/target/fredbet.jar -od avighna-output/Spring-Projects/fredbet/avighna-agent-output/ -rap de.fred4jupiter.fredbet -sdf -sif
```

- Once the application is up and running, Open the browser in the host machine and open the link http://localhost:9001/
- Once you are done with using the fredbet application, go to docker terminal and press 'y' and enter. Avighna will create the DST file. DST file contains dynamic traces, which will be used to merge with the static callgraph.

## start.spring.io application
- **Link:** https://github.com/spring-io/start.spring.io
- **Branch:** main
- **Commit Hash:** 6c0944ead1bfef3444a9e1a422b3f134de2b7f48
- **Module:** start-site. There are many modules in start.spring.io project. For this evaluation, we only run on start-site module.
- **Root Application Package:** io.spring.start.site
- **Present Working Directory:** /root/avighna/ (The below commands uses relative path, make sure you are in the correct present working directory)
- Run Avighna on start.spring.io (start-site).

```shell script
java -jar avighna-cmd-interface-1.0.0.jar -aaj avighna-agent-1.0.0.jar -aj Spring-Projects/start.spring.io/start-site/target/start-site-exec.jar -od avighna-output/Spring-Projects/start.spring.io/start-site/avighna-agent-output/ -rap io.spring.start.site -sdf -sif
```

- Once the application is up and running, Open the browser in the host machine and open the link http://localhost:9001/
- Once you are done with using the start.spring.io application, go to docker terminal and press 'y' and enter. Avighna will create the DST file. DST file contains dynamic traces, which will be used to merge with the static callgraph.

## Zipkin application
- **Link:** https://github.com/openzipkin/zipkin
- **Branch:** master
- **Commit Hash:** 8a4f4b9c9a5a3204d9663ecef39d687785369c9a
- **Module:** zipkin-server. There are many modules in zipkin project. For this evaluation, we only run on zipkin-server module.
- **Root Application Package:** zipkin.server and zipkin2.server
- **Present Working Directory:** /root/avighna/ (The below commands uses relative path, make sure you are in the correct present working directory)
- In order to test zipkin, we need a server and for that we used example project from zipkin---https://github.com/openzipkin/pyramid_zipkin-example.
  First, we need to run both the frontend and backend server from this project.
- Running the front end server

```shell script
python3 Spring-Projects/zipkin/pyramid_zipkin-example/frontend.py &

# The above command runs in background. When you run this command
# It print a process id for example [1] 289
# If you want to stop the server, then run the below command
kill -9 289
```

- Running the back end server

```shell script
python3 Spring-Projects/zipkin/pyramid_zipkin-example/backend.py &

# The above command runs in background. When you run this command
# It print a process id for example [2] 290
# If you want to stop the server, then run the below command
kill -9 290
```
- Run Avighna on zipkin (zipkin-server).

```shell script
java -jar avighna-cmd-interface-1.0.0.jar -aaj avighna-agent-1.0.0.jar -aj Spring-Projects/zipkin/zipkin-server/target/zipkin-server-2.23.17-SNAPSHOT-exec.jar -od avighna-output/Spring-Projects/zipkin/zipkin-server/avighna-agent-output/ -rap zipkin.server:zipkin2.server -sdf -sif
```

- Once the application is up and running, Open the browser in the host machine and open the below links.
- For frontend: http://localhost:8081/
- For backend: http://localhost:9000/api
- For Zipkin main application: http://localhost:9411/
- Once you are done with using the zipkin application, go to docker terminal and press 'y' and enter. Avighna will create the DST file. DST file contains dynamic traces, which will be used to merge with the static callgraph.

## spring-petclinic application
- **Link:** https://github.com/spring-projects/spring-petclinic
- **Branch:** main
- **Commit Hash:** 0c1fa8e8e2744125cc4bee725fe2de6dd76d3a4f
- **Root Application Package:** org.springframework.samples.petclinic
- **Present Working Directory:** /root/avighna/ (The below commands uses relative path, make sure you are in the correct present working directory)
- Run Avighna on spring-petclinic.

```shell script
java -jar avighna-cmd-interface-1.0.0.jar -aaj avighna-agent-1.0.0.jar -aj Spring-Projects/spring-petclinic/target/spring-petclinic-2.7.0-SNAPSHOT.jar -od avighna-output/Spring-Projects/spring-petclinic/avighna-agent-output/ -rap org.springframework.samples.petclinic -sdf -sif
```

- Once the application is up and running, Open the browser in the host machine and open the link http://localhost:9001/
- Once you are done with using the spring-petclinic application, go to docker terminal and press 'y' and enter. Avighna will create the DST file. DST file contains dynamic traces, which will be used to merge with the static callgraph.

# Running Avighna on Guice applications:
##  guice application from CGBench
- **Link:** https://github.com/linghuiluo/CGBench/tree/master/guice
- **Branch:** master
- **Commit Hash:** d3e2587c3a8a2d5c5895851c93eb6699c8b052bc
- **Root Application Package:** com.baeldung.examples
- **Present Working Directory:** /root/avighna/ (The below commands uses relative path, make sure you are in the correct present working directory)
- Run Avighna on guice project.

```shell script
java -Xbootclasspath/p:avighna-agent-1.0.0.jar -javaagent:avighna-agent-1.0.0.jar=Guice-Projects/guice/dynamic_agent.yml -noverify -jar Guice-Projects/guice/target/guice-1.0-SNAPSHOT-jar-with-dependencies.jar
```

**Note:** The above command does not use avighna-cmd-interface, bacause this project needs interactive terminal (We will add this in avighna-cmd-interface in future). Therefore, we run avighna directly on this project with the help of avighna-agent.

- Once the application is up and running, in the terminal start typing some message and click enter. You can send any number of messages.
- Once you are done using the guice application, type 'q' to exit.

### To copy the output of the above Avighna commands

- Get the avighna container id

```shell script
docker ps

# Sample output is shown below. For the below container id is 3f57f1031cd3
CONTAINER ID   IMAGE           COMMAND   CREATED         STATUS         PORTS                                                                                                                                                                        NAMES
3f57f1031cd3   avighna-image   "bash"    9 minutes ago   Up 9 minutes   0.0.0.0:8081->8081/tcp, :::8081->8081/tcp, 0.0.0.0:9000->9000/tcp, :::9000->9000/tcp, 0.0.0.0:9411->9411/tcp, :::9411->9411/tcp, 0.0.0.0:9001->8080/tcp, :::9001->8080/tcp   avighna-container
```

- Copy the output from the container to the host machine to view the results.

```shell script
docker cp 3f57f1031cd3:/root/avighna/avighna-output .

# The above is the sample command. The below is the format
docker cp container-id:/root/avighna/avighna-output <path of the host machine where you want to copy the result>

# /root/avighna/avighna-output is the directory where we store the avighna output because we used this directory as output directory in the avighna command while running.
```