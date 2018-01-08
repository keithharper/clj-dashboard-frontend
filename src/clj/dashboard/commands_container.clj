(ns dashboard.commands-container
  (:require [clojure.data.json :as json]))

;;; command collections ;;;
(def dashboard-commands
  (json/read-json (slurp "resources/conf/dashboard.json")))

(def advanced-commands
  (json/read-json (slurp "resources/conf/commands.json")))

;;; collection lookup ;;;

(def command-types
  {:dashboard dashboard-commands
   :advanced  advanced-commands})

;;; collection helper functions ;;;

(defn get-executor-args [{:keys [section command command-type]}]
  (let [commands-coll (command-type command-types)]
    (->> commands-coll
         (filter #(= (:section %) section))
         (first)
         (:commands)
         (filter #(= (:command %) command))
         (first)
         (:command-value))))

(defn- prepare-command-list [coll]
  (->> coll
       (filter #(not= "Admin" (:section %)))
       (map (fn [section]
              (assoc-in section [:commands]
                        (map #(dissoc % :command-value) (:commands section)))))))

(defn get-commands [{command-type :command-type}]
  (-> (get command-types command-type)
      (prepare-command-list)
      (json/write-str)))