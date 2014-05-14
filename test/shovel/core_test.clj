(ns shovel.core-test
  (:require [clojure.test :refer :all]
            [shovel.core :refer :all]))

(defn shovel-core-pairs [typ] [
  [ {:type typ, :input "data/q1/test0.txt"}       [3   [1 2 3 4 5 6]]  ]
  ; std takes input from -c coll
  [ {:type typ, :input "std", :coll "[0 0 0 1]"}  [0   [0 0 0 1    ]]]
  ; errors - all of the functions catch all exceptions locally
  ; functions may return errors from underlying layer but not exceptions
  ; all the functions returning {:error...} on error
  [ {:type typ, :input "/dev/null"}
    {:error "Exception" :fn "read-file" :exception "Input is not a file"} ]
  ])


(deftest merge-sort-test
  (testing "merge-sort"
    (doseq
      [pair (sort-pairs "merge-sort")]
      (is
        (=
          (merge-sort (nth pair 0)) (nth pair 1))))))

