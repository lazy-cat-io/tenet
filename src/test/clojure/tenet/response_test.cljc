(ns tenet.response-test
  (:require
   #?@(:clj  [[clojure.test :refer [deftest testing is]]]
       :cljs [[cljs.test :refer [deftest testing is]]])
   [tenet.response :as sut]))

(deftest error?-test
  (is (not (sut/error? nil)))
  (is (not (sut/error? 42)))
  (is (not (sut/error? "42")))
  (is (sut/error? ::sut/error))

  (is (not (sut/error? ::error)))
  (sut/derive ::error)
  (is (sut/error? ::error))
  (sut/underive ::error)
  (is (not (sut/error? ::error))))

(deftest kind-test
  (let [e #?(:clj (Exception. "boom!")
             :cljs (js/Error. "boom!"))]
    (is (nil? (sut/kind nil)))
    (is (nil? (sut/kind 42)))
    (is (nil? (sut/kind "42")))
    (is (= ::sut/error (sut/kind e)))
    (is (= ::created (sut/kind [::created 42])))))
