# Shovel

A simple Clojure library for processing Kafka streams using core.async. It has a simple (high level) consumer
and a simple producer. Works with the Kafka 0.8.1 or newer.

## Releases and Dependency Information

There are two versions:

* [0.2.x](https://github.com/l1x/shovel/tree/0.2.2) for the old API
* [0.3.x](https://github.com/l1x/shovel/tree/0.3.1) for the new API (kafka-clients)

Leiningen dependency information:

```clojure
[shovel "0.2.2"]
[shovel "0.3.1"]
```

## Internals

### Talking to Kafka

#### Producing

This is the first attempt to have something remotely safe and simple, the reality is that you cannot catch those exceptions this way which are generated during send. I need to investigate how to reliably deliver messages with Kafka and return error on unsuccessful send.

```Clojure
(defn produce
  [^Producer producer ^KeyedMessage message]
  (log/debug "fn: produce params: " producer message)
  (try
    {:ok (.send producer message)}
  (catch Exception e
    { :error "Exception" :fn "produce" :exception (.getMessage e) :e e})))
```

#### Consuming

First I thought there can a lazy sequence created and returned, that would be a nice way of dealing with Kafka streams in Clojure. For functions with side effects the idiomatic way is to use repeatedly.

```Clojure
(defn lazy-messages
  [^ConsumerIterator iterator]
  (repeatedly #(.next iterator)))
```

[https://clojuredocs.org/clojure.core/repeatedly](https://clojuredocs.org/clojure.core/repeatedly)

Unfortunately the code hold onto the head and the heap exploded. I went for the recommended way of using the Kafka library.

```Clojure
          ...
          (doseq [ ^KafkaStream stream message-streams ]
            (async/thread
              (let [ ^ConsumerIterator iterator (.iterator stream) ]
                (while (.hasNext iterator)
                  (let [message (sh-consumer/message-to-vec (.next iterator))]
                    ...
```


## Usage

Tested only with Kafka 0.8.2.1

[0.8.2.1 Release Notes](https://archive.apache.org/dist/kafka/0.8.2.1/RELEASE_NOTES.html)

### Setting up dev env, the Docker way

```Bash
docker pull istvan/zookeeper:latest
docker pull istvan/kafka:0.8.2.1
docker pull istvan/shovel:0.2.2

docker run --name zookeeper -d -p 127.0.0.1:2181:2181 istvan/zookeeper:latest
docker run --name kafka --link zookeeper:zookeeper  -d -p 127.0.0.1:9092:9092 istvan/kafka:0.8.2.1
docker run istvan/shovel:0.2.2
```

### Setting up dev env, the painful way

Download and extract the Kafka package and make sure Java is installed.

#### Start Zookeeper

```bash
./bin/zookeeper-server-start.sh config/zookeeper.properties
```

Props:

```
dataDir=/tmp/zookeeper
#dataDir=/mnt/md0/zk_data
clientPort=2181
maxClientCnxns=0
tickTime=2000
# initLimit=5
# syncLimit=2
# server.0=ec2-54-190-155-190.us-west-2.compute.amazonaws.com:2888:3888
# server.1=ec2-54-184-5-145.us-west-2.compute.amazonaws.com:2888:3888
# server.2=ec2-54-189-164-10.us-west-2.compute.amazonaws.com:2888:3888
```

#### Start Kafka 

```bash
./bin/kafka-server-start.sh config/server.properties
```

Props:
```
broker.id=0
port=9092
num.network.threads=3
num.io.threads=8
socket.send.buffer.bytes=102400
socket.receive.buffer.bytes=102400
socket.request.max.bytes=104857600
log.dirs=/tmp/kafka-logs
num.partitions=16
num.recovery.threads.per.data.dir=1
log.retention.hours=4
log.segment.bytes=1073741824
log.retention.check.interval.ms=300000
log.cleaner.enable=true
zookeeper.connect=zookeeper:2181
zookeeper.connection.timeout.ms=6000
```

#### Running the app

Producer:

```
lein uberjar && java -jar target/shovel-0.2.2-standalone.jar producer-test -f conf/app.edn
```

Consumer:

```
lein uberjar && java -jar target/shovel-0.2.2-standalone.jar consumer-test -f conf/app.edn
```
## CLOC

```
      29 text files.
      29 unique files.
      19 files ignored.

http://cloc.sourceforge.net v 1.62  T=2.44 s (4.5 files/s, 436.4 lines/s)
-------------------------------------------------------------------------------
Language                     files          blank        comment           code
-------------------------------------------------------------------------------
Clojure                          6             53             88            351
Maven                            1              1              4            347
HTML                             1             38              0            150
Bourne Shell                     3              2              0             30
-------------------------------------------------------------------------------
SUM:                            11             94             92            878
-------------------------------------------------------------------------------
```

## License

Copyright © 2015 Istvan Szukacs 

Distributed under the Apache License, Version 2.0 see LICENSE file.
