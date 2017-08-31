(ns clj-dashboard.shell-executor
	(:require [clojure.java.shell :as shell]))

(defn run-in-bash [args]
	(shell/sh "sh" "-c" args))
