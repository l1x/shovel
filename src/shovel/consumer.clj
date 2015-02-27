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
  ^{:doc "This namespace contains the consumer code."}
  ;ns
  shovel.consumer
  (:require
    ;internal
    [shovel.helpers :refer :all       ]
    ;external
    [clojure.core.async     :as async ]
    [clojure.tools.logging  :as log   ]
    )
  (:import
    [clojure.lang           PersistentHashMap PersistentArrayMap 
                            PersistentVector                      ]
    [kafka.consumer         ConsumerConfig Consumer 
                            ConsumerIterator KafkaStream          ]
    [kafka.javaapi.consumer ConsumerConnector                     ]
    [kafka.message          MessageAndMetadata                    ]
    [java.util              HashMap ArrayList Properties          ])
  (:gen-class))

; internal 
; external 

(defn message-to-string
  "returns a string for a message"
  ^String [^MessageAndMetadata message]
  (log/debug "fn: message-to-string params: " message)
  (String. (.message message)))

(defn message-to-vec
  "returns a vector of all of the message fields"
  ^PersistentVector [^MessageAndMetadata message]
  (log/debug "fn: message-to-vec params: " message)
  [(.topic message) (.offset message) (.partition message) (.key message) (.message message)])

(defn consumer-connector
  "returns a ConsumerConnector that can be used to create consumer streams"
  ^ConsumerConnector [^PersistentArrayMap h]
  (log/debug "fn: consumer-connector params: " h)
  (let [  ^Properties     properties  (hashmap-to-properties h)
          ^ConsumerConfig config      (ConsumerConfig. properties)  ]
    (Consumer/createJavaConsumerConnector config)))

(defn message-streams
  "returning the message-streams with a certain topic and thread-pool-size
  message-streams can be processed in threads with simple blocking on empty queue"
  ^ArrayList [^ConsumerConnector consumer-conn ^String topic ^Integer number-of-streams]
  (let [  ^HashMap    message-streamz         (.createMessageStreams consumer-conn {topic number-of-streams})
          ^ArrayList  topic-message-streamz   (.get message-streamz topic) ]
    topic-message-streamz))

(defn lazy-messages
  [^ConsumerIterator iterator]
  (repeatedly #(.next iterator)))

(defn lazy-channels
  ""
  [channels]
  (lazy-seq
    (cons (let [ [v _] (async/alts!! channels) ] v) (lazy-channels channels))))

(def work-chan (async/chan 32))

; I still haven't found the best solution to deal with KafkaStreams so
; for now it will be a channel based stuff.
; the problem is to turn a set of streams into a lazy-seq so that we
; have from each channel the most (or almost) up to date data
; [stream stream stream ..... stream]
; =>
; (message-from-first-stream message-from-second-stream message-from-third-stream ..... message-from-nth-stream ...
; message-from-first-stream......message-from-nth-stream.....)
;

(defn message-loops
  ""
  [^ArrayList streams]
  (log/info "=========> #streams:" (count streams))
  (doseq [ ^KafkaStream stream streams ]
    (async/thread
      (let [ lazy-messagez (lazy-messages (.iterator stream)) ]
        (async/go-loop [[message-and-metadata & rest-of-the-stream] lazy-messagez]
          (async/>!! work-chan (message-to-vec message-and-metadata))
          (recur rest-of-the-stream))))))

(defn messages
  ""
  [^ArrayList streams]
  (do
    (message-loops streams)
    (lazy-channels [work-chan])))
