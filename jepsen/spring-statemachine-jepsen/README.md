# spring-statemachine-jepsen

A Clojure project implementing jepsen tests for a Spring Statemachine

## Usage

* Setup nodes n1, n2, n3, n4 and n5 having a latest debian installation.
* Install zookeeper for every node and create a cluster.
* Build `spring-statemachine-samples-web-1.0.0.BUILD-SNAPSHOT.jar` and copy it to directory `/root`.

Jepsen tests currently doesn't install `Zookeeper` or `spring-statemachine-samples-web` so this needs to be done manually.

Zookeeper config `zoo.cfg` for each node looks like:

```
tickTime=2000
initLimit=10
syncLimit=5
dataDir=/var/lib/zookeeper
clientPort=2181
server.1=0.0.0.0:2888:3888
server.2=n2:2888:3888
server.3=n3:2888:3888
server.4=n4:2888:3888
server.5=n5:2888:3888
```

I had to define `server.1` to use `0.0.0.0` and other node configs respectively.

Run all tests:

```
# lein test
```

Run particular test:

```
lein test :only spring-statemachine-jepsen.core-test/send-isolated-event
```

## Setting up eclipse

Easiest way to import clojure leiningen project into clipse is to first create a `pom.xml` and then create a project based on that pom.

```
# lein pom
# mvn eclipse:eclipse
```
