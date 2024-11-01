(ns tenet.proto)

(defprotocol Response
  (kind [this]))

(defprotocol Builder
  (as [this kind]))
