(ns clj.handler-test
  (:require [clojure.test :refer :all]
            [dashboard.handler :refer :all]))

(deftest get-dashboard-commands-test
  (testing "Retrieving dashboard commands"
    (testing "Edge cases\n"
      (testing "(foo str)"
        (are [expected actual] (= expected actual)
                               "ClojureScript!" (foo "")
                               "Hello, ClojureScript!" (foo nil))))))
