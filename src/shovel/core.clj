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
    [shovel.consumer        :as     sh-consumer                         ]
    [shovel.producer        :as     sh-producer                         ]
    [shovel.helpers         :refer   :all                               ]
    ;external
    [clojure.tools.logging  :as     log                                 ]
    [metrics.meters         :refer  [defmeter mark! rates]              ]
    [metrics.histograms     :refer  [defhistogram update! percentiles]  ]
    [metrics.counters       :refer  [defcounter inc! value]             ]
    [metrics.core           :refer  [new-registry]                      ]
    [clojure.core.async     :as     async         ]
    [clojure.tools.cli      :refer  [parse-opts]  ])
  (:import 
    [java.io                File                                ]
    [java.util              ArrayList                           ]
    [java.util.concurrent   ThreadPoolExecutor$DiscardPolicy    ]
    [clojure.lang           PersistentHashMap PersistentArrayMap
                            PersistentVector                    ]

    [kafka.consumer         ConsumerConfig Consumer
                            KafkaStream ConsumerIterator        ]
    [kafka.message          MessageAndMetadata                  ]

    [kafka.javaapi.consumer ConsumerConnector                   ]
    [org.apache.kafka.clients.producer Producer                            ]
  )
  (:gen-class))

;; metrics 
;; http://www.apache.org/dist/kafka/0.8.2-beta/java-doc/org/apache/kafka/common/metrics/stats/Rate.html
(def reg (new-registry))
(defmeter     reg messages-read)
(defmeter     reg messages-written)
;(defcounter   reg bytes-read)
;(defcounter   reg bytes-written)


;; Helpers

(defn main-loop [stat-chan timeout]
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

;; PRODUCER

(defn test-producer
  [config]
  (log/info "fn: test-producer params: " config)
  (let [                      stat-chan           (async/chan 8)
          ^Long               main-loop-timeout   (get-in config [:ok :shovel-producer :main-loop-timeout ]   )
          ^Long               counter-reset       (get-in config [:ok :shovel-producer :counter-reset     ]   )
          ^Long               num-of-messages     (get-in config [:ok :shovel-producer :num-of-messages   ]   )
          ^String             producer-topic      (get-in config [:ok :shovel-producer :topic             ]   ) 
          ^PersistentArrayMap producer-config     (get-in config [:ok :producer-config                    ]   )   ]
    (dotimes [i 8]
      ;create i threads
      (async/thread
        (let [ ^Producer  producer-connector  (sh-producer/producer-connector producer-config)
                          counter             (atom 0)
                          message-counter     (atom 0) ]
         (log/info "Producer starting up: " producer-connector)
         (doseq [n (range num-of-messages)]
           (let [  message (sh-producer/message producer-topic (str "{this is my message : " n "}"))
                   return  (sh-producer/produce producer-connector message)    ]
           (do
             (swap! message-counter inc)
             (log/debug "message counter: " @message-counter)
             (mark! messages-written)
             (cond (= @counter counter-reset)
               (do
                 (reset! counter 0)
                 (async/>!! stat-chan {:rates (rates messages-written) :connector producer-connector :metrics (.metrics producer-connector) } ))
             :else
               (do
                 (log/debug @counter)
                 (swap! counter inc)))))))))
    ;this is the event loop
    (main-loop stat-chan main-loop-timeout)))

;; CONSUMER

(defn test-consumer
  [config]
  (log/info "fn: test-consumer params: " config)
  (let [        stat-chan           (async/chan 8)
        ^Long   main-loop-timeout   (get-in config [:ok :shovel-consumer :main-loop-timeout ]     )
        ^Long   counter-reset       (get-in config [:ok :shovel-consumer :counter-reset     ]     ) ]
    (dotimes [i 4]
      ;create i threads
      (async/thread
        ;each thread has its own kafka connector, that has to shut down properly before exit,
        ;also this needs to move up one layer (let) to save some insignificant amount of memory
        (let [  ^PersistentArrayMap consumer-config     (get-in config [:ok :consumer-config])
                ^String             consumer-topic      (get-in config [:ok :shovel-consumer :topic])
                                    consumer-connector  (sh-consumer/consumer-connector consumer-config)
                ^ArrayList          message-streams     (sh-consumer/message-streams consumer-connector consumer-topic (int 1))
                                    counter             (atom 0)
                                    message-counter     (atom 0)                                                                  ]
          (log/info "fn: test-consumer #streams:" (count message-streams))
          (doseq [ ^KafkaStream stream message-streams ]
            (async/thread
              (let [ ^ConsumerIterator iterator (.iterator stream) ]
                (while (.hasNext iterator)
                  (let [message (sh-consumer/message-to-vec (.next iterator))]
                    (do
                      (swap! message-counter inc)
                      (log/debug "message counter: " @message-counter)
                      (mark! messages-read)
                      (cond (= @counter counter-reset)
                        (do
                          (reset! counter 0)
                          (async/>!! stat-chan {:rates (rates messages-read) :connector consumer-connector } ))
                      :else
                        (do
                          (swap! counter inc)))
                      (log/debug message @counter stat-chan))))))))))
  ;this is the event loop
  (main-loop stat-chan main-loop-timeout)))

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
      "producer-test"
        (test-producer config)
      ;default
        (do
          (log/error "Missing arugments")
          (exit 1)))))
;; END
