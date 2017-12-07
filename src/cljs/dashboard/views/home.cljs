(ns dashboard.views.home
  (:require [reagent.core :as reagent]
			[re-frame.core :refer [subscribe dispatch dispatch-sync]]
			[clojure.string :as str]
			[dashboard.views.images :as images]))

(defn loading-spinner []
  [:div.card__spinner
   (:spinner images/svgs)])

(defn alert-container []
  (let [alert-message @(subscribe [:alert-message])]
	(when (not (nil? alert-message))
	  [:div {:id "alert"}
	   [:a.alert.active alert-message]])))

(defn parse-result [result]
  (case result
	"PASS" [:div.command.card__pass]
	"FAIL" [:div.command.card__fail]
	[loading-spinner]))

(defn command-nodes-to-section [{:keys [section command result] :as section->command}]
  ^{:key (str "dashboard-" command)}
  [:div.card__command-node
   [:div.card__command-status {:on-click #(dispatch [:fetch-command-result section->command])}
	(parse-result result)]
   [:div.card__command-label command]])


(defn base-section-node [{:keys [section-id section]}]
  ^{:key (str "dashboard-" section-id)}
  [:div.section
   [:div.card__container
	[:div.card__command-section
	 [:div.card__command-name section-id]
	 (map #(command-nodes-to-section (conj {:section section-id :result (:result (second %))} (second %))) (:commands section))]]])

(defn command-nodes []
  (let [commands @(subscribe [:available-commands])]
	(map #(base-section-node {:section-id (first %) :section (second %)}) commands)))

(defn refresh-all-button []
  [:button.header__button.mdl-button
   {:id         "refresh-button"
	:aria-label "Refresh"
	:style      {:padding "3px" :background-color "transparent"}
	:title      "Refresh output of all selected commands"
	:on-click   #(dispatch [:fetch-all-command-results])}
   "Refresh All"
   (:refresh-all-image images/svgs)])

(defn advanced-page-button []
  [:div.header__button.mdl-button
   {:id         "advanced-button"
	:title      "Switch to Advanced mode"
	:aria-label "Advanced"
	:style      {:padding "3px" :background-color "transparent"}
	:on-click   #(dispatch [:set-active-page :advanced-image])}
   "Advanced"
   (:advanced-image images/svgs)])

(defn rocketiot-logo []
  [:div.header__title {:id "logo" :style {:display "flex" :-webkit-font-smoothing "antialiased"}}
   [:div {:style {:color "#f2a900" :cursor "default" :font-weight "bold"}} "ROCKET"]
   [:div {:style {:color "#fff" :cursor "default"}} "IoT"]])

(defn ui []
  [:div
   [:header.header
	[rocketiot-logo]
	[:div.header-buttons
	 [refresh-all-button]
	 [advanced-page-button]]]
   [alert-container]
   [:div.card.command
	[:div.container
	 [:div.card__date.dashboard-date]
	 [:div.section-container
	  (seq (command-nodes))]]]])

(defn ^:export print-results []
  (let [available-commands @(subscribe [:active-page])]
	(println available-commands)))