(ns dashboard.handler
  (:gen-class)
  (:require
	[compojure.core :refer :all]
	[compojure.route :as route]
	[compojure.middleware :refer [remove-trailing-slash]]
	[ring.middleware.defaults :refer [wrap-defaults site-defaults]]
	[ring.middleware.gzip :refer [wrap-gzip]]
	[ring.util.response :as response]
	[ring.adapter.jetty :as jetty]
	[dashboard.commands-container :as commands-container]
	[dashboard.command-dispatcher :as dispatcher]))


(defn resource-response-as-html [resource]
  (response/content-type
	(response/resource-response resource {:root "public"})
	"text/html"))

(defn response-as-json [body]
  (-> body
	(response/response)
	(response/content-type "application/json")))

(defroutes app-routes
  (context "/dashboard" []
	(GET "/" [] (resource-response-as-html "index.html"))
	(GET "/run/:section/:command" [section command] (response-as-json (dispatcher/dispatch {:command-type :dashboard
																							:section      section
																							:command      command})))
	(GET "/commands" [] (response-as-json (commands-container/prepare-commands-from-coll :dashboard)))
	(route/resources "/")
	(route/not-found "Page not found."))

  (context "/advanced" []
	(GET "/" [] (resource-response-as-html "advanced.html"))
	(GET "/run/:section/:command" [section command] (response-as-json (dispatcher/dispatch {:command-type :advanced
																							:section      section
																							:command      command})))
	(GET "/commands" [] (response-as-json (commands-container/prepare-commands-from-coll :advanced)))
	(route/not-found "File not found."))

  (context "/*" []
	(GET "/" [] (response/redirect "/dashboard"))))

(def app (-> (wrap-defaults app-routes site-defaults)
		   (wrap-gzip)))

(defn -main [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "8080"))]
	(jetty/run-jetty app {:port         port
						  :join?        false
						  :ssl?         true
						  :ssl-port     8443
						  :keystore     "resources/conf/keystore"
						  :key-password "u1t4i8l4ity"})))