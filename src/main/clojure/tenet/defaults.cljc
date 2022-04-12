(ns tenet.defaults
  (:require
    [tenet.core :as t])
  #?(:clj
     (:import
       (clojure.lang
         Keyword))))


(extend-type nil
  t/IAnomaly
  (anomaly? [_] false))


#?(:clj
   (extend-protocol t/IAnomaly
     Object
     (anomaly? [_] false)

     Exception
     (anomaly? [_] true)

     Keyword
     (anomaly? [x]
       (or (boolean (@t/*registry x))
           (isa? x ::t/error))))

   :cljs
   (extend-protocol t/IAnomaly
     default
     (anomaly? [_] false)

     js/Error
     (anomaly? [_] true)

     cljs.core/Keyword
     (anomaly? [x]
       (or (boolean (@t/*registry x))
           (isa? x ::t/error)))))
