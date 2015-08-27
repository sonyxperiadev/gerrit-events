Gerrit Events
=============
This is a Java library used primarily to listen to [stream-events](https://gerrit-documentation.storage.googleapis.com/Documentation/2.8.1/cmd-stream-events.html) from [Gerrit Code Review](https://code.google.com/p/gerrit/) and to send reviews via the SSH CLI or the REST API.
It was originally a module in the [Jenkins Gerrit Trigger plugin](https://github.com/jenkinsci/gerrit-trigger-plugin) and is now broken out to be used in other tools without the dependency to Jenkins.

Most of the development will still target the Jenkins plugin, but now it can also be used elsewhere.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.sonymobile.tools.gerrit/gerrit-events/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.sonymobile.tools.gerrit/gerrit-events)

## Maintainers

* Robert Sandell
  - robert.sandell@cloudbees.com
  - sandell.robert@gmail.com

# Usage
A "real life" example of usage can be found in the [Jenkins Plugin](https://github.com/jenkinsci/gerrit-trigger-plugin/blob/master/gerrithudsontrigger/src/main/java/com/sonyericsson/hudson/plugins/gerrit/trigger/GerritServer.java#L408).

Start by creating a connection, attach some utility listeners and a GerritHandler.

```java
connection = new GerritConnection(name, config);
GerritHandler handler = new GerritHandler();
handler.setIgnoreEMail(name, config.getGerritEMail());
connection.setHandler(handler);
connection.addListener(gerritConnectionListener);
connection.start();
```

The handler handles the event routing and a thread pool to try to keep up with the potentially
huge amount of events coming from Gerrit. One handler can route from several connections.

Then create your event listener. The *GerritEventListener* interface requires you to implement one method
called *gerritEvent* that takes a parameter of type *GerritEvent* which is the base class for all events.
You can implement any number of *gerritEvent* methods that takes more specific event classes as its one parameter.
The handler will use reflection to try to find the most suitable method to call and will fall back to the generic one if it can't find any.

```java
public class MyEventListener implements GerritEventListener {
    @Override
    public void gerritEvent(GerritEvent event) {
        //Do something...
    }

    public void gerritEvent(PatchsetCreated event) {
        //Do something when someone uploads a patchset...
    }
}
```

```java
handler.addListener(new MyEventListener());
```

All event types can be found in the [com.sonymobile.tools.gerrit.gerritevents.dto.events](https://github.com/sonyxperiadev/gerrit-events/tree/master/src/main/java/com/sonymobile/tools/gerrit/gerritevents/dto/events) package.


# Environments
* `linux`
    * `java-1.6`
        * `maven-3.0.4`

The maintainers' development, tests and production environments are
Ubuntu 12.04 so we have no means of detecting or fixing any Windows issues,
but there are no specific reasons why it shouldn't run on Windows
and some kind contributors provide win fixes every now and then.

# Build
You build using maven

    mvn clean package

Run findbugs for future reference
or to make sure you haven't introduced any new warnings

    mvn findbugs:findbugs


# License

    The MIT License

    Copyright 2010 Sony Ericsson Mobile Communications. All rights reserved.
    Copyright 2012 Sony Mobile Communications AB. All rights reserved.

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
