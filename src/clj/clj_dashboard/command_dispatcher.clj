(ns clj-dashboard.command-dispatcher
  (:require
    [clj-dashboard.commands-container :as commands-container]
    [clj-dashboard.executor :as executor]
    [clojure.data.json :as json]
    [clj-dashboard.shell-executor :as shell]))

(defn prepare-response [section command response]
  (json/write-str {:query   {:section section}
                   :command command
                   :item    {:label {:date (.format (java.text.SimpleDateFormat. "yyyy-MM-dd HH:mm:ss") (java.util.Date.))}}
                   :output  response}))

(defn dispatch [{:keys [command-type section command]}]
  (try (let [command-arguments        (commands-container/filter-for-command-value command-type section command)
             command-execution-result (executor/run-command shell/run-in-bash command-arguments)
             command-result           (or (not-empty (:out command-execution-result)) ("Error."))]
         (prepare-response section command command-result))
       (catch Exception e (prepare-response section command "Error"))))