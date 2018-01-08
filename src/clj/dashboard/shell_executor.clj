(ns dashboard.shell-executor
	(:require [clojure.java.shell :as shell]))

(defn bash [args]
	(shell/sh "sh" "-c" args))
