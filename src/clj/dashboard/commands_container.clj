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

(defn filter-for-command-value [command-type section command]
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
	(map (fn [section] (assoc-in section [:commands] (map #(dissoc % :command-value) (:commands section)))) )))

(defn prepare-commands-from-coll [command-type]
  (json/write-str (prepare-command-list (command-type command-types))))