(ns dashboard.helpers
  (:require [dashboard.views.home]
			[dashboard.views.advanced]))

(defn group-dispatcher-and-command
  [dispatcher sections->commands]
  (reduce
	(fn [commands-to-run [section-id section]]
	  (apply conj commands-to-run
		(map (fn [[command-id command]]
			   (identity [(keyword dispatcher) {:section section-id :command command-id}]))
		  (:commands section))))
	[]
	sections->commands))

(defn merge-by
  ([maps [section] [commands command]]
   (into {} (for [node maps]
			  [(get node section) {section  (get node section)
								   commands (merge-by (get node commands) command)}])))
  ([maps command]
   (into {} (for [command-node maps]
			  [(get command-node command) command-node]))))

(defn replace-newline-and-apply-upper [result]
  (-> result
	(clojure.string/replace #"\n" "")
	(clojure.string/upper-case)))