(ns dashboard.handler
  (:gen-class)
  (:require
	[compojure.core :refer :all]
	[compojure.route :as route]
	[compojure.middleware :refer [remove-trailing-slash]]
	[ring.middleware.defaults :refer [wrap-defaults site-defaults]]
	[ring.middleware.json :refer [wrap-json-params]]
	[ring.middleware.session :refer [wrap-session]]
	[ring.middleware.gzip :refer [wrap-gzip]]
	[ring.middleware.anti-forgery :refer [*anti-forgery-token* wrap-anti-forgery]]
	[ring.util.response :as response]
	[ring.util.http-response :as http-response]
	[ring.adapter.jetty :as jetty]
	[buddy.auth.middleware :as auth-middleware :refer [wrap-authentication wrap-authorization]]
	[buddy.auth.backends :as backends]
	[buddy.auth.accessrules :refer [restrict]]
	[buddy.auth :as auth :refer [authenticated? throw-unauthorized]]
	[dashboard.commands-container :as commands-container]
	[dashboard.dispatcher :as dispatcher]
	[dashboard.authenticator :as authenticator]
	[selmer.parser :as parser]
	[clojure.data.json :as json]))

(parser/set-resource-path! (clojure.java.io/resource "public/"))
(declare ^:dynamic *identity*)
(def json "application/json")


(defn resource-response-as-html [resource]
  (response/content-type
	(response/response (parser/render-file resource {:user       *identity*
													 :csrf-token *anti-forgery-token*}))
	"text/html; charset=utf-8"))

(defn ok-response [body]
  (-> body
	(http-response/ok)
	(response/content-type json)))

(defn unauthorized-response
  [{:keys [prepared-response]}]
  (-> prepared-response
	(http-response/unauthorized)
	(response/content-type json)))

(defn handle-get-commands-request
  [request
   command-type]
  (ok-response (commands-container/get-commands-for-type command-type)))

(defn handle-run-command-request
  [{section-command :params
	session         :session :as req}
   command-type]
  (-> section-command
	(assoc :command-type command-type)
	(dispatcher/dispatch)
	(ok-response)))

(defn handle-authentication-request
  [request]
  (let [auth-result (dispatcher/dispatch-authentication (get-in request [:params :credentials]))]
	(when-not auth-result
	  (throw-unauthorized))
	(-> (json/write-str {:user (:user auth-result)})
	  (http-response/ok)
	  (assoc :session (assoc (:session request) :identity {:authenticated? true})))))

(defn handle-logout-request
  [request]
  (-> request
	(http-response/ok)
	(assoc :session {})))

(defn authenticate-user
  [req])

(def backend (backends/session))
(defn wrap-identity [handler]
  (fn [request]
	(binding [*identity* (get-in request [:session :identity])]
	  (handler request))))

;;; Routes ;;;

(defroutes home-routes
  (context "/home" []
	(GET "/commands" [] #(handle-get-commands-request % :dashboard))
	(POST "/run" session #(handle-run-command-request % :dashboard))))

(defroutes admin-routes
  (context "/advanced" []
	(-> (routes
		  (restrict (GET "/commands" [] #(handle-get-commands-request % :advanced))
			{:handler authenticator/authenticated-user})
		  (restrict (POST "/run" session #(handle-run-command-request % :advanced))
			{:handler authenticator/authenticated-user}))
	  (wrap-authorization backend)
	  (wrap-authentication backend))))

(defroutes auth-routes
  (context "/auth" []
	(POST "/login" session handle-authentication-request)
	(POST "/logout" session handle-logout-request)))

(defroutes app-routes
  (context "/dashboard" []
	(-> (routes
		  (GET "/" [] (resource-response-as-html "index.html"))
		  auth-routes
		  home-routes
		  admin-routes

		  (route/resources "/")
		  (route/not-found "Page not found."))
	  wrap-identity))
  (context "/*" []
	(GET "/" [] (response/redirect "/dashboard"))))

(def app (-> app-routes
		   (wrap-defaults           ;site-defaults
			 (assoc-in site-defaults [:security :anti-forgery] false) ;; dev only
			 )
		   (wrap-gzip)
		   (wrap-session)
		   (wrap-json-params)))

(defn -main [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "8080"))]
	(jetty/run-jetty app {:port         port
						  :join?        false
						  :ssl?         true
						  :ssl-port     8443
						  :keystore     "resources/conf/keystore"
						  :key-password "u1t4i8l4ity"})))