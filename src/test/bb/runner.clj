#!/usr/bin/env bb

(require
 '[babashka.classpath :as cp]
 '[clojure.test :as t])

(cp/add-classpath "src/main/clojure:src/test/clojure")

(require
 'tenet.response-test
 'tenet.response.http-test)

(def test-results
  (t/run-tests 'tenet.response-test 'tenet.response.http-test))

(let [{:keys [fail error]} test-results]
  (when (pos? (+ fail error))
    (System/exit 1)))
