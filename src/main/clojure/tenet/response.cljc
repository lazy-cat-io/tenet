(ns tenet.response
  (:refer-clojure :exclude [derive underive])
  (:require
   [tenet.proto :as r])
  #?(:clj
     (:import
      (clojure.lang
       Keyword
       PersistentList
       PersistentVector))))

;;;;
;; Defaults
;;;;

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
     (kind [xs] (first xs))

     PersistentVector
     (kind [xs] (first xs)))

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
     (kind [xs] (first xs))))

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
  (contains? errors (r/kind x)))

(defn kind
  [x]
  (r/kind x))
