(ns dashboard.views.core
  (:require [re-frame.core :as rf]
			[dashboard.views.home :as home]
			[dashboard.views.advanced :as advanced]))

(defn page [page-name]
  (case page-name
	:home (doall
			(rf/dispatch [:fetch-available-commands])
			[home/ui])
	:advanced-image (doall
				(rf/dispatch [:fetch-available-admin-commands])
				[advanced/ui])))

(defn main-panel []
  (let [active-page @(rf/subscribe [:active-page])]
	[page active-page]))
