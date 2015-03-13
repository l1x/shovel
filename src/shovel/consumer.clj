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
    [clojure.tools.logging  :as log   ]
    )
  (:import
    [clojure.lang                       PersistentHashMap PersistentArrayMap 
                                        PersistentVector                      ]
    [org.apache.kafka.clients.consumer  KafkaConsumer Consumer                ]
    [java.util                          HashMap ArrayList Properties          ])
  (:gen-class))

(defn kafka-consumer 
  "returns a KafkaConsumer"
  ^KafkaConsumer [^PersistentArrayMap h]
  (let [ ^Properties properties (hashmap-to-properties h) ]
    (log/info properties)
    (KafkaConsumer. properties)))

(defn subscribe 
  "subscribes a customer to a topic" 
  [^KafkaConsumer consumer ^String topic] 
  (.subscribe consumer topic))

(defn consumer-blocking-poll
  "blocking poll for a kafka-consumer"
  [kafka-consumer]
  (try
   {:ok (.poll kafka-consumer 0) }
  (catch Exception e
   (do
    (let [return {:error "Exception" :fn "consumer-poll" :exception (.getMessage e) }]
    (log/debug "Exception: " (.printStackTrace e))
    return)))))


