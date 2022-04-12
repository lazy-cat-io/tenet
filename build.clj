(ns build
  (:refer-clojure :exclude [test])
  (:require
    [org.corfield.build :as bb]
    [tools.project :as tp]))


(def opts
  (tp/project->tools-build-opts (tp/read-project)))


(defn outdated
  [_]
  (bb/run-task opts [:nop :outdated]))


(defn outdated:upgrade
  [_]
  (bb/run-task opts [:nop :outdated :outdated:upgrade]))


(defn clean
  [_]
  (bb/clean opts))


(defn repl
  [_]
  (bb/run-task opts [:test :develop :repl]))


(defn test
  [_]
  (bb/run-task opts [:test]))


(defn bench
  [_]
  (bb/run-task opts [:bench]))


(defn jar
  [_]
  (bb/jar opts))


(defn uber
  [_]
  (bb/uber opts))


(defn install
  [_]
  (bb/install opts))


(defn deploy
  [_]
  (bb/deploy opts))
