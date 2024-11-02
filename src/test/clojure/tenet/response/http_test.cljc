(ns tenet.response.http-test
  (:require
   #?@(:clj  [[clojure.test :refer [deftest testing is]]]
       :cljs [[cljs.test :refer [deftest testing is]]])
   [tenet.response.http :as sut]))

(def handler
  #(sut/wrap-status-middleware (constantly %)))

(def async-handler
  #(sut/wrap-status-middleware
    (fn [_req respond _raise]
      (respond %))))

(deftest wrap-status-middleware-test
  (testing "sync"
    (testing "plain objects as an unified response"
      (is (= {:status 200, :body 42}
             ((handler {:status 500, :body 42})
              {}))))

    (testing "vector as an unified response"
      (let [before {:status 200, :body [::created 42]}
            after {:status 201, :body [::created 42]}]
        (is (= before
               ((handler {:status 500, :body [::created 42]}) {})))
        (sut/derive ::created ::sut/created)
        (is (= after
               ((handler {:status 500, :body [::created 42]}) {})))
        (sut/underive ::created)
        (is (= before
               ((handler {:status 500, :body [::created 42]}) {}))))))

  (testing "async"
    (testing "plain objects as an unified response"
      (is (= {:status 200, :body 42}
             ((async-handler {:status 200, :body 42}) {} identity nil))))

    (testing "vector as an unified response"
      (let [before {:status 200, :body [::created 42]}
            after {:status 201, :body [::created 42]}]
        (is (= before
               ((async-handler {:status 500, :body [::created 42]}) {} identity nil)))
        (sut/derive ::created ::sut/created)
        (is (= after
               ((async-handler {:status 500, :body [::created 42]}) {} identity nil)))
        (sut/underive ::created)
        (is (= before
               ((async-handler {:status 500, :body [::created 42]}) {} identity nil)))))))
