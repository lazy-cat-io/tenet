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
    (is (= ::created (sut/kind [::created 42])))
    #?(:clj (is (= ::created (sut/kind '(::created 42)))))

    ;; this is the expected behavior
    (is (= 1 (sut/kind [1 2 3])))
    #?(:clj (is (= 1 (sut/kind '(1 2 3)))))))

(deftest as-test
  (let [e #?(:clj (Exception. "boom!")
             :cljs (js/Error. "boom!"))]
    (is (= [::error nil] (sut/as ::error nil)))
    (is (= [::error 42] (sut/as ::error 42)))
    (is (= [::error "42"] (sut/as ::error "42")))
    (is (= [::error ::kw] (sut/as ::error ::kw)))
    (is (= [::error e] (sut/as ::error e)))
    (is (= [::error 42] (sut/as ::error [::created 42])))
    (is (= [::error [1 2 3]] (sut/as ::error [::created [1 2 3]])))
    #?(:clj (is (= [::error 42] (sut/as ::error '(::created 42)))))
    #?(:clj (is (= [::error [1 2 3]] (sut/as ::error '(::created [1 2 3])))))

    ;; this is the expected behavior
    (is (= [::error 2 3] (sut/as ::error [1 2 3])))
    #?(:clj (is (= [::error 2 3] (sut/as ::error '(1 2 3)))))

    (is (= [::created 42]
           (->> 42
                (sut/as ::error)
                (sut/as ::created))))))
