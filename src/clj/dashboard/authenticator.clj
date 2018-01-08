(ns dashboard.authenticator
  (:require [clojure.data.json :as json]
            [clj-http.client :as client]
            [base64-clj.core :as base64]
            [buddy.hashers :as hashers]
            [buddy.auth :refer [authenticated? throw-unauthorized]]))

(defn validate-user
  [uri credentials]
  (:body (client/post uri {:insecure?    true ;; This will change if authentication is not local
                           :conn-timeout 20000
                           :content-type :json
                           :form-params  {:credentials credentials}})))

(defn authenticate
  [credentials]
  (let [decoded-credentials (clojure.string/split (base64/decode credentials) #":")]
    (validate-user "https://localhost:3443/api/auth" decoded-credentials)))

(defn authenticated-user
  [req]
  (when-not (authenticated? req)
    (throw-unauthorized "Authentication is required."))
  true)

(defn run-if-authenticated
  [req event]
  (when (authenticated-user req)
    (event)))