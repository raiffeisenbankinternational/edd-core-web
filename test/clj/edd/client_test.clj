(ns edd.client-test
  (:require
   [clojure.test :refer :all]
   [edd.client-utils :as client-utils]))

(deftest test-responses-status-check
  "Test responses status check"
  (is (= false (client-utils/failed? [{:status 200} {:status 200}])))

  (is (= true (client-utils/failed? [{:status 200} {:status 500}]))))

(run-tests 'edd.client-test)