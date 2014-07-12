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
    ;mild ocd
    [shovel.consumer   :as     sh-consumer    ]
    [shovel.producer   :as     sh-producer    ]
    [shovel.helpers    :refer [read-config]   ]
    ;external
    [clojure.tools.cli :refer [parse-opts]    ])
  (:import 
    [java.io File])
  (:gen-class))

;; OPS

(defn test-consumer 
  [config] 
  (sh-consumer/default-iterator
    (sh-consumer/message-streams
      (sh-consumer/consumer-connector config)
      (:topic config)
      (int (read-string (:thread.pool.size config))))))

(defn test-producer
  [config]
  (let [producer-connection (sh-producer/producer-connector config)]
    (doseq [n (range 100)]
      (sh-producer/produce
        producer-connection
        ;move this to config
        (sh-producer/message "user_profile_test" "asd" (str "this is my message" n))))))

;; CLI

(defn exit [status msg]
  (println msg)
  (System/exit status))

(def cli-options
  [
    ["-f" "--config-file FILE" "Configuration file" :default "conf/app.edn"]
    ["-c" "--connect" "Initiate connections" :default false ]
    ["-h" "--help" "This application is helpless"]
  ])

(defn -main [& args]
  ;same-named symbols to the map keys
  ;parse-opts returns -> {:options {:config-file "file/path"}, :arguments [print-config], :summary...}
  (let [  {:keys [options arguments errors summary]} (parse-opts args cli-options)
          ; options => {:config-file "file/path" :help true ...}
          config (read-config (:config-file options)) ]
    ; Handle help and error conditions
    (cond
      (or (empty? config) (:error config))
        (exit 1 (str "Config cannot be read or parsed..." "\n" config))
      errors
        (exit 1 (str "Incorrect options supplied... Exiting...")))

    ; Execute program with options
    (case (first arguments)
      "print-config"
        (println config)
      "consumer-test"
        (test-consumer (get-in config [:ok :consumer-config]))
      "producer-test"
        (test-producer (get-in config [:ok :producer-config]))
      ;default
        (exit 1 (println "Dead end")))))

;; END
