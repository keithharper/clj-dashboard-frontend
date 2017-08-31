(ns clj-dashboard.commands-container
	(:require [clojure.data.json :as json]))

;;; command collections ;;;
(def dashboard-commands
	(json/read-json (slurp "resources/conf/dashboard.json")))

(def advanced-commands
	(json/read-json (slurp "resources/conf/commands.json")))

;;; collection lookup ;;;

(def command-types
	{:dashboard dashboard-commands
	 :advanced advanced-commands})

;;; collection helper functions ;;;

(defn filter-for-command-value [command-type section command]
	(let [commands-coll (command-type command-types)
		  filtered-command (first (filter #(and (= (:section %) section) (= (:command %) command)) commands-coll))]
		(:command-value filtered-command)))

(defn- prepare-command-list [coll]
	(map #(dissoc % :command-value) coll))

(defn prepare-commands-from-coll [command-type]
	(json/write-str (prepare-command-list (command-type command-types))))