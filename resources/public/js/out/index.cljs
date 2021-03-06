(ns index
  (:require [clojure.string :as s]
            [cljs.reader :as reader]
            [goog.events :as events]
            [goog.dom :as gdom]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [secretary.core :as secretary :refer-macros [defroute]]
            [common :as cm]
            [view :as vw]
            [util :as ul])
  (:require-macros [cljs.core.async.macros :as asyncm :refer (go go-loop)])
  (:import [goog.net XhrIo]
           goog.net.EventType
           [goog.events EventType]))


(declare verifyAssertion
         loginClickHandler
         session-check
         tags-handler
         user-handler)

(defn loginClickHandler []
  (.request  js/navigator.id))

(defn logoutClickHandler []
  (.logout  js/navigator.id))

(defn verifyAssertion [assertion]

  (ul/console-log (str "verifyAssertion CALLED / assertion: " assertion))
  (cm/edn-xhr
   {:method :post
    :url "/verify-assertion"
    :data {:assertion assertion}
    :on-complete (partial cm/basicHandler
                          (fn [e xhr]
                            (let [data (.getResponseText xhr)
                                  responseF  (reader/read-string data)]

                              (cm/user-handler e xhr responseF)
                              (session-check))))}))

(defn enable-signin []
  (om/root (fn [state owner]
             (om/component (html [:div {:on-click loginClickHandler}
                                  "login"])))
           (:user @cm/app-state)
           {:target (. js/document (getElementById "aauth"))}))

(defn enable-signout []
  (om/root (fn [state owner]
             (om/component (html [:div {:on-click logoutClickHandler}
                                  "logout"])))
           (:user @cm/app-state)
           {:target (. js/document (getElementById "aauth"))}))

(defn show-listings
  ([]
   (show-listings "app-container"))

  ([element-container]
   (om/root (fn [state owner]
              (om/component (html [:div
                                   [:nav {:class "top-bar" :data-topbar true :role "navigation"}
                                    [:ul {:class "left" :id "listing-search-field"}
                                     [:li {:class "has-form"}
                                      [:div {:class "row collapse"}
                                       [:div {:class "large-8 small-9 columns"}
                                        [:input {:type "text" :id "search-field" :placeholder "Find Availabilities"}]]
                                       [:div {:class "large-4 small-3 columns"}
                                        [:a {:href "#"
                                             :class "alert button expand"
                                             :on-click (fn [e]
                                                         (cm/search-availabilities vw/availabilities-handler
                                                          (.val (js/$ "#search-field"))))}
                                         "Search"]]]]]
                                    (if (cm/user-logged-in?)
                                      [:ul {:class "right" :id "listing-create-button"}
                                       [:li {:class "has-form show-for-large-up"}
                                        [:a {:href "#"
                                             :class "button"
                                             :on-click (fn [e]
                                                         (let [syncfn (fn []

                                                                        (let [availability {:time :ongoing
                                                                                            :title (.val (js/$ "#availability-title"))
                                                                                            :description
                                                                                            (.val (js/$ "#availability-description"))

                                                                                            :tags
                                                                                            (mapv (fn [e]
                                                                                                    {:name (-> e
                                                                                                               s/lower-case
                                                                                                               (s/replace #"\-" ""))})
                                                                                                  (filter #(re-find #"\w" %)

                                                                                                          (s/split
                                                                                                           (.val (js/$ "#availability-tags"))
                                                                                                           #"\s")))}
                                                                              group-name (:groupname (:user (cm/get-app-state)))]

                                                                          (ul/console-log (str availability))

                                                                          (cm/edn-xhr
                                                                           {:method :post
                                                                            :url (str "/add-availability?groupname=" group-name)
                                                                            :data availability
                                                                            :on-complete
                                                                            (cm/localCommonHandler
                                                                             (fn [e xhr data]
                                                                               (ul/console-log (str "SUCCESS: " data))
                                                                               (set! (.-location js/window) "/")))})))]

                                                           (.val (js/$ "#availability-title") "")
                                                           (.val (js/$ "#availability-description") "")
                                                           (.val (js/$ "#availability-tags") "")

                                                           (.off (js/$ "#availability-save") "click")
                                                           (.click (js/$ "#availability-save")
                                                                   syncfn)

                                                           (secretary/dispatch! "/availabilities")))}
                                         "Create"]]])]
                                   [:div {:id "tags"}]
                                   [:div {:id "availabilities"}]])))
            @cm/app-state
            {:target (. js/document (getElementById element-container))})
   (cm/load-tags tags-handler)
   (cm/load-availabilities vw/availabilities-handler)))


(defn show-landing []
  (let [listings-container "listings-container"]

    ;; 1. set the container into which our listings will go
    (om/root (fn [state owner]
               (vw/landing-view listings-container))
             @cm/app-state
             {:target (. js/document (getElementById "app-container"))})

    ;; 2. Invoke after using a foundation component
    ;; Refer to "Adding New Content After Page Load" here: http://foundation.zurb.com/docs/javascript.html
    (.foundation (js/$ js/document) "tab" "reflow")

    ;; 3.
    (show-listings listings-container)

    ;; 4.
    (let [video (.. js/document (querySelector "video"))
          constraints (clj->js {:audio false
                                :video true
                                :maxWidth 640
                                maxHight 480})
          _ (set! (.-getUserMedia js/navigator)
                  (or (.-getUserMedia js/navigator)
                      (.-webkitGetUserMedia js/navigator)
                      (.-mozGetUserMedia js/navigator)))

          successCallback (fn [stream]
                            (set! (.-stream js/window) stream)
                            (if (.-URL js/window)
                              (set! (.-src video) (.. js/window.URL (createObjectURL stream)))
                              (set! (.-src video) stream)))

          errorCallback (fn [error]
                          (ul/console-log (str "navigator.getUserMedia error: " error)))]

      (.. js/navigator (getUserMedia constraints successCallback errorCallback)))

    ;; 5.
    (secretary/set-config! :prefix "#")
    (defroute "/listings" {:as params} (vw/switch-tab "#tab-listings-link" "/#"))
    (defroute "/availabilities" {:as params} (vw/switch-tab "#tab-availabilities-link" "/#availabilities"))
    (defroute "/session" {:as params} (vw/switch-tab "#tab-session-link" "/#session"))
    (defroute "/account" {:as params} (vw/switch-tab "#tab-account-link" "/#account"))

    #_(let [h (History.)]
      (goog.events/listen h EventType.NAVIGATE #(secretary/dispatch! (.-token %)))
      (doto h
            (.setEnabled true)))))

(defn ^:export route [loc]
  (secretary/dispatch! loc))

(defn session-check []

  (let [currentUser (:username (:user (cm/get-app-state)))
        navigatorId js/navigator.id]

    (.watch navigatorId
            (clj->js {:loggedInUser currentUser
                      :onlogin verifyAssertion
                      :onlogout cm/signoutUser})))

  (if (not (cm/user-logged-in?))
    (do (show-listings)
        (enable-signin))
    (do (show-landing)
        (enable-signout))))

(defn tags-handler [e xhr data]
  (swap! cm/app-state (fn [e]
                        (update-in e [:tags] (fn [f] (into [] data)))))

  (om/root vw/tags-view
           (:tags @cm/app-state)
           {:target (. js/document (getElementById "tags"))}))


(ul/ready
 (fn [_]
   (cm/load-user-data
    (fn [e xhr data]
      (cm/user-handler e xhr data)
      (session-check)))))
