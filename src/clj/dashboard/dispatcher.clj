(ns dashboard.dispatcher
  (:require
	[dashboard.commands-container :as commands-container]
	[dashboard.executor :as executor]
	[dashboard.authenticator :as authenticator]
	[clojure.data.json :as json]
	[dashboard.shell-executor :refer [bash]]))

(defn now []
  (.format (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm:ss") (java.util.Date.)))

(defn result->json [result]
  (json/write-str (assoc result :execution-ts (now))))

(defn get-execution-result [request result]
  (assoc request :result (get result :out "fail")))

(defn dispatch-run-command [request]
  (try
	(->> request
	  (commands-container/get-executor-args)
	  (executor/execute-in bash)
	  (get-execution-result request)
	  (result->json))
	(catch Exception e (result->json (get-execution-result request {:out "fail"})))))

(defn dispatch-get-commands [request]
  (commands-container/get-commands request))

(defn dispatch-authentication [credentials]
  (authenticator/authenticate credentials))