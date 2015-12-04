(ns codepair.shell
  (:require [codepair.http.handler :as hdl]
            [codepair.http.server :as srv]
            [reloaded.repl :refer [go]]
            [system.components.aleph :refer [new-web-server]]
            [clojure.tools.namespace.repl :refer [refresh]]
            [system.core :refer [defsystem]]
            [com.stuartsierra.component :as component]
            [reloaded.repl :refer [system init start stop go]]))


(require '[ring.mock.request :as mock]
         '[aleph.http :as http]
         '[manifold.deferred :as d]
         '[manifold.stream :as s]
         '[manifold.bus :as bus])

(defn generate-state []
  {:room (bus/event-bus)})


(defmacro with-server [server & body]
  `(let [server# ~server]
     (try
       ~@body
       (finally
         (.close ^java.io.Closeable server#)))))

(defmacro with-handler [handler & body]
  `(with-server (http/start-server ~handler {:port 8080})
     ~@body))

(defn echo-handler [req]
  (-> (http/websocket-connection req)
      (d/chain #(s/connect % %))
      (d/catch (fn [e] {}))))

(defn run []
  (with-handler echo-handler
    (let [c @(http/websocket-client "ws://localhost:8080")]
      (s/put! c "3")
      (println (str "1: " @(s/take! c))))
    (println (str "2: " (:status @(http/get "http://localhost:8080" {:throw-exceptions false}))))))



(defrecord Shell []
  component/Lifecycle
  (start [component]
    (let [state (generate-state)]
      (assoc component :state state)))
  (stop [component]
    (dissoc component :state)))

(defn new-shell []
  (map->Shell {}))


;; ===
(defn generate-system []
  (component/system-map
   :shell (new-shell)
   :handler (component/using
             (hdl/new-http-handler)
             [:shell])
   :web (component/using
         (srv/new-web-server 8080)
         [:handler])))

(defn set-init! []
  (reloaded.repl/set-init! #(generate-system)))

(defn bootstrap []
  (set-init!)
  (go))

(defn reset []
  (reloaded.repl/clear)
  (set-init!)
  (refresh :after 'reloaded.repl/go))


