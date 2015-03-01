# Shovel

A simple Clojure library for processing Kafka streams using core.async. It has a simple (high level) consumer
and a simple producer. Works with the Kafka 0.8.1 or newer.

## Releases and Dependency Information

Latest and greatest stable release is 0.9.1:

* [0.9.1](https://github.com/l1x/shovel/tree/0.9.1)

Leiningen dependency information:

```clojure
[shovel "0.9.1"]
```
## Internals

### Talking to Kafka

#### Producing

#### Consuming

First I thought there can a lazy sequence created and returned, that would be a nice way of dealing with Kafka streams in Clojure. For functions with side effects the idiomatic way is to use repeatedly.

```Clojure
(defn lazy-messages
  [^ConsumerIterator iterator]
  (repeatedly #(.next iterator)))
```

[https://clojuredocs.org/clojure.core/repeatedly](https://clojuredocs.org/clojure.core/repeatedly)

Unfortunately the code hold onto the head and the heap exploded. I went for the recommended way of using the Kafka library.



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
lein uberjar && java -jar target/shovel-0.9.1-standalone.jar producer-test -f conf/app.edn
```

Consumer:

```
lein uberjar && java -jar target/shovel-0.9.1-standalone.jar consumer-test -f conf/app.edn
```
## CLOC

```
      29 text files.
      28 unique files.
      19 files ignored.

http://cloc.sourceforge.net v 1.62  T=2.87 s (3.8 files/s, 398.7 lines/s)
-------------------------------------------------------------------------------
Language                     files          blank        comment           code
-------------------------------------------------------------------------------
Clojure                          6             70             98            408
Maven                            1              1              4            347
HTML                             1             38              0            149
Bourne Shell                     3              2              0             28
-------------------------------------------------------------------------------
SUM:                            11            111            102            932
-------------------------------------------------------------------------------
```

## License

Copyright © 2015 Istvan Szukacs 

Distributed under the Apache License, Version 2.0 see LICENSE file.
