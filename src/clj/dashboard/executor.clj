(ns dashboard.executor)

(defn run-command [execution-fn command]
	(let [result (execution-fn command)]
		result))