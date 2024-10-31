(ns tenet.proto)

(defprotocol Response
  :extend-via-metadata true
  (kind [this]))
