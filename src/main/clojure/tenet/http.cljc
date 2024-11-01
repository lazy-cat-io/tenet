(ns tenet.http
  (:refer-clojure :exclude [derive underive])
  (:require
   [tenet.proto :as r]))

(def statuses
  {::continue 100
   ::switching-protocols 101
   ::processing 102
   ::early-hints 103
   ::ok 200
   ::created 201
   ::accepted 202
   ::non-authoritative-information 203
   ::no-content 204
   ::reset-content 205
   ::partial-content 206
   ::multi-status 207
   ::already-reported 208
   ::im-used 226
   ::multiple-choice 300
   ::moved-permanently 301
   ::found 302
   ::see-other 303
   ::not-modified 304
   ::use-proxy 305
   ::switch-proxy 306 ; Unused - This response code is no longer used; it is just reserved. It was used in a previous version of the HTTP/1.1 specification.
   ::temporary-redirect 307
   ::permanent-redirect 308
   ::bad-request 400
   ::unauthorized 401
   ::payment-required 402
   ::forbidden 403
   ::not-found 404
   ::method-not-allowed 405
   ::not-acceptable 406
   ::proxy-authentication-required 407
   ::request-timeout 408
   ::conflict 409
   ::gone 410
   ::length-required 411
   ::precondition-failed 412
   ::payload-too-large 413
   ::uri-too-long 414
   ::unsupported-media-type 415
   ::range-not-satisfiable 416
   ::expectation-failed 417
   ::im-a-teapot 418
   ::misdirected-request 421
   ::unprocessable-entity 422
   ::locked 423
   ::failed-dependency 424
   ::too-early 425
   ::upgrade-required 426
   ::precondition-required 428
   ::too-many-requests 429
   ::request-header-fields-too-large 431
   ::unavailable-for-legal-reasons 451
   ::internal-server-error 500
   ::not-implemented 501
   ::bad-gateway 502
   ::service-unavailable 503
   ::gateway-timeout 504
   ::http-version-not-supported 505
   ::variant-also-negotiates 506
   ::insufficient-storage 507
   ::loop-detected 508
   ::not-extended 510
   ::network-authentication-required 511})

(def ^:private mappings
  {nil ::ok
   :tenet.response/error ::internal-server-error})

(defn derive
  [kind parent]
  #?(:clj (alter-var-root #'mappings assoc kind parent)
     :cljs (set! #'mappings (assoc mappings kind parent)))
  kind)

(defn underive
  [kind]
  #?(:clj (alter-var-root #'mappings dissoc kind)
     :cljs (set! #'mappings (dissoc mappings kind)))
  kind)

(defn status
  [x]
  (->> (r/kind x)
       (get mappings)
       (get statuses)))
