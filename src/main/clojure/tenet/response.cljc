(ns tenet.response
  (:refer-clojure :exclude [derive underive])
  (:require
   [tenet.proto :as r])
  #?(:clj
     (:import
      (clojure.lang
       Keyword
       PersistentHashSet
       PersistentList
       PersistentVector))))

;;;;
;; Defaults
;;;;

#?(:clj
   (extend-protocol r/Builder
     nil
     (as [_ kind] [kind nil])

     Object
     (as [obj kind] [kind obj])

     Throwable
     (as [e kind] [kind e])

     Keyword
     (as [k kind] [kind k])

     PersistentList
     (as [xs kind] (into [kind] (rest xs)))

     PersistentVector
     (as [xs kind] (.assocN xs 0 kind)))

   :cljs
   (extend-protocol r/Builder
     nil
     (as [_ kind] [kind nil])

     default
     (as [obj kind] [kind obj])

     js/Error
     (as [e kind] [kind e])

     cljs.core/Keyword
     (as [k kind] [kind k])

     cljs.core/PersistentVector
     (as [xs kind] (assoc xs 0 kind))))

#?(:clj
   (extend-protocol r/Response
     nil
     (kind [_])

     Object
     (kind [_])

     Throwable
     (kind [_] ::error)

     Keyword
     (kind [k] k)

     PersistentList
     (kind [xs] (.first xs))

     PersistentVector
     (kind [xs] (.nth xs 0)))

   :cljs
   (extend-protocol r/Response
     nil
     (kind [_])

     default
     (kind [_])

     js/Error
     (kind [_] ::error)

     cljs.core/Keyword
     (kind [k] k)

     cljs.core/PersistentVector
     (kind [xs] (-nth xs 0))))

;;;;
;; Error registry
;;;;

(def ^:private errors
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
;; Public API
;;;;

(defn error?
  [x]
  #?(:clj (.contains ^PersistentHashSet errors (r/kind x))
     :cljs (-lookup ^cljs.core/ILookup errors (r/kind x))))

(defn kind
  [x]
  (r/kind x))

(defn as
  [kind x]
  (r/as x kind))
