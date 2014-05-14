(ns 
  ^{:doc "

  This namespace contains the high level consumer code

  More about high level consumers from the Kafka wiki:

  Why use the High Level Consumer

  Sometimes the logic to read messages from Kafka doesn't care about 
  handling the message offsets, it just wants the data. So the High Level 
  Consumer is provided to abstract most of the details of consuming events 
  from Kafka. First thing to know is that the High Level Consumer stores the 
  last offset read from a specific partition in ZooKeeper. This offset is stored 
  based on the name provided to Kafka when the process starts. This name is 
  referred to as the Consumer Group. The Consumer Group name is global across a 
  Kafka cluster, so you should be careful that any 'old' logic Consumers be 
  shutdown before starting new code. When a new process is started with the 
  same Consumer Group name, Kafka will add that processes' threads to the set of 
  threads available to consume the Topic and trigger a 're-balance'. During this 
  re-balance Kafka will assign available partitions to available threads, possibly 
  moving a partition to another process. If you have a mixture of old and new 
  business logic, it is possible that some messages go to the old logic.

  https://cwiki.apache.org/confluence/display/KAFKA/Consumer+Group+Example"}
  ;ns
  shovel.consumer
  (:require
    ;internal
    ;external
    [clojure.pprint :as pprint]
  )
  (:import
    [kafka.consumer ConsumerIterator]
    [kafka.consumer KafkaStream])
  (:gen-class))


