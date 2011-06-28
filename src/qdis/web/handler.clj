(ns qdis.web.handler
  (:use [clojure.contrib.json :only (json-str)])
  (:use compojure.core)
  (:use ring.middleware.reload)
  (:use ring.middleware.stacktrace)
  (:require qdis.engine.queue)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]))

(defroutes main-routes
  (GET "/ping" [] "pong")

  (GET "/queues" []
    {:status 200
     :headers {"Content-Type" "application/json"}
     :body (json-str {"queues" (qdis.engine.queue/queues)})})

  (POST "/queue/:queue/enqueue" [queue item]
    (let [item-uuid (qdis.engine.queue/enqueue queue item)]
      {:status 200
       :headers {"Content-Type" "application/json"
                 "Location" (str "/queue/" queue "/" item-uuid "/status")}
       :body (json-str {"queue" queue
                        "item-uuid" item-uuid})}))

  (GET "/queue/:queue/dequeue" [queue]
    (let [result (qdis.engine.queue/dequeue queue)]
      (if (= result :queue-not-found-or-is-empty)
        {:status 404
         :headers {"Content-Type" "application/json"}
         :body (json-str {"queue" queue
                          "message" "Queue not found or is empty"})}
         
        {:status 200
         :headers {"Content-Type" "application/json"}
         :body (json-str {"queue" queue
                          "item-uuid" (:item-uuid result)
                          "item" (:item result)})})))

  (GET "/queue/:queue/history/dequeue" [queue]
    "Not implemented yet")

  (GET "/queue/:queue/:item-uuid/status" [queue item-uuid]
    "Not implemented yet")

  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> (handler/site main-routes)
      (wrap-reload '[qdis.web.handler]) ; should be use only in development mode
      (wrap-stacktrace)))
