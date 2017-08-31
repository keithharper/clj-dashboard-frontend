;;; If this namespace requires macros, remember that ClojureScript's
;;; macros are written in Clojure and have to be referenced via the
;;; :require-macros directive where the :as keyword is required, while in Clojure is optional. Even
;;; if you can add files containing macros and compile-time only
;;; functions in the :source-paths setting of the :builds, it is
;;; strongly suggested to add them to the leiningen :source-paths.
(ns clj-dashboard-frontend.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs-http.client :as http]
      [cljs.core.async :refer [<! chan]]
      [reagent.core :as reagent]))
(declare get-http-result)
(enable-console-print!)

(defonce app-state (reagent/atom {}))

(def available-commands (reagent/atom []))


(defn loading []
  [:div {:class "card__spinner"}
   [:svg {:class "circular" :view-box "0 0 32 32" :width "32" :height "32"}
    [:circle {:id "spinner" :cx "16" :cy "16" :r "14" :fill "none"}]]])

(defn toggle-loader-visibility [visible?]
  (aset (js/document.querySelector ".loader") "hidden" (complement visible?)))

(defn create-command-node-for-section [{section-name :section command-name :command}]
  [:div {:class "card__command-node" :id (str section-name "/" command-name)}
   [:div {:class "card__command-status card__loading"} [loading]]
   [:div {:class "card__command-label"} command-name]])

(defn create-base-section-node [{section :section}]
  ^{:key section}
  [:div {:class "section" :id section}
   [:div {:class "card__container"}
    [:div {:class "card__command-name"} section]
    [:div {:class "card__command-section"}]]])

(defn add-section-to-base-card
  ([section]
   (add-section-to-base-card section (create-base-section-node (first section))))

  ([section node]
    ;(map #(conj node %) (create-command-node-for-section section))
    ;;; TODO: is there a better way to do this without looping?
   (loop [section section
          node node]
     (let [current-section (first section)
           section-is-nil? (nil? current-section)]
       (if section-is-nil?
         node
         (do
           (get-http-result (str "/run/" (:section current-section) "/" (:command current-section)) #(println %  ))
           (recur (rest section) (conj node (create-command-node-for-section current-section)))))))))

(defn create-sections-node [sections]
  ;;; TODO: change back-end to use grouped collection (use partition to split for filtering?) to remove need for group-by
  (let [grouped-sections (group-by :section sections)]
    ;(map #(add-section-to-base-card %) grouped-sections)
    (map (fn [section] (map #(add-section-to-base-card %) (rest section)))
          grouped-sections)))

(defn create-base-card [sections]
  [:div {:class "card command"}
   [:div {:class "container"}]
   [:div {:class "card__date dashboard-date"}]
   [:div {:class "section-container"} (create-sections-node sections)]])

(defn handle-available-commands-response [response]
  (reagent/render [#(create-base-card (vec response))]
    (js/document.getElementById "main")))

;;;

(defn get-http-result [path callback-fn]
  (go (let [{response :body} (<! (http/get (str js/location.pathname path)))]
        (callback-fn response))))

(defn get-available-commands []
  (get-http-result "/commands" handle-available-commands-response))

;;;

(defn ^:export run []
  (get-available-commands)
  (toggle-loader-visibility false))

(run)