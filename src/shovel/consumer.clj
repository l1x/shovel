;; Copyright (c) Istvan Szukacs, 2014. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

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
    [clojure.walk   :refer [stringify-keys]]
    [clojure.pprint :as pprint])
  (:import
    [kafka.consumer         ConsumerConfig Consumer KafkaStream ]
    [kafka.javaapi.consumer ConsumerConnector                   ]
    [kafka.message          MessageAndMetadata                  ]
    [java.util              Properties                          ])
  (:gen-class))

; internal 

(defn- hashmap-to-properties
  [h]
  (doto (Properties.) 
    (.putAll (stringify-keys h))))

(defn- message-to-string
  [message]
  (String. (.message message)))

; external 

(defn consumer-connector
  [h]
  (println "################ consumer-connector ###########")
  (let [config (ConsumerConfig. (hashmap-to-properties h))]
    (Consumer/createJavaConsumerConnector config)))

(defn message-streams
  [^ConsumerConnector consumer topic thread-pool-size]
  (println "################ message-streams ###########")
  (.get (.createMessageStreams consumer {topic thread-pool-size}) topic))

(defn test-iterate
  [streams]
  (println "################ test iterate ###########")
  (doseq [stream streams]
    (doseq [message stream] (println (message-to-string message)))))

(defn shutdown
  "Closes the connection to Zookeeper and stops consuming messages."
  [^ConsumerConnector consumer]
  (.shutdown consumer))



;#######################










