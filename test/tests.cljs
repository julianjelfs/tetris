(ns tetris.tests
  (:require-macros [cemerick.cljs.test
                    :refer (is deftest with-test run-tests testing test-var)])
  (:require [cemerick.cljs.test :as t]))

(deftest something
  (is (= 3 4)))

(defn what [])
