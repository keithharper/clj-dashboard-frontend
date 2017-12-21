(ns dashboard.views.home
  (:require [reagent.core :as reagent]
			[re-frame.core :refer [subscribe dispatch dispatch-sync]]
			[clojure.string :as str]
			[dashboard.views.images :as images]
			[dashboard.views.components :as components]))

(defn loading-spinner []
  [:div.card__spinner
   (:spinner images/svgs)])

(defn alert-container []
  (let [alert-message @(subscribe [:alert-message])]
	(when (not (nil? alert-message))
	  [:div.nav-item.alert.command-alert.active alert-message])))

(defn parse-result [result]
  (case result
	"PASS" [:div.command.card__pass]
	"FAIL" [:div.command.card__fail]
	[loading-spinner]))

(defn command-nodes-to-section [{:keys [section command result] :as section->command}]
  ^{:key (str "dashboard-" command)}
  [:div.card__command-node
   [:div.card__command-status {:on-click #(dispatch [:fetch-dashboard-command-result section->command])}
	(parse-result result)]
   [:div.card__command-label command]])


(defn base-section-node [{:keys [section-id section]}]
  ^{:key (str "dashboard-" section-id)}
  [:div.section.col-md-4
   [:div.card__container
	[:div.card__command-section
	 [:div.card__command-name section-id]
	 (map #(command-nodes-to-section (conj {:section section-id :result (:result (second %))} (second %))) (:commands section))]]])

(defn command-nodes []
  (when-let [commands @(subscribe [:available-dashboard-commands])]
	(map #(base-section-node {:section-id (first %) :section (second %)}) commands)))

(defn card-container []
  [:div.card.command
   [:div.card__date.dashboard-date]
   [:div.section-container.row
	(seq (command-nodes))]])

(defn header-navbar []
  [:div.header.navbar.navbar-inverse.modal-header
   [:div.container-fluid
	[components/rocketiot-logo]
	[:div.nav
	 [components/refresh-all-button #(dispatch [:fetch-all-dashboard-command-results])]
	 [components/advanced-page-button]
	 (if-not @(subscribe [:user-authenticated?])
	   [components/login-page-button]
	   [components/logout-button])]]])

(defn ui []
  [:div
   [header-navbar]
   [alert-container]
   [:div.modal-body.main-container
	[card-container]]])