(ns user
  "Development helper functions."
  (:require
    [cider.piggieback :as pb]
    [cljs.repl.node :as rn]
    [hashp.core]))


(defmacro jit
  "Just in time loading of dependencies."
  [sym]
  `(requiring-resolve '~sym))


(defn cljs-repl
  "Starts a ClojureScript REPL."
  []
  (pb/cljs-repl (rn/repl-env)))
