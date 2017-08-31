;;; This namespace is used for testing purpose. It use the
;;; clojurescript.test lib.
(ns clj-dashboard-frontend.core-test
  (:require [clj-dashboard-frontend.core :refer (foo)]))

(deftest foo-test
  (testing "I don't do a lot\n"
    (testing "Edge cases\n"
      (testing "(foo str)"
        (are [expected actual] (= expected actual)
             "ClojureScript!" (foo "")
             "Hello, ClojureScript!" (foo nil))))))
