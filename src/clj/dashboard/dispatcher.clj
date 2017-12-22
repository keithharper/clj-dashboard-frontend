(ns dashboard.dispatcher
  (:require
	[dashboard.commands-container :as commands-container]
	[dashboard.executor :as executor]
	[dashboard.authenticator :as authenticator]
	[clojure.data.json :as json]
	[dashboard.shell-executor :as shell]))

(defn now []
  (.format (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm:ss") (java.util.Date.)))

(defn prepare-command-execution-response [{:keys [section command result] :as response}]
  (json/write-str (assoc response :execution-ts (now))))

(defn check-execution-result
  [result]
  (get result :out "fail"))

(defn dispatch [request]
  (try
	(let [command-arguments (commands-container/filter-for-command-value request)
		  execution-result  (check-execution-result (executor/run-command shell/run-in-bash command-arguments))]
	  (prepare-command-execution-response (assoc request :result execution-result)))
	(catch Exception e (prepare-command-execution-response (assoc request :result "fail")))))

(defn dispatch-authentication [credentials]
  (authenticator/authenticate credentials))