Urban Airship Connect Client Library
====================================

This is the official supported Java library for Urban Airship Connect.


Installation
====================

Manual installation
-------------------

Clone the repository, and use

```
    mvn package
```

to build the jar. Add the jar, located at a path similar to:

```
    target/connect-client-<version>.jar
```

If you would like a copy of the javadocs, use

```
    mvn javadoc:javadoc
```

Maven Installation
------------------

Add the following to your pom.xml

```
<!-- Urban Airship Library Dependency-->
    <dependency>
        <groupId>com.urbanairship</groupId>
        <artifactId>connect-client</artifactId>
        <version>2.0.0</version>
    </dependency>
```

Usage
=====

The client library provides all the components you need to consume a mobile event stream.

_Note that Connect requests with this client may experience SSL handshake failures unless using the
**Java Cryptography Extension (JCE) Unlimited Strength** package cipher suite._

Example
-------

 An example of consuming from the client and then disconnecting may look something like:

```
        Configuration config = new MapConfiguration(ImmutableMap.of());
        AsyncHttpClient httpClient = StreamUtils.buildHttpClient(new ConnectClientConfiguration(config));
```
```
        Creds creds = Creds.newBuilder()
            .setAppKey("key")
            .setToken("token")
            .build();
```
```
        // can also set eagle creek filter, subset, or offset specifications
        StreamQueryDescriptor descriptor = StreamQueryDescriptor.newBuilder()
            .setCreds(creds)
            .build();
```
```
        FatalExceptionHandler fatalExceptionHandler = new FatalExceptionHandler() {
            @Override
            public void handle(Exception e) { log.fatal(e); }
        };
```
```
        Consumer<Event> consumer = new Consumer<Event>() {
            @Override
            public void accept(Event event) {
                log.info("Received event " + event.getIdentifier());
                storableEvent = doSomethingToPrepEventForDBStorage(event);
                someDBClient.put(storableEvent)
            }
        };
```
```
        MobileEventConsumerService service = MobileEventConsumerService.newBuilder()
            .setBaseStreamQueryDescriptor(descriptor)
            .setConfig(config)
            .setClient(httpClient)
            .setOffsetManager(new InFileOffsetManager(creds.getAppKey()))
            .setFatalExceptionHandler(fatalExceptionHandler)
            .setConsumer(consumer)
            .build();
```
```
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        Runnable stopConsuming = new Runnable() {
            @Override
            public void run() {
                service.triggerShutdown();
            }
        };
```
```
        scheduledExecutorService.schedule(stopConsuming, 60, TimeUnit.SECONDS);
        service.run();
```

StreamQueryDescriptor
---------------------

Begin by creating a StreamQueryDescriptor instance.  This will contain the app credentials, any request filters,
 a starting offset, and any subset options.

First, store the app credentials (app key and auth token) in a Creds object:

```
        Creds creds = Creds.newBuilder()
            .setAppKey("key")
            .setToken("token")
            .build();
```

Next, you will want to build any request filters or subset.  See the request documentation for the thorough description
 on filter and subset options and combinations.  A basic example might be:

```
         // individual device filters
         DeviceFilter device1 = new DeviceFilter(DeviceFilterType.ANDROID, "152d00c3-c49c-4172-88ce-539c511cf346");
         DeviceFilter device2 = new DeviceFilter(DeviceFilterType.IOS, "67fa2bad-9e83-4259-b925-bc08c184f72e");
         DeviceFilter device3 = new DeviceFilter(DeviceFilterType.NAMED_USER, "cool user");
```
```
         // notification filter by group ID
         NotificationFilter notification = NotificationFilter.createGroupIdFilter("58179035-dd1f-4b04-b023-5035c6335786");
```

```
         Filter filter = Filter.newBuilder()
             .setLatency(20000000)
             // internally adds devices to the same set until the Filter is built
             .addDevices(device1, device2, device3)
             .addDeviceTypes(DeviceFilterType.ANDROID, DeviceFilterType.IOS)
             .addNotification(notification)
             .addType(EventType.OPEN)
             .build();
```

```
         Subset subset = Subset.createSampleSubset(0.3);
```

Including a stream offset, you can then build your StreamQueryDescriptor:

```
        StreamQueryDescriptor descriptor = StreamQueryDescriptor.newBuilder()
              .setCreds(creds)
              .addFilter(filter)
              .setSubset(subset)
              .setOffset(250)
              .build
```

The offset can be a long ("250") or a specification for either the beginning or end of the available data window
("EARLIEST" or "LATEST").  If you don't include an offset here, the stream will default to the starting at LATEST.

OffsetManager
-------------

The library provides a class, OffsetManager, which tracks your stream offset as connections are made and lost.  The library
 currently provides two simple implementations: InMemOffsetManager stores the offset in memory and InFileOffsetManager
 writes and reads the offset from a file named after the relevant app key. The OffsetManager implementation is passed
 into the MobileEventConsumerService as it's built, and will be internally used for reconnecting (but not the original
 connection). The included offset managers will update the value after the stream's first disconnection.

```
        OffsetManager offsetManager = new InFileOffsetManager("app key");
```

FatalExceptionHandler
---------------------

Implementing FatalExceptionHandler will allow the controlling service to be aware of any connection errors as they occur
 on worker threads.  If the controller is implementing the client on the same thread, the implementation of handle() can
 just swallow or log the exceptions as errors.


ConnectClientConfiguration
--------------------------

Configurable fields and defaults:

- API URL: "https://connect.urbanairship.com/api/events"
- HTTP client connect timeout: 10s
- HTTP client read timeout: 5s
- Stream connect timeout: 5s
- Stream consume timeout: 30s
- Max connection retry attempts: 10
- Starting backoff time for connection retries: 1s


MobileEventConsumerService
--------------------------

The final piece needed for the client setup is the MobileEventConsumerService.

```
        MobileEventConsumerService service = return MobileEventConsumerService.newBuilder()
               .setClient(httpClient)
               .setBaseStreamQueryDescriptor(descriptor)
               .setConfig(config)
               .setConsumer(consumer)
               .setOffsetManager(offsetManager)
               .setFatalExceptionHandler(fatalExceptionHandler);
```

The descriptor, offsetManager, and fatalExceptionHandler were addressed above.  The httpClient is the AsyncHttpClient used for
executing and handling the HTTP requests.  If you don't have any particular preferences here, use the client builder method
 buildHttpClient() in StreamUtils.  The builder also accepts client configuration, allowing for tuning of connection /
 consumption timeout and reattempt backoff settings.  Lastly, you will pass in a stream consumer.  The MobileEventConsumerService
 will parse the events into response model objects that the injected consumer can then accept and interact with.

Calling run() in MobileEventConsumerService will run an AbstractExecutionThreadService inheriting service that will connect
 and consume from Connect.  If the server closes the connection, the service will automatically reconnect with an up-to-date
 offset and retry the connection attempts up to a configurable limit with exponential back off.  The service can be stopped
 by calling triggerShutdown().

