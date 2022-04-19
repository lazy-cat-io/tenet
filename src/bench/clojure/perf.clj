(ns perf
  (:require
    [criterium.core :as bench]
    [tenet.core :as t]))


;;
;; Registry
;;

;; mutable registry

(bench/quick-bench
  (boolean (@t/*registry :incorrect))) ; => true


;; Evaluation count : 16074012 in 6 samples of 2679002 calls.
;; Execution time mean : 26.506240 ns
;; Execution time std-deviation : 0.179964 ns
;; Execution time lower quantile : 26.280572 ns ( 2.5%)
;; Execution time upper quantile : 26.690284 ns (97.5%)
;; Overhead used : 10.861229 ns



(bench/quick-bench
  (contains? @t/*registry :incorrect)) ; => true


;; Evaluation count : 7680864 in 6 samples of 1280144 calls.
;; Execution time mean : 68.363034 ns
;; Execution time std-deviation : 1.508964 ns
;; Execution time lower quantile : 66.602688 ns ( 2.5%)
;; Execution time upper quantile : 70.146706 ns (97.5%)
;; Overhead used : 10.861229 ns



;; cached registry

(def registry @t/*registry)


(bench/quick-bench
  (boolean (registry :incorrect))) ; => true


;; Evaluation count : 17192136 in 6 samples of 2865356 calls.
;; Execution time mean : 24.180595 ns
;; Execution time std-deviation : 0.127716 ns
;; Execution time lower quantile : 23.947322 ns ( 2.5%)
;; Execution time upper quantile : 24.275938 ns (97.5%)
;; Overhead used : 10.861229 ns



(bench/quick-bench
  (contains? registry :not-error)) ; => false


;; Evaluation count : 8007564 in 6 samples of 1334594 calls.
;; Execution time mean : 64.311789 ns
;; Execution time std-deviation : 0.963703 ns
;; Execution time lower quantile : 63.063635 ns ( 2.5%)
;; Execution time upper quantile : 65.456035 ns (97.5%)
;; Overhead used : 10.861229 ns



;;
;; Hierarchy
;;

(bench/quick-bench
  (isa? ::error ::error)) ; => true


;; Evaluation count : 34328940 in 6 samples of 5721490 calls.
;; Execution time mean : 6.613871 ns
;; Execution time std-deviation : 0.021800 ns
;; Execution time lower quantile : 6.574481 ns ( 2.5%)
;; Execution time upper quantile : 6.629458 ns (97.5%)
;; Overhead used : 10.861229 ns



(derive ::incorrect ::error)


(bench/quick-bench
  (isa? ::incorrect ::error)) ; => true


;; Evaluation count : 3712800 in 6 samples of 618800 calls.
;; Execution time mean : 155.358881 ns
;; Execution time std-deviation : 9.439643 ns
;; Execution time lower quantile : 150.259546 ns ( 2.5%)
;; Execution time upper quantile : 171.730946 ns (97.5%)
;; Overhead used : 10.861229 ns



(bench/quick-bench
  (isa? ::not-error ::error)) ; => false


;; Evaluation count : 4430190 in 6 samples of 738365 calls.
;; Execution time mean : 124.754985 ns
;; Execution time std-deviation : 0.223896 ns
;; Execution time lower quantile : 124.469531 ns ( 2.5%)
;; Execution time upper quantile : 124.999517 ns (97.5%)
;; Overhead used : 10.861229 ns



(bench/quick-bench
  (= ::t/error ::t/error)) ; => true


;; Evaluation count : 40770312 in 6 samples of 6795052 calls.
;; Execution time mean : 3.891860 ns
;; Execution time std-deviation : 0.015025 ns
;; Execution time lower quantile : 3.876825 ns ( 2.5%)
;; Execution time upper quantile : 3.908882 ns (97.5%)
;; Overhead used : 10.861229 ns
