(ns dashboard.authenticator
  (:require [clojure.data.json :as json]
			[base64-clj.core :as base64]
			[buddy.hashers :as hashers]
			[buddy.auth :refer [authenticated? throw-unauthorized]]))


(def temp-db {"kharper@utility.com" {:username "kharper@utility.com"
									 :password (hashers/encrypt "rocket101")}})

(defn prepare-authentication-result [{:keys [user success?] :as auth-result}]
  {:user              user
   :success?          success?
   :prepared-response auth-result})

(defn validate-user [[username password]]
  (if-let [user (get-in temp-db [username])]
	(if (hashers/check password (:password user))
	  {:user (:username user) :success? true})))

(defn authenticate
  [credentials]
  (let [decoded-credentials (clojure.string/split (base64/decode credentials) #":")]
	(validate-user decoded-credentials)))

(defn authenticated-user
  [req]
  (if (authenticated? req)
	true
	(throw-unauthorized "Authentication is required.")))

(defn run-if-authenticated
  [req event]
  (when (authenticated-user req)
	(event)))