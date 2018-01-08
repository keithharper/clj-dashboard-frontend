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

(defn get-execution-result [request {out :out}]
  {:pre [(or (not (empty? out))
             (throw (Exception. "Pre-condition failed; result is empty.")))]}
  (assoc request :result out))

(defn dispatch-run-command [request]
  (try (->> request
            (commands-container/get-executor-args)
            (executor/execute-in bash)
            (get-execution-result request)
            (result->json))
       (catch Exception e
         (->> {:out "fail"}
              (get-execution-result request)
              (result->json)))))

(defn dispatch-get-commands [request]
  (commands-container/get-commands request))

(defn dispatch-authentication [credentials]
  (authenticator/authenticate credentials))