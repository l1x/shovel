(ns shovel.helpers-test
  (:require [clojure.test :refer :all]
            [shovel.helpers :refer :all]))

(def test-data-hashmap-to-properties [
    {:input {:test :test} :output {:ok {"test" :test}}}
    {:input nil           :output {:error "Input is nil"}}
  ])

(deftest test-run-hashmap-to-properties
  (testing "hashmap-to-properties"
    (doseq
      [pair test-data-hashmap-to-properties]
      (is
        (=
          (hashmap-to-properties (pair :input)) (pair :output))))))
