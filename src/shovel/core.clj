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
                            KafkaStream ConsumerIterator ]
    [kafka.javaapi.consumer ConsumerConnector                   ]
    [kafka.message          MessageAndMetadata                  ])
  (:gen-class))

;; metrics 
(def reg (new-registry))
(defmeter     reg messages-read)
(defmeter     reg messages-written)
(defcounter   reg bytes-read)
(defcounter   reg bytes-written)


;; Helpers

(def handler (proxy [Thread$UncaughtExceptionHandler] [] 
  (uncaughtException [thread exception]
    (log/error thread exception))))

(log/info (Thread/setDefaultUncaughtExceptionHandler handler))

(defn main-loop [stat-chan]
  (while true
    (async/<!!
      (async/go
        (let [[result source] (async/alts! [stat-chan (async/timeout 10000)])]
          (if (= source stat-chan)
            (log/info "main-loop: " result)
                ;else - timeout
              (do
                (log/info "Channel timed out. Stopping...")
                (exit 0))))))))

;;
;; OPS

(defnx safe-producer-connector [config] 
  (do 
    (let [return {:ok (sh-producer/producer-connector config)}]
      (log/debug "fn:safe-producer-connector return: " return)
      return)))

(defnx safe-producer-produce [producer-connection message]
  (do
    (let [return {:ok (sh-producer/produce producer-connection  message)}]
      (log/debug "fn:safe-producer-produce return: " return)
      return)))

(defn test-producer
  [config topic]
  (log/info "fn: test-producer params: " config topic)
  (let [stat-chan (async/chan 8)]
    (dotimes [i 4]
      ;create i threads
      (async/thread
        (log/info "SZOPKI!!!!!")
        ;each thread has its own kafka connector,

    (let [  producer-connection-hm  (safe-producer-connector config) 
            counter                 (atom 0)                          ]

      ;if producer connection is successful
      (cond 
        (contains? producer-connection-hm :ok) 
          (do
            (log/info producer-connection-hm)
            (def producer-connection (:ok producer-connection-hm))) 
      :else 
        (do 
          (log/error "Cannot connect....")
          (exit 1)))

      ;produce N messages and stop
      (log/info producer-connection)
      (doseq [n (range 100)]
        (do
          (log/info "n: " n)
          ;send the message
          (let [message (sh-producer/message topic "asd" (str "{this is my message : " n "}"))
                return (safe-producer-produce producer-connection message)
                ] 
            (log/info 
              "fn:safe-producer-produce : " producer-connection 
              "message: " message 
              "produce:" return)
            ;mark the meter
            (mark! messages-written)
            ;if the counter is 10000 reset the counter and log the metrics
            (cond 
              (= @counter 10) ;if
              (do 
                (reset! counter 0) ;not sure if thread safe
                (log/info (rates messages-written) message return)) 
            :else 
              (do 
                (log/debug @counter) 
                (swap! counter inc)));end cond
            ))))
  {:ok :ok}))))

(defn test-consumer
  [config]
  (log/info "fn: new-consumer-messages params: " config)
  (let [stat-chan (async/chan 8)]
    (dotimes [i 4]
      ;create i threads
      (async/thread
        ;each thread has its own kafka connector,
        (let [  ^PersistentArrayMap consumer-config     (get-in config [:ok :consumer-config])
                ^String             consumer-topic      (get-in config [:ok :common :consumer-topic])
                                    consumer-connector  (sh-consumer/consumer-connector consumer-config)
                ^ArrayList          message-streams     (sh-consumer/message-streams consumer-connector consumer-topic (int 1))
                                    counter             (atom 0)
                                    message-counter     (atom 0)                                                                  ]

          (log/info "=========> #streams:" (count message-streams))
          (doseq [ ^KafkaStream stream message-streams ]
            (async/thread
              (let [ iterator (.iterator stream) ]
                (while (.hasNext iterator)
                  (let [message (sh-consumer/message-to-vec (.next iterator))]
                    (do
                      (swap! message-counter inc)
                      (log/debug "message counter: " @message-counter)
                      (log/debug ".hasNext")
                      (mark! messages-read)
                      (inc! bytes-read 1)
                      (cond (= @counter 100000)
                        (do
                          (reset! counter 0)
                          (async/>!! stat-chan {:rates (rates messages-read) :percentiles (value bytes-read) } ))
                      :else
                        (do
                          (log/debug @counter)
                          (swap! counter inc)))
                      (log/debug message @counter stat-chan))))))))))
(main-loop stat-chan)))
            
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
        (println config)
      "consumer-test"
        (test-consumer config)
      "producer-test"
      (do
        (log/info "producer-test")
        (log/info (test-producer (get-in config [:ok :producer-config]) (get-in config [:ok :common :producer-topic]))))
      ;default
        (do
          (log/error "Missing arugments")
          (exit 1))))
        )


;; END
