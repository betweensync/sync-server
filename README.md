sync-server
===========

## Setting up this project ##
*- You should prepare Eclipse for building this project..*

**1. Clone a git repository using https://github.com/betweensync/sync-server.git**

Check your Eclipse git repository plug-in and import the project to working directory.

**2. Add a org.ektorp-1.4.2.jar to your local maven repository**

```
	// Please run below script in the project home directory
  	mvn install:install-file -Dfile=org.ektorp-1.4.2.jar  -DgroupId=org.ektorp -DartifactId=org.ektorp -Dversion=1.4.2  -Dpackaging=jar  -DgeneratePom=true
```

**3. Running Tomcat Server using project**

Open terminal console and move to working directory to run Tomcat 

```
	$PROJECT_HOME> mvn tomcat:run -Dlogging.directory=/tmp -Dmode=production
```

After tomcat start up successfully, you can browse http://localhost:8080/sync-server. 
Netty Web Socket server binds port 7700

**4. Runtime environment file location in project**

- Local property: sync-server\src\main\filters\local.properties
- Production Property: sync-server\src\main\filters\production.properties
- Log4j Configuration: sync-server\src\main\resources\log4j.xml
- Spring Configuration: sync-server\src\main\resources\spring

**5. Test Scenario**

1. Start tomcat server and confirm port 8080(Tomcat)/7700(WebSocket)
2. Check Cloudant Event Listener - https://jerryj3.cloudant.com
3. Run ChangesFeeder in sync-server\src\test\java\com\athena\dolly\cloudant\test and watch your Tomcat log console.

**6. TO-DO List(Eggboy)**

- WebSocketServerHandler is connected to S3Service for sending file to Amazon S3

```
	/**
     * 창재선생님, 여기 부분에 호출부분 넣어주세요.
     * @param userId User ID
     * @param path Directory path
     * @param localFile Temporary file which is in local tmp directory
     */
    protected void synchronizeStorage(String userId, String path, String localFile) {
    	S3Service s3Service = AppContext.getBean("s3Service", S3Service.class);
    	logger.debug(s3Service.listBuckets().toString());
    }
```

- Fill codes that should send message to client in WebSocketServerHandler

```
    public void sendMessageToClient(String msg) {
    	logger.debug("Send message [%s] to client", msg);
    	ChannelFuture future = channel.writeAndFlush(msg);
    }
```
