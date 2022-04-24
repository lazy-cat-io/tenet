(ns tenet.core-test
  (:require
    #?@(:clj  [[clojure.test :refer [deftest testing is]]
               [clojure.edn :refer [read-string] :rename {read-string parse}]]
        :cljs [[cljs.test :refer [deftest testing is]]
               [cljs.reader :refer [read-string] :rename {read-string parse}]])
    [tenet.core :as sut]))


(deftest anomalies-test
  (testing "expected anomalies"
    (doseq [x [:error
               ::sut/error
               #?(:clj  (Exception. "boom!")
                  :cljs (js/Error. "boom!"))
               (sut/as-response nil :error)
               (sut/as-response 42 :error)
               (sut/as-busy)
               (sut/as-busy 42)]]
      (is (true? (sut/anomaly? x)))))


  (testing "expected non anomalies"
    (doseq [x [:not-error
               ::error
               42
               nil
               #?(:clj  (Object.)
                  :cljs (js/Object.))
               (sut/as-response nil :not-error)
               (sut/as-response 42 :not-error)
               (sut/as-created)
               (sut/as-created 42)]]
      (is (false? (sut/anomaly? x))))))



(def boom!
  (constantly :error))


(deftest thread-first-macro-test
  (is (= 42 (sut/-> 42)))
  (is (= 43 (sut/-> 42 inc)))
  (is (= :error (sut/-> 42 inc boom!)))
  (is (= :error (sut/-> 42 inc boom! inc)))
  (is (= (sut/as-incorrect 43) (sut/-> 42 inc sut/as-created sut/as-incorrect inc))))


(deftest thread-last-macro-test
  (is (= 42 (sut/->> 42)))
  (is (= 43 (sut/->> 42 inc)))
  (is (= :error (sut/->> 42 inc boom!)))
  (is (= :error (sut/->> 42 inc boom! inc)))
  (is (= (sut/as-incorrect 43) (sut/->> 42 inc sut/as-created sut/as-incorrect inc))))



