;;Copyright 2014 Istvan Szukacs

;;Licensed under the Apache License, Version 2.0 (the "License");
;;you may not use this file except in compliance with the License.
;;You may obtain a copy of the License at

;;    http://www.apache.org/licenses/LICENSE-2.0

;;Unless required by applicable law or agreed to in writing, software
;;distributed under the License is distributed on an "AS IS" BASIS,
;;WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;;See the License for the specific language governing permissions and
;;limitations under the License
(ns 
  ^{:doc "
    https://cwiki.apache.org/confluence/display/KAFKA/0.8.0+Producer+Example
  "}
  ;ns
  shovel.producer
  (:require
    ;internal
    [shovel.helpers         :refer :all                     ]
    ;external
    [clojure.tools.logging  :as log                         ])
    ;none
  (:import
    [clojure.lang                       PersistentHashMap PersistentArrayMap
                                        PersistentVector                            ]
    [org.apache.kafka.clients.producer  KafkaProducer ProducerRecord RecordMetadata ]
    [java.util.concurrent               TimeUnit Future 
                                        ExecutionException TimeoutException         ]
    [java.util                          Properties                                  ])
  (:gen-class))


(defn kafka-producer
  "Takes a hashmap for configuration and returns a KafkaProducer that can be used to send messages"
  ^KafkaProducer [^PersistentArrayMap h]
  (log/debug "fn: kafka-producer" " params: " h)
  (let [ ^Properties      properties  (hashmap-to-properties h)
         ^KafkaProducer   producer    (KafkaProducer. properties) ]
    ;return
    producer))

;KafkaProducer<byte[], byte[]> producer = new KafkaProducer<byte[], byte[]>(props);

(defn producer-record
  "Takes a topic and a payload and created a ProducerRecord that is returned"
  ^ProducerRecord [^bytes topic ^bytes payload]
  ;(log/debug "fn: producer-record" " params: " (String. topic) (String. payload))
  (let [ ^ProducerRecord record (ProducerRecord. (String. topic) payload) ]
    ;return
    record))

;ProducerRecord<byte[], byte[]> record = new ProducerRecord<byte[], byte[]>(topicName, payload);

;TODO:
;Callback cb = stats.nextCompletion(sendStart, payload.length, stats);

(defn producer-send-async 
  "Takes a record and sends it, returns immediately" 
  [^KafkaProducer producer ^ProducerRecord record]
  (.send producer record))

(defn producer-send-sync
  "Takes a record and sends it, returns after the send or the timeout"
  [^KafkaProducer producer ^ProducerRecord record ^Long timeout]
  (let [^Future fut (.send producer record) ]
    ;return {:ok|:err}
    (try
      (let [ ^RecordMetadata return (.get fut timeout TimeUnit/MILLISECONDS) ]
        {:ok {:offset (.offset return) :partition (.partition return) :topic (.topic return) }})
    (catch InterruptedException e 
        {:err "InterruptedException" :fn "producer-send-sync" :message (.getMessage e)})
    (catch ExecutionException   e 
        {:err "ExecutionException" :fn "producer-send-sync" :message (.getMessage e)})
    (catch TimeoutException     e 
        {:err "TimeoutException" :fn "producer-send-sync" :message (.getMessage e)}))))

;producer.send(record, cb);

(defn producer-close
  "Takes a producer and calls .close() on it"
  [^KafkaProducer producer]
  (.close producer))

;producer.close();



