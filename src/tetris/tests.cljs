(ns tetris.tests
  (:require-macros [cemerick.cljs.test
                    :refer (is deftest with-test run-tests testing test-var)])
  (:require [cemerick.cljs.test :as t]))

(deftest something
  (is (= 3 4)))

;;can't get this to work at all and all the instructions are just
;;gobbledegook that assume all sorts of knowlegde I don't have
