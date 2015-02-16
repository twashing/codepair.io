(ns codepair
  (:require [cljs.reader :as reader]
            [goog.events :as events]
            [goog.dom :as gdom]
            [landing :as ln]
            [util :as ul])
  (:import [goog.net XhrIo]
           goog.net.EventType
           [goog.events EventType]))


(def ^:private meths
  {:get "GET"
   :put "PUT"
   :post "POST"
   :delete "DELETE"})

(defn edn-xhr [{:keys [method url data on-complete]}]
  (let [xhr (XhrIo.)]
    (events/listen xhr goog.net.EventType.COMPLETE
                   (fn [e]
                     (on-complete (reader/read-string
                                   (.getResponseText xhr))
                                  xhr)))
    (. xhr
       (send url (meths method) (when data (pr-str data))
             #js {"Content-Type" "application/edn"}))))

(defn basicHandler [handlefn e xhr]
  (let [res (.getResponseText xhr)
        status (.getStatus xhr)]
    (ul/console-log (str "basicHandler response: " res))
    (if (= 200 status)
      (do
        (ul/console-log (str "XMLHttpRequest SUCCESS: " res))
        (handlefn e xhr))
      (do
        (ul/console-log (str "XMLHttpRequest ERROR: " res))
        (.logout navigator.id)))))

(defn signoutUser []
  (ul/console-log "signoutUser CALLED")

  (edn-xhr
   {:method :get
    :url "/signout"
    :on-complete (partial basicHandler
                          (fn [e xhr]
                            (ul/console-log (str "signoutUser completed"))))}))
