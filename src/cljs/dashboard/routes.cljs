(ns dashboard.routes
  (:require [re-frame.core :refer [reg-sub subscribe dispatch]]
			[goog.events :as events]
			[secretary.core :as secretary :refer-macros [defroute]])
  (:import [goog History]
		   [goog.history EventType]))

(defn routes
  []
  (set! (.-hash js/location) "/")
  (secretary/set-config! :prefix "#")
  (defroute "/login" [] (dispatch [:set-active-page :login]))
  (defroute "/home" [] (dispatch [:set-active-page :home]))
  (defroute "/advanced" [] (dispatch [:set-active-page :advanced]))
  (defroute "/*" [] (secretary/dispatch! "/home")))

(def history
  (doto (History.)
	(events/listen EventType.NAVIGATE
	  (fn [event] (secretary/dispatch! (.-token event))))
	(.setEnabled true)))