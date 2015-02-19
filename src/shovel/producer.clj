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
    [shovel.helpers         :refer [hashmap-to-properties]  ]
    ;external
    [clojure.tools.logging  :as log                         ])
    ;none
  (:import
    [kafka.javaapi.producer Producer                        ]
    [kafka.producer         KeyedMessage ProducerConfig     ]
    [java.util              Properties                      ])
  (:gen-class))

; internal 

; external 

(defn producer-connector
  [^clojure.lang.PersistentArrayMap h]
  (log/debug "fn: producer-connector" " config: " h)
  (let [config (ProducerConfig. ((hashmap-to-properties h) :ok))]
    (Producer. config)))

(defn message
  (^KeyedMessage [topic key value] 
    (log/debug "fn: message topic: " topic)
    (KeyedMessage. topic key value))
  (^KeyedMessage [topic value]
    (log/debug "fn: message topic: " topic)
    (KeyedMessage. topic nil value)))

(defn produce
  [^Producer producer ^KeyedMessage message]
  (log/debug "fn: produce message: " message)
  (.send producer message))

