(ns clj-dashboard.handler
  (:require
    [compojure.core :refer :all]
    [compojure.route :as route]
    [compojure.middleware :refer [remove-trailing-slash]]
    [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
    [ring.util.response :as response]
    [ring.adapter.jetty :as jetty]
    [clj-dashboard.commands-container :as commands-container]
    [clj-dashboard.command-dispatcher :as dispatcher]))

(defn resource-response-as-html [resource]
  (response/content-type
    (response/resource-response resource {:root "public"})
    "text/html"))



(defroutes app-routes
  (context "/dashboard" []
    (GET "/" [] (resource-response-as-html "index.html"))
    (GET "/run/:section/:command" [section command] (dispatcher/dispatch {:command-type :dashboard
                                                                          :section      section
                                                                          :command      command}))
    (GET "/commands" [] (response/content-type (response/response (commands-container/prepare-commands-from-coll :dashboard)) "application/json")) ; return dashboard commands
    (route/resources "/")
    (route/not-found "Page not found."))

  (context "/advanced" []
    (GET "/" [] (resource-response-as-html "advanced.html"))
    (GET "/run/:section/:command" [section command] (dispatcher/dispatch {:command-type :advanced
                                                                          :section      section
                                                                          :command      command}))
    (GET "/commands" [] (commands-container/prepare-commands-from-coll :advanced)) ; return advanced commands
    (route/not-found "File not found."))

  (context "/*" []
    (GET "/" [] (response/redirect "/dashboard"))))

(def app (wrap-defaults app-routes site-defaults))

;(defn -main []
;	(let [port (Integer/parseInt (or (System/getenv "PORT") "8080"))]
;		(jetty/run-jetty app {:port port :join? false})))