(ns shovel.core
  (:require
    ;internal
    [shovel.consumer            :as sh-consumer         ]
    [shovel.producer            :as sh-producer         ]
    ;external
    [clojure.tools.cli          :refer [parse-opts]     ]
    [clojure.edn                :as     edn             ])
  (:import 
    [java.io File])
  (:gen-class))

;; Helpers 

; Reading a file the safe way
(defn read-file
  "Returns {:ok string } or {:error...}"
  [^String file]
  (try
    (cond
      (.isFile (File. file))
        {:ok (slurp file) }                         ; if .isFile is true {:ok string}
      :else
        (throw (Exception. "Input is not a file"))) ;the input is not a file, throw exception
  (catch Exception e
    {:error "Exception" :fn "read-file" :exception (.getMessage e) }))) ; catch all exceptions

;Parsing a string to Clojure data structures the safe way
(defn parse-edn-string
  [s]
  (try
    {:ok (clojure.edn/read-string s)}
  (catch Exception e
    {:error "Exception" :fn "parse-config" :exception (.getMessage e)})))

;This function wraps the read-file and the parse-edn-string
;so that it only return {:ok ... } or {:error ...} 
(defn read-config 
  [file]
  (let 
    [ file-string (read-file file) ]
    (cond
      (contains? file-string :ok)
        ;this return the {:ok} or {:error} from parse-edn-string
        (parse-edn-string (file-string :ok))
      :else
        file-string)))

;; OPS
(defn test-consumer 
  [config] 
  (sh-consumer/consume
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
        (sh-producer/message "userprofile_test" "asd" (str "this is my message" n))))))

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
        (println (take 10 (first (test-consumer (get-in config [:ok :consumer-config])))))
      "producer-test"
        (println (test-producer (get-in config [:ok :producer-config])))
      ;default
        (exit 1 (println "Dead end")))))

;; END
