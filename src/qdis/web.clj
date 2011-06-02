(ns qdis.web
  (:use compojure.core)
  (:use ring.middleware.reload)
  (:use ring.middleware.stacktrace)
  (:use qdis.queue)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]))

(defroutes main-routes
  (GET "/ping" [] "pong")
  
  (POST "/:queue/enqueue" [queue item]
    (let [item-uuid (qdis.queue/enqueue queue item)]
      {:status 200
       :headers {"Content-Type" "application/json"
                 "Location" (str "/" queue "/" item-uuid "/status")}
       :body (str "{\"queue\":\"" queue "\","
                  " \"item-uuid\":\"" item-uuid "\"}")}))

  (GET "/:queue/dequeue" [queue]
    (let [result (qdis.queue/dequeue queue)]
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
      (wrap-reload '[qdis.web]) ; shoulb be use only in development mode
      (wrap-stacktrace)))
