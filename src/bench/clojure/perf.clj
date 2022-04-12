(ns perf
  (:require
    [criterium.core :as bench]
    [tenet.core :as t]))


;;
;; Registry
;;

;; mutable registry

(bench/quick-bench
  (@t/*registry :incorrect))


;; Evaluation count : 17869662 in 6 samples of 2978277 calls.
;; Execution time mean : 19.466572 ns
;; Execution time std-deviation : 0.724125 ns
;; Execution time lower quantile : 18.748280 ns ( 2.5%)
;; Execution time upper quantile : 20.571278 ns (97.5%)
;; Overhead used : 14.614236 ns


(bench/quick-bench
  (contains? @t/*registry :incorrect))


;; Evaluation count : 7036314 in 6 samples of 1172719 calls.
;; Execution time mean : 70.796702 ns
;; Execution time std-deviation : 1.314323 ns
;; Execution time lower quantile : 69.141025 ns ( 2.5%)
;; Execution time upper quantile : 72.440566 ns (97.5%)
;; Overhead used : 14.614236 ns



;; cached registry

(def registry @t/*registry)


(bench/quick-bench
  (registry :incorrect))


;; Evaluation count : 20078850 in 6 samples of 3346475 calls.
;; Execution time mean : 16.244425 ns
;; Execution time std-deviation : 1.570610 ns
;; Execution time lower quantile : 15.070701 ns ( 2.5%)
;; Execution time upper quantile : 18.713821 ns (97.5%)
;; Overhead used : 14.614236 ns


(bench/quick-bench
  (contains? registry :not-error))


;; Evaluation count : 8133048 in 6 samples of 1355508 calls.
;; Execution time mean : 62.208276 ns
;; Execution time std-deviation : 2.064624 ns
;; Execution time lower quantile : 59.855678 ns ( 2.5%)
;; Execution time upper quantile : 64.898865 ns (97.5%)
;; Overhead used : 14.614236 ns



;;
;; Hierarchy
;;

(derive ::incorrect ::error)

(isa? ::incorrect ::error) ; => true

(bench/quick-bench
  (isa? ::incorrect ::error))


;; Evaluation count : 3655722 in 6 samples of 609287 calls.
;; Execution time mean : 154.388610 ns
;; Execution time std-deviation : 6.430730 ns
;; Execution time lower quantile : 146.875702 ns ( 2.5%)
;; Execution time upper quantile : 162.042765 ns (97.5%)
;; Overhead used : 14.614236 ns


(isa? ::not-error ::error) ; => false

(bench/quick-bench
  (isa? ::not-error ::error))


;; Evaluation count : 4494510 in 6 samples of 749085 calls.
;; Execution time mean : 124.228944 ns
;; Execution time std-deviation : 4.392843 ns
;; Execution time lower quantile : 120.472880 ns ( 2.5%)
;; Execution time upper quantile : 131.339545 ns (97.5%)
;; Overhead used : 14.614236 ns
