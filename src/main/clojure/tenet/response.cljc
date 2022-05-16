(ns tenet.response
  (:refer-clojure :exclude [format type -> ->>])
  #?@(:clj
      [(:require
         [clojure.core :as c]
         [clojure.pprint :as pprint])
       (:import
         (clojure.lang
           Associative
           IDeref
           ILookup
           Indexed
           IObj
           IPersistentCollection
           Keyword)
         (java.io
           Writer))]
      :cljs
      [(:require
         [cljs.pprint :as pprint]
         [goog.string :as gstr]
         [goog.string.format])
       (:require-macros
         [tenet.response])]))


;;
;; Helper functions
;;

(def format
  "Formats a string."
  #?(:clj  c/format
     :cljs gstr/format))


(def cl-format
  "An implementation of a Common Lisp compatible format function."
  pprint/cl-format)



;;
;; Protocols
;;

(defprotocol IAnomaly
  "Anomaly protocol."
  :extend-via-metadata true
  (anomaly? [this] "Returns `true` if it is an anomaly. Otherwise, `false`."))


(defprotocol IResponseBuilder
  "Response builder protocol."
  :extend-via-metadata true
  (as-response [this type] "Returns a response using the given response type."))


(defprotocol IResponse
  "Response protocol."
  :extend-via-metadata true
  (response? [this] "Returns `true` if it is a response. Otherwise, `false`."))



;;
;; Anomalies
;;

(defonce ^{:doc "Registry of anomalies."}
  *registry
  (atom
    #{:busy
      :conflict
      :error
      :forbidden
      :incorrect
      :interrupted
      :not-found
      :unauthorized
      :unavailable
      :unsupported}))


(extend-protocol IAnomaly
  nil
  (anomaly? [_] false))


#?(:clj
   (extend-protocol IAnomaly
     Object
     (anomaly? [_] false)

     Exception
     (anomaly? [_] true)

     Keyword
     (anomaly? [x]
       (or (boolean (@*registry x))
           (isa? x ::error))))

   :cljs
   (extend-protocol IAnomaly
     default
     (anomaly? [_] false)

     js/Error
     (anomaly? [_] true)

     cljs.core/Keyword
     (anomaly? [x]
       (or (boolean (@*registry x))
           (isa? x ::error)))))



;;
;; Response
;;

;; TODO: [2022-05-16, ilshat@sultanov.team] Doesn't work with babashka (sci) `deftype` is not supported

#?(:clj
   (do
     (deftype Response
       [type data _meta]

       IResponseBuilder
       (as-response [_ new-type]
         (Response. new-type data _meta))

       IResponse
       (response? [_] true)

       IAnomaly
       (anomaly? [_]
         (anomaly? type))

       IObj
       (meta [_] _meta)
       (withMeta [_ new-meta]
         (Response. type data new-meta))

       IDeref
       (deref [_] data)

       Indexed
       (nth [_ idx]
         (case idx
           0 type
           1 data
           (throw (IndexOutOfBoundsException.))))
       (nth [_ idx not-found]
         (case idx
           0 type
           1 data
           not-found))

       ILookup
       (valAt [this key]
         (.valAt this key nil))
       (valAt [_ key not-found]
         (case key
           :type type
           :data data
           not-found))

       Associative
       (containsKey [_ key]
         (or (= :type key) (= :data key)))
       (assoc [_ key new-value]
         (case key
           :type (Response. new-value data _meta)
           :data (Response. type new-value _meta)
           (throw (IllegalArgumentException. ^String (cl-format nil "Response has no field for key - `~s`" key)))))

       IPersistentCollection
       (equiv [_ other]
         (and (response? other)
              (= type (:type other))
              (= data (:data other))
              (= _meta (meta other))))
       (count [_] 2)

       Object
       (toString [_]
         (cl-format nil "[~s ~s]" type data))
       (equals [_ other]
         (and (response? other)
              (= type (:type other))
              (= data (:data other))))
       (hashCode [_]
         (.hashCode [type data])))

     (defmethod print-method Response [response ^Writer writer]
       (.write writer (str "#tenet " response)))

     (defmethod print-dup Response [response ^Writer writer]
       (.write writer (str "#tenet " response))))


   :cljs
   (deftype Response
     [type data _meta]

     IResponseBuilder
     (as-response [_ new-type]
       (Response. new-type data _meta))

     IResponse
     (response? [_] true)

     IAnomaly
     (anomaly? [_]
       (anomaly? type))

     IMeta
     (-meta [_] _meta)

     IWithMeta
     (-with-meta [_ new-meta]
       (Response. type data new-meta))

     IDeref
     (-deref [_] data)

     ICounted
     (-count [_] 2)

     IIndexed
     (-nth [_ idx]
       (case idx
         0 type
         1 data
         (throw (js/Error. "Index out of bounds"))))
     (-nth [_ idx not-found]
       (case idx
         0 type
         1 data
         not-found))

     ILookup
     (-lookup [this key]
       (-lookup this key nil))
     (-lookup [_ key not-found]
       (case key
         :type type
         :data data
         not-found))

     IAssociative
     (-contains-key? [_ key]
       (or (= :type key) (= :data key)))
     (-assoc [_ key new-value]
       (case key
         :type (Response. new-value data _meta)
         :data (Response. type new-value _meta)
         (throw (js/Error. (cl-format nil "Response has no field for key - `~s`" key)))))

     Object
     (toString [_]
       (cl-format nil "[~s ~s]" type data))

     IEquiv
     (-equiv [_ other]
       (and (response? other)
            (= type (:type other))
            (= data (:data other))
            (= _meta (meta other))))

     IHash
     (-hash [_]
       (-hash [type data]))

     IPrintWithWriter
     (-pr-writer [response writer _opts]
       (-write writer (str "#tenet " response)))))


