(ns tenet.core
  #?(:clj
     (:refer-clojure :exclude [-> ->>]))
  #?(:clj
     (:require
       [clojure.core :as c])
     :cljs
     (:require-macros
       [tenet.core])))


(defprotocol IAnomaly
  (anomaly? [this] "Returns `true` if the given value is an anomaly. Otherwise, `false`."))


(defonce ^{:doc "Registry of anomalies."}
  *registry
  (atom
    #{:error
      :exception
      :unavailable
      :interrupted
      :incorrect
      :unauthorized
      :forbidden
      :not-found
      :unsupported
      :conflict
      :busy}))



#?(:clj
   (defmacro ->
     "This macro is the same as `clojure.core/some->`, but the check is done
     using the predicate `anomaly?` of the `IAnomaly` protocol and
     the substitution occurs as in macro `->` (the `thread-first` macro)."
     [expr & forms]
     (let [g     (gensym)
           steps (map (fn [step]
                        `(let [g# ~g]
                           (if (anomaly? g#) g# (c/-> g# ~step))))
                      forms)]
       `(let [~g ~expr
              ~@(interleave (repeat g) (butlast steps))]
          ~(if (empty? steps)
             g
             (last steps))))))


#?(:clj
   (defmacro ->>
     "This macro is the same as `clojure.core/some->>`, but the check is done
     using the predicate `anomaly?` of the `IAnomaly` protocol and
     the substitution occurs as in macro `->>` (the `thread-last` macro)."
     [expr & forms]
     (let [g     (gensym)
           steps (map (fn [step]
                        `(let [g# ~g]
                           (if (anomaly? g#) g# (c/->> g# ~step))))
                      forms)]
       `(let [~g ~expr
              ~@(interleave (repeat g) (butlast steps))]
          ~(if (empty? steps)
             g
             (last steps))))))
