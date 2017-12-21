(ns dashboard.handlers
  (:require [re-frame.core :as rf :refer [reg-fx dispatch]]))

(defonce timeouts
  (atom {}))

(reg-fx :dispatch-debounce
  (fn [{:keys [key event delay]}]
	(js/clearTimeout (@timeouts key))
	(swap! timeouts assoc key
	  (js/setTimeout (fn []
					   (dispatch event)
					   (swap! timeouts dissoc key))
		delay))))

(reg-fx :stop-debounce
  (fn [{:keys [key]}]
	(js/clearTimeout (@timeouts key))
	(swap! timeouts dissoc key)))