;; Copyright (c) Istvan Szukacs, 2014. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

(ns 
  ^{:doc "
    https://cwiki.apache.org/confluence/display/KAFKA/0.8.0+Producer+Example
  "}
  ;ns
  shovel.producer
  (:require
    ;internal
    ;external
    [clojure.walk   :refer [stringify-keys]]
    [clojure.pprint :as pprint])
  (:import
    [kafka.javaapi.producer Producer                    ]
    [kafka.producer         KeyedMessage ProducerConfig ]
    [java.util              Properties                  ])
  (:gen-class))

; internal 
; move this to shovel.helpers
(defn hashmap-to-properties
  [h]
  (doto (Properties.) 
    (.putAll (stringify-keys h))))

; external 

(defn producer-connector
  [h]
  (let [config (ProducerConfig. (hashmap-to-properties h))]
    (Producer. config)))

(defn message
  [topic key value] 
  (println topic key value)
  (KeyedMessage. topic key value))

(defn produce
  [^Producer producer ^KeyedMessage message]
  (println producer message)
  (.send producer message))
