# shovel

A simple Clojure library for processing Kafka streams using core.async

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
lein uberjar && java -jar target/shovel-0.0.1-standalone.jar producer-test -f conf/app.edn
```

Consumer:

```
lein uberjar && java -jar target/shovel-0.0.1-standalone.jar consumer-test -f conf/app.edn
```
## CLOC

```
      17 text files.
      17 unique files.
     167 files ignored.

http://cloc.sourceforge.net v 1.60  T=4.02 s (2.0 files/s, 173.1 lines/s)
-------------------------------------------------------------------------------
Language                     files          blank        comment           code
-------------------------------------------------------------------------------
Maven                            1              1              4            291
Clojure                          7             61            102            236
-------------------------------------------------------------------------------
SUM:                             8             62            106            527
-------------------------------------------------------------------------------
```

## License

Copyright © 2014 Istvan Szukacs 

Distributed under the Apache License, Version 2.0 see LICENSE file.
