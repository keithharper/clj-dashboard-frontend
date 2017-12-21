(ns dashboard.views.core
  (:require [re-frame.core :as rf]
			[dashboard.views.home :as home]
			[dashboard.views.advanced :as advanced]
			[dashboard.views.login :as login]))

(defn page [page-name]
  (case page-name
	:login (doall
			 (rf/dispatch [:set-authentication-error nil])
			 [login/ui])
	:home (doall
			(rf/dispatch [:fetch-dashboard-commands])
			[home/ui])
	:advanced (doall [(rf/dispatch [:fetch-advanced-commands])
					  (rf/dispatch [:fetch-all-selected-command-results])]
				[advanced/ui])))

(defn main-panel []
  (let [active-page @(rf/subscribe [:active-page])]
	[page active-page]))
