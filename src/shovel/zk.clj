;; Copyright (c) Istvan Szukacs, 2014. All rights reserved.  The use
;; and distribution terms for this software are covered by the Eclipse
;; Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this
;; distribution.  By using this software in any fashion, you are
;; agreeing to be bound by the terms of this license.  You must not
;; remove this notice, or any other, from this software.

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


