(ns perf
  (:require
   [criterium.core :as bench]
   [tenet.http :as http]
   [tenet.response :as r]))

;;;;
;; Defaults
;;;;

(bench/quick-bench
 (r/error? ::r/error)) ; => true

; (out) Evaluation count : 20083626 in 6 samples of 3347271 calls.
; (out)              Execution time mean : 14.875154 ns
; (out)     Execution time std-deviation : 0.147461 ns
; (out)    Execution time lower quantile : 14.692583 ns ( 2.5%)
; (out)    Execution time upper quantile : 15.040448 ns (97.5%)
; (out)                    Overhead used : 15.049260 ns

(bench/quick-bench
 (r/error? ::not-error)) ; => false

; (out) Evaluation count : 19714662 in 6 samples of 3285777 calls.
; (out)              Execution time mean : 15.423519 ns
; (out)     Execution time std-deviation : 0.092422 ns
; (out)    Execution time lower quantile : 15.261812 ns ( 2.5%)
; (out)    Execution time upper quantile : 15.515286 ns (97.5%)
; (out)                    Overhead used : 15.049260 ns

;;;;
;; Java objects
;;;;

(bench/quick-bench
 (r/kind 42)) ; => nil

; (out) Evaluation count : 45745500 in 6 samples of 7624250 calls.
; (out)              Execution time mean : -1.919144 ns
; (out)     Execution time std-deviation : 0.014673 ns
; (out)    Execution time lower quantile : -1.935134 ns ( 2.5%)
; (out)    Execution time upper quantile : -1.905225 ns (97.5%)
; (out)                    Overhead used : 15.049260 ns

;;;;
;; Vector
;;;;

(def user-exists
  [:user/exists {:user/id 42}])

(bench/quick-bench
 (r/kind user-exists)) ; => :user/exists

; (out) Evaluation count : 31009008 in 6 samples of 5168168 calls.
; (out)              Execution time mean : 4.626609 ns
; (out)     Execution time std-deviation : 0.274217 ns
; (out)    Execution time lower quantile : 4.281799 ns ( 2.5%)
; (out)    Execution time upper quantile : 4.951825 ns (97.5%)
; (out)                    Overhead used : 15.041038 ns

(r/derive :user/exists)

(bench/quick-bench
 (r/error? user-exists)) ; => true

; (out) Evaluation count : 12798372 in 6 samples of 2133062 calls.
; (out)              Execution time mean : 28.207162 ns
; (out)     Execution time std-deviation : 0.263008 ns
; (out)    Execution time lower quantile : 27.779911 ns ( 2.5%)
; (out)    Execution time upper quantile : 28.468211 ns (97.5%)
; (out)                    Overhead used : 15.048625 ns

(r/underive :user/exists) ; => :user/exists

;;;;
;; Builders
;;;;

(bench/quick-bench
 (r/as ::error 42)) ; => [:perf/error 42]

; (out) Evaluation count : 28434054 in 6 samples of 4739009 calls.
; (out)              Execution time mean : 13.717179 ns
; (out)     Execution time std-deviation : 10.464559 ns
; (out)    Execution time lower quantile : 6.355319 ns ( 2.5%)
; (out)    Execution time upper quantile : 28.825937 ns (97.5%)
; (out)                    Overhead used : 15.041038 ns

(bench/quick-bench
 (r/as ::error user-exists)) ; => [:perf/error {:user/id 42}]

; eval (root-form): (bench/quick-bench (r/as ::error user-exists))
; (out) Evaluation count : 14432754 in 6 samples of 2405459 calls.
; (out)              Execution time mean : 27.933007 ns
; (out)     Execution time std-deviation : 1.615869 ns
; (out)    Execution time lower quantile : 26.703075 ns ( 2.5%)
; (out)    Execution time upper quantile : 30.424757 ns (97.5%)
; (out)                    Overhead used : 15.048625 ns

;;;;
;; Http
;;;;

(r/derive :user/exists) ; => :user/exists
(http/derive :user/exists ::http/conflict) ;; :user/exists
(r/error? user-exists) ; => true

(bench/quick-bench
 (http/status user-exists)) ; => 409

; (out) Evaluation count : 10354914 in 6 samples of 1725819 calls.
; (out)              Execution time mean : 43.376203 ns
; (out)     Execution time std-deviation : 0.175731 ns
; (out)    Execution time lower quantile : 43.201832 ns ( 2.5%)
; (out)    Execution time upper quantile : 43.553214 ns (97.5%)
; (out)                    Overhead used : 15.041038 ns

(r/underive :user/exists) ; => :user/exists
(http/underive :user/exists) ; => :user/exists

(r/error? user-exists) ; => false
