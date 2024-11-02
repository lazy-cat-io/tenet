(ns tenet.response
  (:refer-clojure :exclude [derive underive])
  (:require
   [tenet.proto :as r])
  #?(:clj
     (:import
      (clojure.lang
       IPersistentSet
       Keyword
       PersistentList
       PersistentVector))))

;;;;
;; Defaults
;;;;

(extend-type nil
  r/Builder
  (as [_ kind] [kind nil])

  r/Response
  (kind [_]))

(extend-type #?(:clj Object :cljs default)
  r/Builder
  (as [obj kind] [kind obj])

  r/Response
  (kind [_]))

(extend-type #?(:clj Throwable :cljs js/Error)
  r/Builder
  (as [e kind] [kind e])

  r/Response
  (kind [_] ::error))

(extend-type #?(:clj Keyword :cljs cljs.core/Keyword)
  r/Builder
  (as [k kind] [kind k])

  r/Response
  (kind [k] k))

#?(:clj
   (extend-type PersistentList
     r/Builder
     (as [xs kind] (into [kind] (rest xs)))

     r/Response
     (kind [xs]
       #?(:bb (first xs)
          :clj (.first xs)))))

(extend-type #?(:clj PersistentVector :cljs cljs.core/PersistentVector)
  r/Builder
  (as [xs kind]
    #?(:bb (assoc xs 0 kind)
       :clj (.assocN xs 0 kind)
       :cljs (-assoc-n xs 0 kind)))

  r/Response
  (kind [xs]
    #?(:bb (nth xs 0)
       :clj (.nth xs 0)
       :cljs (-nth xs 0))))

;;;;
;; Registry
;;;;

(defonce errors
  #{::error})

(defn derive
  [kind]
  #?(:clj (alter-var-root #'errors conj kind)
     :cljs (set! errors (conj errors kind)))
  kind)

(defn underive
  [kind]
  #?(:clj (alter-var-root #'errors disj kind)
     :cljs (set! errors (disj errors kind)))
  kind)

;;;;
;; Response
;;;;

(defn error?
  [x]
  #?(:bb (contains? errors (r/kind x))
     :clj (.contains ^IPersistentSet errors (r/kind x))
     :cljs (boolean (-lookup ^cljs.core/ILookup errors (r/kind x)))))

(defn kind
  [x]
  (r/kind x))

(defn as
  [kind x]
  (r/as x kind))
