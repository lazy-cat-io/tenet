(ns tenet.response-test
  (:require
   #?@(:clj  [[clojure.test :refer [deftest testing is]]]
       :cljs [[cljs.test :refer [deftest testing is]]])
   [tenet.response :as sut]))

(deftest error?-test
  (testing "dummy test"
    (is (false? (sut/error? 42)))
    (is (false? (sut/error? "42")))
    (is (false? (sut/error? ::error)))
    (is (true? (sut/error? ::sut/error)))))
