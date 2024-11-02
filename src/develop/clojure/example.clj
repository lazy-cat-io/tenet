(ns example
  (:require
   [tenet.response :as r]
   [tenet.response.http :as http]))

;;;;
;; Defaults
;;;;

(r/error? nil) ;; => false
(r/error? 42) ;; => false
(r/error? ::error) ;; => false

;; By default, only keyword `:tenet.response/error`, `Throwable` and `js/Error` is considered an error.

;; keyword
(r/error? ::r/error) ;; => true
;; throwable
(r/error? (ex-info "boom!" {})) ;; => true
;; vector using the hiccup syntax
(r/error? [::r/error "Something went wrong"]) ;; => true

;;;;
;; Custom errors
;;;;

(r/error? :example/error) ;; => false

;; Add a custom error kind to the error registry
(r/derive :example/error) ;; => :example/error
(r/error? :example/error) ;; => true

;; Remove a custom error kind from the error registry
(r/underive :example/error) ;; => :example/error

;;;;
;; Responses
;;;;

(declare valid? explain exists? insert!)

;; In this example, we do not require our library, as we can construct the responses without helpers

(defn create-user!
  [user]
  (cond
    (not (valid? user)) [:user/invalid (explain user)] ;; returns a response that the given data is not valid
    (exists? user) [:user/exists user] ;; returns a response that the email is occupied
    :else
    (try
      (let [profile (insert! user)]
        [:user/created profile]) ;; returns a response that a new user has been created
      (catch Exception e
        [:user/not-created e] ;; returns a response indicating that there was a problem writing data to the database
        ))))

;; But we have to register our error kinds

(r/derive :user/invalid) ;; => :user/invalid
(r/derive :user/exists) ;; => :user/exists
(r/derive :user/not-created) ;; => :user/not-created

(r/error? [:user/exists {:user/id 42}]) ;; => true
(r/kind [:user/exists {:user/id 42}]) ;; => :user/exists

;; If necessary, you can change the kind of error to make the correct context
(->> [:db/conflict {:user/id 42}]
     (r/as :user/exists)) ;; => [:user/exists {:user/id 42}]

;;;;
;; Http responses
;;;;

;; With a unified approach to response management, we can easily add mappings to HTTP responses

(http/status 42) ;; => 200
(http/status [:user/exists {:user/id 42}]) ;; => 200

;; By default,
;;   - all unknown non-error response kinds have the status - 200 OK
;;   - all error response kinds have the status - 500 Internal Server Error

;; But we have to add our custom mappings
(http/derive :user/exists ::http/conflict) ;; => :user/exists
(http/status [:user/exists {:user/id 42}]) ;; => 409

;; Namespace `tenet.response.http` contains `wrap-status-middleware' - perhaps this middleware will be useful for you
