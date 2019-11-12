Airship Real Time Data Stream Client Library
====================================

This is the official supported Java library for Airship Real Time Data Stream (formerly known as Connect).


Questions
=========

The best place to ask questions or report a problem is our support site:
http://support.airship.com/


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
        <version>VERSION</version>
        <!-- Replace VERSION with the version you want to use -->
    </dependency>
```

Usage
=====

The client library provides all the components you need to consume a RTDS direct stream.

_Note that RTDS requests with this client may experience SSL handshake failures unless using the
**Java Cryptography Extension (JCE) Unlimited Strength** package cipher suite._

If you encounter a generic connection failure `java.lang.RuntimeException`, the max strength encryption policy might be the culprit, and you should ensure this JCE Unlimited Strength package is installed on your system.

You can find the JCE Unlimited Strength package at the following locations.  Choose the one that corresponds to your JRE version.

- [JCE Unlimited Strength Jurisdiction Policy Files 7](http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html)
- [JCE Unlimited Strength Jurisdiction Policy Files 8](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html)

_These files are not required for JRE 9 or for JRE 8u151 or newer._

Example
-------

 An example of consuming from the client and then disconnecting may look something like:
```
        Creds creds = Creds.newBuilder()
            .setAppKey("key")
            .setToken("token")
            .build();
```
```
        // can also set filter, subset, or offset specifications
        StreamQueryDescriptor descriptor = StreamQueryDescriptor.newBuilder()
            .setCreds(creds)
            .build();
```
```
        final Stream stream = new Stream(descriptor, Optional.<StartPosition>absent());
```
```
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        Runnable stopConsuming = new Runnable() {
            @Override
            public void run() {
                try {
                    stream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    scheduledExecutorService.shutdown();
                }
            }
        };
```
```
        scheduledExecutorService.schedule(stopConsuming, 60, TimeUnit.SECONDS);

        while(stream.hasNext()){
            String event = stream.next();
            System.out.println("Event: " + event);
        }
```

StreamQueryDescriptor
---------------------

Begin by creating a StreamQueryDescriptor instance.  This will contain the app credentials, any request filters,
 a starting offset, offset update preference, endpoint URL, and any subset options.

If offset updates are enabled, then regardless of other filters provided the stream may contain events with type
OFFSET_UPDATE. These don't correspond to any activity in Airship's systems and requests for the same stream
position will not return the same OFFSET_UPDATE events. The offsets on them will be the same as some other event
in the stream. They serve to allow clients to update stored offsets in the case of low traffic or filters removing
large portions of the stream.

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
        DeviceFilter device1 = new DeviceFilter(DeviceFilterType.ANDROID_CHANNEL, "152d00c3-c49c-4172-88ce-539c511cf346");
        DeviceFilter device2 = new DeviceFilter(DeviceFilterType.IOS_CHANNEL, "67fa2bad-9e83-4259-b925-bc08c184f72e");
        DeviceFilter device3 = new DeviceFilter(DeviceFilterType.NAMED_USER_ID, "cool_user");
```
```
         // notification filter by group ID
        NotificationFilter notification = new NotificationFilter(NotificationFilter.Type.GROUP_ID, "58179035-dd1f-4b04-b023-5035c6335786");
```

```
         Filter filter = Filter.newBuilder()
             .setLatency(20000000)
             .addDevices(device1, device2, device3)
             .addDeviceTypes(DeviceType.ANDROID, DeviceType.IOS)
             .addNotifications(notification)
             .addEventTypes("OPEN")
             .build();
```

```
         Subset subset = Subset.createSampleSubset(0.3);
```

```
        StreamQueryDescriptor descriptor = StreamQueryDescriptor.newBuilder()
            .setCreds(creds)
            .addFilters(filter)
            .setSubset(subset)
            .enableOffsetUpdates()
            .build();
```

