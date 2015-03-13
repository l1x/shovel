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
(ns shovel.core
  (:require
    ;internal
    [shovel.consumer        :as     sh-consumer              ]
    [shovel.producer        :as     sh-producer              ]
    [shovel.helpers         :refer   :all                    ]
    ;external
    [clojure.tools.logging  :as     log                      ]
    [metrics.meters         :refer  [defmeter mark! rates]   ]
    [metrics.core           :refer  [new-registry]           ]
    [clojure.core.async     :as     async                    ]
    [clojure.tools.cli      :refer  [parse-opts]             ])
  (:import 
    [java.io                            File                                  ]
    [java.util                          ArrayList                             ]
    [clojure.lang                       PersistentHashMap PersistentArrayMap
                                        PersistentVector                      ]

    [org.apache.kafka.clients.consumer  ConsumerConfig KafkaConsumer
                                        Consumer
                                        ConsumerRecord ConsumerRecords        ]

    [org.apache.kafka.clients.producer  Producer                               ]
  )
  (:gen-class))

;; metrics -todo use the kafka metrics 
;; http://www.apache.org/dist/kafka/0.8.2-beta/java-doc/org/apache/kafka/common/metrics/stats/Rate.html
(def reg (new-registry))
(defmeter reg messages-read)
(defmeter reg messages-written)

;; Helpers

(defn main-loop
  "Simple main loop that block until timeout and stops or receives a message through the channel."
  [stat-chan timeout]
  (while true
    (async/<!!
      (async/go
        (let [[result source] (async/alts! [stat-chan (async/timeout 10000)])]
          (if (= source stat-chan)
            (log/info "main-loop: " result)
              ;else - timeout
              (do
                (log/info "Channel timed out after " timeout " ms. Stopping...")
                ;(.shutdown connector)
                (exit 0))))))))

;; PRODUCER - todo

(defn test-consumer
  [config]
  (log/info "fn: test-consumer params: " config)
  (let [                    stat-chan           (async/chan 8)
        ^Long               main-loop-timeout   (get-in config [:ok :shovel-consumer :main-loop-timeout ]     )
        ^Long               counter-reset       (get-in config [:ok :shovel-consumer :counter-reset     ]     ) 
        ^PersistentArrayMap consumer-config     (get-in config [:ok :consumer-config]                         )
        ^String             consumer-topic      (get-in config [:ok :shovel-consumer :topic]                  )
        ^KafkaConsumer      kafka-consumer      (sh-consumer/kafka-consumer consumer-config)                    
                            subscribed          (sh-consumer/subscribe kafka-consumer consumer-topic)]
    (log/info subscribed) ; <- needs error handling (cond ...)
    (dotimes [i 4]
      ;create i threads
      (async/thread
        (log/info "Thread")
        (let [ counter             (atom 0)
               message-counter     (atom 0) ]
          (loop []
            (let [messages (:ok (sh-consumer/consumer-blocking-poll kafka-consumer))]
            (log/info "messages" messages)
            (recur))))))))

;; CLI

(def cli-options
  [
    ["-f" "--config-file FILE" "Configuration file" :default "conf/app.edn"]
    ["-c" "--connect" "Initiate connections" :default false ]
    ["-h" "--help" "This application is helpless"]
  ])

(defn -main [& args]
  (log/info "-main starts")
  (let [  {:keys [options arguments errors summary]} (parse-opts args cli-options)
          config (read-config (:config-file options))                               ]
    ;; INIT
    (log/info "init :: start")
    (log/info "checking config...")
    (cond 
      (contains? config :ok)
        (config-ok config)
      :else
        ;; exit 1 here
        (config-err config))
    ; Execute program with options
    (case (first arguments)
      "print-config"
        (log/info config)
      "consumer-test"
        (test-consumer config)
      ;"producer-test"
      ;  (test-producer config)
      ;default
        (do
          (log/error "Missing arugments")
          (exit 1)))))
;; END
