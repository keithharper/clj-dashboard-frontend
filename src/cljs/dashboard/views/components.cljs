(ns dashboard.views.components
  (:require [dashboard.views.images :as images]
			[re-frame.core :refer [subscribe dispatch dispatch-sync]]))

(defn rocketiot-logo []
  [:div.header__title.navbar-header {:id "logo" :style {:display "flex" :-webkit-font-smoothing "antialiased"}}
   [:div {:style {:color "#f2a900" :cursor "default" :font-weight "bold"}} "ROCKET"]
   [:div {:style {:color "#fff" :cursor "default"}} "IoT"]])

(defn refresh-all-button [dispatcher]
  [:span.header__button.mdl-button
   {:id         "refresh-button"
	:aria-label "Refresh"
	:title      "Refresh output of all selected commands"
	:on-click   dispatcher}
   "Refresh All"
   (:refresh-all-image images/svgs)])

(defn login-page-button []
  [:a.header__button.mdl-button
   {:id         "login-button"
	:title      "Go to login page"
	:aria-label "Login"
	:href       "#/login"}
   (:login-image images/svgs)])

(defn logout-button []
  [:a.header__button.mdl-button
   {:id         "logout-button"
	:title      "Logout"
	:aria-label "Logout"
	:on-click #(dispatch [:logout])}
   (:logout-image images/svgs)])

(defn home-page-button []
  [:a.header__button.mdl-button
   {:id         "dashboard-button"
	:title      "Switch to Dashboard mode"
	:aria-label "Dashboard"
	:href       "#/home"}
   "Dashboard"
   (:dashboard-image images/svgs)])

(defn advanced-page-button []
  [:a.header__button.mdl-button
   {:id         "advanced-button"
	:title      "Switch to Advanced mode"
	:aria-label "Advanced"
	:href       "#/advanced"}
   "Advanced"
   (:advanced-image images/svgs)])