(ns shovel.core-test
  (:require 
    [clojure.test                   :refer :all ]
    [shovel.core                    :refer :all ]
    [shovel.helpers                 :refer :all ]
    [clojure.tools.logging          :as log     ]
    [clojure.test.check             :as tc      ]
    [clojure.test.check.generators  :as gen     ]
    [clojure.test.check.properties  :as prop    ])
  (:import
    [java.io      File                                  ]
    [java.util    Properties                            ]
    [clojure.lang PersistentHashMap PersistentArrayMap  ]))


(def test-hashmap-to-properties
  (prop/for-all 
    [h (gen/map gen/keyword gen/any-printable)]
    (log/info h)
    (hashmap-to-properties h)))

(println (tc/quick-check 10 test-hashmap-to-properties))

