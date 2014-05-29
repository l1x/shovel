;;Copyright 2014 Istvan Szukacs

;;Licensed under the Apache License, Version 2.0 (the "License");
;;you may not use this file except in compliance with the License.
;;You may obtain a copy of the License at

;;    http://www.apache.org/licenses/LICENSE-2.0

;;Unless required by applicable law or agreed to in writing, software
;;distributed under the License is distributed on an "AS IS" BASIS,
;;WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;;See the License for the specific language governing permissions and
;;limitations under the License.
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
    [shovel.helpers :refer [hashmap-to-properties uuid]]
    ;external
    [clojure.core.async :as async])
  (:import
    [kafka.consumer         ConsumerConfig Consumer KafkaStream ]
    [kafka.javaapi.consumer ConsumerConnector                   ]
    [kafka.message          MessageAndMetadata                  ])
  (:gen-class))

; internal 

(defn- message-to-string
  "returns a string for a message"
  [^kafka.message.MessageAndMetadata message]
  (String. (.message message)))

(defn- message-to-vec
  "return a vector of all of the message fields"
  [^kafka.message.MessageAndMetadata message]
  [(.topic message) (.offset message) (.partition message) (.key message) (.message message)])

; external 

(defn consumer-connector
  "returns a ConsumerConnector that can be used to create consumer streams"
  ^ConsumerConnector [^clojure.lang.PersistentArrayMap h]
  (let [config (ConsumerConfig. (hashmap-to-properties h))]
    (Consumer/createJavaConsumerConnector config)))

(defn message-streams
  "returning the message-streams for with a certain topic and thread-pool-size
  message-streams can be processed in threads with simple blocking on empty queue"
  ^java.util.ArrayList [^ConsumerConnector consumer ^String topic ^Integer thread-pool-size]
  (.get (.createMessageStreams consumer {topic thread-pool-size}) topic))

(defn default-iterator
  "processing all streams in a thread and printing the message field for each message"
  [^java.util.ArrayList streams]
  (let [c (async/chan)]
    ;; create a thread for each stream
    (doseq 
      [^kafka.consumer.KafkaStream stream streams]
      (let [uuid (uuid)] 
        (async/thread 
          (async/>!! c 
            (doseq 
              [^kafka.message.MessageAndMetadata message stream] 
              (println (str "uuid: " uuid " :: "(String. (nth (message-to-vec message) 4)))))))))
    ;; read the channel forever
    (while true
      (async/<!! c))))
