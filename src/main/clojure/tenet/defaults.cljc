(ns tenet.defaults
  (:require
    [tenet.core :as t])
  #?(:clj
     (:import
       (clojure.lang
         Keyword))))


(extend-type nil
  t/IAnomaly
  (anomaly? [_] false)
  (category [_] nil))


#?(:clj
   (extend-protocol t/IAnomaly
     Object
     (anomaly? [_] false)
     (category [_] nil)

     Exception
     (anomaly? [_] true)
     (category [_] ::t/error)

     Keyword
     (anomaly? [x]
       (or (boolean (@t/*registry x))
           (isa? x ::t/error)))
     (category [x] (when (t/anomaly? x) x)))

   :cljs
   (extend-protocol t/IAnomaly
     default
     (anomaly? [_] false)
     (category [_] nil)

     js/Error
     (anomaly? [_] true)
     (category [_] ::t/error)

     cljs.core/Keyword
     (anomaly? [x]
       (or (boolean (@t/*registry x))
           (isa? x ::t/error)))
     (category [x] (when (t/anomaly? x) x))))
