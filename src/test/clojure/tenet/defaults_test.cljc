(ns tenet.defaults-test
  (:require
    #?@(:clj  [[clojure.test :refer [deftest testing is]]]
        :cljs [[cljs.test :refer [deftest testing is]]])
    [tenet.core :as sut]
    [tenet.defaults]))


(deftest anomaly?-test
  (testing "expected anomalies"
    (doseq [x [:error ::sut/error #?(:clj  (Exception. "boom!")
                                     :cljs (js/Error. "boom!"))]]
      (is (true? (sut/anomaly? x)))))

  (testing "expected non anomalies"
    (doseq [x [:not-error ::error 42 nil #?(:clj  (Object.)
                                            :cljs (js/Object.))]]
      (is (false? (sut/anomaly? x))))))


(def boom!
  (constantly :error))


(deftest thread-first-macro-test
  (is (= 43 (sut/-> 42 inc)))
  (is (= :error (sut/-> 42 inc boom!)))
  (is (= :error (sut/-> 42 inc boom! inc))))


(deftest thread-last-macro-test
  (is (= 43 (sut/->> 42 inc)))
  (is (= :error (sut/->> 42 inc boom!)))
  (is (= :error (sut/->> 42 inc boom! inc))))