(deftest response-test
  (testing "response?"
    (testing "expected non responses"
      (doseq [x [nil "foo" :foo 42 #?(:clj (Object.) :cljs (js/Object.)) #?(:clj (Exception. "boom!") :cljs (js/Error. "boom!"))]]
        (is (false? (sut/response? x)))))


    (testing "expected responses"
      (doseq [x [(sut/as-error) (sut/as-success)]]
        (is (true? (sut/response? x))))))



  (testing "success response"
    (testing "destructuring as collection"
      (let [expected-type ::success
            expected-data {:a 42}
            expected      [expected-type expected-data]
            [type data :as actual] (sut/as-response expected-data expected-type)]
        (is (= false (sut/anomaly? actual) (sut/anomaly? type)))
        (is (= expected [type data]))
        (is (= expected-type type (:type actual) (get actual :type) (get actual 0) (nth actual 0)))
        (is (= expected-data data (:data actual) (get actual :data) (get actual 1) (nth actual 1)))
        (is (= :not-found (get actual ::bad-key :not-found) (get actual 2 :not-found) (nth actual 2 :not-found)))
        (is (= ::foo (-> actual (assoc :type ::foo) :type) (-> actual (assoc 0 ::foo) :type)))
        (is (= {:foo :bar} (-> actual (assoc :data {:foo :bar}) :data) (-> actual (assoc 1 {:foo :bar}) :data)))
        (is (= (assoc expected-data :foo :bar) (-> actual (assoc-in [:data :foo] :bar) :data) (-> actual (assoc-in [1 :foo] :bar) :data)))
        (is (= (update expected-data :a inc) (-> actual (update-in [:data :a] inc) :data) (-> actual (update-in [1 :a] inc) :data)))
        (is (= {:foo :bar} (meta (with-meta actual {:foo :bar}))))
        (is (= 2 (count actual)))
        (is (every? #(contains? actual %) [:type :data]))
        (is (= (sut/as-response expected-data expected-type) actual))
        (is (= #{actual} (into #{} [(sut/as-response expected-data expected-type) actual])))
        (is (= "[:success nil]" (str (sut/as-success)) (str (sut/as-success nil))))
        (is (= "[:success nil]" (str (with-meta (sut/as-success) {:foo :bar})) (str (with-meta (sut/as-success nil) {:foo :bar}))))
        (is (= "#tenet [:success nil]" (pr-str (sut/as-success)) (pr-str (sut/as-success nil))))
        (is (= "#tenet [:success nil]" (pr-str (with-meta (sut/as-success) {:foo :bar})) (pr-str (with-meta (sut/as-success nil) {:foo :bar}))))
        (is (= "[:tenet.core-test/success {:a 42}]" (str expected) (str actual)))
        (is (= "[:tenet.core-test/success {:a 42}]" (str (with-meta expected {:foo :bar})) (str (with-meta actual {:foo :bar}))))
        (is (= "#tenet [:tenet.core-test/success {:a 42}]" (pr-str actual) (pr-str (with-meta actual {:foo :bar}))))
        #?@(:clj
            [(is (= (.hashCode expected) (.hashCode actual)))
             (is (thrown? IndexOutOfBoundsException (nth actual 42)))
             (is (thrown-with-msg? IllegalArgumentException #"Response has no field for key - `nil`" (assoc actual nil 42)))
             (is (thrown-with-msg? IllegalArgumentException #"Response has no field for key - `:bad-key`" (assoc actual :bad-key 42)))]
            :cljs
            [(is (= (-hash expected) (-hash actual)))
             (is (thrown-with-msg? js/Error #"Index out of bounds" (nth actual 42)))
             (is (thrown-with-msg? js/Error #"Response has no field for key - `nil`" (assoc actual nil 42)))
             (is (thrown-with-msg? js/Error #"Response has no field for key - `:bad-key`" (assoc actual :bad-key 42)))])))


    (testing "destructuring as map"
      (let [expected-type ::success
            expected-data {:a 42}
            expected      [expected-type expected-data]
            {:as actual :keys [type data]} (sut/as-response expected-data expected-type)]
        (is (= false (sut/anomaly? actual) (sut/anomaly? type)))
        (is (= expected [type data]))
        (is (= expected-type type (:type actual) (get actual :type) (get actual 0) (nth actual 0)))
        (is (= expected-data data (:data actual) (get actual :data) (get actual 1) (nth actual 1)))
        (is (= :not-found (get actual ::bad-key :not-found) (get actual 2 :not-found) (nth actual 2 :not-found)))
        (is (= ::foo (-> actual (assoc :type ::foo) :type) (-> actual (assoc 0 ::foo) :type)))
        (is (= {:foo :bar} (-> actual (assoc :data {:foo :bar}) :data) (-> actual (assoc 1 {:foo :bar}) :data)))
        (is (= (assoc expected-data :foo :bar) (-> actual (assoc-in [:data :foo] :bar) :data) (-> actual (assoc-in [1 :foo] :bar) :data)))
        (is (= (update expected-data :a inc) (-> actual (update-in [:data :a] inc) :data) (-> actual (update-in [1 :a] inc) :data)))
        (is (= {:foo :bar} (meta (with-meta actual {:foo :bar}))))
        (is (= 2 (count actual)))
        (is (every? #(contains? actual %) [:type :data]))
        (is (= (sut/as-response expected-data expected-type) actual))
        (is (= #{actual} (into #{} [(sut/as-response expected-data expected-type) actual])))
        (is (= "[:success nil]" (str (sut/as-success)) (str (sut/as-success nil))))
        (is (= "[:success nil]" (str (with-meta (sut/as-success) {:foo :bar})) (str (with-meta (sut/as-success nil) {:foo :bar}))))
        (is (= "#tenet [:success nil]" (pr-str (sut/as-success)) (pr-str (sut/as-success nil))))
        (is (= "#tenet [:success nil]" (pr-str (with-meta (sut/as-success) {:foo :bar})) (pr-str (with-meta (sut/as-success nil) {:foo :bar}))))
        (is (= "[:tenet.core-test/success {:a 42}]" (str expected) (str actual)))
        (is (= "[:tenet.core-test/success {:a 42}]" (str (with-meta expected {:foo :bar})) (str (with-meta actual {:foo :bar}))))
        (is (= "#tenet [:tenet.core-test/success {:a 42}]" (pr-str actual) (pr-str (with-meta actual {:foo :bar}))))
        #?@(:clj
            [(is (= (.hashCode expected) (.hashCode actual)))
             (is (thrown? IndexOutOfBoundsException (nth actual 42)))
             (is (thrown-with-msg? IllegalArgumentException #"Response has no field for key - `nil`" (assoc actual nil 42)))
             (is (thrown-with-msg? IllegalArgumentException #"Response has no field for key - `:bad-key`" (assoc actual :bad-key 42)))]
            :cljs
            [(is (= (-hash expected) (-hash actual)))
             (is (thrown-with-msg? js/Error #"Index out of bounds" (nth actual 42)))
             (is (thrown-with-msg? js/Error #"Response has no field for key - `nil`" (assoc actual nil 42)))
             (is (thrown-with-msg? js/Error #"Response has no field for key - `:bad-key`" (assoc actual :bad-key 42)))]))))



  (testing "error response"
    (testing "destructuring as collection"
      (let [expected-type :incorrect
            expected-data {:a 42}
            expected      [expected-type expected-data]
            [type data :as actual] (sut/as-response expected-data expected-type)]
        (is (= true (sut/anomaly? actual) (sut/anomaly? type)))
        (is (= expected [type data]))
        (is (= expected-type type (:type actual) (get actual :type) (get actual 0) (nth actual 0)))
        (is (= expected-data data (:data actual) (get actual :data) (get actual 1) (nth actual 1)))
        (is (= :not-found (get actual ::bad-key :not-found) (get actual 2 :not-found) (nth actual 2 :not-found)))
        (is (= ::foo (-> actual (assoc :type ::foo) :type) (-> actual (assoc 0 ::foo) :type)))
        (is (= {:foo :bar} (-> actual (assoc :data {:foo :bar}) :data) (-> actual (assoc 1 {:foo :bar}) :data)))
        (is (= (assoc expected-data :foo :bar) (-> actual (assoc-in [:data :foo] :bar) :data) (-> actual (assoc-in [1 :foo] :bar) :data)))
        (is (= (update expected-data :a inc) (-> actual (update-in [:data :a] inc) :data) (-> actual (update-in [1 :a] inc) :data)))
        (is (= {:foo :bar} (meta (with-meta actual {:foo :bar}))))
        (is (= 2 (count actual)))
        (is (every? #(contains? actual %) [:type :data]))
        (is (= (sut/as-response expected-data expected-type) actual))
        (is (= #{actual} (into #{} [(sut/as-response expected-data expected-type) actual])))
        (is (= "[:incorrect nil]" (str (sut/as-incorrect)) (str (sut/as-incorrect nil))))
        (is (= "[:incorrect nil]" (str (with-meta (sut/as-incorrect) {:foo :bar})) (str (with-meta (sut/as-incorrect nil) {:foo :bar}))))
        (is (= "#tenet [:incorrect nil]" (pr-str (sut/as-incorrect)) (pr-str (sut/as-incorrect nil))))
        (is (= "#tenet [:incorrect nil]" (pr-str (with-meta (sut/as-incorrect) {:foo :bar})) (pr-str (with-meta (sut/as-incorrect nil) {:foo :bar}))))
        (is (= "[:incorrect {:a 42}]" (str expected) (str actual)))
        (is (= "[:incorrect {:a 42}]" (str (with-meta expected {:foo :bar})) (str (with-meta actual {:foo :bar}))))
        (is (= "#tenet [:incorrect {:a 42}]" (pr-str actual) (pr-str (with-meta actual {:foo :bar}))))
        #?@(:clj
            [(is (= (.hashCode expected) (.hashCode actual)))
             (is (thrown? IndexOutOfBoundsException (nth actual 42)))
             (is (thrown-with-msg? IllegalArgumentException #"Response has no field for key - `nil`" (assoc actual nil 42)))
             (is (thrown-with-msg? IllegalArgumentException #"Response has no field for key - `:bad-key`" (assoc actual :bad-key 42)))]
            :cljs
            [(is (= (-hash expected) (-hash actual)))
             (is (thrown-with-msg? js/Error #"Index out of bounds" (nth actual 42)))
             (is (thrown-with-msg? js/Error #"Response has no field for key - `nil`" (assoc actual nil 42)))
             (is (thrown-with-msg? js/Error #"Response has no field for key - `:bad-key`" (assoc actual :bad-key 42)))])))


    (testing "destructuring as map"
      (let [expected-type :incorrect
            expected-data {:a 42}
            expected      [expected-type expected-data]
            {:as actual :keys [type data]} (sut/as-response expected-data expected-type)]
        (is (= true (sut/anomaly? actual) (sut/anomaly? type)))
        (is (= expected [type data]))
        (is (= expected-type type (:type actual) (get actual :type) (get actual 0) (nth actual 0)))
        (is (= expected-data data (:data actual) (get actual :data) (get actual 1) (nth actual 1)))
        (is (= :not-found (get actual ::bad-key :not-found) (get actual 2 :not-found) (nth actual 2 :not-found)))
        (is (= ::foo (-> actual (assoc :type ::foo) :type) (-> actual (assoc 0 ::foo) :type)))
        (is (= {:foo :bar} (-> actual (assoc :data {:foo :bar}) :data) (-> actual (assoc 1 {:foo :bar}) :data)))
        (is (= (assoc expected-data :foo :bar) (-> actual (assoc-in [:data :foo] :bar) :data) (-> actual (assoc-in [1 :foo] :bar) :data)))
        (is (= (update expected-data :a inc) (-> actual (update-in [:data :a] inc) :data) (-> actual (update-in [1 :a] inc) :data)))
        (is (= {:foo :bar} (meta (with-meta actual {:foo :bar}))))
        (is (= 2 (count actual)))
        (is (every? #(contains? actual %) [:type :data]))
        (is (= (sut/as-response expected-data expected-type) actual))
        (is (= #{actual} (into #{} [(sut/as-response expected-data expected-type) actual])))
        (is (= "[:incorrect nil]" (str (sut/as-incorrect)) (str (sut/as-incorrect nil))))
        (is (= "[:incorrect nil]" (str (with-meta (sut/as-incorrect) {:foo :bar})) (str (with-meta (sut/as-incorrect nil) {:foo :bar}))))
        (is (= "#tenet [:incorrect nil]" (pr-str (sut/as-incorrect)) (pr-str (sut/as-incorrect nil))))
        (is (= "#tenet [:incorrect nil]" (pr-str (with-meta (sut/as-incorrect) {:foo :bar})) (pr-str (with-meta (sut/as-incorrect nil) {:foo :bar}))))
        (is (= "[:incorrect {:a 42}]" (str expected) (str actual)))
        (is (= "[:incorrect {:a 42}]" (str (with-meta expected {:foo :bar})) (str (with-meta actual {:foo :bar}))))
        (is (= "#tenet [:incorrect {:a 42}]" (pr-str actual) (pr-str (with-meta actual {:foo :bar}))))
        #?@(:clj
            [(is (= (.hashCode expected) (.hashCode actual)))
             (is (thrown? IndexOutOfBoundsException (nth actual 42)))
             (is (thrown-with-msg? IllegalArgumentException #"Response has no field for key - `nil`" (assoc actual nil 42)))
             (is (thrown-with-msg? IllegalArgumentException #"Response has no field for key - `:bad-key`" (assoc actual :bad-key 42)))]
            :cljs
            [(is (= (-hash expected) (-hash actual)))
             (is (thrown-with-msg? js/Error #"Index out of bounds" (nth actual 42)))
             (is (thrown-with-msg? js/Error #"Response has no field for key - `nil`" (assoc actual nil 42)))
             (is (thrown-with-msg? js/Error #"Response has no field for key - `:bad-key`" (assoc actual :bad-key 42)))]))))



  (testing "error response (derived)"
    (derive ::error ::sut/error)
    (testing "destructuring as collection"
      (let [expected-type ::error
            expected-data {:a 42}
            expected      [expected-type expected-data]
            [type data :as actual] (sut/as-response expected-data expected-type)]
        (is (= true (sut/anomaly? actual) (sut/anomaly? type)))
        (is (= expected [type data]))
        (is (= expected-type type (:type actual) (get actual :type) (get actual 0) (nth actual 0)))
        (is (= expected-data data (:data actual) (get actual :data) (get actual 1) (nth actual 1)))
        (is (= :not-found (get actual ::bad-key :not-found) (get actual 2 :not-found) (nth actual 2 :not-found)))
        (is (= ::foo (-> actual (assoc :type ::foo) :type) (-> actual (assoc 0 ::foo) :type)))
        (is (= {:foo :bar} (-> actual (assoc :data {:foo :bar}) :data) (-> actual (assoc 1 {:foo :bar}) :data)))
        (is (= (assoc expected-data :foo :bar) (-> actual (assoc-in [:data :foo] :bar) :data) (-> actual (assoc-in [1 :foo] :bar) :data)))
        (is (= (update expected-data :a inc) (-> actual (update-in [:data :a] inc) :data) (-> actual (update-in [1 :a] inc) :data)))
        (is (= {:foo :bar} (meta (with-meta actual {:foo :bar}))))
        (is (= 2 (count actual)))
        (is (every? #(contains? actual %) [:type :data]))
        (is (= (sut/as-response expected-data expected-type) actual))
        (is (= #{actual} (into #{} [(sut/as-response expected-data expected-type) actual])))
        (is (= "[:error nil]" (str (sut/as-error)) (str (sut/as-error nil))))
        (is (= "[:error nil]" (str (with-meta (sut/as-error) {:foo :bar})) (str (with-meta (sut/as-error nil) {:foo :bar}))))
        (is (= "#tenet [:error nil]" (pr-str (sut/as-error)) (pr-str (sut/as-error nil))))
        (is (= "#tenet [:error nil]" (pr-str (with-meta (sut/as-error) {:foo :bar})) (pr-str (with-meta (sut/as-error nil) {:foo :bar}))))
        (is (= "[:tenet.core-test/error {:a 42}]" (str expected) (str actual)))
        (is (= "[:tenet.core-test/error {:a 42}]" (str (with-meta expected {:foo :bar})) (str (with-meta actual {:foo :bar}))))
        (is (= "#tenet [:tenet.core-test/error {:a 42}]" (pr-str actual) (pr-str (with-meta actual {:foo :bar}))))
        #?@(:clj
            [(is (= (.hashCode expected) (.hashCode actual)))
             (is (thrown? IndexOutOfBoundsException (nth actual 42)))
             (is (thrown-with-msg? IllegalArgumentException #"Response has no field for key - `nil`" (assoc actual nil 42)))
             (is (thrown-with-msg? IllegalArgumentException #"Response has no field for key - `:bad-key`" (assoc actual :bad-key 42)))]
            :cljs
            [(is (= (-hash expected) (-hash actual)))
             (is (thrown-with-msg? js/Error #"Index out of bounds" (nth actual 42)))
             (is (thrown-with-msg? js/Error #"Response has no field for key - `nil`" (assoc actual nil 42)))
             (is (thrown-with-msg? js/Error #"Response has no field for key - `:bad-key`" (assoc actual :bad-key 42)))])))


    (testing "destructuring as map"
      (let [expected-type ::error
            expected-data {:a 42}
            expected      [expected-type expected-data]
            {:as actual :keys [type data]} (sut/as-response expected-data expected-type)]
        (is (= true (sut/anomaly? actual) (sut/anomaly? type)))
        (is (= expected [type data]))
        (is (= expected-type type (:type actual) (get actual :type) (get actual 0) (nth actual 0)))
        (is (= expected-data data (:data actual) (get actual :data) (get actual 1) (nth actual 1)))
        (is (= :not-found (get actual ::bad-key :not-found) (get actual 2 :not-found) (nth actual 2 :not-found)))
        (is (= ::foo (-> actual (assoc :type ::foo) :type) (-> actual (assoc 0 ::foo) :type)))
        (is (= {:foo :bar} (-> actual (assoc :data {:foo :bar}) :data) (-> actual (assoc 1 {:foo :bar}) :data)))
        (is (= (assoc expected-data :foo :bar) (-> actual (assoc-in [:data :foo] :bar) :data) (-> actual (assoc-in [1 :foo] :bar) :data)))
        (is (= (update expected-data :a inc) (-> actual (update-in [:data :a] inc) :data) (-> actual (update-in [1 :a] inc) :data)))
        (is (= {:foo :bar} (meta (with-meta actual {:foo :bar}))))
        (is (= 2 (count actual)))
        (is (every? #(contains? actual %) [:type :data]))
        (is (= (sut/as-response expected-data expected-type) actual))
        (is (= #{actual} (into #{} [(sut/as-response expected-data expected-type) actual])))
        (is (= "[:error nil]" (str (sut/as-error)) (str (sut/as-error nil))))
        (is (= "[:error nil]" (str (with-meta (sut/as-error) {:foo :bar})) (str (with-meta (sut/as-error nil) {:foo :bar}))))
        (is (= "#tenet [:error nil]" (pr-str (sut/as-error)) (pr-str (sut/as-error nil))))
        (is (= "#tenet [:error nil]" (pr-str (with-meta (sut/as-error) {:foo :bar})) (pr-str (with-meta (sut/as-error nil) {:foo :bar}))))
        (is (= "[:tenet.core-test/error {:a 42}]" (str expected) (str actual)))
        (is (= "[:tenet.core-test/error {:a 42}]" (str (with-meta expected {:foo :bar})) (str (with-meta actual {:foo :bar}))))
        (is (= "#tenet [:tenet.core-test/error {:a 42}]" (pr-str actual) (pr-str (with-meta actual {:foo :bar}))))
        #?@(:clj
            [(is (= (.hashCode expected) (.hashCode actual)))
             (is (thrown? IndexOutOfBoundsException (nth actual 42)))
             (is (thrown-with-msg? IllegalArgumentException #"Response has no field for key - `nil`" (assoc actual nil 42)))
             (is (thrown-with-msg? IllegalArgumentException #"Response has no field for key - `:bad-key`" (assoc actual :bad-key 42)))]
            :cljs
            [(is (= (-hash expected) (-hash actual)))
             (is (thrown-with-msg? js/Error #"Index out of bounds" (nth actual 42)))
             (is (thrown-with-msg? js/Error #"Response has no field for key - `nil`" (assoc actual nil 42)))
             (is (thrown-with-msg? js/Error #"Response has no field for key - `:bad-key`" (assoc actual :bad-key 42)))])))
    (underive ::error ::sut/error))



  (testing "parse response from string"
    (let [opts {:readers {'tenet sut/vec->response}}]
      (is (= (sut/as-response 42 :created)
             (parse opts (pr-str (sut/as-response 42 :created)))
             (parse opts "#tenet [:created 42]")
             (parse opts "#tenet[:created 42]")))

      (is (= (sut/as-busy 42)
             (parse opts (pr-str (sut/as-busy 42)))
             (parse opts "#tenet [:busy 42]")
             (parse opts "#tenet[:busy 42]")))))



  (testing "convert vector to response"
    (is (= (sut/as-created 42) (sut/vec->response [:created 42])))
    (is (= (sut/as-busy 42) (sut/vec->response [:busy 42]))))



  (testing "convert map to response"
    (is (= (sut/as-created 42) (sut/map->response {:type :created, :data 42})))
    (is (= (sut/as-busy 42) (sut/map->response {:type :busy, :data 42})))))



(deftest response-builders-test
  (testing "error response builders"
    (is (= true (sut/anomaly? (sut/as-busy)) (sut/anomaly? (sut/as-busy 42))))
    (is (= (sut/as-response nil :busy) (sut/as-busy)))
    (is (= (sut/as-response 42 :busy) (sut/as-busy 42)))

    (is (= true (sut/anomaly? (sut/as-conflict)) (sut/anomaly? (sut/as-conflict 42))))
    (is (= (sut/as-response nil :conflict) (sut/as-conflict)))
    (is (= (sut/as-response 42 :conflict) (sut/as-conflict 42)))

    (is (= true (sut/anomaly? (sut/as-error)) (sut/anomaly? (sut/as-error 42))))
    (is (= (sut/as-response nil :error) (sut/as-error)))
    (is (= (sut/as-response 42 :error) (sut/as-error 42)))

    (is (= true (sut/anomaly? (sut/as-forbidden)) (sut/anomaly? (sut/as-forbidden 42))))
    (is (= (sut/as-response nil :forbidden) (sut/as-forbidden)))
    (is (= (sut/as-response 42 :forbidden) (sut/as-forbidden 42)))

    (is (= true (sut/anomaly? (sut/as-incorrect)) (sut/anomaly? (sut/as-incorrect 42))))
    (is (= (sut/as-response nil :incorrect) (sut/as-incorrect)))
    (is (= (sut/as-response 42 :incorrect) (sut/as-incorrect 42)))

    (is (= true (sut/anomaly? (sut/as-interrupted)) (sut/anomaly? (sut/as-interrupted 42))))
    (is (= (sut/as-response nil :interrupted) (sut/as-interrupted)))
    (is (= (sut/as-response 42 :interrupted) (sut/as-interrupted 42)))

    (is (= true (sut/anomaly? (sut/as-not-found)) (sut/anomaly? (sut/as-not-found 42))))
    (is (= (sut/as-response nil :not-found) (sut/as-not-found)))
    (is (= (sut/as-response 42 :not-found) (sut/as-not-found 42)))

    (is (= true (sut/anomaly? (sut/as-unauthorized)) (sut/anomaly? (sut/as-unauthorized 42))))
    (is (= (sut/as-response nil :unauthorized) (sut/as-unauthorized)))
    (is (= (sut/as-response 42 :unauthorized) (sut/as-unauthorized 42)))

    (is (= true (sut/anomaly? (sut/as-unavailable)) (sut/anomaly? (sut/as-unavailable 42))))
    (is (= (sut/as-response nil :unavailable) (sut/as-unavailable)))
    (is (= (sut/as-response 42 :unavailable) (sut/as-unavailable 42)))

    (is (= true (sut/anomaly? (sut/as-unsupported)) (sut/anomaly? (sut/as-unsupported 42))))
    (is (= (sut/as-response nil :unsupported) (sut/as-unsupported)))
    (is (= (sut/as-response 42 :unsupported) (sut/as-unsupported 42))))


  (testing "success response builders"
    (is (= false (sut/anomaly? (sut/as-accepted)) (sut/anomaly? (sut/as-accepted 42))))
    (is (= (sut/as-response nil :accepted) (sut/as-accepted)))
    (is (= (sut/as-response 42 :accepted) (sut/as-accepted 42)))

    (is (= false (sut/anomaly? (sut/as-created)) (sut/anomaly? (sut/as-created 42))))
    (is (= (sut/as-response nil :created) (sut/as-created)))
    (is (= (sut/as-response 42 :created) (sut/as-created 42)))

    (is (= false (sut/anomaly? (sut/as-deleted)) (sut/anomaly? (sut/as-deleted 42))))
    (is (= (sut/as-response nil :deleted) (sut/as-deleted)))
    (is (= (sut/as-response 42 :deleted) (sut/as-deleted 42)))

    (is (= false (sut/anomaly? (sut/as-found)) (sut/anomaly? (sut/as-found 42))))
    (is (= (sut/as-response nil :found) (sut/as-found)))
    (is (= (sut/as-response 42 :found) (sut/as-found 42)))

    (is (= false (sut/anomaly? (sut/as-success)) (sut/anomaly? (sut/as-success 42))))
    (is (= (sut/as-response nil :success) (sut/as-success)))
    (is (= (sut/as-response 42 :success) (sut/as-success 42)))

    (is (= false (sut/anomaly? (sut/as-updated)) (sut/anomaly? (sut/as-updated 42))))
    (is (= (sut/as-response nil :updated) (sut/as-updated)))
    (is (= (sut/as-response 42 :updated) (sut/as-updated 42)))))
