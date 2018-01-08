(ns dashboard.handler
  (:gen-class)
  (:require
    [compojure.core :refer :all]
    [compojure.route :as route]
    [compojure.middleware :refer [remove-trailing-slash]]
    [ring.middleware.defaults :as ring-defaults :refer [wrap-defaults site-defaults]]
    [ring.middleware.json :refer [wrap-json-params]]
    [ring.middleware.session :refer [wrap-session]]
    [ring.middleware.gzip :refer [wrap-gzip]]
    [ring.middleware.anti-forgery :refer [*anti-forgery-token* wrap-anti-forgery]]
    [ring.util.response :as response]
    [ring.util.http-response :as http-response]
    [ring.adapter.jetty :as jetty]
    [buddy.auth.middleware :as auth-middleware :refer [wrap-authentication wrap-authorization]]
    [buddy.auth.backends :as backends]
    [buddy.auth.accessrules :refer [restrict wrap-access-rules]]
    [buddy.auth :as auth :refer [authenticated? throw-unauthorized]]
    [dashboard.dispatcher :as dispatcher]
    [selmer.parser :as parser]
    [clojure.data.json :as json]))

(parser/set-resource-path! (clojure.java.io/resource "public/"))
(declare ^:dynamic *identity*)
(def backend (backends/session))
(defn wrap-identity [handler]
  (fn [request]
    (binding [*identity* (get-in request [:session :identity])]
      (handler request))))

(defn resource-response-as-html [resource]
  (-> resource
      (parser/render-file {:user *identity* :csrf-token *anti-forgery-token*})
      (response/response)
      (response/content-type "text/html; charset=utf-8")))

(defn ok-response-as-json [body]
  (-> body
      (http-response/ok)
      (response/content-type "application/json")))

(defn handle-get-commands-request [{request :params}]
  (ok-response-as-json (dispatcher/dispatch-get-commands request)))

(defn handle-run-command-request [{command-request :params}]
  (-> command-request
      (dispatcher/dispatch-run-command)
      (ok-response-as-json)))

(defn handle-authentication-request
  [{{credentials :credentials} :params :as request}]
  (try (-> (dispatcher/dispatch-authentication credentials)
           (:body)
           (ok-response-as-json)
           (assoc :session (assoc (:session request) :identity {:authenticated? true})))
       (catch Exception ex    ;; TODO: Add special handling to account for unreachable authenticator?
         (http-response/unauthorized))))

(defn handle-logout-request [request]
  (-> (http-response/ok)
      (assoc :session {})))

;;; Routes ;;;
(defn secure-routes
  [& unsecured-routes]
  (-> (apply routes unsecured-routes)
      (restrict {:handler authenticated?})
      (wrap-authorization backend)
      (wrap-authentication backend)))

(defroutes default-routes
  (route/resources "/")
  (route/not-found "Page not found."))

(defroutes home-routes
  (context "/home" []
    (GET "/commands" [] #(handle-get-commands-request (assoc-in % [:params :command-type] :dashboard)))
    (POST "/run" session #(handle-run-command-request (assoc-in % [:params :command-type] :dashboard)))))

(defroutes advanced-routes
  (context "/advanced" []
    (secure-routes
      (GET "/commands" [] #(handle-get-commands-request (assoc-in % [:params :command-type] :advanced)))
      (POST "/run" session #(handle-run-command-request (assoc-in % [:params :command-type] :advanced))))))

(defroutes auth-routes
  (context "/auth" []
    (POST "/login" session handle-authentication-request)
    (POST "/logout" session handle-logout-request)))

(defroutes index-route
  (GET "/" [] (resource-response-as-html "index.html")))

(defn dashboard-routes [& dashboard-routes]
  (wrap-identity (apply routes dashboard-routes)))

(defroutes app-routes
  (context "/dashboard" []
    (dashboard-routes
      index-route
      auth-routes
      home-routes
      advanced-routes
      default-routes))
  (context "/*" []
    (GET "/" [] (response/redirect "/dashboard"))))

(def app (-> app-routes
             ;(wrap-defaults ring-defaults/secure-site-defaults)
             (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false)) ;; dev only
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
                          :key-password (slurp "resources/conf/keystore-password")})))