 Shovel

A simple Clojure library for processing Kafka streams using core.async. It has a simple (high level) consumer
and a simple producer. Works with the Kafka 0.8.1 or newer.

## Releases and Dependency Information

Latest and greatest stable release is 0.1.3:

* [0.1.3](https://github.com/l1x/shovel/tree/0.1.3)

Leiningen dependency information:

```clojure
[shovel "0.1.3"]
```


## Usage

Tested only with Kafka 0.8.2.0

[0.8.2.0 Release Notes](https://archive.apache.org/dist/kafka/0.8.2.0/RELEASE_NOTES.html)

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
lein uberjar && java -jar target/shovel-0.1.1-standalone.jar producer-test -f conf/app.edn
```

Consumer:

```
lein uberjar && java -jar target/shovel-0.1.1-standalone.jar consumer-test -f conf/app.edn
```
## CLOC

```
      23 text files.
      22 unique files.
      15 files ignored.

http://cloc.sourceforge.net v 1.62  T=3.25 s (2.8 files/s, 300.5 lines/s)
-------------------------------------------------------------------------------
Language                     files          blank        comment           code
-------------------------------------------------------------------------------
Clojure                          7             64             98            330
Maven                            1              1              4            293
HTML                             1             38              0            150
-------------------------------------------------------------------------------
SUM:                             9            103            102            773
-------------------------------------------------------------------------------
```

## License

Copyright © 2015 Istvan Szukacs 

Distributed under the Apache License, Version 2.0 see LICENSE file.
