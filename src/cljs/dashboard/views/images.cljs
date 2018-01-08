(ns dashboard.views.images)

(def svgs
  {:spinner                     [:svg.circular {:view-box "0 0 32 32" :width "32" :height "29"}
                                 [:circle#spinner {:cx "16" :cy "16" :r "14" :fill "none"}]]
   :deselect-single-image-black [:svg {:fill "#000000" :height "30" :width "30" :view-box "0 0 24 24"}
                                 [:path {:d "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"}]
                                 [:path {:d "M0 0h24v24H0z" :fill "none"}]]
   :deselect-single-image-white [:svg {:fill "#FFF" :height "26" :width "26" :view-box "0 0 24 24"}
                                 [:path {:d "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"}]
                                 [:path {:d "M0 0h24v24H0z" :fill "none"}]]
   :refresh-single-image        [:svg {:fill "#000000" :height "30" :width "30" :view-box "0 0 24 24"}
                                 [:path {:d "M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"}]
                                 [:path {:d "M0 0h24v24H0z" :fill "none"}]]
   :refresh-all-image           [:svg {:fill "#FFFFFF" :height "30" :width "30" :view-box "0 0 24 24"}
                                 [:path {:d "M12 6v3l4-4-4-4v3c-4.42 0-8 3.58-8 8 0 1.57.46 3.03 1.24 4.26L6.7 14.8c-.45-.83-.7-1.79-.7-2.8 0-3.31 2.69-6 6-6zm6.76 1.74L17.3 9.2c.44.84.7 1.79.7 2.8 0 3.31-2.69 6-6 6v-3l-4 4 4 4v-3c4.42 0 8-3.58 8-8 0-1.57-.46-3.03-1.24-4.26z"}]
                                 [:path {:d "M0 0h24v24H0z" :fill "none"}]]
   :login-image                 [:svg {:fill "#FFFFFF" :height "32" :width "28" :view-box "0 0 8 8"}
                                 [:path {:d "M3 0v1h4v5h-4v1h5v-7h-5zm1 2v1h-4v1h4v1l2-1.5-2-1.5z"}]]
   :logout-image                [:svg {:fill "#FFFFFF" :height "32" :width "28" :view-box "0 0 8 8"}
                                 [:path {:d "M3 0v1h4v5h-4v1h5v-7h-5zm-1 2l-2 1.5 2 1.5v-1h4v-1h-4v-1z"}]]
   :username-image              [:svg {:fill "#FFFFFF" :height "16" :width "12" :view-box "0 0 12 16"}
                                 [:path {:d "M12 14.002a.998.998 0 0 1-.998.998H1.001A1 1 0 0 1 0 13.999V13c0-2.633 4-4 4-4s.229-.409 0-1c-.841-.62-.944-1.59-1-4 .173-2.413 1.867-3 3-3s2.827.586 3 3c-.056 2.41-.159 3.38-1 4-.229.59 0 1 0 1s4 1.367 4 4v1.002z"}]]
   :password-image              [:svg {:fill "#FFFFFF" :height "16" :width "12" :view-box "0 0 12 16"}
                                 [:path {:d "M4 13H3v-1h1v1zm8-6v7c0 .55-.45 1-1 1H1c-.55 0-1-.45-1-1V7c0-.55.45-1 1-1h1V4c0-2.2 1.8-4 4-4s4 1.8 4 4v2h1c.55 0 1 .45 1 1zM3.8 6h4.41V4c0-1.22-.98-2.2-2.2-2.2-1.22 0-2.2.98-2.2 2.2v2H3.8zM11 7H2v7h9V7zM4 8H3v1h1V8zm0 2H3v1h1v-1z"}]]
   :dashboard-image             [:svg {:fill "#FFFFFF" :height "30" :width "30" :view-box "0 0 24 24"}
                                 [:path {:d "M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z"}]
                                 [:path {:d "M0 0h24v24H0z" :fill "none"}]]
   :advanced-image              [:svg {:fill "#FFFFFF" :height "30" :width "30" :view-box "0 0 24 24"}
                                 [:path {:d "M4 8h4V4H4v4zm6 12h4v-4h-4v4zm-6 0h4v-4H4v4zm0-6h4v-4H4v4zm6 0h4v-4h-4v4zm6-10v4h4V4h-4zm-6 4h4V4h-4v4zm6 6h4v-4h-4v4zm0 6h4v-4h-4v4z"}]
                                 [:path {:d "M0 0h24v24H0z" :fill "none"}]]
   :shutdown-image              [:svg {:fill "#FFFFFF" :height "30" :width "30" :view-box "0 0 24 24"}
                                 [:path {:d "M13 3h-2v10h2V3zm4.83 2.17l-1.42 1.42C17.99 7.86 19 9.81 19 12c0 3.87-3.13 7-7 7s-7-3.13-7-7c0-2.19 1.01-4.14 2.58-5.42L6.17 5.17C4.23 6.82 3 9.26 3 12c0 4.97 4.03 9 9 9s9-4.03 9-9c0-2.74-1.23-5.18-3.17-6.83z"}]
                                 [:path {:d "M0 0h24v24H0z" :fill "none"}]]
   :configure-wan-image         [:svg {:fill "#FFFFFF" :height "30" :width "30" :view-box "0 0 24 24"}
                                 [:path {:d "M7,15H9V18H11V15H13V18H15V15H17V18H19V9H15V6H9V9H5V18H7V15M4.38,3H19.63C20.94,3 22,4.06 22,5.38V19.63A2.37,2.37 0 0,1 19.63,22H4.38C3.06,22 2,20.94 2,19.63V5.38C2,4.06 3.06,3 4.38,3Z"}]]
   :initiate-upload-image       [:svg {:fill "#FFFFFF" :height "30" :width "30" :view-box "0 0 24 24"}
                                 [:path {:d "M19.35 10.04C18.67 6.59 15.64 4 12 4 9.11 4 6.6 5.64 5.35 8.04 2.34 8.36 0 10.91 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96zM14 13v4h-4v-4H7l5-5 5 5h-3z"}]
                                 [:path {:d "M0 0h24v24H0z" :fill "none"}]]
   :deselect-all-image          [:svg {:fill "#FFFFFF" :height "30" :width "30" :view-box "0 0 24 24"}
                                 [:path {:d "M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z"}]
                                 [:path {:d "M0 0h24v24H0z" :fill "none"}]]
   :expand-section-image        [:svg {:style                 {:pointer-events "none" :display "block" :width "100%" :height "100%"}
                                       :view-box              "0 0 24 24"
                                       :preserve-aspect-ratio "xMidYMid meet"}
                                 [:g [:path {:d "M7 14l5-5 5 5z"}]]]})