(defn vec->response
  [[type data :as coll]]
  (with-meta (as-response data type) (meta coll)))


(defn map->response
  [{:keys [type data] :as map}]
  (with-meta (as-response data type) (meta map)))


(extend-type nil
  IResponseBuilder
  (as-response [_ type]
    (Response. type nil nil))

  IResponse
  (response? [_] false))


#?(:clj
   (extend-type Object
     IResponseBuilder
     (as-response [x type]
       (Response. type x nil))

     IResponse
     (response? [_] false))

   :cljs
   (extend-type default
     IResponseBuilder
     (as-response [x type]
       (Response. type x nil))

     IResponse
     (response? [_] false)))



;;
;; Helper macros
;;

#?(:clj
   (defn cljs?
     "Checks &env in macro and returns `true` if that cljs env. Otherwise `false`."
     {:added "0.0.6"}
     [env]
     (boolean (:ns env))))


;; TODO: add finally? [body handler finally]
#?(:clj
   (defmacro safe
     "Extended version of try-catch."
     {:added "0.0.6"}
     ([body]
      `(safe ~body nil))

     ([body handler]
      `(try
         ~body
         (catch ~(if-not (cljs? &env) 'Exception :default) error#
           (when-some [handler# ~handler]
             (handler# error#)))))))


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



;;
;; Response builders
;;

;; Common response builder

(defn as
  ([type]
   (as-response nil type))
  ([type data]
   (as-response data type)))


;; Error response builders

(defn as-busy
  ([] (as-response nil :busy))
  ([x] (as-response x :busy)))


(defn as-conflict
  ([] (as-response nil :conflict))
  ([x] (as-response x :conflict)))


(defn as-error
  ([] (as-response nil :error))
  ([x] (as-response x :error)))


(defn as-forbidden
  ([] (as-response nil :forbidden))
  ([x] (as-response x :forbidden)))


(defn as-incorrect
  ([] (as-response nil :incorrect))
  ([x] (as-response x :incorrect)))


(defn as-interrupted
  ([] (as-response nil :interrupted))
  ([x] (as-response x :interrupted)))


(defn as-not-found
  ([] (as-response nil :not-found))
  ([x] (as-response x :not-found)))


(defn as-unauthorized
  ([] (as-response nil :unauthorized))
  ([x] (as-response x :unauthorized)))


(defn as-unavailable
  ([] (as-response nil :unavailable))
  ([x] (as-response x :unavailable)))


(defn as-unsupported
  ([] (as-response nil :unsupported))
  ([x] (as-response x :unsupported)))


;; Success response builders

(defn as-accepted
  ([] (as-response nil :accepted))
  ([x] (as-response x :accepted)))


(defn as-created
  ([] (as-response nil :created))
  ([x] (as-response x :created)))


(defn as-deleted
  ([] (as-response nil :deleted))
  ([x] (as-response x :deleted)))


(defn as-found
  ([] (as-response nil :found))
  ([x] (as-response x :found)))


(defn as-success
  ([] (as-response nil :success))
  ([x] (as-response x :success)))


(defn as-updated
  ([] (as-response nil :updated))
  ([x] (as-response x :updated)))
