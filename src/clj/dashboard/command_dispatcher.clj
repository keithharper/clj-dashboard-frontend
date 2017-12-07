(ns dashboard.command-dispatcher
  (:require
    [dashboard.commands-container :as commands-container]
    [dashboard.executor :as executor]
    [clojure.data.json :as json]
    [dashboard.shell-executor :as shell]))

(defn prepare-response [section command response]
  (json/write-str {:section section
                   :command command
                   :execution-ts (.format (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm:ss") (java.util.Date.))
                   :result  response}))

(defn dispatch [{:keys [command-type section command]}]
  (try (let [command-arguments        (commands-container/filter-for-command-value command-type section command)
             command-execution-result (executor/run-command shell/run-in-bash command-arguments)
             command-result           (or (not-empty (:out command-execution-result)) ("fail"))]
         (prepare-response section command command-result))
       (catch Exception e (prepare-response section command "fail"))))