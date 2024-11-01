(ns tenet.proto)

(defprotocol Response
  :extend-via-metadata true
  (kind [this]))

(defprotocol Builder
  :extend-via-metadata true
  (as [this kind]))
