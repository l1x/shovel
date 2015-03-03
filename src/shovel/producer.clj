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
                                        PersistentVector                      ]
    [org.apache.kafka.clients.producer  Producer                              ]
    [kafka.producer                     KeyedMessage ProducerConfig           ]
    [java.util                          Properties                            ])
  (:gen-class))

; internal 

; external 

(defn producer-connector
  ^Producer [^PersistentArrayMap h]
  (log/debug "fn: producer-connector" " params: " h)
  (let [ ^Properties      properties  (hashmap-to-properties h)
         ^ProducerConfig  config      (ProducerConfig. properties) ]
    (Producer. config)))

(defn message
  (^KeyedMessage [topic key value] 
    (log/debug "fn: message params: " topic key value)
    (KeyedMessage. topic key value))
  (^KeyedMessage [topic value]
    (log/debug "fn: message params: " topic value)
    (KeyedMessage. topic nil value)))

(defn produce
  "This has to be rewritten and take the producer configuration 
  into consideration, potentially return the latency of the call as well"
  [^Producer producer ^KeyedMessage message]
  (log/debug "fn: produce params: " producer message)
  (try 
    {:ok (.send producer message)}
  (catch Exception e 
    { :error "Exception" :fn "produce" :exception (.getMessage e) :e e})))

