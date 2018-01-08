(ns dashboard.commands-container
  (:require [clojure.data.json :as json]))

;;; command collections ;;;
(def dashboard-commands
  (json/read-str (slurp "resources/conf/dashboard.json") :key-fn keyword))

(def advanced-commands
  (json/read-str (slurp "resources/conf/commands.json") :key-fn keyword))

;;; collection lookup ;;;

(def command-types
  {:dashboard dashboard-commands
   :advanced  advanced-commands})

;;; collection helper functions ;;;

(defn get-executor-args [{:keys [section command command-type]}]
  (let [commands-coll (command-type command-types)]
    (->> commands-coll
         (some #(if (= (:section %) section) (:commands %)))
         (some #(if (= (:command %) command) (:command-value %))))))

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