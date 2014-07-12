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
  shovel.helpers
  ^{:doc ""}
  (:require
    ;internal
    ;external
    [clojure.walk   :refer [stringify-keys]]
    [clojure.pprint :as pprint]
    [clojure.edn    :as edn])
  (:import
    [java.util Properties]
    [java.io File]))


;; Helpers

; All function here should return a hash-map
; either with {:ok return_value} or {:error "Ex" :fn.....}
; the caller decides to log that exact error message or
; change it to something else that way the

;int

;ext

(defn hashmap-to-properties
  ^java.util.Properties [^clojure.lang.PersistentArrayMap h]
  (cond
    (not (nil? h))
      {:ok (doto (Properties.) (.putAll (stringify-keys h)))}
    :else
      {:error "Input is nil"}))

(defn uuid
  "Returns a new java.util.UUID as string" 
  []
  (str (java.util.UUID/randomUUID)))

; Reading a file (the safe way)
; the only problem if the input file is huge
; todo check size and refuse to read over 100k
(defn read-file
  "Returns {:ok string } or {:error...}"
  [^String file]
  (try
    (cond
      (nil? file)
      (throw (Exception. "Input is nil"))
      (.isFile (File. file))
      {:ok (slurp file) }
      :else
      (throw (Exception. (str " is not a file: "))))
    ;Catch all
    (catch Exception e
      {:error "Exception" :fn "read-file" :exception (.getMessage e) })))

;Parsing a string to Clojure data structures the safe way
(defn parse-edn-string
  [s]
  (try
    {:ok (clojure.edn/read-string s)}
    (catch Exception e
      {:error "Exception" :fn "parse-config" :exception (.getMessage e)})))

;This function wraps the read-file and the parse-edn-string
(defn read-config
  [file]
  (let [file-string (read-file file)]
    (cond
      ;file could be successfully read
      (contains? file-string :ok)
      (let [ config (parse-edn-string (file-string :ok))  ]
        (cond
          (contains? config :ok)
          ;return {:ok hash-map}
          {:ok config}
          (contains? config :error)
          ;return
          {:error "Exception" :fn "read-config"
           :exception (str "Parsing the EDN string into Clojure (hash-map) has failed: " (config :exception))}
          :else
          ;return
          {:error "Exception" :fn "read-config"
           :exception (str "Something unexpected is returned from fn read-file" file-string)}))
      ;file cannot be read
      (contains? file-string :error)
      ;return
      {:error "Exception" :fn "read-config"
       :exception (str "Reading the file has failed. Reason: " file (file-string :exception))}
      :else
      ;return
      {:error "Exception" :fn "read-config"
       :exception (str "Reading the following file has failed with something unexpected: " file-string)})))
