(ns tenet.http-test
  (:require
   #?@(:clj  [[clojure.test :refer [deftest testing is]]]
       :cljs [[cljs.test :refer [deftest testing is]]])
   [tenet.http :as sut]
   [tenet.response]))

(def f
  #(sut/wrap-status-middleware (constantly %)))

(deftest wrap-status-middleware-test
  (testing "plain objects as an unified response"
    (is (= {:status 200, :body 42}
           ((f {:status 500, :body 42})
            {}))))

  (testing "vector as unified response"
    (sut/derive ::created ::sut/created)
    (is (= {:status 201, :body [::created 42]}
           ((f {:status 500, :body [::created 42]})
            {})))
    (sut/underive ::created)))
