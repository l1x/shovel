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
    [clojure.walk           :refer [stringify-keys] ]
    [clojure.edn            :as edn                 ]
    [clojure.tools.logging  :as log                 ])
  (:import
    [java.io      File                                  ]
    [java.util    Properties                            ]
    [clojure.lang PersistentHashMap PersistentArrayMap  ]
    )
  (:gen-class))


;; Helpers

; All function here should return a hash-map
; either with {:ok return_value} or {:error "Ex" :fn.....}
; the caller decides to log that exact error message or
; change it to something else that way the

;int

;ext
;;is this a good idea?
(defmacro defnx
  [name args body]
  `(def ~name
    (fn ~args
      (try ~body
        (catch Exception e# { :error "Exception"
                              :fn (str
                                    (:ns (meta (var ~name)))
                                    "/"
                                    (:name (meta (var ~name))))
                              :file (:file (meta (var ~name)))
                              :exception (.getMessage e#) })))))

(defmulti hashmap-to-properties identity)

(defmethod hashmap-to-properties nil 
  [_]  
  (hashmap-to-properties {}))
(defmethod hashmap-to-properties :default 
  ^Properties [^PersistentArrayMap h] 
  (log/debug "hashmap-to-properties input: " h)
    (let [ ^Properties properties (doto (Properties.) (.putAll (stringify-keys h)))]
      (log/debug "hashmap-to-properties output: " properties)
      properties))

(defn uuid
  "Returns a new java.util.UUID as string" 
  []
  (str (java.util.UUID/randomUUID)))

(defn read-file
  "Returns {:ok string } or {:error...}"
  [^String file]
  (log/debug "read-file input: " file)
  (try
    (cond
      (.isFile (File. file))
        (do
          (let [  ^String file-content (slurp file)
                  return {:ok file-content }]
            (log/debug "read-file output: " return)
            return))
      :else
        (do
          (log/debug "Exception: Input is not a file")
          (throw (Exception. "Input is not a file")))) ;the input is not a file, throw exception
  (catch Exception e
    (do
      (let [return {:error "Exception" :fn "read-file" :exception (.getMessage e) }]
        (log/debug "Exception: " return)
        return))))) ; catch all exceptions

(defn parse-edn-string
  "Returns the Clojure data structure representation of s"
  [s]
  (try
    {:ok (edn/read-string s)}
  (catch Exception e
    {:error "Exception" :fn "parse-config" :exception (.getMessage e)})))

(defn read-config
  "Returns the Clojure hashmap version of the config file"
  [file]
  (log/debug "read-config input: " file)
  (let
    [ file-string (read-file file) ]
    (cond
      (contains? file-string :ok)
        ;this return the {:ok} or {:error} from parse-edn-string
        (parse-edn-string (file-string :ok))
      :else
        ;the read-file operation returned an error
        file-string)))

(defn exit [n]
  (log/info "init :: stop")
  (System/exit n)
  true)

(defn config-ok [config]
  (log/info "config [ok]")
  (log/debug config))

(defn config-err
  [config]
  (log/error "config [error]")
  (log/error config)
  (exit 1))

;(def ^PersistentHashMap config (read-config "conf/app.edn"))
