(ns dashboard.views.login
  (:require [reagent.core :as reagent]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [clojure.string :as str]
            [dashboard.views.images :as images]
            [dashboard.views.components :as components]))

(defn login [credentials]
  (dispatch [:authenticate credentials]))

(defn on-enter [event action]
  (when (= (.-keyCode event) 13)
    (action)))


(defn header-navbar []
  [:div.header.navbar.navbar-inverse.modal-header
   [:div.container-fluid
    [components/rocketiot-logo]
    [:div.nav
     [components/home-page-button]]]])

(defn authentication-error-message [error-message]
  [:div.row
   [:div.alert.alert-danger.container.col-md-5
    (str "Error: " error-message)]])

(defn username-container [credentials]
  [:div.input-group.login-input-group
   [:span.input-group-addon
    [:i (:username-image images/svgs)]]
   [:input.form-control
    {:type         "email"
     :placeholder  "Username"
     :value        (or (:username @credentials) "")
     :on-change    #(swap! credentials assoc :username (-> % .-target .-value))
     :on-key-press #(on-enter % (fn [] (login @credentials)))}]])

(defn password-container [credentials]
  [:div.input-group.login-input-group
   [:span.input-group-addon
    [:i (:password-image images/svgs)]]
   [:input.form-control
    {:type         "password"
     :placeholder  "Password"
     :value        (or (:password @credentials) "")
     :on-change    #(swap! credentials assoc :password (-> % .-target .-value))
     :on-key-press #(on-enter % (fn [] (login @credentials)))}]])

(defn submit-button [credentials]
  [:button.btn.btn-sm.login-button
   {:on-click #(login @credentials)}
   "Login"])

(defn login-container []
  (let [credentials (reagent/atom nil)]
    [:form
     {:on-submit #(.preventDefault %)}
     [:div.row
      [:div.container.col-md-5
       [username-container credentials]
       [password-container credentials]
       [submit-button credentials]]]]))

(defn ui []
  (let [error-message @(subscribe [:authentication-error])]
    [:div
     [header-navbar]
     [:div.modal-body.main-container
      (when error-message
        [authentication-error-message error-message])
      [login-container]]]))