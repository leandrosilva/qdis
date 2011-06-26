(ns qdis.web.handler
  (:use compojure.core)
  (:use ring.middleware.reload)
  (:use ring.middleware.stacktrace)
  (:require qdis.engine.queue)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]))

(defroutes main-routes
  (GET "/ping" [] "pong")

  (GET "/queues" []
    "Not implemented yet")

  (GET "/queues/history" []
    "Not implemented yet")

  (GET "/queue/:queue" [queue]
    "Not implemented yet")
  
  (POST "/queue/:queue/enqueue" [queue item]
    (let [item-uuid (qdis.engine.queue/enqueue queue item)]
      {:status 200
       :headers {"Content-Type" "application/json"
                 "Location" (str "/queue/" queue "/" item-uuid "/status")}
       :body (str "{\"queue\":\"" queue "\","
                  " \"item-uuid\":\"" item-uuid "\"}")}))

  (GET "/queue/:queue/dequeue" [queue]
    (let [result (qdis.engine.queue/dequeue queue)]
      (if (= result :queue-not-found-or-is-empty)
        {:status 404
         :headers {"Content-Type" "application/json"}
         :body (str "{\"queue\":\"" queue "\","
                    " \"message\":\"Queue not found or is empty\"}")}
         
        {:status 200
         :headers {"Content-Type" "application/json"}
         :body (str "{\"queue\":\"" queue "\","
                    " \"item-uuid\":\"" (:item-uuid result) "\","
                    " \"item\":" (:item result) "}")})))

  (GET "/:queue/:item-uuid/status" [queue item-uuid]
    "Not implemented yet")

  (route/resources "/")
  (route/not-found "Page not found"))

(def app
  (-> (handler/site main-routes)
      (wrap-reload '[qdis.web.handler]) ; should be use only in development mode
      (wrap-stacktrace)))
