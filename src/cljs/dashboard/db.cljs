(ns dashboard.db
  (:require [re-frame.cofx :as re-frame-cofx :refer [reg-cofx]]
			[cljs.reader :as reader]))

(def default-db
  {:alert-message nil
   :active-page   :home
   :user          {}
   :dashboard     {:available-commands {}}
   :advanced      {:selected-commands {}
				   :menu-status       false}})

(def local-storage-key "previously-selected-commands")

(defn selected-commands->local-store
  "Puts selected commands from the Advanced page
  into localStorage"
  [{:keys [selected-commands]}]
  (.setItem js/localStorage local-storage-key (str selected-commands)))

(re-frame-cofx/reg-cofx
  :local-store-selected-commands
  (fn [cofx _]
	(assoc cofx :local-store-selected-commands
				(into {}
				  (some->> (.getItem js/localStorage local-storage-key)
					(cljs.reader/read-string))))))

(re-frame-cofx/reg-cofx
  :active-authentication
  (fn [cofx _]
	(if-let [prior-auth (js->clj js/user :keywordize-keys true)]
	  (assoc cofx :authenticated? (:authenticated? prior-auth))
	  (assoc cofx :authenticated? false))))