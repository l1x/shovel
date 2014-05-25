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
  ^{:author "Istvan Szukacs" :doc "" }
  ;ns
  shovel.zk
  (:require
    ;internal
    ;external
    [clojure.data.json  :as json  ]
    [zookeeper          :as zk    ])
  (:import
    [org.apache.zookeeper ZooKeeper ])
  (:gen-class))

; this is not required for Kafka at all, the high level consumer
; takes care of the Zookeper related calls

;; Basic Zookeeper functions 

; todo come up with something for watchers
(defn connect
  [^clojure.lang.PersistentArrayMap config]
  (zk/connect
    (get-in config [:zookeeper.connect])))

(defn disconnect
  [^ZooKeeper client]
  (zk/close client))

(defn get-state
  [^ZooKeeper client]
  (zk/state client))

;; Advanced Zookeeper functions


