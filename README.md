# Shovel

A simple Clojure library for processing Kafka streams using core.async. It has a simple (high level) consumer
and a simple producer. Works with the Kafka 0.8.1 or newer.

## Current version

* [0.0.2](https://github.com/l1x/shovel/tree/0.0.2)

## Usage

Tested only with Kafka 0.8.1.1

[0.8.1.1 Release Notes](https://archive.apache.org/dist/kafka/0.8.1.1/RELEASE_NOTES.html)

### Download Kafka and set up the dev environment

#### Start Zookeeper

```bash
./bin/zookeeper-server-start.sh config/zookeeper.properties
```

Props:

```
dataDir=/tmp/zookeeper
clientPort=2181
maxClientCnxns=0
```

#### Start Kafka 

```bash
./bin/kafka-server-start.sh config/server.properties
```

Props:
```
broker.id=0
port=9092
num.network.threads=2 
num.io.threads=8
socket.send.buffer.bytes=1048576
socket.receive.buffer.bytes=1048576
socket.request.max.bytes=104857600
log.dirs=/tmp/kafka-logsnum.partitions=2
log.retention.hours=168
log.segment.bytes=536870912
log.retention.check.interval.ms=60000
log.cleaner.enable=false
zookeeper.connect=localhost:2181
zookeeper.connection.timeout.ms=1000000
```

#### Running the app

Producer:

```
lein uberjar && java -jar target/shovel-0.0.2-standalone.jar producer-test -f conf/app.edn
```

Consumer:

```
lein uberjar && java -jar target/shovel-0.0.2-standalone.jar consumer-test -f conf/app.edn
```
## CLOC

```
      19 text files.
      19 unique files.
     234 files ignored.

http://cloc.sourceforge.net v 1.60  T=3.63 s (2.2 files/s, 218.3 lines/s)
-------------------------------------------------------------------------------
Language                     files          blank        comment           code
-------------------------------------------------------------------------------
Maven                            1              1              4            320
Clojure                          6             47             86            214
HTML                             1             27              0             93
-------------------------------------------------------------------------------
SUM:                             8             75             90            627
-------------------------------------------------------------------------------
```

## License

Copyright © 2014 Istvan Szukacs 

Distributed under the Apache License, Version 2.0 see LICENSE file.
