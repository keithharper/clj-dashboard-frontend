(ns dashboard.dispatcher
  (:require
	[dashboard.commands-container :as commands-container]
	[dashboard.executor :as executor]
	[dashboard.authenticator :as authenticator]
	[clojure.data.json :as json]
	[dashboard.shell-executor :as shell]))

(defn prepare-command-execution-response [section command response]
  (json/write-str {:section      section
				   :command      command
				   :execution-ts (.format (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm:ss") (java.util.Date.))
				   :result       response}))

(defn dispatch [{:keys [section command command-type] :as request}]
  (try (let [command-arguments        (commands-container/filter-for-command-value request)
			 command-execution-result (executor/run-command shell/run-in-bash command-arguments)
			 command-result           (or (not-empty (:out command-execution-result)) ("fail"))]
		 (prepare-command-execution-response section command command-result))
	   (catch Exception e (prepare-command-execution-response section command "fail"))))

(defn dispatch-authentication [credentials]
  (authenticator/authenticate credentials))